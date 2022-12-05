package com.voiceroom.mic.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@Configuration
@RestControllerAdvice
public class VoiceRoomExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({VoiceRoomException.class})
    public ResponseEntity<Object> voiceRoomExceptionHandler(VoiceRoomException exception,
            WebRequest request) {
        if (log.isInfoEnabled()) {
            log.info("[BizException]业务异常信息 ex={}", exception.getMessage(), exception);
        }
        HttpHeaders headers = new HttpHeaders();
        ExceptionResult response =
                new ExceptionResult(exception.getCode(), exception.getMessage());
        return handleExceptionInternal(exception, response, headers, exception.getHttpStatus(),
                request);
    }

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<Object> runtimeExceptionHandler(RuntimeException exception,
            WebRequest request) {
        HttpHeaders headers = new HttpHeaders();
        ExceptionResult response =
                new ExceptionResult("000500", exception.getMessage());
        return handleExceptionInternal(exception, response, headers, HttpStatus.INTERNAL_SERVER_ERROR,
                request);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Object> illegalArgumentExceptionHandler(IllegalArgumentException exception,
            WebRequest request) {
        if (log.isInfoEnabled()) {
            log.info("[BizException]业务异常信息 ex={}", exception.getMessage(), exception);
        }
        HttpHeaders headers = new HttpHeaders();
        ExceptionResult response =
                new ExceptionResult("000400", exception.getMessage());
        return handleExceptionInternal(exception, response, headers, HttpStatus.BAD_REQUEST,
                request);
    }

    @Override protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        ExceptionResult response =
                new ExceptionResult("000400",
                        "Request body not readable.Please check content type is correct!");
        return handleExceptionInternal(ex,
                response, headers, status, request);
    }

    @Override protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        ExceptionResult response =
                new ExceptionResult("000405",
                        "Request method not support.Please check request method is correct!");
        return handleExceptionInternal(ex, response, headers, status, request);
    }

    @Override protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        ExceptionResult response =
                new ExceptionResult("000400",
                        "Request param is missing.Please check request param is exist!");
        return handleExceptionInternal(ex, response, headers, status, request);
    }

    @Override protected ResponseEntity<Object> handleServletRequestBindingException(
            ServletRequestBindingException ex, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        ExceptionResult response =
                new ExceptionResult("000400",
                        "Request bind miss param.Please check param is correct!");
        return handleExceptionInternal(ex, response, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        ExceptionResult response =
                new ExceptionResult("000400",
                        "Request path variable is missing.Please check request path is correct!");
        return handleExceptionInternal(ex, response, headers, status, request);
    }

    @Override protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        ExceptionResult response =
                new ExceptionResult("000400",
                        "Request media type not supported.Please check media type is correct!");
        return handleExceptionInternal(ex, response, headers, status, request);
    }


}
