package com.arremateai.userprofile.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Deve retornar 400 ao tratar BusinessException com mensagem correta")
    void deveRetornar400AoTratarBusinessException() {
        var resultado = handler.handleBusiness(new BusinessException("erro de negócio teste"));

        assertThat(resultado.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(resultado.getBody()).containsEntry("error", "erro de negócio teste");
        assertThat(resultado.getBody()).containsKey("timestamp");
        assertThat(resultado.getBody()).containsKey("status");
    }

    @Test
    @DisplayName("Deve retornar 400 ao tratar IllegalArgumentException com mensagem padrão")
    void deveRetornar400AoTratarIllegalArgumentException() {
        var resultado = handler.handleIllegalArgument(new IllegalArgumentException("argumento inválido"));

        assertThat(resultado.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(resultado.getBody()).containsKey("error");
        assertThat(resultado.getBody()).containsEntry("status", 400);
    }

    @Test
    @DisplayName("Deve retornar 409 ao tratar IllegalStateException")
    void deveRetornar409AoTratarIllegalStateException() {
        var resultado = handler.handleIllegalState(new IllegalStateException("estado inválido"));

        assertThat(resultado.getStatusCode().value()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(resultado.getBody()).containsKey("error");
        assertThat(resultado.getBody()).containsEntry("status", 409);
    }

    @Test
    @DisplayName("Deve retornar 400 ao tratar MaxUploadSizeExceededException")
    void deveRetornar400AoTratarMaxUploadSizeExceededException() {
        var resultado = handler.handleMaxUploadSize(new MaxUploadSizeExceededException(5 * 1024 * 1024L));

        assertThat(resultado.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(resultado.getBody()).containsKey("error");
        assertThat(resultado.getBody()).containsKey("timestamp");
    }

    @Test
    @DisplayName("Deve retornar 500 ao tratar Exception genérica")
    void deveRetornar500AoTratarExcecaoGenerica() {
        var resultado = handler.handleGeneric(new RuntimeException("erro genérico inesperado"));

        assertThat(resultado.getStatusCode().value()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(resultado.getBody()).containsKey("error");
        assertThat(resultado.getBody()).containsEntry("status", 500);
    }

    @Test
    @DisplayName("Deve retornar 400 com erros de campo ao tratar MethodArgumentNotValidException")
    @SuppressWarnings("unchecked")
    void deveRetornar400ComErrosDeCampoAoTratarMethodArgumentNotValidException() {
        var bindingResult = mock(BindingResult.class);
        var erroEmail = new FieldError("objeto", "email", "Email inválido");
        var erroNome = new FieldError("objeto", "nome", "Nome é obrigatório");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(erroEmail, erroNome));

        var ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        var resultado = handler.handleValidation(ex);

        assertThat(resultado.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(resultado.getBody()).containsKey("fieldErrors");
        assertThat(resultado.getBody()).containsKey("timestamp");
        assertThat(resultado.getBody()).containsEntry("status", 400);

        Map<String, String> fieldErrors = (Map<String, String>) resultado.getBody().get("fieldErrors");
        assertThat(fieldErrors).containsEntry("email", "Email inválido");
        assertThat(fieldErrors).containsEntry("nome", "Nome é obrigatório");
    }

    @Test
    @DisplayName("Deve retornar 400 sem erros de campo quando MethodArgumentNotValidException não tiver campos com erro")
    @SuppressWarnings("unchecked")
    void deveRetornar400SemErrosDeCampoQuandoListaVazia() {
        var bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        var ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        var resultado = handler.handleValidation(ex);

        assertThat(resultado.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Map<String, String> fieldErrors = (Map<String, String>) resultado.getBody().get("fieldErrors");
        assertThat(fieldErrors).isEmpty();
    }
}
