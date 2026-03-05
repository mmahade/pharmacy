package com.pharmacy.controller.web;

import com.pharmacy.dto.LoginRequest;
import com.pharmacy.dto.RegisterPharmacyRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Registration {

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterPharmacyRequest("", "", "", "", "", ""));
        }
        return "registration";
    }

    @GetMapping("/login")
    public String showLoginForm(Model model, HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt_token".equals(cookie.getName())) {
                    return "redirect:/dashboard";
                }
            }
        }
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginRequest("", ""));
        }
        return "login";
    }
}
