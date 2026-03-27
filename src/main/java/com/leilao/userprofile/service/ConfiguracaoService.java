package com.leilao.userprofile.service;

import com.leilao.userprofile.domain.ConfiguracaoUsuario;
import com.leilao.userprofile.domain.Usuario;
import com.leilao.userprofile.dto.AtualizarConfiguracaoRequest;
import com.leilao.userprofile.dto.ConfiguracaoResponse;
import com.leilao.userprofile.dto.DesativarContaRequest;
import com.leilao.userprofile.exception.BusinessException;
import com.leilao.userprofile.repository.ConfiguracaoUsuarioRepository;
import com.leilao.userprofile.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConfiguracaoService {

    private final ConfiguracaoUsuarioRepository configuracaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ConfiguracaoResponse buscarConfiguracoes(UUID userId) {
        ConfiguracaoUsuario config = configuracaoRepository.findByUsuarioId(userId)
                .orElseGet(() -> criarConfiguracaoPadrao(userId));
        return mapToResponse(config);
    }

    @Transactional
    public ConfiguracaoResponse atualizarConfiguracoes(UUID userId, AtualizarConfiguracaoRequest req) {
        ConfiguracaoUsuario config = configuracaoRepository.findByUsuarioId(userId)
                .orElseGet(() -> criarConfiguracaoPadrao(userId));

        if (req.notifEmailNovosLeiloes() != null) config.setNotifEmailNovosLeiloes(req.notifEmailNovosLeiloes());
        if (req.notifEmailFavoritos() != null) config.setNotifEmailFavoritos(req.notifEmailFavoritos());
        if (req.notifEmailDocumentos() != null) config.setNotifEmailDocumentos(req.notifEmailDocumentos());
        if (req.notifEmailMarketing() != null) config.setNotifEmailMarketing(req.notifEmailMarketing());
        if (req.notifAlertaLeilaoProximos() != null) config.setNotifAlertaLeilaoProximos(req.notifAlertaLeilaoProximos());
        if (req.notifAlertaMudancaPreco() != null) config.setNotifAlertaMudancaPreco(req.notifAlertaMudancaPreco());
        if (req.notifSmsAlertasImportantes() != null) config.setNotifSmsAlertasImportantes(req.notifSmsAlertasImportantes());
        if (req.tempoAntecedenciaAlerta() != null) config.setTempoAntecedenciaAlerta(req.tempoAntecedenciaAlerta());
        if (req.perfilPublico() != null) config.setPerfilPublico(req.perfilPublico());
        if (req.mostrarFavoritos() != null) config.setMostrarFavoritos(req.mostrarFavoritos());
        if (req.mostrarHistoricoAtividades() != null) config.setMostrarHistoricoAtividades(req.mostrarHistoricoAtividades());
        if (req.doisFatoresAtivo() != null) config.setDoisFatoresAtivo(req.doisFatoresAtivo());
        if (req.lembrarDispositivos() != null) config.setLembrarDispositivos(req.lembrarDispositivos());
        if (req.notifLoginNovoDispositivo() != null) config.setNotifLoginNovoDispositivo(req.notifLoginNovoDispositivo());
        if (req.tema() != null) config.setTema(req.tema());
        if (req.idioma() != null) config.setIdioma(req.idioma());
        if (req.moeda() != null) config.setMoeda(req.moeda());
        if (req.filtroPadraoUf() != null) config.setFiltroPadraoUf(req.filtroPadraoUf());
        if (req.filtroPadraoCidade() != null) config.setFiltroPadraoCidade(req.filtroPadraoCidade());
        if (req.filtroPadraoTipo() != null) config.setFiltroPadraoTipo(req.filtroPadraoTipo());
        if (req.filtroPadraoValorMin() != null) config.setFiltroPadraoValorMin(req.filtroPadraoValorMin());
        if (req.filtroPadraoValorMax() != null) config.setFiltroPadraoValorMax(req.filtroPadraoValorMax());
        if (req.anuncioPadraoAceitaFinanciamento() != null) config.setAnuncioPadraoAceitaFinanciamento(req.anuncioPadraoAceitaFinanciamento());
        if (req.anuncioPadraoVisibilidade() != null) config.setAnuncioPadraoVisibilidade(req.anuncioPadraoVisibilidade());
        if (req.receberPropostasDiretas() != null) config.setReceberPropostasDiretas(req.receberPropostasDiretas());

        return mapToResponse(configuracaoRepository.save(config));
    }

    @Transactional
    public ConfiguracaoResponse restaurarPadrao(UUID userId) {
        ConfiguracaoUsuario config = configuracaoRepository.findByUsuarioId(userId)
                .orElseGet(() -> criarConfiguracaoPadrao(userId));

        config.setNotifEmailNovosLeiloes(true);
        config.setNotifEmailFavoritos(true);
        config.setNotifEmailDocumentos(true);
        config.setNotifEmailMarketing(false);
        config.setNotifAlertaLeilaoProximos(true);
        config.setNotifAlertaMudancaPreco(true);
        config.setNotifSmsAlertasImportantes(false);
        config.setTempoAntecedenciaAlerta(24);
        config.setPerfilPublico(false);
        config.setMostrarFavoritos(false);
        config.setMostrarHistoricoAtividades(false);
        config.setDoisFatoresAtivo(false);
        config.setLembrarDispositivos(true);
        config.setNotifLoginNovoDispositivo(true);
        config.setTema("light");
        config.setIdioma("pt-BR");
        config.setMoeda("BRL");
        config.setFiltroPadraoUf(null);
        config.setFiltroPadraoCidade(null);
        config.setFiltroPadraoTipo(null);
        config.setFiltroPadraoValorMin(null);
        config.setFiltroPadraoValorMax(null);
        config.setAnuncioPadraoAceitaFinanciamento(true);
        config.setAnuncioPadraoVisibilidade("PUBLICO");
        config.setReceberPropostasDiretas(true);

        return mapToResponse(configuracaoRepository.save(config));
    }

    @Transactional
    public void desativarConta(UUID userId, DesativarContaRequest req) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        if (!passwordEncoder.matches(req.senha(), usuario.getSenha())) {
            throw new BusinessException("Senha incorreta");
        }

        usuario.setAtivo(false);
        usuarioRepository.save(usuario);

        ConfiguracaoUsuario config = configuracaoRepository.findByUsuarioId(userId)
                .orElseGet(() -> criarConfiguracaoPadrao(userId));
        config.setContaDesativada(true);
        config.setDataDesativacao(LocalDateTime.now());
        config.setMotivoDesativacao(req.motivo());
        configuracaoRepository.save(config);
    }

    @Transactional
    public void reativarConta(UUID userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        usuario.setAtivo(true);
        usuarioRepository.save(usuario);

        configuracaoRepository.findByUsuarioId(userId).ifPresent(config -> {
            config.setContaDesativada(false);
            config.setDataDesativacao(null);
            config.setMotivoDesativacao(null);
            configuracaoRepository.save(config);
        });
    }

    public Map<String, Object> exportarDados(UUID userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
        ConfiguracaoResponse config = buscarConfiguracoes(userId);

        return Map.of(
                "usuario", Map.of(
                        "id", usuario.getId(),
                        "nome", usuario.getNome(),
                        "email", usuario.getEmail(),
                        "createdAt", usuario.getCreatedAt()
                ),
                "configuracoes", config
        );
    }

    private ConfiguracaoUsuario criarConfiguracaoPadrao(UUID userId) {
        ConfiguracaoUsuario config = ConfiguracaoUsuario.builder()
                .usuarioId(userId)
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
        return configuracaoRepository.save(config);
    }

    private ConfiguracaoResponse mapToResponse(ConfiguracaoUsuario c) {
        return new ConfiguracaoResponse(
                c.getId().toString(),
                c.getNotifEmailNovosLeiloes(),
                c.getNotifEmailFavoritos(),
                c.getNotifEmailDocumentos(),
                c.getNotifEmailMarketing(),
                c.getNotifAlertaLeilaoProximos(),
                c.getNotifAlertaMudancaPreco(),
                c.getNotifSmsAlertasImportantes(),
                c.getTempoAntecedenciaAlerta(),
                c.getPerfilPublico(),
                c.getMostrarFavoritos(),
                c.getMostrarHistoricoAtividades(),
                c.getDoisFatoresAtivo(),
                c.getLembrarDispositivos(),
                c.getNotifLoginNovoDispositivo(),
                c.getTema(),
                c.getIdioma(),
                c.getMoeda(),
                c.getFiltroPadraoUf(),
                c.getFiltroPadraoCidade(),
                c.getFiltroPadraoTipo(),
                c.getFiltroPadraoValorMin(),
                c.getFiltroPadraoValorMax(),
                c.getAnuncioPadraoAceitaFinanciamento(),
                c.getAnuncioPadraoVisibilidade(),
                c.getReceberPropostasDiretas(),
                c.getContaDesativada(),
                c.getDataDesativacao(),
                c.getMotivoDesativacao(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
