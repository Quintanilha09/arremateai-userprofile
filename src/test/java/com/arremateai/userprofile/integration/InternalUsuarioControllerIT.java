package com.arremateai.userprofile.integration;

import com.arremateai.userprofile.dto.UsuarioSyncRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DisplayName("InternalUsuarioController - Integração")
class InternalUsuarioControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/internal/usuarios sem api-key retorna 403")
    void deveRejeitarSemApiKey() throws Exception {
        UsuarioSyncRequest request = new UsuarioSyncRequest(
                UUID.randomUUID(), "Teste", "teste@x.com", null, null, "COMPRADOR"
        );
        mockMvc.perform(post("/api/internal/usuarios")
                        .header("X-Gateway-Auth", "test-gateway-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/internal/usuarios com api-key correta cria usuário (201)")
    void deveSincronizarUsuario() throws Exception {
        UsuarioSyncRequest request = new UsuarioSyncRequest(
                UUID.randomUUID(),
                "Usuário Sync",
                "sync-" + UUID.randomUUID() + "@teste.com",
                null, null, "COMPRADOR"
        );
        mockMvc.perform(post("/api/internal/usuarios")
                        .header("X-Gateway-Auth", "test-gateway-secret")
                        .header("X-Internal-Api-Key", "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/internal/usuarios com payload inválido retorna 400")
    void deveRejeitarPayloadInvalido() throws Exception {
        String invalidJson = "{\"id\":null,\"nome\":\"\",\"email\":\"x\",\"tipo\":\"\"}";
        mockMvc.perform(post("/api/internal/usuarios")
                        .header("X-Gateway-Auth", "test-gateway-secret")
                        .header("X-Internal-Api-Key", "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/internal/usuarios para usuário já existente retorna 200")
    void deveRetornarOkParaUsuarioExistente() throws Exception {
        UUID id = UUID.randomUUID();
        UsuarioSyncRequest request = new UsuarioSyncRequest(
                id, "Usuário Dup", "dup-" + id + "@teste.com", null, null, "COMPRADOR"
        );
        mockMvc.perform(post("/api/internal/usuarios")
                        .header("X-Gateway-Auth", "test-gateway-secret")
                        .header("X-Internal-Api-Key", "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/internal/usuarios")
                        .header("X-Gateway-Auth", "test-gateway-secret")
                        .header("X-Internal-Api-Key", "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
