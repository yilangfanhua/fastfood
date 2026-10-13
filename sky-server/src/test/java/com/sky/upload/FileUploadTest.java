package com.sky.upload;

import com.sky.exception.BusinessException;
import com.sky.service.impl.FileUploadServiceImpl;
import com.sky.utils.AliOssUtil;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.*;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FileUploadTest {
    private final AliOssUtil oss = mock(AliOssUtil.class);
    private final FileUploadServiceImpl service = new FileUploadServiceImpl(oss, Clock.fixed(Instant.parse("2026-10-13T00:00:00Z"),ZoneId.of("Asia/Shanghai")));

    private byte[] image(String format) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2,2,BufferedImage.TYPE_INT_RGB),format,bytes);
        return bytes.toByteArray();
    }
    @Test void realPngUsesUniqueObjectKeysRegardlessOfOriginalName() throws Exception {
        when(oss.upload(anyString(),any(byte[].class),eq("image/png"))).thenAnswer(call -> "https://images.example.com/"+call.getArgument(0));
        MockMultipartFile file=new MockMultipartFile("file","../../same.png","image/png",image("png"));
        String first=service.upload(file), second=service.upload(file);
        assertTrue(first.matches("https://images.example.com/2026-10-13/[a-f0-9-]+\\.png"));
        assertNotEquals(first,second);
    }
    @Test void realJpegIsAccepted() throws Exception {
        when(oss.upload(anyString(),any(byte[].class),eq("image/jpeg"))).thenReturn("https://images.example.com/image.jpg");
        assertEquals("https://images.example.com/image.jpg",service.upload(new MockMultipartFile("file","x.jpg","image/jpeg",image("jpg"))));
    }
    @Test void spoofedImageIsRejectedBeforeStorage() {
        BusinessException e=assertThrows(BusinessException.class, () -> service.upload(new MockMultipartFile("file","x.png","image/png","text".getBytes())));
        assertEquals(400,e.getStatus()); verifyNoInteractions(oss);
    }
    @Test void mismatchedDeclaredTypeIsRejected() throws Exception {
        byte[] bytes=image("jpg");
        assertEquals(400,assertThrows(BusinessException.class, () -> service.upload(new MockMultipartFile("file","x.png","image/png",bytes))).getStatus());
        verifyNoInteractions(oss);
    }
    @Test void emptyAndOversizedFilesAreRejected() {
        assertEquals(400,assertThrows(BusinessException.class, () -> service.upload(new MockMultipartFile("file",new byte[0]))).getStatus());
        assertEquals(400,assertThrows(BusinessException.class, () -> service.upload(new MockMultipartFile("file","large.png","image/png",new byte[5*1024*1024+1]))).getStatus());
        verifyNoInteractions(oss);
    }
    @Test void upstreamFailureNeverReturnsSuccessfulUrl() throws Exception {
        when(oss.upload(anyString(),any(byte[].class),anyString())).thenThrow(new BusinessException(502,"Upload failed"));
        byte[] bytes=image("png");
        assertEquals(502,assertThrows(BusinessException.class, () -> service.upload(new MockMultipartFile("file","x.png","image/png",bytes))).getStatus());
    }
}
