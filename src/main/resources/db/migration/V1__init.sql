-- ============================================================================
-- E16-H7: Schema inicial do arremateai-userprofile
-- ----------------------------------------------------------------------------
-- Deriva das entidades JPA em com.arremateai.userprofile.domain:
--   - Usuario, Favorito, ConfiguracaoUsuario
--
-- Nota: ImovelRef mapeia a tabela `imovel` que pertence ao microsservico
-- arremateai-property-catalog (dono do schema). Portanto NAO e criada aqui
-- para evitar conflito de ownership entre servicos no banco compartilhado.
--
-- Uso de CREATE TABLE IF NOT EXISTS: o banco `arremateai` ja esta em uso
-- compartilhado; a idempotencia garante que esta baseline nao quebre
-- ambientes existentes. Cada servico possui seu proprio
-- `flyway_schema_history_*` (ver application.yml).
-- ============================================================================

-- Usuario (dados principais de perfil)
CREATE TABLE IF NOT EXISTS usuario (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    senha VARCHAR(255),
    telefone VARCHAR(255),
    cpf VARCHAR(255),
    tipo VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(255),
    ativo BOOLEAN NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_userprofile_usuario_email UNIQUE (email)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_userprofile_usuario_email ON usuario (email);

-- Favorito (imoveis marcados pelo usuario)
CREATE TABLE IF NOT EXISTS favorito (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL,
    imovel_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_favorito_usuario_imovel UNIQUE (usuario_id, imovel_id)
);

CREATE INDEX IF NOT EXISTS idx_favorito_usuario_id ON favorito (usuario_id);
CREATE INDEX IF NOT EXISTS idx_favorito_imovel_id ON favorito (imovel_id);

-- Configuracao do usuario (preferencias, notificacoes, privacidade, seguranca)
CREATE TABLE IF NOT EXISTS configuracao_usuario (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL,
    -- Notificacoes
    notif_email_novos_leiloes BOOLEAN,
    notif_email_favoritos BOOLEAN,
    notif_email_documentos BOOLEAN,
    notif_email_marketing BOOLEAN,
    notif_alerta_leilao_proximos BOOLEAN,
    notif_alerta_mudanca_preco BOOLEAN,
    notif_sms_alertas_importantes BOOLEAN,
    tempo_antecedencia_alerta INTEGER,
    -- Privacidade
    perfil_publico BOOLEAN,
    mostrar_favoritos BOOLEAN,
    mostrar_historico_atividades BOOLEAN,
    -- Seguranca
    dois_fatores_ativo BOOLEAN,
    lembrar_dispositivos BOOLEAN,
    notif_login_novo_dispositivo BOOLEAN,
    -- Preferencias
    tema VARCHAR(10),
    idioma VARCHAR(5),
    moeda VARCHAR(3),
    filtro_padrao_uf VARCHAR(2),
    filtro_padrao_cidade VARCHAR(100),
    filtro_padrao_tipo VARCHAR(20),
    filtro_padrao_valor_min DOUBLE PRECISION,
    filtro_padrao_valor_max DOUBLE PRECISION,
    -- Vendedor
    anuncio_padrao_aceita_financiamento BOOLEAN,
    anuncio_padrao_visibilidade VARCHAR(20),
    receber_propostas_diretas BOOLEAN,
    -- Conta
    conta_desativada BOOLEAN,
    data_desativacao TIMESTAMP,
    motivo_desativacao TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_configuracao_usuario_usuario_id UNIQUE (usuario_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_configuracao_usuario_usuario_id
    ON configuracao_usuario (usuario_id);
