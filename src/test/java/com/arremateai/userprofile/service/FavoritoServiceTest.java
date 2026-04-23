package com.arremateai.userprofile.service;

import com.arremateai.userprofile.domain.Favorito;
import com.arremateai.userprofile.domain.ImovelRef;
import com.arremateai.userprofile.dto.FavoritoResponse;
import com.arremateai.userprofile.exception.BusinessException;
import com.arremateai.userprofile.repository.FavoritoRepository;
import com.arremateai.userprofile.repository.ImovelRefRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoritoServiceTest {

    @Mock
    private FavoritoRepository favoritoRepository;

    @Mock
    private ImovelRefRepository imovelRefRepository;

    @InjectMocks
    private FavoritoService favoritoService;

    private static final UUID USER_ID_PADRAO = UUID.randomUUID();
    private static final UUID IMOVEL_ID_PADRAO = UUID.randomUUID();
    private static final UUID FAVORITO_ID_PADRAO = UUID.randomUUID();

    private ImovelRef criarImovelRefAtivo() {
        return new ImovelRef(IMOVEL_ID_PADRAO, true);
    }

    private Favorito criarFavoritoPadrao() {
        return Favorito.builder()
                .id(FAVORITO_ID_PADRAO)
                .userId(USER_ID_PADRAO)
                .imovelId(IMOVEL_ID_PADRAO)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ---- adicionarFavorito ----

    @Test
    @DisplayName("Deve adicionar favorito com sucesso")
    void deveAdicionarFavoritoComSucesso() {
        when(imovelRefRepository.findById(IMOVEL_ID_PADRAO)).thenReturn(Optional.of(criarImovelRefAtivo()));
        when(favoritoRepository.existsByUserIdAndImovelId(USER_ID_PADRAO, IMOVEL_ID_PADRAO)).thenReturn(false);
        when(favoritoRepository.save(any(Favorito.class))).thenReturn(criarFavoritoPadrao());

        FavoritoResponse resultado = favoritoService.adicionarFavorito(USER_ID_PADRAO, IMOVEL_ID_PADRAO);

        assertThat(resultado).isNotNull();
        assertThat(resultado.imovelId()).isEqualTo(IMOVEL_ID_PADRAO);
        verify(favoritoRepository).save(any(Favorito.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando imóvel não encontrado ou inativo")
    void deveLancarExcecaoQuandoImovelNaoEncontradoOuInativo() {
        when(imovelRefRepository.findById(IMOVEL_ID_PADRAO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoritoService.adicionarFavorito(USER_ID_PADRAO, IMOVEL_ID_PADRAO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não encontrado ou inativo");
    }

    @Test
    @DisplayName("Deve lançar exceção quando imóvel estiver inativo")
    void deveLancarExcecaoQuandoImovelEstiverInativo() {
        var imovelInativo = new ImovelRef(IMOVEL_ID_PADRAO, false);
        when(imovelRefRepository.findById(IMOVEL_ID_PADRAO)).thenReturn(Optional.of(imovelInativo));

        assertThatThrownBy(() -> favoritoService.adicionarFavorito(USER_ID_PADRAO, IMOVEL_ID_PADRAO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não encontrado ou inativo");
    }

    @Test
    @DisplayName("Deve lançar exceção quando imóvel já estiver nos favoritos")
    void deveLancarExcecaoQuandoImovelJaEstiverNosFavoritos() {
        when(imovelRefRepository.findById(IMOVEL_ID_PADRAO)).thenReturn(Optional.of(criarImovelRefAtivo()));
        when(favoritoRepository.existsByUserIdAndImovelId(USER_ID_PADRAO, IMOVEL_ID_PADRAO)).thenReturn(true);

        assertThatThrownBy(() -> favoritoService.adicionarFavorito(USER_ID_PADRAO, IMOVEL_ID_PADRAO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já está nos favoritos");
    }

    // ---- removerFavorito ----

    @Test
    @DisplayName("Deve remover favorito com sucesso")
    void deveRemoverFavoritoComSucesso() {
        var favorito = criarFavoritoPadrao();
        when(favoritoRepository.findByUserIdAndImovelId(USER_ID_PADRAO, IMOVEL_ID_PADRAO))
                .thenReturn(Optional.of(favorito));

        favoritoService.removerFavorito(USER_ID_PADRAO, IMOVEL_ID_PADRAO);

        verify(favoritoRepository).delete(favorito);
    }

    @Test
    @DisplayName("Deve lançar exceção quando favorito não encontrado ao remover")
    void deveLancarExcecaoQuandoFavoritoNaoEncontradoAoRemover() {
        when(favoritoRepository.findByUserIdAndImovelId(USER_ID_PADRAO, IMOVEL_ID_PADRAO))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoritoService.removerFavorito(USER_ID_PADRAO, IMOVEL_ID_PADRAO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Favorito não encontrado");
    }

    // ---- listarFavoritos ----

    @Test
    @DisplayName("Deve retornar lista de favoritos do usuário")
    void deveRetornarListaDeFavoritosDoUsuario() {
        when(favoritoRepository.findByUserIdOrderByCreatedAtDesc(USER_ID_PADRAO))
                .thenReturn(List.of(criarFavoritoPadrao()));

        List<FavoritoResponse> resultado = favoritoService.listarFavoritos(USER_ID_PADRAO);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).imovelId()).isEqualTo(IMOVEL_ID_PADRAO);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver favoritos")
    void deveRetornarListaVaziaQuandoNaoHouverFavoritos() {
        when(favoritoRepository.findByUserIdOrderByCreatedAtDesc(USER_ID_PADRAO)).thenReturn(List.of());

        List<FavoritoResponse> resultado = favoritoService.listarFavoritos(USER_ID_PADRAO);

        assertThat(resultado).isEmpty();
    }

    // ---- isFavorito ----

    @Test
    @DisplayName("Deve retornar true quando imóvel for favorito")
    void deveRetornarTrueQuandoImovelForFavorito() {
        when(favoritoRepository.existsByUserIdAndImovelId(USER_ID_PADRAO, IMOVEL_ID_PADRAO)).thenReturn(true);

        assertThat(favoritoService.isFavorito(USER_ID_PADRAO, IMOVEL_ID_PADRAO)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando imóvel não for favorito")
    void deveRetornarFalseQuandoImovelNaoForFavorito() {
        when(favoritoRepository.existsByUserIdAndImovelId(USER_ID_PADRAO, IMOVEL_ID_PADRAO)).thenReturn(false);

        assertThat(favoritoService.isFavorito(USER_ID_PADRAO, IMOVEL_ID_PADRAO)).isFalse();
    }

    // ---- contarFavoritos ----

    @Test
    @DisplayName("Deve retornar contagem de favoritos do usuário")
    void deveRetornarContagemDeFavoritosDoUsuario() {
        when(favoritoRepository.countByUserId(USER_ID_PADRAO)).thenReturn(3L);

        assertThat(favoritoService.contarFavoritos(USER_ID_PADRAO)).isEqualTo(3L);
    }
}
