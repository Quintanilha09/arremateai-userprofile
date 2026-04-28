-- ============================================================================
-- QUINT-206: Projecao local minima da tabela `imovel` para userprofile
-- ----------------------------------------------------------------------------
-- CONTEXTO:
-- userprofile possui a entidade ImovelRef (com.arremateai.userprofile.domain)
-- que mapeia para a tabela `imovel`. Essa entidade existe APENAS para validar
-- que o imovel referenciado em Favorito existe e esta ativo (consultas
-- read-only via ImovelRefRepository).
--
-- O dono real da tabela `imovel` e o microsservico arremateai-property-catalog.
-- Em ambiente de banco compartilhado (legacy / dev local), a tabela ja existe
-- e ImovelRef funciona como projecao read-only. Em ambiente isolado (cada
-- servico com banco proprio, como staging Neon), a tabela nao existe e
-- Hibernate `ddl-auto: validate` falha no startup com:
--   "Schema-validation: missing table [imovel]"
--
-- SOLUCAO (curto prazo, debito tecnico):
-- Esta migration cria a tabela com a estrutura MINIMA que ImovelRef requer
-- (apenas `id` e `ativo`). Usa `IF NOT EXISTS` para nao conflitar em ambiente
-- compartilhado onde a tabela ja foi criada pelo property-catalog.
--
-- LIMITACAO:
-- Em ambiente isolado, a tabela ficara VAZIA. Operacoes que dependem dela
-- (FavoritoService.adicionarFavorito) retornarao "Imovel nao encontrado ou
-- inativo" para qualquer ID. Isso eh aceitavel para smoke test (so hita
-- /actuator/health) e para testes locais (mocks via @MockBean).
--
-- DEBITO TECNICO:
-- Esta projecao deve ser substituida por chamada HTTP ao property-catalog
-- (RestClient/Feign) ou por replicacao via eventos. Acompanhar QUINT-206
-- (https://app.plane.so/quintanilha/projects/8a43f65d-7b2e-4545-8b69-389bf5b116e0/issues/6b048d43-f8c0-4629-87fc-36371532bb0a/)
--
-- IMPORTANTE:
-- NAO popular esta tabela em userprofile. Manter VAZIA em staging/prod
-- isolado. Em ambiente compartilhado, sera populada pelo property-catalog
-- (e a estrutura completa la sobrescrevera essa minima).
-- ============================================================================

CREATE TABLE IF NOT EXISTS imovel (
    id UUID PRIMARY KEY,
    ativo BOOLEAN
);
