package com.arremateai.userprofile.controller;

import com.arremateai.userprofile.dto.FavoritoResponse;
import com.arremateai.userprofile.service.FavoritoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoritoControllerTest {

    @Mock
    private FavoritoService favoritoService;

    @InjectMocks
    private FavoritoController favoritoController;

    private static final String USER_ID_PADRAO = "123e4567-e89b-12d3-a456-426614174000";
    private static final UUID USER_UUID = UUID.fromString(USER_ID_PADRAO);
    private static final UUID IMOVEL_ID = UUID.randomUUID();

    private FavoritoResponse criarFavoritoResponse() {
        return new FavoritoResponse(UUID.randomUUID(), USER_UUID, IMOVEL_ID, null);
    }

    // ===== adicionar =====

    @Test
    @DisplayName("Deve adicionar favorito e retornar 201")
    void deveAdicionarFavoritoERetornar201() {
        var resposta = criarFavoritoResponse();
        when(favoritoService.adicionarFavorito(USER_UUID, IMOVEL_ID)).thenReturn(resposta);

        var resultado = favoritoController.adicionar(USER_ID_PADRAO, IMOVEL_ID);

        assertThat(resultado.getStatusCode().value()).isEqualTo(201);
        assertThat(resultado.getBody()).isNotNull();
        verify(favoritoService).adicionarFavorito(USER_UUID, IMOVEL_ID);
    }

    // ===== remover =====

    @Test
    @DisplayName("Deve remover favorito e retornar 204")
    void deveRemoverFavoritoERetornar204() {
        doNothing().when(favoritoService).removerFavorito(USER_UUID, IMOVEL_ID);

        var resultado = favoritoController.remover(USER_ID_PADRAO, IMOVEL_ID);

        assertThat(resultado.getStatusCode().value()).isEqualTo(204);
        verify(favoritoService).removerFavorito(USER_UUID, IMOVEL_ID);
    }

    // ===== listar =====

    @Test
    @DisplayName("Deve retornar lista de favoritos do usuário")
    void deveRetornarListaDeFavoritosDoUsuario() {
        when(favoritoService.listarFavoritos(USER_UUID)).thenReturn(List.of(criarFavoritoResponse()));

        var resultado = favoritoController.listar(USER_ID_PADRAO);

        assertThat(resultado.getStatusCode().value()).isEqualTo(200);
        assertThat(resultado.getBody()).hasSize(1);
    }

    // ===== verificar =====

    @Test
    @DisplayName("Deve retornar true quando imóvel é favorito")
    void deveRetornarTrueQuandoImovelEFavorito() {
        when(favoritoService.isFavorito(USER_UUID, IMOVEL_ID)).thenReturn(true);

        var resultado = favoritoController.verificar(USER_ID_PADRAO, IMOVEL_ID);

        assertThat(resultado.getStatusCode().value()).isEqualTo(200);
        assertThat(resultado.getBody()).containsEntry("favorito", true);
    }

    @Test
    @DisplayName("Deve retornar false quando imóvel não é favorito")
    void deveRetornarFalseQuandoImovelNaoEFavorito() {
        when(favoritoService.isFavorito(USER_UUID, IMOVEL_ID)).thenReturn(false);

        var resultado = favoritoController.verificar(USER_ID_PADRAO, IMOVEL_ID);

        assertThat(resultado.getBody()).containsEntry("favorito", false);
    }

    // ===== contar =====

    @Test
    @DisplayName("Deve retornar contagem total de favoritos")
    void deveRetornarContagemTotalDeFavoritos() {
        when(favoritoService.contarFavoritos(USER_UUID)).thenReturn(10L);

        var resultado = favoritoController.contar(USER_ID_PADRAO);

        assertThat(resultado.getStatusCode().value()).isEqualTo(200);
        assertThat(resultado.getBody()).containsEntry("total", 10L);
    }
}
