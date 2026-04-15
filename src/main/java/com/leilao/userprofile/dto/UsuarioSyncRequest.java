package com.leilao.userprofile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UsuarioSyncRequest(
        @NotNull UUID id,
        @NotBlank String nome,
        @NotBlank @Email String email,
        String telefone,
        String cpf,
        @NotBlank String tipo
) {}
