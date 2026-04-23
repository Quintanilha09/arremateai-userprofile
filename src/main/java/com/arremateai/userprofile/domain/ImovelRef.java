package com.arremateai.userprofile.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "imovel")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImovelRef {

    @Id
    private UUID id;

    @Column(name = "ativo")
    private Boolean ativo;
}
