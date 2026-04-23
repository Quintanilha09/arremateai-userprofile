package com.arremateai.userprofile.controller;

import com.arremateai.userprofile.dto.AtualizarConfiguracaoRequest;
import com.arremateai.userprofile.dto.ConfiguracaoResponse;
import com.arremateai.userprofile.dto.DesativarContaRequest;
import com.arremateai.userprofile.service.ConfiguracaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/configuracoes")
@RequiredArgsConstructor
public class ConfiguracaoController {

    private final ConfiguracaoService configuracaoService;

    @GetMapping
    public ResponseEntity<ConfiguracaoResponse> buscarConfiguracoes(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(configuracaoService.buscarConfiguracoes(UUID.fromString(userId)));
    }

    @PutMapping
    public ResponseEntity<ConfiguracaoResponse> atualizarConfiguracoes(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody AtualizarConfiguracaoRequest request) {
        return ResponseEntity.ok(configuracaoService.atualizarConfiguracoes(UUID.fromString(userId), request));
    }

    @PostMapping("/restaurar")
    public ResponseEntity<ConfiguracaoResponse> restaurarPadrao(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(configuracaoService.restaurarPadrao(UUID.fromString(userId)));
    }

    @PostMapping("/desativar-conta")
    public ResponseEntity<Void> desativarConta(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody DesativarContaRequest request) {
        configuracaoService.desativarConta(UUID.fromString(userId), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reativar-conta")
    public ResponseEntity<Void> reativarConta(
            @RequestHeader("X-User-Id") String userId) {
        configuracaoService.reativarConta(UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exportar-dados")
    public ResponseEntity<Map<String, Object>> exportarDados(
            @RequestHeader("X-User-Id") String userId) {
        Map<String, Object> dados = configuracaoService.exportarDados(UUID.fromString(userId));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("meus-dados.json")
                .build());
        return ResponseEntity.ok().headers(headers).body(dados);
    }
}
