//package com.conectebem.backend.exception;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.time.LocalDateTime;
//import java.util.LinkedHashMap;
//import java.util.Map;
//
///**
// * Padroniza as respostas de erro da API.
// *
// * <p>Além de recurso não encontrado, trata validações de DTO e conflitos das
// * regras únicas do banco usadas no módulo ONG.</p>
// */
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(ResourceNotFoundException.class)
//    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
//        return montarResposta(HttpStatus.NOT_FOUND, ex.getMessage());
//    }
//
//    /** Converte campos inválidos enviados ao controller em HTTP 400. */
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
//        String mensagem = ex.getBindingResult()
//                .getFieldErrors()
//                .stream()
//                .findFirst()
//                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
//                .orElse("Dados inválidos.");
//
//        return montarResposta(HttpStatus.BAD_REQUEST, mensagem);
//    }
//
//    /** Converte violações de unicidade ou chave estrangeira do banco em HTTP 409. */
//    @ExceptionHandler(DataIntegrityViolationException.class)
//    public ResponseEntity<Map<String, Object>> handleConflict(DataIntegrityViolationException ex) {
//        return montarResposta(
//                HttpStatus.CONFLICT,
//                "Já existe uma ONG com este CNPJ ou vinculada a este usuário."
//        );
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Map<String, Object>> handleGenerico(Exception ex) {
//        return montarResposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor.");
//    }
//
//    private ResponseEntity<Map<String, Object>> montarResposta(HttpStatus status, String mensagem) {
//        Map<String, Object> corpo = new LinkedHashMap<>();
//        corpo.put("timestamp", LocalDateTime.now());
//        corpo.put("status", status.value());
//        corpo.put("erro", status.getReasonPhrase());
//        corpo.put("mensagem", mensagem);
//        return ResponseEntity.status(status).body(corpo);
//    }
//}
//
//@ExceptionHandler(IllegalArgumentException.class)
//public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
//    Map<String, Object> body = new HashMap<>();
//    body.put("timestamp", LocalDateTime.now());
//    body.put("status", HttpStatus.BAD_REQUEST.value());
//    body.put("erro", "Bad Request");
//    body.put("mensagem", ex.getMessage());
//
//    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
//}

package com.conectebem.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Padroniza as respostas de erro da API.
 *
 * <p>Trata recurso não encontrado, validações de DTO, argumentos inválidos e
 * conflitos das regras de integridade do banco.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return montarResposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Converte campos inválidos enviados ao controller em HTTP 400.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .orElse("Dados inválidos.");

        return montarResposta(HttpStatus.BAD_REQUEST, mensagem);
    }

    /**
     * Converte argumentos inválidos lançados pelos services em HTTP 400.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return montarResposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Converte violações de unicidade ou chave estrangeira do banco em HTTP 409.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade de dados: {}", ex.getMostSpecificCause().getMessage());

        String causa = ex.getMostSpecificCause().getMessage();
        String mensagem = (causa != null && causa.toLowerCase().contains("cnpj"))
                ? "Já existe uma ONG com este CNPJ."
                : "A operação viola uma regra de integridade dos dados.";

        return montarResposta(HttpStatus.CONFLICT, mensagem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenerico(Exception ex) {
        log.error("Erro inesperado", ex);
        return montarResposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor.");
    }

    private ResponseEntity<Map<String, Object>> montarResposta(HttpStatus status, String mensagem) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("timestamp", OffsetDateTime.now());
        corpo.put("status", status.value());
        corpo.put("erro", status.getReasonPhrase());
        corpo.put("mensagem", mensagem);
        return ResponseEntity.status(status).body(corpo);
    }
}
