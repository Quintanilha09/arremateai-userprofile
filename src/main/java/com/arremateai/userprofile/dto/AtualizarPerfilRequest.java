package com.arremateai.userprofile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AtualizarPerfilRequest(

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    String nome,

    @Pattern(regexp = "^\\(\\d{2}\\) \\d{4,5}-\\d{4}$",
             message = "Telefone deve estar no formato (99) 99999-9999")
    String telefone,

    @Pattern(regexp = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$",
             message = "CPF deve estar no formato 999.999.999-99")
    String cpf
) {}
