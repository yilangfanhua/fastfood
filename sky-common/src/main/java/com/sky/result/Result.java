package com.sky.result;

public record Result<T>(int code, String msg, T data) {
    public static <T> Result<T> success(T data) { return new Result<>(1, null, data); }
    public static Result<Void> success() { return success(null); }
    public static Result<Void> error(String message) { return new Result<>(0, message, null); }
}
