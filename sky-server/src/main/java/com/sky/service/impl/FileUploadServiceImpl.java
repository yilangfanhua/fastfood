package com.sky.service.impl;

import com.sky.utils.AliOssUtil;
import java.time.Clock;
import org.springframework.web.multipart.MultipartFile;
import com.sky.service.FileUploadService;
import com.sky.exception.BusinessException;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

@Service
public class FileUploadServiceImpl implements FileUploadService {
    private static final long MAX_BYTES=5L*1024*1024;
    private static final long MAX_PIXELS=20_000_000;
    private final AliOssUtil oss;
    private final Clock clock;
    public FileUploadServiceImpl(AliOssUtil oss,Clock clock) { this.oss=oss; this.clock=clock; }

    @Override
    public String upload(MultipartFile file) {
        if (file==null || file.isEmpty() || file.getSize()>MAX_BYTES) throw invalid();
        String declared=file.getContentType();
        if (!"image/png".equalsIgnoreCase(declared) && !"image/jpeg".equalsIgnoreCase(declared)) throw invalid();
        try {
            byte[] bytes=file.getBytes();
            if (bytes.length==0 || bytes.length>MAX_BYTES) throw invalid();
            String actual=detectImage(bytes);
            if (!actual.equalsIgnoreCase(declared)) throw invalid();
            String key=LocalDate.now(clock)+"/"+UUID.randomUUID()+(actual.equals("image/png")?".png":".jpg");
            return oss.upload(key,bytes,actual);
        } catch (IOException e) {
            throw invalid();
        }
    }

    private String detectImage(byte[] bytes) throws IOException {
        try (ImageInputStream input=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (input==null) throw invalid();
            Iterator<ImageReader> readers=ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw invalid();
            ImageReader reader=readers.next();
            try {
                reader.setInput(input,true,true);
                String format=reader.getFormatName();
                String type;
                if ("png".equalsIgnoreCase(format)) type="image/png";
                else if ("jpeg".equalsIgnoreCase(format) || "jpg".equalsIgnoreCase(format)) type="image/jpeg";
                else throw invalid();
                int width=reader.getWidth(0), height=reader.getHeight(0);
                if (width<=0 || height<=0 || (long)width*height>MAX_PIXELS) throw invalid();
                if (reader.read(0)==null) throw invalid();
                return type;
            } finally { reader.dispose(); }
        }
    }

    private BusinessException invalid() { return new BusinessException(400,"仅支持5 MiB以内的有效 JPEG/PNG 图片（最多2000万像素）"); }
}
