package com.coding.service;

import com.coding.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 本地磁盘图片存储（头像、书籍封面等）
 */
@Service
@RequiredArgsConstructor
public class LocalImageStorageService {

    private static final Set<String> ALLOWED_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    )));

    private final AppProperties appProperties;

    /**
     * @return 可访问的相对路径，如 /upload/book-cover/xxx.jpg
     * @throws IllegalArgumentException 校验失败
     */
    public String save(MultipartFile file, String subDir, long maxBytes) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择图片文件");
        }
        if (file.getSize() > maxBytes) {
            long mb = maxBytes / (1024 * 1024);
            throw new IllegalArgumentException("图片大小不能超过 " + Math.max(1, mb) + "MB");
        }
        String contentType = file.getContentType();
        if (StringUtils.isBlank(contentType) || !ALLOWED_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("仅支持 JPG、PNG、GIF、WebP 图片");
        }
        String ext = extensionForMime(contentType.toLowerCase(Locale.ROOT));
        String base = appProperties.getUploadDir() != null ? appProperties.getUploadDir() : "upload";
        Path targetDir = Paths.get(base).toAbsolutePath().normalize().resolve(subDir);
        Files.createDirectories(targetDir);
        String name = UUID.randomUUID().toString().replace("-", "") + ext;
        Path dest = targetDir.resolve(name);
        file.transferTo(dest.toFile());
        return "/upload/" + subDir + "/" + name;
    }

    private static String extensionForMime(String mime) {
        switch (mime) {
            case "image/jpeg":
                return ".jpg";
            case "image/png":
                return ".png";
            case "image/gif":
                return ".gif";
            case "image/webp":
                return ".webp";
            default:
                return ".bin";
        }
    }
}
