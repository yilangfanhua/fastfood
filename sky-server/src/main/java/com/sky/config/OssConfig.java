package com.sky.config;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.sky.utils.AliOssUtil;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.net.URI;

@Configuration
@EnableConfigurationProperties(OssProperties.class)
public class OssConfig {
    @Bean(destroyMethod="shutdown")
    @ConditionalOnProperty(name="sky.oss.enabled",havingValue="true")
    OSS ossClient(OssProperties properties) {
        if (blank(properties.endpoint()) || blank(properties.bucket()) || blank(properties.accessKeyId())
                || blank(properties.accessKeySecret()) || blank(properties.publicBaseUrl())) {
            throw new IllegalArgumentException("Enabled OSS requires endpoint, bucket, credentials and public base URL");
        }
        URI url=URI.create(properties.publicBaseUrl());
        if (!"https".equalsIgnoreCase(url.getScheme()) || url.getHost()==null || url.getUserInfo()!=null
                || url.getQuery()!=null || url.getFragment()!=null) {
            throw new IllegalArgumentException("OSS public base URL must be an absolute HTTPS URL without credentials/query/fragment");
        }
        ClientBuilderConfiguration config=new ClientBuilderConfiguration();
        config.setConnectionTimeout(5000); config.setSocketTimeout(10000);
        config.setRequestTimeout(15000); config.setRequestTimeoutEnabled(true); config.setMaxErrorRetry(0);
        return new OSSClientBuilder().build(properties.endpoint(),properties.accessKeyId(),properties.accessKeySecret(),config);
    }
    @Bean AliOssUtil aliOssUtil(ObjectProvider<OSS> client,OssProperties properties) {
        return new AliOssUtil(client.getIfAvailable(),properties.bucket(),properties.publicBaseUrl());
    }
    private boolean blank(String value) { return value==null || value.isBlank(); }
}
