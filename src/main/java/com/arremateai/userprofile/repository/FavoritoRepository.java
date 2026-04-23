package com.arremateai.userprofile.repository;

import com.arremateai.userprofile.domain.Favorito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoritoRepository extends JpaRepository<Favorito, UUID> {
    List<Favorito> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<Favorito> findByUserIdAndImovelId(UUID userId, UUID imovelId);
    boolean existsByUserIdAndImovelId(UUID userId, UUID imovelId);
    long countByUserId(UUID userId);
    long countByImovelId(UUID imovelId);
}
