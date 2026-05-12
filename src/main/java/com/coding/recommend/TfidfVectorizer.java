package com.coding.recommend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 轻量 TF-IDF（字符级 bigram + 英文/数字词），用于书名与描述的基于内容相似度。
 */
public final class TfidfVectorizer {

    private static final Pattern ASCII_TOKENS = Pattern.compile("[a-z0-9]+");

    private Map<String, Integer> termIndex = Collections.emptyMap();
    private double[] idf = new double[0];

    public void fit(List<String> documents) {
        if (documents == null || documents.isEmpty()) {
            termIndex = Collections.emptyMap();
            idf = new double[0];
            return;
        }
        Map<String, Integer> df = new HashMap<>();
        int n = 0;
        for (String raw : documents) {
            List<String> toks = tokenize(raw);
            if (toks.isEmpty()) {
                continue;
            }
            n++;
            Map<String, Boolean> seen = new HashMap<>();
            for (String t : toks) {
                seen.put(t, true);
            }
            for (String t : seen.keySet()) {
                df.merge(t, 1, Integer::sum);
            }
        }
        if (n == 0 || df.isEmpty()) {
            termIndex = Collections.emptyMap();
            idf = new double[0];
            return;
        }
        List<String> terms = new ArrayList<>(df.keySet());
        Collections.sort(terms);
        termIndex = new HashMap<>();
        for (int i = 0; i < terms.size(); i++) {
            termIndex.put(terms.get(i), i);
        }
        idf = new double[terms.size()];
        for (int i = 0; i < terms.size(); i++) {
            int dfi = df.get(terms.get(i));
            idf[i] = Math.log((n + 1.0) / (dfi + 1.0)) + 1.0;
        }
    }

    public boolean isEmpty() {
        return termIndex.isEmpty();
    }

    public double[] vectorize(String text) {
        if (termIndex.isEmpty()) {
            return new double[0];
        }
        double[] v = new double[termIndex.size()];
        List<String> toks = tokenize(text);
        if (toks.isEmpty()) {
            return v;
        }
        Map<String, Integer> tf = new HashMap<>();
        for (String t : toks) {
            tf.merge(t, 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> e : tf.entrySet()) {
            Integer idx = termIndex.get(e.getKey());
            if (idx != null) {
                v[idx] = e.getValue() * idf[idx];
            }
        }
        l2Normalize(v);
        return v;
    }

    public static double cosine(double[] a, double[] b) {
        if (a == null || b == null || a.length == 0 || a.length != b.length) {
            return 0.0;
        }
        double s = 0.0;
        for (int i = 0; i < a.length; i++) {
            s += a[i] * b[i];
        }
        if (s < 0.0) {
            return 0.0;
        }
        if (s > 1.0) {
            return 1.0;
        }
        return s;
    }

    public static double[] centroid(List<double[]> vectors) {
        if (vectors == null || vectors.isEmpty()) {
            return new double[0];
        }
        int dim = vectors.get(0).length;
        double[] c = new double[dim];
        int cnt = 0;
        for (double[] v : vectors) {
            if (v == null || v.length != dim) {
                continue;
            }
            cnt++;
            for (int i = 0; i < dim; i++) {
                c[i] += v[i];
            }
        }
        if (cnt == 0) {
            return new double[dim];
        }
        for (int i = 0; i < dim; i++) {
            c[i] /= cnt;
        }
        l2Normalize(c);
        return c;
    }

    public static List<String> tokenize(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        String s = text.toLowerCase(Locale.ROOT).trim();
        List<String> out = new ArrayList<>();
        Matcher m = ASCII_TOKENS.matcher(s);
        while (m.find()) {
            out.add(m.group());
        }
        StringBuilder cjk = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch >= '\u4e00' && ch <= '\u9fff') {
                cjk.append(ch);
            }
        }
        String compact = cjk.toString();
        for (int i = 0; i < compact.length(); i++) {
            out.add(String.valueOf(compact.charAt(i)));
            if (i + 1 < compact.length()) {
                out.add(compact.substring(i, i + 2));
            }
        }
        return out;
    }

    private static void l2Normalize(double[] v) {
        double n = 0.0;
        for (double x : v) {
            n += x * x;
        }
        if (n < 1e-12) {
            return;
        }
        double inv = 1.0 / Math.sqrt(n);
        for (int i = 0; i < v.length; i++) {
            v[i] *= inv;
        }
    }
}
