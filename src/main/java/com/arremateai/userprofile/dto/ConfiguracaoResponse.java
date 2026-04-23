package com.arremateai.userprofile.dto;

import java.time.LocalDateTime;

public record ConfiguracaoResponse(
        String id,
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
        String tema,
        String idioma,
        String moeda,
        String filtroPadraoUf,
        String filtroPadraoCidade,
        String filtroPadraoTipo,
        Double filtroPadraoValorMin,
        Double filtroPadraoValorMax,
        // Vendedor
        Boolean anuncioPadraoAceitaFinanciamento,
        String anuncioPadraoVisibilidade,
        Boolean receberPropostasDiretas,
        // Conta
        Boolean contaDesativada,
        LocalDateTime dataDesativacao,
        String motivoDesativacao,
        // Auditoria
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
