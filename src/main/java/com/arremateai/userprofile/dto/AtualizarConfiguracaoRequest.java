package com.arremateai.userprofile.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AtualizarConfiguracaoRequest(
        // Notificações
        Boolean notifEmailNovosLeiloes,
        Boolean notifEmailFavoritos,
        Boolean notifEmailDocumentos,
        Boolean notifEmailMarketing,
        Boolean notifAlertaLeilaoProximos,
        Boolean notifAlertaMudancaPreco,
        Boolean notifSmsAlertasImportantes,
        Integer tempoAntecedenciaAlerta,
        // Privacidade
        Boolean perfilPublico,
        Boolean mostrarFavoritos,
        Boolean mostrarHistoricoAtividades,
        // Segurança
        Boolean doisFatoresAtivo,
        Boolean lembrarDispositivos,
        Boolean notifLoginNovoDispositivo,
        // Preferências
        @Pattern(regexp = "light|dark|auto", message = "Tema deve ser light, dark ou auto")
        String tema,
        String idioma,
        String moeda,
        @Size(max = 2) String filtroPadraoUf,
        @Size(max = 100) String filtroPadraoCidade,
        @Size(max = 20) String filtroPadraoTipo,
        Double filtroPadraoValorMin,
        Double filtroPadraoValorMax,
        // Vendedor
        Boolean anuncioPadraoAceitaFinanciamento,
        @Pattern(regexp = "PUBLICO|PRIVADO", message = "Visibilidade deve ser PUBLICO ou PRIVADO")
        String anuncioPadraoVisibilidade,
        Boolean receberPropostasDiretas
) {
}
