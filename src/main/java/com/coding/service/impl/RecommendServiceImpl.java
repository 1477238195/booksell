package com.coding.service.impl;

import com.coding.dto.recommend.BuyerBookPair;
import com.coding.entity.Book;
import com.coding.entity.BookWanted;
import com.coding.mapper.BookMapper;
import com.coding.mapper.BookWantedMapper;
import com.coding.mapper.OrderMapper;
import com.coding.recommend.TfidfVectorizer;
import com.coding.service.IRecommendService;
import com.coding.utils.PageResult;
import com.coding.utils.RequestPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 混合推荐：内容（TF-IDF）+ 用户协同过滤（Jaccard 近邻 + 隐式反馈）+ 冷启动热门。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements IRecommendService {

    private static final int MAX_NEIGHBORS = 25;

    private final BookMapper bookMapper;
    private final BookWantedMapper bookWantedMapper;
    private final OrderMapper orderMapper;

    @Override
    public PageResult<Book> recommendHomeBooks(String title, Long categoryId, Integer status,
                                               Long viewerUserId, RequestPage param) {
        List<Book> corpusBooks = bookMapper.selectBookListWithDetails(null, null, 1, null);
        if (corpusBooks == null) {
            corpusBooks = Collections.emptyList();
        }

        Integer effStatus = status != null ? status : 1;
        String titleQ = StringUtils.isBlank(title) ? null : title.trim();
        List<Book> rawCandidates = bookMapper.selectBookListWithDetails(titleQ, categoryId, effStatus, null);
        if (rawCandidates == null) {
            rawCandidates = Collections.emptyList();
        }

        Set<Long> purchasedIds = new HashSet<>();
        if (viewerUserId != null) {
            List<Long> ids = orderMapper.listBookIdsPurchasedByBuyer(viewerUserId);
            if (ids != null) {
                purchasedIds.addAll(ids);
            }
        }

        List<String> fitBookDocs = new ArrayList<>();
        Set<Long> onSaleIds = new HashSet<>();
        for (Book b : corpusBooks) {
            if (b.getBookId() != null) {
                onSaleIds.add(b.getBookId());
            }
            fitBookDocs.add(bookDoc(b));
        }
        for (Long pid : purchasedIds) {
            if (onSaleIds.contains(pid)) {
                continue;
            }
            Book pb = bookMapper.selectByPrimaryKey(pid);
            if (pb != null && (pb.getDeleted() == null || pb.getDeleted() == 0)) {
                fitBookDocs.add(bookDoc(pb));
            }
        }

        TfidfVectorizer bookVec = new TfidfVectorizer();
        bookVec.fit(fitBookDocs);

        List<double[]> profileVectors = new ArrayList<>();
        for (Long pid : purchasedIds) {
            Book pb = bookMapper.selectByPrimaryKey(pid);
            if (pb != null && (pb.getDeleted() == null || pb.getDeleted() == 0)) {
                double[] v = bookVec.vectorize(bookDoc(pb));
                if (v.length > 0) {
                    profileVectors.add(v);
                }
            }
        }
        double[] profile = TfidfVectorizer.centroid(profileVectors);
        boolean hasProfile = profile.length > 0;

        Map<Long, Set<Long>> userBooks = buildUserBookSets(orderMapper.listPurchasedBuyerBookPairs());
        List<Neighbor> neighbors = findNeighbors(viewerUserId, userBooks);

        // 不排除「已购」：种子/测试数据常为每本书造已完成订单，硬排除会导致首页只剩极少数书。
        // 对已购书仅降权排序，仍参与推荐。
        List<Book> candidates = rawCandidates.stream()
                .filter(b -> viewerUserId == null || !Objects.equals(b.getSellerId(), viewerUserId))
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            return PageResult.success(0L, Collections.emptyList());
        }

        Map<Long, Double> contentScores = new HashMap<>();
        Map<Long, Double> cfScores = new HashMap<>();
        Map<Long, Double> popScores = new HashMap<>();

        for (Book b : candidates) {
            Long id = b.getBookId();
            double pop = Math.log1p(b.getViewCount() == null ? 0 : b.getViewCount());
            popScores.put(id, pop);

            double c = 0.0;
            if (hasProfile && !bookVec.isEmpty()) {
                double[] bv = bookVec.vectorize(bookDoc(b));
                c = TfidfVectorizer.cosine(profile, bv);
            }
            contentScores.put(id, c);

            double cf = neighborCfBookScore(id, neighbors, userBooks);
            cfScores.put(id, cf);
        }

        minMaxNormalize(contentScores);
        minMaxNormalize(cfScores);
        minMaxNormalize(popScores);

        boolean hasCf = neighbors.stream().anyMatch(n -> n.jaccard > 0);

        List<Scored<Book>> ranked = new ArrayList<>();
        for (Book b : candidates) {
            Long id = b.getBookId();
            double combined;
            if (!hasProfile && !hasCf) {
                combined = popScores.getOrDefault(id, 0.0);
            } else if (hasProfile && hasCf) {
                combined = 0.42 * contentScores.getOrDefault(id, 0.0)
                        + 0.38 * cfScores.getOrDefault(id, 0.0)
                        + 0.20 * popScores.getOrDefault(id, 0.0);
            } else if (hasProfile) {
                combined = 0.72 * contentScores.getOrDefault(id, 0.0)
                        + 0.28 * popScores.getOrDefault(id, 0.0);
            } else {
                combined = 0.62 * cfScores.getOrDefault(id, 0.0)
                        + 0.38 * popScores.getOrDefault(id, 0.0);
            }
            if (purchasedIds.contains(id)) {
                combined *= 0.22;
            }
            ranked.add(new Scored<>(b, combined));
        }

        ranked.sort(Comparator.comparingDouble((Scored<Book> s) -> s.score).reversed());

        int page = Math.max(1, param.getPage());
        int size = Math.max(1, param.getSize());
        int from = (page - 1) * size;
        long total = ranked.size();
        if (from >= ranked.size()) {
            return PageResult.success(total, Collections.emptyList());
        }
        int to = Math.min(from + size, ranked.size());
        List<Book> slice = ranked.subList(from, to).stream().map(s -> s.item).collect(Collectors.toList());
        return PageResult.success(total, slice);
    }

    @Override
    public PageResult<BookWanted> recommendHomeWanted(String bookTitle, Integer status,
                                                      Long viewerUserId, RequestPage param) {
        List<BookWanted> corpusWanted = bookWantedMapper.selectWantedListWithDetails(null, null, 1);
        if (corpusWanted == null) {
            corpusWanted = Collections.emptyList();
        }

        Integer effStatus = status != null ? status : 1;
        String titleQ = StringUtils.isBlank(bookTitle) ? null : bookTitle.trim();
        List<BookWanted> rawCandidates = bookWantedMapper.selectWantedListWithDetails(titleQ, null, effStatus);
        if (rawCandidates == null) {
            rawCandidates = Collections.emptyList();
        }

        Set<Long> purchasedIds = new HashSet<>();
        if (viewerUserId != null) {
            List<Long> ids = orderMapper.listBookIdsPurchasedByBuyer(viewerUserId);
            if (ids != null) {
                purchasedIds.addAll(ids);
            }
        }

        List<String> fitWantedDocs = new ArrayList<>();
        for (BookWanted w : corpusWanted) {
            fitWantedDocs.add(wantedDoc(w));
        }
        for (Long pid : purchasedIds) {
            Book pb = bookMapper.selectByPrimaryKey(pid);
            if (pb != null && (pb.getDeleted() == null || pb.getDeleted() == 0)) {
                fitWantedDocs.add(bookDoc(pb));
            }
        }

        TfidfVectorizer wantedVec = new TfidfVectorizer();
        wantedVec.fit(fitWantedDocs);

        List<double[]> profileVectors = new ArrayList<>();
        for (Long pid : purchasedIds) {
            Book pb = bookMapper.selectByPrimaryKey(pid);
            if (pb != null && (pb.getDeleted() == null || pb.getDeleted() == 0)) {
                double[] v = wantedVec.vectorize(bookDoc(pb));
                if (v.length > 0) {
                    profileVectors.add(v);
                }
            }
        }
        double[] profile = TfidfVectorizer.centroid(profileVectors);
        boolean hasProfile = profile.length > 0;

        Map<Long, Set<Long>> userBooks = buildUserBookSets(orderMapper.listPurchasedBuyerBookPairs());
        List<Neighbor> neighbors = findNeighbors(viewerUserId, userBooks);
        List<BookWanted> candidates = rawCandidates.stream()
                .filter(w -> viewerUserId == null || !Objects.equals(w.getUserId(), viewerUserId))
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            return PageResult.success(0L, Collections.emptyList());
        }

        double[] tasteNeighbors = new double[0];
        boolean hasNeighborTaste = false;
        if (!neighbors.isEmpty() && !wantedVec.isEmpty()) {
            String nbrTitles = neighborPurchasedTitles(neighbors, userBooks);
            if (StringUtils.isNotBlank(nbrTitles)) {
                tasteNeighbors = wantedVec.vectorize(nbrTitles);
                hasNeighborTaste = tasteNeighbors.length > 0;
            }
        }

        Map<Long, Double> contentScores = new HashMap<>();
        Map<Long, Double> cfScores = new HashMap<>();
        Map<Long, Double> popScores = new HashMap<>();

        for (BookWanted w : candidates) {
            Long id = w.getWantedId();
            double pop = Math.log1p(w.getViewCount() == null ? 0 : w.getViewCount());
            popScores.put(id, pop);

            double c = 0.0;
            if (hasProfile && !wantedVec.isEmpty()) {
                double[] wv = wantedVec.vectorize(wantedDoc(w));
                c = TfidfVectorizer.cosine(profile, wv);
            }
            contentScores.put(id, c);

            double cf = 0.0;
            if (hasNeighborTaste) {
                double[] wv = wantedVec.vectorize(wantedDoc(w));
                cf = TfidfVectorizer.cosine(tasteNeighbors, wv);
            }
            cfScores.put(id, cf);
        }

        minMaxNormalize(contentScores);
        minMaxNormalize(cfScores);
        minMaxNormalize(popScores);

        boolean hasCfSignal = hasNeighborTaste;

        List<Scored<BookWanted>> ranked = new ArrayList<>();
        for (BookWanted w : candidates) {
            Long id = w.getWantedId();
            double combined;
            if (!hasProfile && !hasCfSignal) {
                combined = popScores.getOrDefault(id, 0.0);
            } else if (hasProfile && hasCfSignal) {
                combined = 0.48 * contentScores.getOrDefault(id, 0.0)
                        + 0.32 * cfScores.getOrDefault(id, 0.0)
                        + 0.20 * popScores.getOrDefault(id, 0.0);
            } else if (hasProfile) {
                combined = 0.70 * contentScores.getOrDefault(id, 0.0)
                        + 0.30 * popScores.getOrDefault(id, 0.0);
            } else {
                combined = 0.55 * cfScores.getOrDefault(id, 0.0)
                        + 0.45 * popScores.getOrDefault(id, 0.0);
            }
            ranked.add(new Scored<>(w, combined));
        }

        ranked.sort(Comparator.comparingDouble((Scored<BookWanted> s) -> s.score).reversed());

        int page = Math.max(1, param.getPage());
        int size = Math.max(1, param.getSize());
        int from = (page - 1) * size;
        long total = ranked.size();
        if (from >= ranked.size()) {
            return PageResult.success(total, Collections.emptyList());
        }
        int to = Math.min(from + size, ranked.size());
        List<BookWanted> slice = ranked.subList(from, to).stream().map(s -> s.item).collect(Collectors.toList());
        return PageResult.success(total, slice);
    }

    /**
     * 口味相近用户买过的书名拼接，用于求购侧的协同过滤（与求购文案在统一 TF-IDF 空间比相似度）。
     */
    private String neighborPurchasedTitles(List<Neighbor> neighbors, Map<Long, Set<Long>> userBooks) {
        Set<Long> bookIds = new HashSet<>();
        for (Neighbor n : neighbors) {
            if (n.jaccard <= 0) {
                continue;
            }
            Set<Long> bs = userBooks.get(n.userId);
            if (bs != null) {
                bookIds.addAll(bs);
            }
        }
        if (bookIds.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Long bid : bookIds) {
            Book b = bookMapper.selectByPrimaryKey(bid);
            if (b != null && StringUtils.isNotBlank(b.getTitle())) {
                sb.append(b.getTitle()).append(' ');
            }
        }
        return sb.toString().trim();
    }

    private static String bookDoc(Book b) {
        String t = b.getTitle() == null ? "" : b.getTitle();
        String a = b.getAuthor() == null ? "" : b.getAuthor();
        String d = b.getDescription() == null ? "" : b.getDescription();
        return t + " " + a + " " + d;
    }

    private static String wantedDoc(BookWanted w) {
        String t = w.getBookTitle() == null ? "" : w.getBookTitle();
        String a = w.getAuthor() == null ? "" : w.getAuthor();
        String d = w.getDescription() == null ? "" : w.getDescription();
        return t + " " + a + " " + d;
    }

    private static Map<Long, Set<Long>> buildUserBookSets(List<BuyerBookPair> pairs) {
        Map<Long, Set<Long>> map = new HashMap<>();
        if (pairs == null) {
            return map;
        }
        for (BuyerBookPair p : pairs) {
            if (p.getBuyerId() == null || p.getBookId() == null) {
                continue;
            }
            map.computeIfAbsent(p.getBuyerId(), k -> new HashSet<>()).add(p.getBookId());
        }
        return map;
    }

    private static List<Neighbor> findNeighbors(Long viewerUserId, Map<Long, Set<Long>> userBooks) {
        if (viewerUserId == null) {
            return Collections.emptyList();
        }
        Set<Long> mine = userBooks.get(viewerUserId);
        if (mine == null || mine.isEmpty()) {
            return Collections.emptyList();
        }
        List<Neighbor> list = new ArrayList<>();
        for (Map.Entry<Long, Set<Long>> e : userBooks.entrySet()) {
            if (e.getKey().equals(viewerUserId)) {
                continue;
            }
            Set<Long> other = e.getValue();
            if (other == null || other.isEmpty()) {
                continue;
            }
            double j = jaccard(mine, other);
            if (j > 0) {
                list.add(new Neighbor(e.getKey(), j));
            }
        }
        list.sort(Comparator.comparingDouble((Neighbor n) -> n.jaccard).reversed());
        if (list.size() > MAX_NEIGHBORS) {
            return new ArrayList<>(list.subList(0, MAX_NEIGHBORS));
        }
        return list;
    }

    private static double jaccard(Set<Long> a, Set<Long> b) {
        int inter = 0;
        for (Long x : a) {
            if (b.contains(x)) {
                inter++;
            }
        }
        if (inter == 0) {
            return 0.0;
        }
        Set<Long> union = new HashSet<>(a);
        union.addAll(b);
        return (double) inter / union.size();
    }

    private static double neighborCfBookScore(Long bookId, List<Neighbor> neighbors, Map<Long, Set<Long>> userBooks) {
        double num = 0.0;
        double denom = 0.0;
        for (Neighbor n : neighbors) {
            if (n.jaccard <= 0) {
                continue;
            }
            denom += n.jaccard;
            Set<Long> books = userBooks.get(n.userId);
            if (books != null && books.contains(bookId)) {
                num += n.jaccard;
            }
        }
        if (denom < 1e-12) {
            return 0.0;
        }
        return num / denom;
    }

    private static void minMaxNormalize(Map<Long, Double> scores) {
        if (scores.isEmpty()) {
            return;
        }
        double min = Collections.min(scores.values());
        double max = Collections.max(scores.values());
        if (max - min < 1e-12) {
            scores.replaceAll((k, v) -> 1.0);
            return;
        }
        for (Map.Entry<Long, Double> e : scores.entrySet()) {
            e.setValue((e.getValue() - min) / (max - min));
        }
    }

    private static final class Neighbor {
        final long userId;
        final double jaccard;

        Neighbor(long userId, double jaccard) {
            this.userId = userId;
            this.jaccard = jaccard;
        }
    }

    private static final class Scored<T> {
        final T item;
        final double score;

        Scored(T item, double score) {
            this.item = item;
            this.score = score;
        }
    }
}
