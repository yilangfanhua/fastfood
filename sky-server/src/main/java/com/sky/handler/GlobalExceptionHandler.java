package com.sky.handler;

import com.sky.exception.BusinessException;
import com.sky.result.Result;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<Result<Void>> business(BusinessException e) { return error(e.getStatus(), e.getMessage()); }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class,
            HandlerMethodValidationException.class, HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestPartException.class, MaxUploadSizeExceededException.class, MultipartException.class,
            HttpMediaTypeNotSupportedException.class, ArithmeticException.class})
    ResponseEntity<Result<Void>> invalid(Exception e) { return error(400, "请求参数无效"); }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<Result<Void>> methodNotAllowed(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(405).header("Allow",String.join(", ",e.getSupportedMethods()==null?new String[0]:e.getSupportedMethods()))
                .body(Result.error("请求方法不支持"));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    ResponseEntity<Result<Void>> duplicate() { return error(409, "名称或用户名已存在"); }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Result<Void>> integrity() { return error(400, "数据不符合约束"); }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<Result<Void>> notFound() { return error(404, "资源不存在"); }

    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<Result<Void>> database() { return error(500, "数据库操作失败"); }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Result<Void>> unexpected() { return error(500, "服务器内部错误"); }

    private ResponseEntity<Result<Void>> error(int status, String message) {
        return ResponseEntity.status(status).body(Result.error(message));
    }
}
