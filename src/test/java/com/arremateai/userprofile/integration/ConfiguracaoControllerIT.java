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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DisplayName("ConfiguracaoController - Integração")
class ConfiguracaoControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID criarUsuario() throws Exception {
        UUID id = UUID.randomUUID();
        UsuarioSyncRequest sync = new UsuarioSyncRequest(
                id, "Config User", "cfg-" + id + "@teste.com", null, null, "COMPRADOR"
        );
        mockMvc.perform(post("/api/internal/usuarios")
                        .header("X-Gateway-Auth", "test-gateway-secret")
                        .header("X-Internal-Api-Key", "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sync)))
                .andExpect(status().isCreated());
        return id;
    }

    @Test
    @DisplayName("GET /api/configuracoes sem gateway auth retorna 401")
    void deveRejeitarSemGatewayAuth() throws Exception {
        mockMvc.perform(get("/api/configuracoes")
                        .header("X-User-Id", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/configuracoes retorna configurações padrão")
    void deveRetornarConfiguracoes() throws Exception {
        UUID id = criarUsuario();
        mockMvc.perform(get("/api/configuracoes")
                        .header("X-Gateway-Auth", "test-gateway-secret")
                        .header("X-User-Id", id.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/configuracoes/restaurar restaura padrões")
    void deveRestaurarPadrao() throws Exception {
        UUID id = criarUsuario();
        mockMvc.perform(post("/api/configuracoes/restaurar")
                        .header("X-Gateway-Auth", "test-gateway-secret")
                        .header("X-User-Id", id.toString()))
                .andExpect(status().isOk());
    }
}
