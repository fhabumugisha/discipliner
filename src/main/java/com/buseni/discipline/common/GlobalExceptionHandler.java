package com.buseni.discipline.common;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.buseni.discipline.common.exception.BusinessException;
import com.buseni.discipline.common.exception.InvalidOperationException;
import com.buseni.discipline.common.exception.ResourceNotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFoundException(
            ResourceNotFoundException ex, 
            HttpServletRequest request,
            Model model) {
        log.error("Resource not found exception: {}", ex.getMessage());
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getArgs(), 
                LocaleContextHolder.getLocale());
        model.addAttribute("error", message);
        model.addAttribute("status", 404);
        return "error/404";
    }

    @ExceptionHandler(InvalidOperationException.class)
    public String handleInvalidOperationException(
            InvalidOperationException ex, 
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        log.error("Invalid operation exception: {}", ex.getMessage());
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getArgs(), 
                LocaleContextHolder.getLocale());
        redirectAttributes.addFlashAttribute("error", message);
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(
            BusinessException ex, 
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        log.error("Business exception: {}", ex.getMessage());
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getArgs(), 
                LocaleContextHolder.getLocale());
        redirectAttributes.addFlashAttribute("error", message);
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(
            AccessDeniedException ex, 
            HttpServletRequest request,
            Model model) {
        log.error("Access denied exception: {}", ex.getMessage());
        String message = messageSource.getMessage("error.access.denied", null, 
                LocaleContextHolder.getLocale());
        model.addAttribute("error", message);
        model.addAttribute("status", 403);
        return "error/403";
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public String handleValidationException(
            Exception ex, 
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        log.error("Validation exception: {}", ex.getMessage());
        String message;
        if (ex instanceof MethodArgumentNotValidException) {
            FieldError fieldError = ((MethodArgumentNotValidException) ex).getBindingResult().getFieldError();
            message = fieldError != null ? 
                    messageSource.getMessage(fieldError, LocaleContextHolder.getLocale()) :
                    messageSource.getMessage("error.validation", null, LocaleContextHolder.getLocale());
        } else {
            FieldError fieldError = ((BindException) ex).getBindingResult().getFieldError();
            message = fieldError != null ? 
                    messageSource.getMessage(fieldError, LocaleContextHolder.getLocale()) :
                    messageSource.getMessage("error.validation", null, LocaleContextHolder.getLocale());
        }
        redirectAttributes.addFlashAttribute("error", message);
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(
            Exception ex, 
            HttpServletRequest request,
            Model model) {
        log.error("Unexpected error occurred", ex);
        String message = messageSource.getMessage("error.unexpected", null, 
                LocaleContextHolder.getLocale());
        model.addAttribute("error", message);
        model.addAttribute("status", 500);
        return "error/500";
    }
} 