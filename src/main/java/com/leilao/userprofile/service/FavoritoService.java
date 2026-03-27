package com.leilao.userprofile.service;

import com.leilao.userprofile.domain.Favorito;
import com.leilao.userprofile.dto.FavoritoResponse;
import com.leilao.userprofile.exception.BusinessException;
import com.leilao.userprofile.repository.FavoritoRepository;
import com.leilao.userprofile.repository.ImovelRefRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final ImovelRefRepository imovelRefRepository;

    @Transactional
    public FavoritoResponse adicionarFavorito(UUID userId, UUID imovelId) {
        imovelRefRepository.findById(imovelId)
                .filter(i -> Boolean.TRUE.equals(i.getAtivo()))
                .orElseThrow(() -> new BusinessException("Imóvel não encontrado ou inativo"));

        if (favoritoRepository.existsByUserIdAndImovelId(userId, imovelId)) {
            throw new BusinessException("Imóvel já está nos favoritos");
        }

        Favorito favorito = Favorito.builder()
                .userId(userId)
                .imovelId(imovelId)
                .build();

        return mapToResponse(favoritoRepository.save(favorito));
    }

    @Transactional
    public void removerFavorito(UUID userId, UUID imovelId) {
        Favorito favorito = favoritoRepository.findByUserIdAndImovelId(userId, imovelId)
                .orElseThrow(() -> new BusinessException("Favorito não encontrado"));
        favoritoRepository.delete(favorito);
    }

    public List<FavoritoResponse> listarFavoritos(UUID userId) {
        return favoritoRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public boolean isFavorito(UUID userId, UUID imovelId) {
        return favoritoRepository.existsByUserIdAndImovelId(userId, imovelId);
    }

    public long contarFavoritos(UUID userId) {
        return favoritoRepository.countByUserId(userId);
    }

    private FavoritoResponse mapToResponse(Favorito f) {
        return new FavoritoResponse(f.getId(), f.getUserId(), f.getImovelId(), f.getCreatedAt());
    }
}
