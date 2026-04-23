package com.arremateai.userprofile.repository;

import com.arremateai.userprofile.domain.ImovelRef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ImovelRefRepository extends JpaRepository<ImovelRef, UUID> {
}
