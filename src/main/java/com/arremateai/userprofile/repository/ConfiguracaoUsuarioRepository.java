package com.arremateai.userprofile.repository;

import com.arremateai.userprofile.domain.ConfiguracaoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConfiguracaoUsuarioRepository extends JpaRepository<ConfiguracaoUsuario, UUID> {
    Optional<ConfiguracaoUsuario> findByUsuarioId(UUID usuarioId);
}
