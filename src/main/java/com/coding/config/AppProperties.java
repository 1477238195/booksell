package com.coding.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Set;

/**
 * @author guanweiming
 */
@Data
@ConfigurationProperties("app")
public class AppProperties {
    /**
     * appId
     */
    private String appId;

    private String endPoint = "http://117.159.17.27:10308";
    private String ak = "admin";
    private String sk = "请搭建自己的minio服务器";
    private String bn = "avatar";
    private Set<String> whiteList;

    private String wsUrl;

    /**
     * 本地上传根目录（相对运行目录），头像保存在 {uploadDir}/avatar/
     */
    private String uploadDir = "upload";
}
