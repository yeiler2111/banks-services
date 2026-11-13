package com.nexusbank.account.infrastructure.adapter.in.web;

import com.nexusbank.account.domain.model.Account;
import com.nexusbank.account.domain.model.Money;
import com.nexusbank.account.domain.port.in.GetAccountQuery;
import com.nexusbank.account.domain.port.in.OpenAccountUseCase;
import com.nexusbank.account.domain.port.in.OpenAccountUseCase.OpenAccountCommand;
import com.nexusbank.account.domain.port.in.UpdateBalanceUseCase;
import com.nexusbank.account.infrastructure.adapter.in.web.dto.AccountResponse;
import com.nexusbank.account.infrastructure.adapter.in.web.dto.AmountRequest;
import com.nexusbank.account.infrastructure.adapter.in.web.dto.OpenAccountRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * ADAPTADOR DE ENTRADA (web): traduce HTTP/JSON <-> casos de uso.
 *
 * <p>Responsabilidades: recibir la petición, validar su FORMATO (@Valid), identificar al usuario
 * a partir del JWT, convertir DTO -> dominio, delegar en el puerto de entrada y convertir el
 * resultado -> DTO. Ninguna regla de negocio aquí.
 *
 * <p>Cuando llega aquí, Spring Security YA verificó el token (firma, caducidad, issuer, rol).
 * El usuario es jwt.getSubject() ("sub"): viene firmado por Keycloak, no se puede falsificar.
 */
@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Cuentas", description = "Apertura, consulta y movimientos de saldo (requiere rol ROLE_CUSTOMER)")
public class AccountController {

    private final OpenAccountUseCase openAccountUseCase;
    private final UpdateBalanceUseCase updateBalanceUseCase;
    private final GetAccountQuery getAccountQuery;

    public AccountController(OpenAccountUseCase openAccountUseCase,
                             UpdateBalanceUseCase updateBalanceUseCase,
                             GetAccountQuery getAccountQuery) {
        this.openAccountUseCase = openAccountUseCase;
        this.updateBalanceUseCase = updateBalanceUseCase;
        this.getAccountQuery = getAccountQuery;
    }

    @Operation(summary = "Abrir una cuenta",
            description = "Crea una cuenta con saldo 0 y estado ACTIVE para el usuario del token. "
                    + "Devuelve la URL del recurso en la cabecera Location.")
    @ApiResponse(responseCode = "201", description = "Cuenta creada")
    @ApiResponse(responseCode = "400", description = "Petición inválida", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping
    public ResponseEntity<AccountResponse> open(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
                                                @Valid @RequestBody OpenAccountRequest request) {
        Account account = openAccountUseCase.open(new OpenAccountCommand(jwt.getSubject(), request.type()));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(account.getId()).toUri();
        return ResponseEntity.created(location).body(AccountResponse.from(account));
    }

    @Operation(summary = "Listar mis cuentas")
    @ApiResponse(responseCode = "200", description = "Cuentas del usuario del token (puede ser una lista vacía)")
    @GetMapping
    public List<AccountResponse> myAccounts(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return getAccountQuery.getByUserId(jwt.getSubject()).stream()
                .map(AccountResponse::from)
                .toList();
    }

    @Operation(summary = "Consultar una de mis cuentas")
    @ApiResponse(responseCode = "200", description = "Cuenta encontrada")
    @ApiResponse(responseCode = "404", description = "No existe o no es tuya", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{id}")
    public AccountResponse getById(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
                                   @PathVariable UUID id) {
        return AccountResponse.from(getAccountQuery.getById(id, jwt.getSubject()));
    }

    @Operation(summary = "Depositar", description = "Suma el monto al saldo. La cuenta debe ser tuya y estar ACTIVE.")
    @ApiResponse(responseCode = "200", description = "Depósito realizado; devuelve la cuenta actualizada")
    @ApiResponse(responseCode = "400", description = "Monto inválido (<= 0 o más de 2 decimales)", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "No existe o no es tuya", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Conflicto de concurrencia: reintentar", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "Cuenta bloqueada", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping("/{id}/deposit")
    public AccountResponse deposit(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
                                   @PathVariable UUID id,
                                   @Valid @RequestBody AmountRequest request) {
        return AccountResponse.from(
                updateBalanceUseCase.deposit(id, jwt.getSubject(), Money.of(request.amount())));
    }

    @Operation(summary = "Retirar", description = "Resta el monto del saldo. Nunca deja el saldo en negativo.")
    @ApiResponse(responseCode = "200", description = "Retiro realizado; devuelve la cuenta actualizada")
    @ApiResponse(responseCode = "400", description = "Monto inválido (<= 0 o más de 2 decimales)", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "No existe o no es tuya", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Conflicto de concurrencia: reintentar", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "Saldo insuficiente o cuenta bloqueada", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping("/{id}/withdraw")
    public AccountResponse withdraw(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
                                    @PathVariable UUID id,
                                    @Valid @RequestBody AmountRequest request) {
        return AccountResponse.from(
                updateBalanceUseCase.withdraw(id, jwt.getSubject(), Money.of(request.amount())));
    }
}
