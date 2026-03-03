package com.pharmacy.controller.web;

import com.pharmacy.dto.UserResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserWebController {

    private final UserManagementService userManagementService;

    @GetMapping
    public String listUsers(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        model.addAttribute("users", userManagementService.listPharmacyUsers(principal));
        model.addAttribute("activePage", "users");
        return "users";
    }

    @GetMapping("/new")
    public String newUserForm(Model model) {
        model.addAttribute("activePage", "users");
        model.addAttribute("isEdit", false);
        return "user-form";
    }

    @GetMapping("/edit/{id}")
    public String editUserForm(@AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long id,
            Model model) {
        UserResponse user = userManagementService.getUserById(principal, id);
        model.addAttribute("user", user);
        model.addAttribute("isEdit", true);
        model.addAttribute("activePage", "users");
        return "user-form";
    }
}
