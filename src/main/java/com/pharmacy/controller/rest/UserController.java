package com.pharmacy.controller.rest;

import com.pharmacy.dto.CreateUserRequest;
import com.pharmacy.dto.UserResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Users", description = "User management within the authenticated pharmacy")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserManagementService userManagementService;

    @Operation(summary = "Create user", description = "Creates a new staff user scoped to the same pharmacy as the authenticated admin.")
    @PostMapping
    public UserResponse createUser(@AuthenticationPrincipal AppUserPrincipal principal,
                                   @Valid @RequestBody CreateUserRequest request) {
        return userManagementService.createUserUnderSamePharmacy(principal, request);
    }

    @Operation(summary = "List pharmacy users", description = "Returns all staff accounts registered under the authenticated user's pharmacy.")
    @GetMapping
    public List<UserResponse> listUsers(@AuthenticationPrincipal AppUserPrincipal principal) {
        return userManagementService.listPharmacyUsers(principal);
    }
}
