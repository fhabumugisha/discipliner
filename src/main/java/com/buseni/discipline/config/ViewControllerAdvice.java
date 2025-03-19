package com.buseni.discipline.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ViewControllerAdvice {

    @ModelAttribute("activePage")
    public String getActivePage(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/") ? path : "/" + path;
    }
} 