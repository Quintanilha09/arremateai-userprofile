package com.arremateai.userprofile.controller;

import com.arremateai.userprofile.dto.AlterarSenhaRequest;
import com.arremateai.userprofile.dto.AtualizarPerfilRequest;
import com.arremateai.userprofile.dto.PerfilResponse;
import com.arremateai.userprofile.service.PerfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/api/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfilService;

    @GetMapping
    public ResponseEntity<PerfilResponse> buscarPerfil(
            @RequestHeader("X-User-Id") String userId) {
        PerfilResponse response = perfilService.buscarPerfil(UUID.fromString(userId));
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<PerfilResponse> atualizarPerfil(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody AtualizarPerfilRequest request) {
        PerfilResponse response = perfilService.atualizarPerfil(UUID.fromString(userId), request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/senha")
    public ResponseEntity<Void> alterarSenha(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody AlterarSenhaRequest request) {
        perfilService.alterarSenha(UUID.fromString(userId), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PerfilResponse> uploadAvatar(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("avatar") MultipartFile avatar) {
        PerfilResponse response = perfilService.uploadAvatar(UUID.fromString(userId), avatar);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/avatar")
    public ResponseEntity<PerfilResponse> removerAvatar(
            @RequestHeader("X-User-Id") String userId) {
        PerfilResponse response = perfilService.removerAvatar(UUID.fromString(userId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/avatar/arquivo/{filename:.+}")
    public ResponseEntity<Resource> servirAvatar(@PathVariable String filename) {
        Path caminho = perfilService.resolverCaminhoAvatar(filename);
        if (!Files.exists(caminho)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(caminho);
        String contentType = "image/jpeg";
        try {
            String detected = Files.probeContentType(caminho);
            if (detected != null) contentType = detected;
        } catch (Exception ignored) {
        }
        String safeFilename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + safeFilename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
