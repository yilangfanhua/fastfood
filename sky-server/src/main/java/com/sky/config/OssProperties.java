package com.sky.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("sky.oss")
public record OssProperties(boolean enabled,String endpoint,String bucket,String accessKeyId,
                            String accessKeySecret,String publicBaseUrl) {
    @Override public String toString() { return "OssProperties[enabled="+enabled+", credentials=[REDACTED]]"; }
}
