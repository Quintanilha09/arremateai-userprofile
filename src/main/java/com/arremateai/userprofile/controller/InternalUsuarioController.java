package com.arremateai.userprofile.controller;

import com.arremateai.userprofile.dto.UsuarioSyncRequest;
import com.arremateai.userprofile.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/internal/usuarios")
public class InternalUsuarioController {

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

        log.info("Sync request recebido para usuário={}. Key presente: {}", request.id(), apiKey != null && !apiKey.isBlank());

        if (internalApiKey == null || internalApiKey.isBlank()) {
            log.warn("INTERNAL_API_KEY não configurada — endpoint /internal/usuarios desativado");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Serviço indisponível — configuração ausente"));
        }

        if (!MessageDigest.isEqual(internalApiKey.getBytes(StandardCharsets.UTF_8), apiKey != null ? apiKey.getBytes(StandardCharsets.UTF_8) : new byte[0])) {
            log.warn("Acesso negado ao endpoint interno para usuário={}", request.id());
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
