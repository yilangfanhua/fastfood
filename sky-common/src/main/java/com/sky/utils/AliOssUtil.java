package com.sky.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.sky.exception.BusinessException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class AliOssUtil {
    private final OSS client;
    private final String bucket;
    private final String publicBaseUrl;
    public AliOssUtil(OSS client,String bucket,String publicBaseUrl) {
        this.client=client; this.bucket=bucket;
        this.publicBaseUrl=publicBaseUrl == null ? "" : publicBaseUrl.replaceAll("/+$","");
    }
    public String upload(String key,byte[] bytes,String contentType) {
        if (client==null) throw new BusinessException(502,"OSS 尚未配置");
        ObjectMetadata metadata=new ObjectMetadata();
        metadata.setContentType(contentType); metadata.setContentLength(bytes.length);
        try (ByteArrayInputStream input=new ByteArrayInputStream(bytes)) {
            PutObjectRequest request=new PutObjectRequest(bucket,key,input);
            request.setMetadata(metadata); client.putObject(request);
            return publicBaseUrl+"/"+key;
        } catch (RuntimeException | IOException e) {
            // Do not propagate SDK exception details: they may contain credentials or request URLs.
            throw new BusinessException(502,"OSS 上传失败");
        }
    }
}
