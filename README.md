# 👤 ArremateAI - UserProfile Service

Microsserviço responsável pelo gerenciamento de perfil de usuários, favoritos e configurações personalizadas.

## 📋 Descrição

O UserProfile Service gerencia todas as informações e preferências do usuário:

- **Gerenciamento de perfil** (dados pessoais, avatar, senha)
- **Sistema de favoritos** (imóveis salvos)
- **Configurações do usuário** (notificações, privacidade, preferências)
- **Histórico de atividades**
- **Desativação de conta**

## 🛠️ Tecnologias

- **Java 17** (LTS)
- **Spring Boot 3.2.2**
- **Spring Data JPA** - Persistência
- **PostgreSQL 16** - Banco de dados
- **Redis 7** - Cache de perfis e favoritos
- **Spring Cache** - Abstração de cache
- **Validation API** - Validação de dados
- **MapStruct** - Mapeamento DTO/Entity

## 🏗️ Arquitetura

```
┌──────────────────┐
│  Gateway :8080   │
└────────┬─────────┘
         │
         ▼
┌─────────────────────────────┐
│   UserProfile Service       │
│      (Port 8082)            │
├─────────────────────────────┤
│ Controllers                 │
│  ├─ PerfilController        │
│  ├─ FavoritoController      │
│  └─ ConfiguracaoController  │
├─────────────────────────────┤
│ Services                    │
│  ├─ PerfilService           │
│  ├─ FavoritoService         │
│  └─ ConfiguracaoService     │
└─────────┬─────────┬─────────┘
          │         │
          ▼         ▼
    PostgreSQL    Redis
     (5436)      (6381)
```

## 📦 Estrutura do Projeto

```
src/main/java/com/arremateai/userprofile/
├── UserProfileApplication.java
├── controller/
│   ├── PerfilController.java             # Gerenciamento de perfil
│   ├── FavoritoController.java           # Favoritos
│   └── ConfiguracaoController.java       # Configurações
├── domain/
│   ├── Usuario.java                      # Entidade usuário
│   ├── Favorito.java                     # Relação user-imovel
│   ├── ImovelRef.java                    # Referência do imóvel
│   └── ConfiguracaoUsuario.java          # Configurações key-value
├── dto/
│   ├── PerfilResponse.java
│   ├── AtualizarPerfilRequest.java
│   ├── AlterarSenhaRequest.java
│   ├── FavoritoResponse.java
│   ├── ConfiguracaoResponse.java
│   └── DesativarContaRequest.java
├── repository/
│   ├── UsuarioRepository.java
│   ├── FavoritoRepository.java
│   ├── ImovelRefRepository.java
│   └── ConfiguracaoUsuarioRepository.java
├── service/
│   ├── PerfilService.java
│   ├── FavoritoService.java
│   └── ConfiguracaoService.java
└── exception/
    ├── BusinessException.java
    └── GlobalExceptionHandler.java
```

## 🚀 Endpoints Principais

### Perfil do Usuário

#### GET `/api/perfil`
Obtém perfil completo do usuário autenticado.

**Headers:**
```
Authorization: Bearer {token}
```

**Response 200:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nome": "João Silva",
  "email": "joao@example.com",
  "telefone": "+5511999999999",
  "cpf": "12345678900",
  "avatar": "https://cdn.arremateai.com/avatars/550e8400.jpg",
  "tipoUsuario": "USER",
  "statusVendedor": null,
  "emailVerificado": true,
  "ativo": true,
  "createdAt": "2026-01-15T10:00:00Z",
  "updatedAt": "2026-03-27T10:30:00Z",
  "estatisticas": {
    "totalFavoritos": 8,
    "totalImoveis": 0,
    "totalVisualizacoes": 0
  }
}
```

#### PUT `/api/perfil`
Atualiza dados do perfil.

**Request:**
```json
{
  "nome": "João Silva Santos",
  "telefone": "+5511988888888",
  "cpf": "12345678900"
}
```

**Response 200:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nome": "João Silva Santos",
  "telefone": "+5511988888888",
  "updatedAt": "2026-03-27T11:00:00Z"
}
```

#### POST `/api/perfil/avatar`
Upload de avatar do usuário.

**Headers:**
```
Content-Type: multipart/form-data
Authorization: Bearer {token}
```

**Request (Form Data):**
```
avatar: [image file]
```

**Response 200:**
```json
{
  "avatarUrl": "https://cdn.arremateai.com/avatars/550e8400.jpg",
  "updatedAt": "2026-03-27T11:00:00Z"
}
```

#### PUT `/api/perfil/senha`
Alterar senha do usuário.

**Request:**
```json
{
  "senhaAtual": "SenhaAntiga@123",
  "novaSenha": "SenhaNova@456",
  "confirmarSenha": "SenhaNova@456"
}
```

