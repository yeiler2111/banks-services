package com.nexusbank.account.infrastructure.adapter.in.web;

import com.nexusbank.account.domain.exception.AccountNotFoundException;
import com.nexusbank.account.domain.exception.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Traduce excepciones a respuestas HTTP con formato RFC 7807 (ProblemDetail).
 *
 * <p>Heredar de ResponseEntityExceptionHandler ya cubre los 400 de Spring MVC
 * (JSON mal formado, falta una cabecera, UUID inválido en la URL...).
 * Spring elige el @ExceptionHandler MÁS ESPECÍFICO: AccountNotFoundException (404)
 * gana a DomainException (422) aunque herede de ella.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 404: el recurso no existe. */
    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleNotFound(AccountNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Cuenta no encontrada", ex.getMessage());
    }

    /** 422: petición bien formada, pero viola una regla de negocio (saldo, bloqueo, monto...). */
    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException ex) {
        return problem(HttpStatus.UNPROCESSABLE_CONTENT, "Regla de negocio violada", ex.getMessage());
    }

    /** 409: otra transacción modificó la cuenta a la vez (@Version). El cliente puede reintentar. */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleConcurrency(OptimisticLockingFailureException ex) {
        return problem(HttpStatus.CONFLICT, "Conflicto de concurrencia",
                "La cuenta fue modificada por otra operación. Vuelve a intentarlo.");
    }

    /** 500: error inesperado. Se registra completo en el log, pero NO se exponen detalles al cliente. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Error inesperado", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error inesperado. Inténtalo más tarde.");
    }

    /** 400 de validación (@Valid): añade qué campo falló y por qué. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> String.valueOf(fe.getDefaultMessage()),
                        (first, second) -> first));

        ProblemDetail body = problem(HttpStatus.BAD_REQUEST, "Petición inválida",
                "Uno o más campos no son válidos");
        body.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        return pd;
    }
}
