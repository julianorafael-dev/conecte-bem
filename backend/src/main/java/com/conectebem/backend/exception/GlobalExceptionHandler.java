package com.conectebem.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Padroniza as respostas de erro da API.
 *
 * <p>Além de recurso não encontrado, trata validações de DTO e conflitos das
 * regras de integridade do banco.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return montarResposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** Converte campos inválidos enviados ao controller em HTTP 400. */
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
     * Trata JSON malformado e valores que não podem ser convertidos para os
     * tipos esperados, como uma data enviada fora do formato ISO.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        return montarResposta(
                HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido. Verifique o JSON e os formatos dos campos."
        );
    }

    /** Trata parâmetros de rota ou query string enviados com tipo incompatível. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return montarResposta(
                HttpStatus.BAD_REQUEST,
                "Parâmetro '" + ex.getName() + "' deve ter um valor válido."
        );
    }

    /** Trata parâmetros obrigatórios ausentes sem expor detalhes internos do Spring. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(MissingServletRequestParameterException ex) {
        return montarResposta(
                HttpStatus.BAD_REQUEST,
                "Parâmetro obrigatório ausente: '" + ex.getParameterName() + "'."
        );
    }

    /** Converte violações de unicidade ou chave estrangeira do banco em HTTP 409. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(DataIntegrityViolationException ex) {
        return montarResposta(
                HttpStatus.CONFLICT,
                "Não foi possível concluir a operação porque os dados conflitam com um registro existente."
        );
    }

    /** Converte conflitos previstos no módulo de inscrições em HTTP 409. */
    @ExceptionHandler(InscricaoConflictException.class)
    public ResponseEntity<Map<String, Object>> handleInscricaoConflict(InscricaoConflictException ex) {
        return montarResposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    /** Converte e-mail/senha incorretos no login em HTTP 401. */
    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<Map<String, Object>> handleCredenciaisInvalidas(CredenciaisInvalidasException ex) {
        return montarResposta(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /** Converte erros de regra de negócio (ex: e-mail ou CNPJ já cadastrado) em HTTP 400. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return montarResposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Converte erros de autorização em HTTP 403.
     *
     * <p>Sem este handler, a {@code AccessDeniedException} lançada na camada
     * de serviço seria capturada pelo handler genérico de {@code Exception}
     * e respondida como 500 "Erro interno no servidor", mascarando o erro
     * de autorização.</p>
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return montarResposta(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenerico(Exception ex) {
        return montarResposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor.");
    }

    private ResponseEntity<Map<String, Object>> montarResposta(HttpStatus status, String mensagem) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("timestamp", LocalDateTime.now());
        corpo.put("status", status.value());
        corpo.put("erro", status.getReasonPhrase());
        corpo.put("mensagem", mensagem);
        return ResponseEntity.status(status).body(corpo);
    }

    @ExceptionHandler(CategoriaEmUsoException.class)
    public ResponseEntity<Map<String, Object>> handleCategoriaEmUso(CategoriaEmUsoException ex) {
        return montarResposta(HttpStatus.CONFLICT, ex.getMessage());
    }
}
