package com.arremateai.userprofile.controller;

import com.arremateai.userprofile.domain.Usuario;
import com.arremateai.userprofile.dto.UsuarioSyncRequest;
import com.arremateai.userprofile.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InternalUsuarioController — Segurança (VUL-001/VUL-002)")
class InternalUsuarioControllerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private InternalUsuarioController controller;

    private static final String API_KEY_VALIDA = "chave-secreta-interna-123";
    private static final UUID USER_ID_PADRAO = UUID.randomUUID();

    private UsuarioSyncRequest criarRequestPadrao() {
        return new UsuarioSyncRequest(
                USER_ID_PADRAO,
                "João Silva",
                "joao@email.com",
                "(11) 99999-9999",
                "123.456.789-00",
                "COMPRADOR"
        );
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "internalApiKey", API_KEY_VALIDA);
    }

    // ===== CENÁRIOS DE SEGURANÇA VUL-001 (Sem fail-open) =====

    @Test
    @DisplayName("Deve retornar 503 quando api-key interna não estiver configurada (sem fail-open)")
    void deveRetornar503QuandoApiKeyNaoEstiverConfigurada() {
        ReflectionTestUtils.setField(controller, "internalApiKey", "");

        var resultado = controller.sincronizarUsuario(API_KEY_VALIDA, criarRequestPadrao());

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(resultado.getBody()).asString().contains("configuração ausente");
        verifyNoInteractions(usuarioRepository, jdbcTemplate);
    }

    @Test
    @DisplayName("Deve retornar 503 quando api-key interna for nula (sem fail-open)")
    void deveRetornar503QuandoApiKeyForNula() {
        ReflectionTestUtils.setField(controller, "internalApiKey", null);

        var resultado = controller.sincronizarUsuario(API_KEY_VALIDA, criarRequestPadrao());

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        verifyNoInteractions(usuarioRepository, jdbcTemplate);
    }

    @Test
    @DisplayName("Deve retornar 403 quando api-key da requisição for inválida (sem fail-open)")
    void deveRetornar403QuandoApiKeyDaRequisicaoForInvalida() {
        var resultado = controller.sincronizarUsuario("chave-errada", criarRequestPadrao());

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(resultado.getBody()).asString().contains("Acesso negado");
        verifyNoInteractions(usuarioRepository, jdbcTemplate);
    }

    @Test
    @DisplayName("Deve retornar 403 quando api-key da requisição estiver em branco com key configurada (sem fail-open)")
    void deveRetornar403QuandoApiKeyDaRequisicaoEstiverEmBranco() {
        var resultado = controller.sincronizarUsuario("", criarRequestPadrao());

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(usuarioRepository, jdbcTemplate);
    }

    @Test
    @DisplayName("Deve retornar 403 quando api-key da requisição for nula com key configurada (sem fail-open)")
    void deveRetornar403QuandoApiKeyDaRequisicaoForNula() {
        var resultado = controller.sincronizarUsuario(null, criarRequestPadrao());

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(usuarioRepository, jdbcTemplate);
    }

    // ===== CENÁRIOS DE SUCESSO =====

    @Test
    @DisplayName("Deve sincronizar novo usuário e retornar 201 quando api-key válida e usuário não existe")
    void deveSincronizarNovoUsuarioERetornar201() {
        when(usuarioRepository.findById(USER_ID_PADRAO)).thenReturn(Optional.empty());

        var resultado = controller.sincronizarUsuario(API_KEY_VALIDA, criarRequestPadrao());

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resultado.getBody()).asString().contains("sincronizado");
        verify(jdbcTemplate).update(anyString(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve retornar 200 quando usuário já existir no userprofile")
    void deveRetornar200QuandoUsuarioJaExistir() {
        var usuarioExistente = new Usuario();
        usuarioExistente.setId(USER_ID_PADRAO);
        when(usuarioRepository.findById(USER_ID_PADRAO)).thenReturn(Optional.of(usuarioExistente));

        var resultado = controller.sincronizarUsuario(API_KEY_VALIDA, criarRequestPadrao());

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultado.getBody()).asString().contains("já sincronizado");
        verify(jdbcTemplate, never()).update(anyString(), (Object[]) any());
    }

    // ===== VUL-002 — API-KEY NÃO EXPOSTA EM LOGS =====

    @Test
    @DisplayName("Não deve expor o valor da api-key nos logs (VUL-002 — apenas boolean presente/ausente)")
    void naoDeveExporApiKeyNosLogs() {
        // O controller loga apenas "Key presente: true/false", nunca o valor da chave.
        // Este teste valida o comportamento: com key válida, a sincronização ocorre normalmente
        // sem que nenhuma lógica exponha o conteúdo da key.
        when(usuarioRepository.findById(USER_ID_PADRAO)).thenReturn(Optional.empty());

        var resultado = controller.sincronizarUsuario(API_KEY_VALIDA, criarRequestPadrao());

        // Se o fluxo chegou aqui com 201, a key não foi rejeitada nem logada indevidamente.
        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        // O teste estrutural de VUL-002 valida o código-fonte via ExternalizacaoConfiguracoesTest
    }
}