**Response 200:**
```json
{
  "message": "Senha alterada com sucesso"
}
```

#### POST `/api/perfil/desativar`
Desativa conta do usuário (soft delete).

**Request:**
```json
{
  "senha": "Senha@123",
  "motivo": "Não utilizo mais o serviço",
  "feedback": "Encontrei outra plataforma mais adequada"
}
```

**Response 200:**
```json
{
  "message": "Conta desativada com sucesso",
  "dataDesativacao": "2026-03-27T11:00:00Z",
  "recuperacaoPossivel": true,
  "prazoRecuperacao": "30 dias"
}
```

### Favoritos

#### GET `/api/favoritos`
Lista todos os imóveis favoritados pelo usuário.

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
```
?page=0&size=20
```

**Response 200:**
```json
{
  "content": [
    {
      "id": "fav-uuid",
      "imovel": {
        "id": "imovel-uuid",
        "titulo": "Casa 3 quartos no Jardim Paulista",
        "preco": 850000.00,
        "tipo": "RESIDENCIAL",
        "cidade": "São Paulo",
        "estado": "SP",
        "imagemPrincipal": "https://cdn.arremateai.com/imoveis/abc123/sala.jpg"
      },
      "favoritadoEm": "2026-03-20T10:00:00Z"
    }
  ],
  "totalElements": 8,
  "totalPages": 1
}
```

#### POST `/api/favoritos/{imovelId}`
Adiciona imóvel aos favoritos.

**Response 201:**
```json
{
  "id": "fav-uuid",
  "imovelId": "imovel-uuid",
  "usuarioId": "550e8400-e29b-41d4-a716-446655440000",
  "favoritadoEm": "2026-03-27T11:00:00Z"
}
```

#### DELETE `/api/favoritos/{imovelId}`
Remove imóvel dos favoritos.

**Response 204** (No Content)

#### GET `/api/favoritos/check/{imovelId}`
Verifica se imóvel está favoritado.

**Response 200:**
```json
{
  "isFavorito": true,
  "favoritoId": "fav-uuid"
}
```

### Configurações

#### GET `/api/configuracoes`
Obtém todas as configurações do usuário.

**Headers:**
```
Authorization: Bearer {token}
```

**Response 200:**
```json
{
  "notificacoes": {
    "email": true,
    "push": true,
    "sms": false,
    "novosImoveis": true,
    "atualizacoesPreco": true,
    "leiloes": true,
    "mensagensVendedor": true
  },
  "privacidade": {
    "perfilPublico": false,
    "exibirTelefone": false,
    "exibirEmail": false
  },
  "preferencias": {
    "idioma": "pt-BR",
    "moeda": "BRL",
    "tema": "light",
    "itensPerPage": 20
  }
}
```

#### PUT `/api/configuracoes`
Atualiza configurações do usuário.

**Request:**
```json
{
  "chave": "notificacoes.email",
  "valor": "false"
}
```

**Response 200:**
```json
{
  "chave": "notificacoes.email",
  "valor": "false",
  "updatedAt": "2026-03-27T11:00:00Z"
}
```

#### PUT `/api/configuracoes/batch`
Atualiza múltiplas configurações de uma vez.

**Request:**
```json
{
  "configuracoes": [
    {
      "chave": "notificacoes.email",
      "valor": "false"
    },
    {
      "chave": "privacidade.perfilPublico",
      "valor": "true"
    },
    {
      "chave": "preferencias.tema",
      "valor": "dark"
    }
  ]
}
```

**Response 200:**
```json
{
  "atualizadas": 3,
  "updatedAt": "2026-03-27T11:00:00Z"
}
```

#### DELETE `/api/configuracoes/{chave}`
Remove uma configuração (volta ao padrão).

**Response 204** (No Content)

## 📊 Sistema de Configurações

### Estrutura Key-Value

As configurações são armazenadas em formato key-value com suporte a namespaces:

```
notificacoes.email = true
notificacoes.push = true
notificacoes.novosImoveis = true
privacidade.perfilPublico = false
preferencias.idioma = pt-BR
preferencias.tema = light
```

### Configurações Padrão

| Chave | Valor Padrão | Descrição |
|-------|--------------|-----------|
| `notificacoes.email` | `true` | Notificações por email |
| `notificacoes.push` | `true` | Notificações push |
| `notificacoes.sms` | `false` | Notificações por SMS |
| `notificacoes.novosImoveis` | `true` | Alerta novos imóveis |
| `notificacoes.atualizacoesPreco` | `true` | Alerta mudança de preço |
| `privacidade.perfilPublico` | `false` | Perfil visível publicamente |
| `privacidade.exibirTelefone` | `false` | Mostrar telefone no perfil |
| `privacidade.exibirEmail` | `false` | Mostrar email no perfil |
| `preferencias.idioma` | `pt-BR` | Idioma da interface |
| `preferencias.moeda` | `BRL` | Moeda para exibição |
| `preferencias.tema` | `light` | Tema (light/dark) |
| `preferencias.itensPerPage` | `20` | Itens por página |

