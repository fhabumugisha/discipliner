package com.buseni.discipline.users.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.buseni.discipline.users.dto.RegisterRequest;
import com.buseni.discipline.users.dto.UserDto;
import com.buseni.discipline.users.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private static final String AUTH_REGISTER_PAGE = "auth/register";

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(@RequestParam(required = false) String email,
                             @RequestParam(required = false) String phone,
                             Model model) {
        log.debug("Register page with email: {} and phone: {}", email, phone);
        model.addAttribute("registerRequest", new RegisterRequest("", "", email, phone, ""));
        return AUTH_REGISTER_PAGE;
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute RegisterRequest registerRequest,
            BindingResult result,
            Model model) {
        
        if (result.hasErrors()) {
            return AUTH_REGISTER_PAGE;
        }

        try {
            authService.register(registerRequest);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return AUTH_REGISTER_PAGE;
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        UserDto user = authService.getCurrentUser();
        model.addAttribute("user", user);
        return "dashboard";
    }
} 