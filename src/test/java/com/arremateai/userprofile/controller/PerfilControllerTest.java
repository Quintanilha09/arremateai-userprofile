package com.arremateai.userprofile.controller;

import com.arremateai.userprofile.domain.TipoUsuario;
import com.arremateai.userprofile.dto.AlterarSenhaRequest;
import com.arremateai.userprofile.dto.AtualizarPerfilRequest;
import com.arremateai.userprofile.dto.PerfilResponse;
import com.arremateai.userprofile.service.PerfilService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpHeaders;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerfilControllerTest {

    @Mock
    private PerfilService perfilService;

    @InjectMocks
    private PerfilController perfilController;

    private static final String USER_ID_PADRAO = "123e4567-e89b-12d3-a456-426614174000";
    private static final UUID USER_UUID = UUID.fromString(USER_ID_PADRAO);

    private PerfilResponse criarPerfilResponse() {
        return new PerfilResponse(USER_UUID, "Teste", "teste@email.com", "(11) 99999-0000",
                "123.456.789-00", TipoUsuario.COMPRADOR, null, true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    // ===== buscarPerfil =====

    @Test
    @DisplayName("Deve retornar perfil do usuário")
    void deveRetornarPerfilDoUsuario() {
        when(perfilService.buscarPerfil(USER_UUID)).thenReturn(criarPerfilResponse());

        var resultado = perfilController.buscarPerfil(USER_ID_PADRAO);

        assertThat(resultado.getStatusCode().value()).isEqualTo(200);
        assertThat(resultado.getBody().nome()).isEqualTo("Teste");
        verify(perfilService).buscarPerfil(USER_UUID);
    }

    // ===== atualizarPerfil =====

    @Test
    @DisplayName("Deve atualizar perfil e retornar dados atualizados")
    void deveAtualizarPerfilERetornarDadosAtualizados() {
        var request = new AtualizarPerfilRequest("Novo Nome", "(11) 99999-0000", "123.456.789-00");
        when(perfilService.atualizarPerfil(eq(USER_UUID), any())).thenReturn(criarPerfilResponse());

        var resultado = perfilController.atualizarPerfil(USER_ID_PADRAO, request);

        assertThat(resultado.getStatusCode().value()).isEqualTo(200);
        verify(perfilService).atualizarPerfil(eq(USER_UUID), eq(request));
    }

    // ===== alterarSenha =====

    @Test
    @DisplayName("Deve alterar senha e retornar 204")
    void deveAlterarSenhaERetornar204() {
        var request = new AlterarSenhaRequest("senhaAtual", "novaSenha123", "novaSenha123");
        doNothing().when(perfilService).alterarSenha(eq(USER_UUID), any());

        var resultado = perfilController.alterarSenha(USER_ID_PADRAO, request);

        assertThat(resultado.getStatusCode().value()).isEqualTo(204);
        verify(perfilService).alterarSenha(USER_UUID, request);
    }

    // ===== uploadAvatar =====

    @Test
    @DisplayName("Deve fazer upload de avatar e retornar perfil atualizado")
    void deveFazerUploadDeAvatarERetornarPerfilAtualizado() {
        var arquivo = new MockMultipartFile("avatar", "foto.jpg", "image/jpeg", "conteudo".getBytes());
        when(perfilService.uploadAvatar(eq(USER_UUID), any())).thenReturn(criarPerfilResponse());

        var resultado = perfilController.uploadAvatar(USER_ID_PADRAO, arquivo);

        assertThat(resultado.getStatusCode().value()).isEqualTo(200);
        verify(perfilService).uploadAvatar(eq(USER_UUID), any());
    }

    // ===== removerAvatar =====

    @Test
    @DisplayName("Deve remover avatar e retornar perfil atualizado")
    void deveRemoverAvatarERetornarPerfilAtualizado() {
        when(perfilService.removerAvatar(USER_UUID)).thenReturn(criarPerfilResponse());

        var resultado = perfilController.removerAvatar(USER_ID_PADRAO);

        assertThat(resultado.getStatusCode().value()).isEqualTo(200);
        verify(perfilService).removerAvatar(USER_UUID);
    }

    // ===== servirAvatar =====

    @Test
    @DisplayName("Deve retornar 404 quando avatar não existe")
    void deveRetornar404QuandoAvatarNaoExiste() {
        when(perfilService.resolverCaminhoAvatar("inexistente.jpg"))
                .thenReturn(Paths.get("/tmp/inexistente.jpg"));

        var resultado = perfilController.servirAvatar("inexistente.jpg");

        assertThat(resultado.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    @DisplayName("Deve servir avatar existente e retornar 200 com Content-Disposition")
    void deveServirAvatarExistenteERetornar200(@TempDir Path tempDir) throws Exception {
        Path arquivoAvatar = tempDir.resolve("avatar.jpg");
        Files.write(arquivoAvatar, "conteudo-imagem".getBytes());

        when(perfilService.resolverCaminhoAvatar("avatar.jpg")).thenReturn(arquivoAvatar);

        var resultado = perfilController.servirAvatar("avatar.jpg");

        assertThat(resultado.getStatusCode().value()).isEqualTo(200);
        assertThat(resultado.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains("inline");
        assertThat(resultado.getBody()).isNotNull();
    }
}