## ⚙️ Variáveis de Ambiente

```bash
# Server
SERVER_PORT=8082

# Database
DB_HOST=localhost
DB_PORT=5436
DB_NAME=userprofile_db
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Redis (Cache)
REDIS_HOST=localhost
REDIS_PORT=6381
CACHE_TTL=3600

# Avatar Upload
AVATAR_STORAGE_TYPE=S3
AWS_S3_BUCKET=arremateai-avatars
AWS_S3_REGION=us-east-1
AVATAR_MAX_SIZE=5242880
AVATAR_ALLOWED_TYPES=image/jpeg,image/png,image/webp

# Media Service Integration
MEDIA_SERVICE_URL=http://localhost:8085

# Property Catalog Integration
PROPERTY_CATALOG_URL=http://localhost:8084

# Cache
PROFILE_CACHE_TTL=3600
FAVORITOS_CACHE_TTL=1800
CONFIGURACOES_CACHE_TTL=7200
```

## 🏃 Como Executar

```bash
# Clone o repositório
git clone https://github.com/Quintanilha09/arremateai-userprofile.git
cd arremateai-userprofile

# Suba o banco de dados
docker-compose up -d postgres redis

# Execute a aplicação
./mvnw spring-boot:run
```

## 📊 Banco de Dados

### `usuario`
```sql
CREATE TABLE usuario (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    telefone VARCHAR(20),
    cpf VARCHAR(11) UNIQUE,
    avatar VARCHAR(500),
    tipo_usuario VARCHAR(20) NOT NULL,
    status_vendedor VARCHAR(50),
    email_verificado BOOLEAN DEFAULT FALSE,
    ativo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

### `favorito`
```sql
CREATE TABLE favorito (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    imovel_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(usuario_id, imovel_id)
);

CREATE INDEX idx_favorito_usuario ON favorito(usuario_id);
CREATE INDEX idx_favorito_imovel ON favorito(imovel_id);
```

### `imovel_ref`
```sql
CREATE TABLE imovel_ref (
    id UUID PRIMARY KEY,
    titulo VARCHAR(255),
    preco DECIMAL(15,2),
    tipo VARCHAR(50),
    cidade VARCHAR(100),
    estado VARCHAR(2),
    imagem_principal VARCHAR(500),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

### `configuracao_usuario`
```sql
CREATE TABLE configuracao_usuario (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    chave VARCHAR(100) NOT NULL,
    valor TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(usuario_id, chave)
);

CREATE INDEX idx_configuracao_usuario ON configuracao_usuario(usuario_id);
CREATE INDEX idx_configuracao_chave ON configuracao_usuario(chave);
```

## 🔄 Cache Strategy

### Níveis de Cache

1. **Perfil do Usuário** (Redis)
   - TTL: 1 hora
   - Invalidado em UPDATE
   - Key pattern: `profile:user:{userId}`

2. **Favoritos** (Redis)
   - TTL: 30 minutos
   - Invalidado em ADD/REMOVE favorito
   - Key pattern: `favorites:user:{userId}`

3. **Configurações** (Redis)
   - TTL: 2 horas
   - Invalidado em UPDATE configuração
   - Key pattern: `config:user:{userId}`

```java
@Cacheable(value = "profiles", key = "#userId")
public PerfilResponse getProfile(UUID userId) {
    // ...
}

@CacheEvict(value = "profiles", key = "#userId")
public void updateProfile(UUID userId, AtualizarPerfilRequest request) {
    // ...
}
```

## 🧪 Testes

```bash
# Unit tests
./mvnw test

# Integration tests
./mvnw verify

# Coverage
./mvnw jacoco:report
```

## 📈 Integração com Outros Serviços

### Property Catalog Service
- Busca detalhes de imóveis favoritados
- Valida existência do imóvel ao favoritar
- Sincroniza dados de imóveis (cache local)

### Media Service
- Upload de avatar via endpoint interno
- Processamento de imagens (resize, compress)
- CDN URLs para avatares

### Identity Service
- Validação de senha ao alterar perfil
- Atualização de dados de autenticação
- Integração com processo de desativação de conta

## 🔧 Troubleshooting

### Avatar não está sendo salvo
- Verifique permissões do bucket S3
- Confirme tamanho máximo do arquivo (5MB)
- Valide extensão da imagem (jpeg, png, webp)

### Favoritos não aparecem
- Verifique cache Redis: `redis-cli GET favorites:user:UUID`
- Confirme conexão com Property Catalog Service
- Valide se imóvel ainda existe

## 📄 Licença

Proprietary - © 2026 ArremateAI
