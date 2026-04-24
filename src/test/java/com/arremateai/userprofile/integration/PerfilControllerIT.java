package com.arremateai.userprofile.integration;

import com.arremateai.userprofile.dto.AtualizarPerfilRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DisplayName("PerfilController - Integração")
class PerfilControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID criarUsuario(String nome) throws Exception {
        UUID id = UUID.randomUUID();
        UsuarioSyncRequest sync = new UsuarioSyncRequest(
                id, nome, "perfil-" + id + "@teste.com", null, null, "COMPRADOR"
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
    @DisplayName("GET /api/perfil sem X-Gateway-Auth retorna 401")
    void deveRejeitarSemGatewayAuth() throws Exception {
        mockMvc.perform(get("/api/perfil")
                        .header("X-User-Id", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/perfil retorna dados do usuário")
    void deveRetornarPerfil() throws Exception {
        UUID id = criarUsuario("João da Silva");
        mockMvc.perform(get("/api/perfil")
                        .header("X-Gateway-Auth", "test-gateway-secret")
                        .header("X-User-Id", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João da Silva"));
    }

    @Test
    @DisplayName("PUT /api/perfil atualiza nome")
    void deveAtualizarPerfil() throws Exception {
        UUID id = criarUsuario("Nome Antigo");
        AtualizarPerfilRequest req = new AtualizarPerfilRequest("Nome Novo Atualizado", null, null);
        mockMvc.perform(put("/api/perfil")
                        .header("X-Gateway-Auth", "test-gateway-secret")
                        .header("X-User-Id", id.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Novo Atualizado"));
    }
}
