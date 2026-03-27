package com.leilao.userprofile.repository;

import com.leilao.userprofile.domain.ConfiguracaoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConfiguracaoUsuarioRepository extends JpaRepository<ConfiguracaoUsuario, UUID> {
    Optional<ConfiguracaoUsuario> findByUsuarioId(UUID usuarioId);
}
