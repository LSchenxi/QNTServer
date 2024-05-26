package com.ninelock.api.core.handler;

import com.ninelock.api.response.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author gongym
 * @version 创建时间: 2023-12-13 16:59
 */
@Slf4j
@AllArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public Result<?> missingServletRequestParameterExceptionHandler(MissingServletRequestParameterException ex) {
        log.error("请求参数缺失", ex);
        return Result.error400(String.format("请求参数缺失: %s", ex.getParameterName()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<?> methodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException ex) {
        log.error("请求参数类型错误", ex);
        return Result.error400(String.format("请求参数类型错误: %s", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> methodArgumentNotValidExceptionExceptionHandler(MethodArgumentNotValidException ex) {
        log.error("参数校验不正确", ex);
        final StringBuilder buf = new StringBuilder();
        final BindingResult result = ex.getBindingResult();
        if (result.hasErrors()) {
            final List<ObjectError> errors = result.getAllErrors();
            for (int i = 0; i < errors.size(); i++) {
                final FieldError fieldError = (FieldError) errors.get(i);
                buf.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage());
                if (i < errors.size() - 1) {
                    buf.append(", ");
                }
            }
        }
        return Result.error400(String.format("参数校验不正确: %s", buf));
    }

    @ExceptionHandler(BindException.class)
    public Result<?> bindExceptionHandler(BindException ex) {
        log.error("参数绑定不正确", ex);
        final StringBuilder buf = new StringBuilder();
        final BindingResult result = ex.getBindingResult();
        if (result.hasErrors()) {
            final List<FieldError> errors = result.getFieldErrors();
            for (int i = 0; i < errors.size(); i++) {
                buf.append(errors.get(i).getField())
                        .append(": ")
                        .append(errors.get(i).getDefaultMessage());
                if (i < errors.size() - 1) {
                    buf.append(", ");
                }
            }
        }
        return Result.error400(String.format("参数绑定不正确: %s", buf));
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public Result<?> constraintViolationExceptionHandler(ConstraintViolationException ex) {
        log.error("参数校验不正确", ex);
        final StringBuilder buf = new StringBuilder();
        final Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        if (!violations.isEmpty()) {
            final Iterator<ConstraintViolation<?>> iterator = violations.iterator();
            while (iterator.hasNext()) {
                final ConstraintViolation<?> violation = iterator.next();
                buf.append(violation.getMessage());
                if (iterator.hasNext()) {
                    buf.append(", ");
                }
            }
        }

        return Result.error400(String.format("参数校验不正确: %s", buf));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<?> noHandlerFoundExceptionHandler(HttpServletRequest req, NoHandlerFoundException ex) {
        log.error("请求地址不存在", ex);
        return Result.error404(String.format("请求地址不存在: %s %s", req.getRequestURL(), ex.getRequestURL()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> httpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException ex) {
        log.error("请求方法不正确", ex);
        return Result.error405(String.format("请求方法不正确: %s", ex.getMessage()));
    }

    @ExceptionHandler(value = Exception.class)
    public Result<?> defaultExceptionHandler(HttpServletRequest req, Throwable ex) {
        log.error("Internal Server Error", ex);
        // 插入异常日志
        // this.createExceptionLog(req, ex);
        return Result.error500(String.format("Internal Server Error: %s %s", req.getRequestURL(), ex.getMessage()));
    }
}
