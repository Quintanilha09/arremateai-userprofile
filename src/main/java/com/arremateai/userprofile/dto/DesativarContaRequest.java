package com.arremateai.userprofile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DesativarContaRequest(
        @NotBlank(message = "Senha é obrigatória")
        String senha,

        @Size(max = 500, message = "Motivo não pode ter mais de 500 caracteres")
        String motivo
) {
}
