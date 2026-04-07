package com.leilao.userprofile.service;

import com.leilao.userprofile.domain.TipoUsuario;
import com.leilao.userprofile.domain.Usuario;
import com.leilao.userprofile.dto.AlterarSenhaRequest;
import com.leilao.userprofile.dto.AtualizarPerfilRequest;
import com.leilao.userprofile.dto.PerfilResponse;
import com.leilao.userprofile.exception.BusinessException;
import com.leilao.userprofile.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerfilServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PerfilService perfilService;

    private static final UUID USER_ID_PADRAO = UUID.randomUUID();
    private static final String NOME_PADRAO = "João Silva";
    private static final String EMAIL_PADRAO = "joao@email.com";
    private static final String SENHA_ENCODED = "$2a$10$encoded";

    private Usuario criarUsuarioPadrao() {
        var usuario = new Usuario();
        usuario.setId(USER_ID_PADRAO);
        usuario.setNome(NOME_PADRAO);
        usuario.setEmail(EMAIL_PADRAO);
        usuario.setSenha(SENHA_ENCODED);
        usuario.setTelefone("(11) 99999-9999");
        usuario.setCpf("123.456.789-00");
        usuario.setTipo(TipoUsuario.COMPRADOR);
        usuario.setAtivo(true);
        return usuario;
    }

    // ---- buscarPerfil ----

    @Test
    @DisplayName("Deve retornar perfil quando usuário existir")
    void deveRetornarPerfilQuandoUsuarioExistir() {
        var usuario = criarUsuarioPadrao();
        when(usuarioRepository.findById(USER_ID_PADRAO)).thenReturn(Optional.of(usuario));

        PerfilResponse resultado = perfilService.buscarPerfil(USER_ID_PADRAO);

        assertThat(resultado).isNotNull();
        assertThat(resultado.nome()).isEqualTo(NOME_PADRAO);
        assertThat(resultado.email()).isEqualTo(EMAIL_PADRAO);
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando usuário não encontrado")
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        when(usuarioRepository.findById(USER_ID_PADRAO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> perfilService.buscarPerfil(USER_ID_PADRAO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    // ---- atualizarPerfil ----

    @Test
    @DisplayName("Deve atualizar perfil com sucesso")
    void deveAtualizarPerfilComSucesso() {
        var usuario = criarUsuarioPadrao();
        var request = new AtualizarPerfilRequest("Novo Nome", "(11) 88888-8888", null);
        when(usuarioRepository.findById(USER_ID_PADRAO)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        PerfilResponse resultado = perfilService.atualizarPerfil(USER_ID_PADRAO, request);

        assertThat(resultado).isNotNull();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Deve lançar exceção quando CPF já estiver em uso por outro usuário")
    void deveLancarExcecaoQuandoCpfJaEstiverEmUso() {
        var usuario = criarUsuarioPadrao();
        var outroUsuario = criarUsuarioPadrao();
        outroUsuario.setId(UUID.randomUUID());
        var request = new AtualizarPerfilRequest("Nome", null, "123.456.789-00");

        when(usuarioRepository.findById(USER_ID_PADRAO)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByCpf("123.456.789-00")).thenReturn(Optional.of(outroUsuario));

        assertThatThrownBy(() -> perfilService.atualizarPerfil(USER_ID_PADRAO, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CPF já está em uso");
    }

    @Test
    @DisplayName("Deve permitir manter o mesmo CPF do próprio usuário")
    void devePermitirManterMesmoCpfDoProprioUsuario() {
        var usuario = criarUsuarioPadrao();
        var request = new AtualizarPerfilRequest("Nome", null, "123.456.789-00");

        when(usuarioRepository.findById(USER_ID_PADRAO)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByCpf("123.456.789-00")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        assertThatCode(() -> perfilService.atualizarPerfil(USER_ID_PADRAO, request))
                .doesNotThrowAnyException();
    }

    // ---- alterarSenha ----

    @Test
    @DisplayName("Deve alterar senha com sucesso")
    void deveAlterarSenhaComSucesso() {
        var usuario = criarUsuarioPadrao();
        var request = new AlterarSenhaRequest("senhaAtual", "novaSenha123", "novaSenha123");

        when(usuarioRepository.findById(USER_ID_PADRAO)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaAtual", SENHA_ENCODED)).thenReturn(true);
        when(passwordEncoder.matches("novaSenha123", SENHA_ENCODED)).thenReturn(false);
        when(passwordEncoder.encode("novaSenha123")).thenReturn("$2a$10$nova");

        perfilService.alterarSenha(USER_ID_PADRAO, request);

        verify(usuarioRepository).save(usuario);
        assertThat(usuario.getSenha()).isEqualTo("$2a$10$nova");
    }

    @Test
    @DisplayName("Deve lançar exceção quando senhas não conferem")
    void deveLancarExcecaoQuandoSenhasNaoConferem() {
        var request = new AlterarSenhaRequest("senhaAtual", "nova1", "nova2");

        assertThatThrownBy(() -> perfilService.alterarSenha(USER_ID_PADRAO, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não conferem");
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não possui senha cadastrada")
    void deveLancarExcecaoQuandoUsuarioNaoPossuiSenhaCadastrada() {
        var usuario = criarUsuarioPadrao();
        usuario.setSenha(null);
        var request = new AlterarSenhaRequest("senhaAtual", "novaSenha123", "novaSenha123");

        when(usuarioRepository.findById(USER_ID_PADRAO)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> perfilService.alterarSenha(USER_ID_PADRAO, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não possui senha cadastrada");
    }

    @Test
    @DisplayName("Deve lançar exceção quando senha atual incorreta")
    void deveLancarExcecaoQuandoSenhaAtualIncorreta() {
        var usuario = criarUsuarioPadrao();
        var request = new AlterarSenhaRequest("senhaErrada", "novaSenha123", "novaSenha123");

        when(usuarioRepository.findById(USER_ID_PADRAO)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaErrada", SENHA_ENCODED)).thenReturn(false);

        assertThatThrownBy(() -> perfilService.alterarSenha(USER_ID_PADRAO, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Senha atual incorreta");
    }

    @Test
    @DisplayName("Deve lançar exceção quando nova senha é igual à atual")
    void deveLancarExcecaoQuandoNovaSenhaIgualAtual() {
        var usuario = criarUsuarioPadrao();
        var request = new AlterarSenhaRequest("senhaAtual", "senhaAtual", "senhaAtual");

        when(usuarioRepository.findById(USER_ID_PADRAO)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaAtual", SENHA_ENCODED)).thenReturn(true);
        when(passwordEncoder.matches("senhaAtual", SENHA_ENCODED)).thenReturn(true);

        assertThatThrownBy(() -> perfilService.alterarSenha(USER_ID_PADRAO, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("igual à senha atual");
    }

    // ---- uploadAvatar ----

    @Test
    @DisplayName("Deve lançar exceção quando arquivo de avatar for nulo")
    void deveLancarExcecaoQuandoArquivoAvatarForNulo() {
        assertThatThrownBy(() -> perfilService.uploadAvatar(USER_ID_PADRAO, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando avatar exceder 5MB")
    void deveLancarExcecaoQuandoAvatarExceder5MB() {
        byte[] conteudo = new byte[6 * 1024 * 1024];
        var arquivo = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", conteudo);

        assertThatThrownBy(() -> perfilService.uploadAvatar(USER_ID_PADRAO, arquivo))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("5MB");
    }

    @Test
    @DisplayName("Deve lançar exceção quando tipo do avatar for inválido")
    void deveLancarExcecaoQuandoTipoDoAvatarForInvalido() {
        var arquivo = new MockMultipartFile("file", "avatar.gif", "image/gif", new byte[]{1});

        assertThatThrownBy(() -> perfilService.uploadAvatar(USER_ID_PADRAO, arquivo))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("permitidas");
    }

    @Test
    @DisplayName("Deve lançar exceção quando extensão do avatar não for permitida")
    void deveLancarExcecaoQuandoExtensaoDoAvatarNaoForPermitida() {
        var arquivo = new MockMultipartFile("file", "avatar.bmp", "image/jpeg", new byte[]{1});

        assertThatThrownBy(() -> perfilService.uploadAvatar(USER_ID_PADRAO, arquivo))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não permitida");
    }

    // ---- removerAvatar ----

    @Test
    @DisplayName("Deve remover avatar com sucesso")
    void deveRemoverAvatarComSucesso() {
        var usuario = criarUsuarioPadrao();
        usuario.setAvatarUrl(null);
        when(usuarioRepository.findById(USER_ID_PADRAO)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        PerfilResponse resultado = perfilService.removerAvatar(USER_ID_PADRAO);

        assertThat(resultado).isNotNull();
        assertThat(usuario.getAvatarUrl()).isNull();
        verify(usuarioRepository).save(usuario);
    }

    // ---- resolverCaminhoAvatar ----

    @Test
    @DisplayName("Deve lançar exceção ao tentar path traversal no avatar")
    void deveLancarExcecaoAoTentarPathTraversalNoAvatar() {
        ReflectionTestUtils.setField(perfilService, "uploadDir", "/tmp/avatars");

        assertThatThrownBy(() -> perfilService.resolverCaminhoAvatar("../../etc/passwd"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inválido");
    }
}
