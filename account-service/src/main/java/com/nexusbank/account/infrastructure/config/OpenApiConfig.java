package com.nexusbank.account.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadatos de la documentación OpenAPI.
 * Los endpoints NO se declaran aquí: springdoc los descubre leyendo los @RestController y sus DTOs.
 *
 * Swagger UI:  http://localhost:8081/swagger-ui.html  (botón "Authorize" para pegar el JWT)
 * OpenAPI JSON: http://localhost:8081/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearer-jwt";

    @Bean
    public OpenAPI accountServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("NexusBank · Account Service")
                        .version("v1")
                        .description("""
                                Gestión de cuentas y saldos.

                                **Autenticación:** JWT de Keycloak (realm `nexusbank`). Obtén un token con:
                                `POST http://localhost:8180/auth/realms/nexusbank/protocol/openid-connect/token`
                                (form: `grant_type=password`, `client_id=nexusbank-web`, `username`, `password`)
                                y pégalo en **Authorize**.

                                **Errores** con formato RFC 7807 (ProblemDetail):
                                `400` petición mal formada · `401` sin token o inválido · `403` sin rol ·
                                `404` cuenta no existe o no es tuya · `409` conflicto de concurrencia ·
                                `422` regla de negocio violada.
                                """))
                // Declara el esquema "Bearer JWT" y lo exige en todos los endpoints
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
