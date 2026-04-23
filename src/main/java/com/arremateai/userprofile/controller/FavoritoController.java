package com.arremateai.userprofile.controller;

import com.arremateai.userprofile.dto.FavoritoResponse;
import com.arremateai.userprofile.service.FavoritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/favoritos")
@RequiredArgsConstructor
public class FavoritoController {

    private final FavoritoService favoritoService;

    @PostMapping("/{imovelId}")
    public ResponseEntity<FavoritoResponse> adicionar(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID imovelId) {
        return ResponseEntity.status(201).body(
                favoritoService.adicionarFavorito(UUID.fromString(userId), imovelId));
    }

    @DeleteMapping("/{imovelId}")
    public ResponseEntity<Void> remover(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID imovelId) {
        favoritoService.removerFavorito(UUID.fromString(userId), imovelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<FavoritoResponse>> listar(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(favoritoService.listarFavoritos(UUID.fromString(userId)));
    }

    @GetMapping("/{imovelId}/status")
    public ResponseEntity<Map<String, Boolean>> verificar(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID imovelId) {
        boolean favorito = favoritoService.isFavorito(UUID.fromString(userId), imovelId);
        return ResponseEntity.ok(Map.of("favorito", favorito));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> contar(
            @RequestHeader("X-User-Id") String userId) {
        long total = favoritoService.contarFavoritos(UUID.fromString(userId));
        return ResponseEntity.ok(Map.of("total", total));
    }

    @GetMapping("/imovel/{imovelId}/count")
    public ResponseEntity<Map<String, Long>> contarPorImovel(
            @PathVariable UUID imovelId) {
        long total = favoritoService.contarFavoritosPorImovel(imovelId);
        return ResponseEntity.ok(Map.of("total", total));
    }
}
