package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/common")
@Tag(name="公共接口")
public class CommonController {
    private final FileUploadService uploads;
    public CommonController(FileUploadService uploads) { this.uploads=uploads; }
    @PostMapping(value="/upload",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary="上传 JPEG/PNG 图片")
    public Result<String> upload(@RequestPart("file") MultipartFile file) { return Result.success(uploads.upload(file)); }
}
