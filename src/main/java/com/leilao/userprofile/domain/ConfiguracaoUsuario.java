package com.leilao.userprofile.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "configuracao_usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracaoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false, unique = true)
    private UUID usuarioId;

    // Notificações
    @Column(name = "notif_email_novos_leiloes")
    private Boolean notifEmailNovosLeiloes = true;

    @Column(name = "notif_email_favoritos")
    private Boolean notifEmailFavoritos = true;

    @Column(name = "notif_email_documentos")
    private Boolean notifEmailDocumentos = true;

    @Column(name = "notif_email_marketing")
    private Boolean notifEmailMarketing = false;

    @Column(name = "notif_alerta_leilao_proximos")
    private Boolean notifAlertaLeilaoProximos = true;

    @Column(name = "notif_alerta_mudanca_preco")
    private Boolean notifAlertaMudancaPreco = true;

    @Column(name = "notif_sms_alertas_importantes")
    private Boolean notifSmsAlertasImportantes = false;

    @Column(name = "tempo_antecedencia_alerta")
    private Integer tempoAntecedenciaAlerta = 24;

    // Privacidade
    @Column(name = "perfil_publico")
    private Boolean perfilPublico = false;

    @Column(name = "mostrar_favoritos")
    private Boolean mostrarFavoritos = false;

    @Column(name = "mostrar_historico_atividades")
    private Boolean mostrarHistoricoAtividades = false;

    // Segurança
    @Column(name = "dois_fatores_ativo")
    private Boolean doisFatoresAtivo = false;

    @Column(name = "lembrar_dispositivos")
    private Boolean lembrarDispositivos = true;

    @Column(name = "notif_login_novo_dispositivo")
    private Boolean notifLoginNovoDispositivo = true;

    // Preferências
    @Column(name = "tema", length = 10)
    private String tema = "light";

    @Column(name = "idioma", length = 5)
    private String idioma = "pt-BR";

    @Column(name = "moeda", length = 3)
    private String moeda = "BRL";

    @Column(name = "filtro_padrao_uf", length = 2)
    private String filtroPadraoUf;

    @Column(name = "filtro_padrao_cidade", length = 100)
    private String filtroPadraoCidade;

    @Column(name = "filtro_padrao_tipo", length = 20)
    private String filtroPadraoTipo;

    @Column(name = "filtro_padrao_valor_min")
    private Double filtroPadraoValorMin;

    @Column(name = "filtro_padrao_valor_max")
    private Double filtroPadraoValorMax;

    // Vendedor
    @Column(name = "anuncio_padrao_aceita_financiamento")
    private Boolean anuncioPadraoAceitaFinanciamento = true;

    @Column(name = "anuncio_padrao_visibilidade", length = 20)
    private String anuncioPadraoVisibilidade = "PUBLICO";

    @Column(name = "receber_propostas_diretas")
    private Boolean receberPropostasDiretas = true;

    // Conta
    @Column(name = "conta_desativada")
    private Boolean contaDesativada = false;

    @Column(name = "data_desativacao")
    private LocalDateTime dataDesativacao;

    @Column(name = "motivo_desativacao", columnDefinition = "TEXT")
    private String motivoDesativacao;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
