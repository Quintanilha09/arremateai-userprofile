package com.leilao.userprofile.controller;

import com.leilao.userprofile.dto.AtualizarConfiguracaoRequest;
import com.leilao.userprofile.dto.ConfiguracaoResponse;
import com.leilao.userprofile.dto.DesativarContaRequest;
import com.leilao.userprofile.service.ConfiguracaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfiguracaoControllerTest {

    @Mock
    private ConfiguracaoService configuracaoService;

    @InjectMocks
    private ConfiguracaoController configuracaoController;

    private static final String USER_ID_PADRAO = "123e4567-e89b-12d3-a456-426614174000";
    private static final UUID USER_UUID = UUID.fromString(USER_ID_PADRAO);

    private ConfiguracaoResponse criarConfiguracaoResponse() {
        return new ConfiguracaoResponse(
                USER_ID_PADRAO,
                // Notificações
                true, true, true, false, true, true, false, 30,
                // Privacidade
                true, false, false,
                // Segurança
                false, true, true,
                // Preferências
                "light", "pt-BR", "BRL", null, null, null, null, null,
                // Vendedor
                true, "PUBLICO", false,
                // Conta
                false, null, null,
                // Auditoria
                null, null);
    }

    // ===== buscarConfiguracoes =====

    @Test
    @DisplayName("Deve retornar configurações do usuário")
    void deveRetornarConfiguracoesDoUsuario() {
        when(configuracaoService.buscarConfiguracoes(USER_UUID)).thenReturn(criarConfiguracaoResponse());

        var resultado = configuracaoController.buscarConfiguracoes(USER_ID_PADRAO);

        assertThat(resultado.getStatusCode().value()).isEqualTo(200);
        assertThat(resultado.getBody()).isNotNull();
        verify(configuracaoService).buscarConfiguracoes(USER_UUID);
    }

    // ===== atualizarConfiguracoes =====

    @Test
    @DisplayName("Deve atualizar configurações e retornar dados atualizados")
    void deveAtualizarConfiguracoesERetornarDadosAtualizados() {
        var request = new AtualizarConfiguracaoRequest(
                true, true, true, false, true, true, false, 30,
                true, false, false, false, true, true,
                "dark", "pt-BR", "BRL", null, null, null, null, null,
                true, "PUBLICO", false);
        when(configuracaoService.atualizarConfiguracoes(eq(USER_UUID), any())).thenReturn(criarConfiguracaoResponse());

        var resultado = configuracaoController.atualizarConfiguracoes(USER_ID_PADRAO, request);

        assertThat(resultado.getStatusCode().value()).isEqualTo(200);
        verify(configuracaoService).atualizarConfiguracoes(eq(USER_UUID), eq(request));
    }

    // ===== restaurarPadrao =====

    @Test
    @DisplayName("Deve restaurar configurações padrão")
    void deveRestaurarConfiguracoesPadrao() {
        when(configuracaoService.restaurarPadrao(USER_UUID)).thenReturn(criarConfiguracaoResponse());

        var resultado = configuracaoController.restaurarPadrao(USER_ID_PADRAO);

        assertThat(resultado.getStatusCode().value()).isEqualTo(200);
        verify(configuracaoService).restaurarPadrao(USER_UUID);
    }

    // ===== desativarConta =====

    @Test
    @DisplayName("Deve desativar conta e retornar 204")
    void deveDesativarContaERetornar204() {
        var request = new DesativarContaRequest("minhaSenha", "Não uso mais");
        doNothing().when(configuracaoService).desativarConta(eq(USER_UUID), any());

        var resultado = configuracaoController.desativarConta(USER_ID_PADRAO, request);

        assertThat(resultado.getStatusCode().value()).isEqualTo(204);
        verify(configuracaoService).desativarConta(USER_UUID, request);
    }

    // ===== reativarConta =====

    @Test
    @DisplayName("Deve reativar conta e retornar 204")
    void deveReativarContaERetornar204() {
        doNothing().when(configuracaoService).reativarConta(USER_UUID);

        var resultado = configuracaoController.reativarConta(USER_ID_PADRAO);

        assertThat(resultado.getStatusCode().value()).isEqualTo(204);
        verify(configuracaoService).reativarConta(USER_UUID);
    }

    // ===== exportarDados =====

    @Test
    @DisplayName("Deve exportar dados do usuário com headers de download")
    void deveExportarDadosDoUsuarioComHeadersDeDownload() {
        var dados = Map.<String, Object>of("nome", "Teste", "email", "teste@email.com");
        when(configuracaoService.exportarDados(USER_UUID)).thenReturn(dados);

        var resultado = configuracaoController.exportarDados(USER_ID_PADRAO);

        assertThat(resultado.getStatusCode().value()).isEqualTo(200);
        assertThat(resultado.getHeaders().getContentDisposition().getFilename()).isEqualTo("meus-dados.json");
        assertThat(resultado.getBody()).containsEntry("nome", "Teste");
    }
}
