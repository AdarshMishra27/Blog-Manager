package com.example.BlogManager.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL) // Only include non-null fields in JSON
public class ApiResponseWrapper<T> {
    private LocalDateTime timestamp;
    private int status;
    private String message;
    private String exceptionError;
    private T data; // success payload

    public ApiResponseWrapper(LocalDateTime timestamp, int status, String message, String exceptionError, T data) {
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
        this.exceptionError = exceptionError;
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getExceptionError() {
        return exceptionError;
    }

    public void setExceptionError(String exceptionError) {
        this.exceptionError = exceptionError;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
