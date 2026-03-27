package com.leilao.userprofile.dto;

import com.leilao.userprofile.domain.TipoUsuario;

import java.time.LocalDateTime;
import java.util.UUID;

public record PerfilResponse(
    UUID id,
    String nome,
    String email,
    String telefone,
    String cpf,
    TipoUsuario tipo,
    String avatarUrl,
    Boolean ativo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
