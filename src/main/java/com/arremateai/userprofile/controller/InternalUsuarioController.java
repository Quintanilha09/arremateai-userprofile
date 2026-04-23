package com.arremateai.userprofile.controller;

import com.arremateai.userprofile.dto.UsuarioSyncRequest;
import com.arremateai.userprofile.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/internal/usuarios")
public class InternalUsuarioController {

    private static final Logger log = LoggerFactory.getLogger(InternalUsuarioController.class);

    @Value("${app.internal.api-key:}")
    private String internalApiKey;

    private final UsuarioRepository usuarioRepository;
    private final JdbcTemplate jdbcTemplate;

    public InternalUsuarioController(UsuarioRepository usuarioRepository, JdbcTemplate jdbcTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping
    public ResponseEntity<?> sincronizarUsuario(
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String apiKey,
            @Valid @RequestBody UsuarioSyncRequest request) {

        log.info("Sync request recebido. internalApiKey=[{}], apiKey=[{}], isBlank={}", 
                internalApiKey, apiKey, internalApiKey != null ? internalApiKey.isBlank() : "null");

        if (internalApiKey != null && !internalApiKey.isBlank() && !internalApiKey.equals(apiKey)) {
            log.warn("Acesso negado. internalApiKey=[{}], apiKey=[{}]", internalApiKey, apiKey);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acesso negado"));
        }

        if (usuarioRepository.findById(request.id()).isPresent()) {
            log.info("Usuário já existe no userprofile: {}", request.id());
            return ResponseEntity.ok(Map.of("message", "Usuário já sincronizado"));
        }

        jdbcTemplate.update(
                "INSERT INTO usuario (id, nome, email, senha, telefone, cpf, tipo, ativo, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, true, NOW(), NOW())",
                request.id(), request.nome(), request.email(), "SYNCED_FROM_IDENTITY",
                request.telefone(), request.cpf(), request.tipo()
        );

        log.info("Usuário sincronizado com sucesso: {}", request.id());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Usuário sincronizado"));
    }
}
