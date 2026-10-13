package com.sky.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OssConfigTest {
    @Test void enabledStorageRequiresCompleteConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new OssConfig().ossClient(new OssProperties(true,"","","","","")));
    }
    @Test void publicUrlMustBeHttpsWithoutCredentialsOrSignatures() {
        for (String url:new String[]{"http://images.example.com","https://user:pass@images.example.com","https://images.example.com?secret=value","https://images.example.com#fragment"}) {
            assertThrows(IllegalArgumentException.class, () -> new OssConfig().ossClient(new OssProperties(true,"https://oss-cn-hangzhou.aliyuncs.com","bucket","id","key",url)));
        }
    }
}
