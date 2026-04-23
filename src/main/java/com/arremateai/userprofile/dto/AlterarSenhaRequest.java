package com.arremateai.userprofile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarSenhaRequest(

    @NotBlank(message = "Senha atual é obrigatória")
    String senhaAtual,

    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 8, max = 100, message = "Nova senha deve ter no mínimo 8 caracteres")
    String novaSenha,

    @NotBlank(message = "Confirmação de senha é obrigatória")
    String confirmarSenha
) {
    public boolean senhasConferem() {
        return novaSenha != null && novaSenha.equals(confirmarSenha);
    }
}
