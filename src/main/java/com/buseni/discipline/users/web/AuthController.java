package com.buseni.discipline.users.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.buseni.discipline.users.dto.RegisterRequest;
import com.buseni.discipline.users.dto.UserDto;
import com.buseni.discipline.users.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest("", "", "", ""));
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute RegisterRequest registerRequest,
            BindingResult result,
            Model model) {
        
        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.register(registerRequest);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        UserDto user = authService.getCurrentUser();
        model.addAttribute("user", user);
        return "dashboard";
    }
} 