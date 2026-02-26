package com.pharmacy.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Production-level OpenAPI / Swagger configuration.
 *
 * <p>
 * Declares a global JWT Bearer security scheme so every secured endpoint
 * shows a padlock in Swagger UI and requires a token to be tried out.
 * </p>
 *
 * <p>
 * Public endpoints (e.g. /api/auth/**) override this at the operation level
 * via {@code @SecurityRequirements({})} to remove the lock icon.
 * </p>
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI pharmacyOpenAPI() {
        return new OpenAPI()
                // ── API metadata ──────────────────────────────────────────
                .info(new Info()
                        .title("Pharmacy Management System API")
                        .description("""
                                REST API for the Pharmacy Management System.
                                
                                **Authentication**: All secured endpoints require a JWT Bearer token.
                                Obtain a token via `POST /api/auth/login`, then click the
                                **Authorize** button above and enter: `<your-token>`.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Pharmacy IT")
                                .email("support@pharmacy.local"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://pharmacy.local/license")))

                // ── Global security scheme (JWT Bearer) ───────────────────
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH_SCHEME,
                                new SecurityScheme()
                                        .name(BEARER_AUTH_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(
                                                "Paste the JWT token obtained from `/api/auth/login`. " +
                                                        "The `Bearer ` prefix is added automatically.")));
    }
}
