package cl.duoc.gaselvolcan;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiModels.ErrorResponse> validation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst().orElse("Datos inválidos");
        return ResponseEntity.badRequest().body(new ApiModels.ErrorResponse(detail));
    }
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiModels.ErrorResponse> known(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
            .body(new ApiModels.ErrorResponse(ex.getReason() == null ? "Solicitud inválida" : ex.getReason()));
    }
    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ApiModels.ErrorResponse> credentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiModels.ErrorResponse("Credenciales incorrectas"));
    }
}
