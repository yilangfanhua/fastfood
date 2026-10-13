package com.sky.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.sky.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AliOssUtilTest {
    @Test void objectMetadataAndPublicUrlAreCorrect() {
        OSS client=mock(OSS.class);
        AliOssUtil util=new AliOssUtil(client,"test-bucket","https://images.example.com/");
        assertEquals("https://images.example.com/a.png",util.upload("a.png",new byte[]{1,2},"image/png"));
        ArgumentCaptor<PutObjectRequest> request=ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(client).putObject(request.capture());
        assertEquals("test-bucket",request.getValue().getBucketName());
        assertEquals(2,request.getValue().getMetadata().getContentLength());
        assertEquals("image/png",request.getValue().getMetadata().getContentType());
    }
    @Test void sdkFailureIsSanitized() {
        OSS client=mock(OSS.class);
        when(client.putObject(any(PutObjectRequest.class))).thenThrow(new RuntimeException("sensitive upstream details"));
        BusinessException error=assertThrows(BusinessException.class, () -> new AliOssUtil(client,"test-bucket","https://images.example.com").upload("a.png",new byte[]{1},"image/png"));
        assertEquals(502,error.getStatus()); assertFalse(error.getMessage().contains("sensitive"));
    }
    @Test void unconfiguredStorageFailsExplicitly() {
        assertEquals(502,assertThrows(BusinessException.class, () -> new AliOssUtil(null,"","").upload("a.png",new byte[]{1},"image/png")).getStatus());
    }
}
