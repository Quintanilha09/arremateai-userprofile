package com.leilao.userprofile.service;

import com.leilao.userprofile.domain.ConfiguracaoUsuario;
import com.leilao.userprofile.domain.TipoUsuario;
import com.leilao.userprofile.domain.Usuario;
import com.leilao.userprofile.dto.AtualizarConfiguracaoRequest;
import com.leilao.userprofile.dto.ConfiguracaoResponse;
import com.leilao.userprofile.dto.DesativarContaRequest;
import com.leilao.userprofile.exception.BusinessException;
import com.leilao.userprofile.repository.ConfiguracaoUsuarioRepository;
import com.leilao.userprofile.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfiguracaoServiceTest {

    @Mock
    private ConfiguracaoUsuarioRepository configuracaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ConfiguracaoService configuracaoService;

    private static final UUID USER_ID_PADRAO = UUID.randomUUID();
    private static final UUID CONFIG_ID_PADRAO = UUID.randomUUID();

    private Usuario criarUsuarioPadrao() {
        var usuario = new Usuario();
        usuario.setId(USER_ID_PADRAO);
        usuario.setNome("João");
        usuario.setEmail("joao@email.com");
        usuario.setSenha("$2a$10$encoded");
        usuario.setTipo(TipoUsuario.COMPRADOR);
        usuario.setAtivo(true);
        usuario.setCreatedAt(LocalDateTime.now());
        return usuario;
    }

    private ConfiguracaoUsuario criarConfiguracaoPadrao() {
        return ConfiguracaoUsuario.builder()
                .id(CONFIG_ID_PADRAO)
                .usuarioId(USER_ID_PADRAO)
                .notifEmailNovosLeiloes(true)
                .notifEmailFavoritos(true)
                .notifEmailDocumentos(true)
                .notifEmailMarketing(false)
                .notifAlertaLeilaoProximos(true)
                .notifAlertaMudancaPreco(true)
                .notifSmsAlertasImportantes(false)
                .tempoAntecedenciaAlerta(24)
                .perfilPublico(false)
                .mostrarFavoritos(false)
                .mostrarHistoricoAtividades(false)
                .doisFatoresAtivo(false)
                .lembrarDispositivos(true)
                .notifLoginNovoDispositivo(true)
                .tema("light")
                .idioma("pt-BR")
                .moeda("BRL")
                .anuncioPadraoAceitaFinanciamento(true)
                .anuncioPadraoVisibilidade("PUBLICO")
                .receberPropostasDiretas(true)
                .contaDesativada(false)
                .build();
    }

    // ---- buscarConfiguracoes ----

    @Test
    @DisplayName("Deve retornar configurações existentes do usuário")
    void deveRetornarConfiguracoesExistentesDoUsuario() {
        var config = criarConfiguracaoPadrao();
        when(configuracaoRepository.findByUsuarioId(USER_ID_PADRAO)).thenReturn(Optional.of(config));

        ConfiguracaoResponse resultado = configuracaoService.buscarConfiguracoes(USER_ID_PADRAO);

        assertThat(resultado).isNotNull();
        assertThat(resultado.tema()).isEqualTo("light");
        assertThat(resultado.idioma()).isEqualTo("pt-BR");
    }

    @Test
    @DisplayName("Deve criar configurações padrão quando não existirem")
    void deveCriarConfiguracoesPadraoQuandoNaoExistirem() {
        var configNova = criarConfiguracaoPadrao();
        when(configuracaoRepository.findByUsuarioId(USER_ID_PADRAO)).thenReturn(Optional.empty());
        when(configuracaoRepository.save(any(ConfiguracaoUsuario.class))).thenReturn(configNova);

        ConfiguracaoResponse resultado = configuracaoService.buscarConfiguracoes(USER_ID_PADRAO);

        assertThat(resultado).isNotNull();
        verify(configuracaoRepository).save(any(ConfiguracaoUsuario.class));
    }

    // ---- atualizarConfiguracoes ----

    @Test
    @DisplayName("Deve atualizar configurações com sucesso")
    void deveAtualizarConfiguracoesComSucesso() {
        var config = criarConfiguracaoPadrao();
        when(configuracaoRepository.findByUsuarioId(USER_ID_PADRAO)).thenReturn(Optional.of(config));
        when(configuracaoRepository.save(any(ConfiguracaoUsuario.class))).thenReturn(config);

        var request = new AtualizarConfiguracaoRequest(
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null,
                "dark", null, null, null, null, null, null, null,
                null, null, null);

        ConfiguracaoResponse resultado = configuracaoService.atualizarConfiguracoes(USER_ID_PADRAO, request);

        assertThat(resultado).isNotNull();
        assertThat(config.getTema()).isEqualTo("dark");
        verify(configuracaoRepository).save(config);
    }

    @Test
    @DisplayName("Deve criar configuração padrão ao atualizar quando não existir")
    void deveCriarConfiguracaoPadraoAoAtualizarQuandoNaoExistir() {
        var configNova = criarConfiguracaoPadrao();
        when(configuracaoRepository.findByUsuarioId(USER_ID_PADRAO)).thenReturn(Optional.empty());
        when(configuracaoRepository.save(any(ConfiguracaoUsuario.class))).thenReturn(configNova);

        var request = new AtualizarConfiguracaoRequest(
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null,
                null, null, null);

        configuracaoService.atualizarConfiguracoes(USER_ID_PADRAO, request);

        verify(configuracaoRepository, atLeast(1)).save(any(ConfiguracaoUsuario.class));
    }

    // ---- restaurarPadrao ----

    @Test
    @DisplayName("Deve restaurar configurações para o padrão")
    void deveRestaurarConfiguracoesPadrao() {
        var config = criarConfiguracaoPadrao();
        config.setTema("dark");
        config.setNotifEmailMarketing(true);
        when(configuracaoRepository.findByUsuarioId(USER_ID_PADRAO)).thenReturn(Optional.of(config));
        when(configuracaoRepository.save(any(ConfiguracaoUsuario.class))).thenReturn(config);

        ConfiguracaoResponse resultado = configuracaoService.restaurarPadrao(USER_ID_PADRAO);

        assertThat(resultado).isNotNull();
        assertThat(config.getTema()).isEqualTo("light");
        assertThat(config.getNotifEmailMarketing()).isFalse();
        verify(configuracaoRepository).save(config);
    }

    // ---- desativarConta ----

    @Test
    @DisplayName("Deve desativar conta com sucesso")
    void deveDesativarContaComSucesso() {
        var usuario = criarUsuarioPadrao();
        var config = criarConfiguracaoPadrao();
        var request = new DesativarContaRequest("senhaCorreta", "Motivo teste");

        when(usuarioRepository.findById(USER_ID_PADRAO)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaCorreta", usuario.getSenha())).thenReturn(true);
        when(configuracaoRepository.findByUsuarioId(USER_ID_PADRAO)).thenReturn(Optional.of(config));

        configuracaoService.desativarConta(USER_ID_PADRAO, request);

        assertThat(usuario.getAtivo()).isFalse();
        assertThat(config.getContaDesativada()).isTrue();
        assertThat(config.getMotivoDesativacao()).isEqualTo("Motivo teste");
        verify(usuarioRepository).save(usuario);
        verify(configuracaoRepository).save(config);
    }

    @Test
    @DisplayName("Deve lançar exceção quando senha incorreta ao desativar")
    void deveLancarExcecaoQuandoSenhaIncorretaAoDesativar() {
        var usuario = criarUsuarioPadrao();
        var request = new DesativarContaRequest("senhaErrada", "Motivo");

        when(usuarioRepository.findById(USER_ID_PADRAO)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaErrada", usuario.getSenha())).thenReturn(false);

        assertThatThrownBy(() -> configuracaoService.desativarConta(USER_ID_PADRAO, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Senha incorreta");
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado ao desativar")
    void deveLancarExcecaoQuandoUsuarioNaoEncontradoAoDesativar() {
        when(usuarioRepository.findById(USER_ID_PADRAO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> configuracaoService.desativarConta(USER_ID_PADRAO,
                new DesativarContaRequest("senha", "motivo")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    // ---- reativarConta ----

    @Test
    @DisplayName("Deve reativar conta com sucesso")
    void deveReativarContaComSucesso() {
        var usuario = criarUsuarioPadrao();
        usuario.setAtivo(false);
        var config = criarConfiguracaoPadrao();
        config.setContaDesativada(true);
        config.setMotivoDesativacao("Motivo");

        when(usuarioRepository.findById(USER_ID_PADRAO)).thenReturn(Optional.of(usuario));
        when(configuracaoRepository.findByUsuarioId(USER_ID_PADRAO)).thenReturn(Optional.of(config));

        configuracaoService.reativarConta(USER_ID_PADRAO);

        assertThat(usuario.getAtivo()).isTrue();
        assertThat(config.getContaDesativada()).isFalse();
        assertThat(config.getMotivoDesativacao()).isNull();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado ao reativar")
    void deveLancarExcecaoQuandoUsuarioNaoEncontradoAoReativar() {
        when(usuarioRepository.findById(USER_ID_PADRAO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> configuracaoService.reativarConta(USER_ID_PADRAO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    // ---- exportarDados ----

    @Test
    @DisplayName("Deve exportar dados do usuário com sucesso")
    void deveExportarDadosDoUsuarioComSucesso() {
        var usuario = criarUsuarioPadrao();
        var config = criarConfiguracaoPadrao();
        when(usuarioRepository.findById(USER_ID_PADRAO)).thenReturn(Optional.of(usuario));
        when(configuracaoRepository.findByUsuarioId(USER_ID_PADRAO)).thenReturn(Optional.of(config));

        Map<String, Object> resultado = configuracaoService.exportarDados(USER_ID_PADRAO);

        assertThat(resultado).containsKeys("usuario", "configuracoes");
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado ao exportar dados")
    void deveLancarExcecaoQuandoUsuarioNaoEncontradoAoExportarDados() {
        when(usuarioRepository.findById(USER_ID_PADRAO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> configuracaoService.exportarDados(USER_ID_PADRAO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Usuário não encontrado");
    }
}
