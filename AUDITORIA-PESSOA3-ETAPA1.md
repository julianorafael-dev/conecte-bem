# Audiência Técnica ConecteBem - Pessoa 3 - Etapa 1

## 1. Confirmar o Ambiente

### Caminho do Projeto
- `C:\Users\Gilmar\Documents\conecte-bem`
- Projeto Git válido com histórico completo

### Branch Atual e `git status`
- Branch: `develop`
- Status: `On branch develop, Your branch is up to date with 'origin/develop'.`
- Arquivos sem rastreio: Apenas `GIT-RECOVERY-REPORT.md` (arquivo criado durante tarefa de recuperação anterior, não modifica código-fonte)
- Working tree: Clean (nenhuma alteração pendente nos arquivos de código)

### Últimos cinco commits
```
9048ec1 Merge pull request #36 from julianorafael-dev/refactor/estrutura-pastas-import
6da3924 refactor: ajusta pastas e imports
09e925d Merge pull request #35 from julianorafael-dev/feature/login
a0fa9bd feat: implementa tela de login
64791de Merge pull request #34 from julianorafael-dev/candidaturas
```

### Branches locais e remotas
**Locais:** develop, feature/criar-oportunidade, feature/documentacao-readme, feature/oportunidade-crud, main
**Remotas (origin):**
- develop em `9048ec1` (corresponde ao local)
- main em `d4a0f91`
- feature/criar-oportunidade, feature/documentacao-readme, feature/ong-crud, feature/oportunidade-crud
- main, login-google, revert-25, revert-26, etc.

### Merge pendente
- Não existe merge pendente. Anteriormente havia merge em andamento em `main` ("All conflicts fixed but still merging"), mas foi abortado com `git merge --abort` e `main` restaurado para `origin/main`.

### URL do remote `origin`
- `https://github.com/julianorafael-dev/conecte-bem.git`
- Fetch e push configurados

---
## 2. Auditar o Backend (Spring Boot)

### Estrutura de Controladores e Serviços

#### OportunidadeController (`/oportunidades`)
- **GET /oportunidades/{id}**: Busca oportunidade por ID → retorna `OportunidadeResponseDTO`
- **POST /oportunidades**: Cria nova oportunidade → usa `OportunidadeRequestDTO` como body
- **PUT /oportunidades/{id}**: Atualiza oportunidade existente → usa `OportunidadeRequestDTO` body
- **DELETE /oportunidades/{id}**: Exclui oportunidade por ID
- **GET /oportunidades**: Lista oportunidades com filtros opcionais:
  - `cidade` (String, opcional)
  - `categoria` (Integer, opcional)
  - `status` (String, opcional)
  - Se nenhum filtro: retorna tudo (método `listar()`)

#### OportunidadeService
Responsável pela lógica de negócio:

**listar()**: Retorna todas oportunidades via `oportunidadeRepository.findAll()`

**buscarPorId(Integer id)**: Busca no repositório, lança `ResourceNotFoundException` se não encontrado

**criar(OportunidadeRequestDTO dados)**:
1. Busca ONG pelo `ongId` do RequestDTO (valida existência)
2. Busca Categoria pelo `categoriaId` do RequestDTO (valida existência)
3. Cria nova entidade Oportunidade, relaciona com ONG e Categoria
4. Preenche todos os campos: titulo, descricao, data, horario, cidade, estado, vagas, status
5. Define `criadoEm` com data/hora atual
6. Salva no repositório e retorna DTO de resposta

**atualizar(Integer id, OportunidadeRequestDTO dados)**:
1. Verifica se oportunidade existe (lança exceção se não encontrar)
2. Busca ONG pelo `ongId` do RequestDTO (apenas valida existência, NÃO verifica propriedade)
3. Busca Categoria pelo `categoriaId` (apenas valida existência)
4. Atualiza campos: ong, categoria, titulo, descricao, data, horario, cidade, estado, vagas, status
5. Salva e retorna DTO

**excluir(Integer id)**:
- Verifica se existe antes de excluir
- Lança exceção se não existir
- Deleta do repositório

**Métodos de filtro:**
- `filtrarPorCidade(String cidade)`: `oportunidadeRepository.findByCidade(cidade)`
- `filtrarPorCategoria(Integer categoriaId)`: valida categoria, depois `findByCategoria`
- `filtrarPorStatus(String status)`: `oportunidadeRepository.findByStatus(status)`

#### OportunidadeRepository (Spring Data JPA)
Extende `JpaRepository<Oportunidade, Integer>`:
- Métodos nativos: `findByCidade`, `findByCategoria`, `findByStatus`
- Métodos úteis para Person 3:
  - `countByOngUsuarioId(Integer usuarioId)`: conta oportunidades da ONG do usuário
  - `countByOngUsuarioIdAndStatus(Integer usuarioId, String status)`: conta por status
  - `countInscricoesByOngUsuarioId(Integer usuarioId)`: conta inscrições ativas

#### OngRepository
Extende `JpaRepository<Ong, Integer>`:
- `findByUsuarioId(Integer usuarioId)`: busca ONG pelo ID do usuário logado

### Entidades Relacionadas

#### Ong
- Atributos: id, usuarioId (único), nome, cnpj, descricao, telefone, cidade, estado
- Relacionamento: One ONG has many Opportunities (ManyToOne inverso)
- Chave estrangeira: `usuario_id` vincula perfil de usuário ao cadastro de ONG
- Método `getUsuarioId()` expõe o ID do usuário dono da ONG

#### Oportunidade
- Atributos: id, ong (ManyToOne), categoria (ManyToOne), titulo, descricao, data, horario, cidade, estado, vagas, status, criadoEm
- Relacionamentos:
  - ManyToOne com Ong (via `@JoinColumn(name = "ong_id")`)
  - ManyToOne com Categoria (via `@JoinColumn(name = "categoria_id")`)
- A entidade expõe `getOng()` e `getCategoria()` para acesso aos relacionamentos

#### Categoria
- Atributos: id, nome, descricao

#### Usuario
- Atributos: id, nome, email, senha, tipo (VOLUNTARIO ou ONG), criadoEm
- O campo `tipo` determina se o usuário é ONG ou Voluntário

#### TipoUsuario Enum
- VOLUNTARIO, ONG

### Segurança e Autorização (`SecurityConfig`)

#### Configuração atual
```java
.requestMatchers("/health").permitAll()
.requestMatchers("/auth/**").permitAll() // login/registro
.requestMatchers("/ongs/me/**").hasRole("ONG") // somente ONG
.requestMatchers("/voluntarios/me").hasRole("VOLUNTARIO") // somente voluntário
.requestMatchers(HttpMethod.GET, "/ongs/**").permitAll() // públicas
.requestMatchers(HttpMethod.GET, "/oportunidades/**").permitAll() // PÚBLICO - CRITICAL
.requestMatchers(HttpMethod.GET, "/categorias/**").permitAll()
.anyRequest().authenticated()
```

#### Problemas de segurança identificados

**1. CRÍTICO: `/oportunidades/**` GET é público**
- Qualquer pessoa pode listar e buscar oportunidades sem estar autenticado
- O frontend contorna isso filtrando por `ongId` localmente, mas o backend não impõe restrição

**2. `/ongs/me/**` requer ROLE ONG**
- Correta restrição, mas depende do JWT ter o papel definido corretamente

**3. Validação de propriedade da ausente no serviço**
- No `OportunidadeService.criar()`: o `ongId` vem do RequestDTO (enviado pelo frontend) e apenas valida se a ONG existe no banco, não se pertence ao usuário autenticado
- No `OportunidadeService.atualizar()`: mesmo problema - busca a ONG pelo ID do DTO, não verifica se é a ONG do usuário logado
- Isso significa que, tecnicamente, qualquer um poderia criar/editar oportunidades passando o `ongId` de outra ONG no RequestDTO

**4. Como a autenticação flui no momento**
- Frontend obtém token via `localStorage.getItem('token')`
- API wrapper adiciona `Authorization: Bearer ${token}` nos headers
- O `JwtAuthFilter` processa o token e popula o contexto de segurança do Spring
- O backend usa `@AuthenticationPrincipal Usuario usuario` para obter dados do usuário logado (já usado em `OngController.me`)
- Porém, `OportunidadeController` e `OportunidadeService` NÃO recebem atualmente o `Usuario` logado para validar propriedade

### DTOs de Oportunidade

#### OportunidadeRequestDTO (entrada)
```java
public record OportunidadeRequestDTO(
    @NotNull Integer ongId,
    @NotNull Integer categoriaId,
    @NotBlank @Size(max = 200) String titulo,
    String descricao,
    @NotNull LocalDate data,
    @NotNull LocalTime horario,
    @Size(max = 100) String cidade,
    @Pattern(regexp = "^[A-Z]{2}$") String estado,
    @NotNull @Min(1) Integer vagas,
    @NotBlank String status
) {}
```

**Campos obrigatórios e validações:**
- `ongId` - ID da ONG que está criando/editingando (vindo do frontend)
- `categoriaId` - ID da categoria
- `titulo` - obrigatório, máximo 200 caracteres
- `data` - formato yyyy-MM-dd, obrigatório
- `horario` - formato HH:mm, obrigatório
- `estado` - deve ter exatamente 2 letras maiúsculas (validado via regex)
- `vagas` - mínimo 1
- `status` - deve ser preenchido

#### OportunidadeResponseDTO (saída)
```java
public record OportunidadeResponseDTO(
    Integer id,
    Integer ongId,
    String nomeOng,
    Integer categoriaId,
    String nomeCategoria,
    String titulo,
    String descricao,
    LocalDate data,
    LocalTime horario,
    String cidade,
    String estado,
    Integer vagas,
    String status,
    LocalDateTime criadoEm
) {}
```

**Campos devolvidos ao frontend:**
- `ongId` e `nomeOng` - identificam a ONG dona da oportunidade
- `categoriaId` e `nomeCategoria` - categoria da oportunidade
- Todos os campos da oportunidade mais `criadoEm`

### Como a identidade autenticada é obtida

#### Backend
- O `JwtAuthFilter` intercepta requisições e decodifica o JWT do header `Authorization`
- O usuário logado pode ser injetado via `@AuthenticationPrincipal Usuario usuario` nos controllers
- Já usado em `OngController.me()` e `OngController.meuPainel()`
- **Não está sendo usado** nos controllers/services de oportunidade atualmente

#### Frontend
- Token salvo em `localStorage` com chave 'token'
- `getToken()` retorna o token armazenado
- `getUsuario()` decodifica o JWT e retorna `{id, email, tipo}`
- `isOng()` verifica se `tipo === 'ONG'`
- `isVoluntario()` verifica se `tipo === 'VOLUNTARIO'`
- Essas funções são usadas no `RotaONG` para proteger rotas e em `FormOportunidade` para validade local

### Vínculo ONG - Usuário

#### Estrutura atual
1. Usuário se cadastra como `tipo = ONG` no sistema
2. Ao criar perfil de ONG, o sistema gerencia um `usuarioId` único na tabela `ongs`
3. Cada ONG tem exatamente um `usuarioId` (constraint `unique = true`)
4. O frontend obtém o `ongId` chamando `api('/ongs/me')`
5. O `ongId` vem do campo `id` da entidade Ong, que está vinculado ao `usuarioId` do usuário

#### Gap identificado
- Não há validação direta de que o `usuarioId` do usuário logado corresponde ao `usuarioId` da ONG dona da oportunidade
- O sistema confia no `ongId` enviado pelo frontend no RequestDTO
- O `SecurityConfig` não tem regras específicas para endpoints de oportunidade baseadas no papel da ONG

---
## 3. Auditar o Frontend (React)

### App.jsx e Rotas

#### Estrutura de rotas
- Rotas públicas: `/`, `/Home`, `/Oportunidades`, `/ComoFunciona`, `/Sobre`, `/Contato`
- Área de ONG protegida por `<RotaONG>` component:
  - `/painel` (com PainelOng)
  - `/painel/oportunidades` (GerenciarOportunidades)
  - `/painel/oportunidades/nova` (FormOportunidade para criar)
  - `/painel/oportunidades/:id/editar` (FormOportunidade para editar)
  - `/painel/oportunidades/:id/inscritos` (VoluntariosInscritos)
- Rotas de perfil: `/PerfilOng`, `/PerfilVoluntario` (abertas temporariamente)
- Área de voluntário: `/painel/candidaturas` (protegida por RotaVoluntario)

#### Componente RotaONG
```jsx
function RotaONG({ children }) {
  return isOng() ? children : <Navigate to="/entrar" replace />;
}
```
- Deixa passar apenas quem tem `tipo === 'ONG'` no JWT
- Outros usuários são redirecionados para `/entrar`

### GerenciarOportunidades

#### Operações realizadas
1. **Carregamento**:
   - `api('/ongs/me')` → obtém perfil da ONG logada (`minhaOng`)
   - `api('/oportunidades')` → lista todas oportunidades do backend
   - Filtro local: `todas.filter((op) => op.ongId === minhaOng.id)`
   - Resultado: apenas oportunidades da ONG logada são exibidas

2. **Excluir**:
   - Confirmação via `window.confirm`
   - API call: `DELETE /oportunidades/${op.id}`
   - Remove da lista local após sucesso

3. **Editar** (via link para FormOportunidade):
   - Link para `/painel/oportunidades/${op.id}/editar`
   - Passa o ID da oportunidade como parâmetro

4. **Ver inscritos**:
   - Link para `/painel/oportunidades/${op.id}/inscritos`
   - Chama tela VoluntariosInscritos (depende do backend)

#### Lacuna de segurança
- O frontend filtra oportunidades por `ongId` após buscar todas
- Não há validação no frontend para impedir que o usuário manipule URLs ou IDs
- O botão "Excluir" e link "Editar" estão visíveis para todas as oportunidades exibidas, mas a ação real depende da validação do backend (que atualmente não existe)

### FormOportunidade

#### Formulário para criar e editar

**Carregamento de dados:**
1. Busca categorias: `api('/categorias')`
2. Obtém ONG logada: `api('/ongs/me')` → define `setOngId(minhaOng.id)`
3. Se em modo edição (`editando = Boolean(id)`):
   - Busca oportunidade: `api('/oportunidades/${id}')`
   - Validação local: `if (op.ongId !== minhaOng.id)` → mostra erro se tentar editar oportunidade de OUTRA ONG
   - Se válido, preenche o formulário com os dados da oportunidade

#### Payload enviado ao backend

**Criar** (`POST /oportunidades`):
```json
{
  "ongId": 1,  // do estado, vem de api('/ongs/me')
  "categoriaId": 1,
  "titulo": "Título da oportunidade",
  "descricao": "Descrição...",
  "data": "2026-10-25",
  "horario": "14:30",
  "cidade": "Cidade",
  "estado": "RS",
  "vagas": 5,
  "status": "ABERTA"
}
```

**Editar** (`PUT /oportunidades/{id}`):
- Mesmo payload da criação, mas enviado com method PUT
- O `ongId` vem do estado (obtido no carregamento)

#### Validações no frontend
- Estado deve ter 2 letras (ex: RS)
- Título máximo 200 caracteres (input maxLength)
- Vagas mínimo 1
- Status deve ser preenchido
- **Validação de propriedade**: Se editando e `op.ongId !== minhaOng.id`, mostra erro e não envia o formulário

#### Lacuna
- A validação `op.ongId !== minhaOng.id` é apenas no frontend
- O backend (OportunidadeService.atualizar()) recebe o `ongId` do RequestDTO e apenas verifica se a ONG existe, não se pertence ao usuário logado

### PainelOng

#### Dados carregados
1. `api('/ongs/me/painel')` → indicadores do painel (totalOportunidades, oportunidadesAtivas, oportunidadesEncerradas, totalVoluntariosInscritos)
2. `api('/ongs/me')` → perfil da ONG logada (nome, id, etc.)
3. `api('/oportunidades')` → todas oportunidades (filtro local para últimas 3 da própria ONG)

#### Filtro aplicado
```javascript
const minhasOportunidades = todasOportunidades
  .filter((oportunidade) => oportunidade.ongId === minhaOng.id)
  .sort((primeira, segunda) => new Date(segunda.criadoEm) - new Date(primeira.criadoEm))
  .slice(0, 3);
```
- Apenas as 3 últimas oportunidades da ONG logada são exibidas no painel
- Ordenadas por `criadoEm` decrescente (mais recentes primeiro)

#### Indicadores exibidos
- Total de oportunidades
- Oportunidades ativas (status ABERTA)
- Oportunidades encerradas
- Voluntários inscritos (total da contagem no backend)

### Serviços HTTP e Autenticação

#### api.js (wrapper fetch)
- Adiciona token JWT automaticamente nos headers: `Authorization: Bearer ${token}`
- Trata erros 401/403: lança erro "Sessão expirada ou sem permissão"
- Retorna null se status for 204 (sem conteúdo)
- Lança erro genérico em caso de falha de conexão

#### auth.js
- `getToken()`: lê de `localStorage.getItem('token')`
- `getUsuario()`: decodifica JWT, retorna `{id, email, tipo}` ou null se expirado/sem token
- `isOng()`: retorna `true` se `getUsuario().tipo === 'ONG'`
- `isVoluntario()`: retorna `true` se `getUsuario().tipo === 'VOLUNTARIO'`

#### Fluxo de autenticação usado nas telas
1. Usuário faz login → token salvo em localStorage
2. Componentes verificam `isOng()` antes de renderizar área administrativa
3. Operações HTTP incluem o token nos headers via o wrapper `api()`
4. Dados do usuário (`ongId`) obtidos via `api('/ongs/me')`

---
## 4. Comparar os Contratos

### Endpoints Existente vs Expectativa

| Endpoint | Método | O que o Frontend Espera | O que o Backend Entrega | Observação |
|----------|--------|------------------------|-------------------------|------------|
| `GET /oportunidades` | GET | Lista oportunidades, frontend filtra por `ongId` | Lista TODAS sem filtro (SecurityConfig: permitAll) | **Gap**: backend não filtra por ONG |
| `GET /oportunidades/{id}` | GET | Buscar uma oportunidade específica | Busca por ID, retorna DTO | OK |
| `POST /oportunidades` | POST | Criar oportunidade com dados completos | Cria e retorna DTO de resposta | Frontend envia `ongId` do usuário logado |
| `PUT /oportunidades/{id}` | Editar | Atualizar oportunidade, validar propriedade | Atualiza dados, mas validação é apenas no frontend | **Gap**: backend não valida propriedade |
| `DELETE /oportunidades/{id}` | Excluir | Remover oportunidade | Remove se existir, mas sem validação de propriedade | **Gap**: backend não valida propriedade |

### Diferenças Críticas de Contrato

**1. Validação de Propriedade da ONG**
- **Frontend**: Valida `op.ongId !== minhaOng.id` antes de permitir edição
- **Backend**: Não existe validação - o `ongId` do RequestDTO é aceito se a ONG existir no banco
- **Risco**: Qualquer usuário pode tentar criar/editar passando `ongId` de outra ONG

**2. Filtragem de Listagem**
- **Frontend**: Busca todas e filtra localmente por `ongId === minhaOng.id`
- **Backend**: `GET /oportunidades` retorna tudo (permitAll em SecurityConfig)
- **Consistência**: O backend deveria ter método `findByOngId` ou similar

**3. Campo `ongId` nos DTOs**
- **RequestDTO**: `ongId` é um campo obrigatório vindo do frontend
- **ResponseDTO**: Devolve `ongId` e `nomeOng` para identificar a proprietária
- **Expectativa**: O backend deveria inferir o `ongId` do token do usuário autenticado, não aceitá-lo apenas do corpo da requisição

### Identificação de Dependências

#### Pessoa 2 (Autenticação)
- **Status**: Já implementado e funcional
- **O que fornece**: JWT token no localStorage, `getUsuario()`, `isOng()`, `isVoluntario()`
- **O que Person 3 precisa**: Garantir que o backend use essas informações para validar propriedade
- **Dependência**: As mudanças no `SecurityConfig` e `OportunidadeService` dependem do fluxo de autenticação da Pessoa 2

#### Pessoa 4 (Inscrições)
- **Status**: Frontend `VoluntariosInscritos.jsx` já desenvolvido
- **Backend**: Endpoint `/oportunidades/{id}/inscricoes` NÃO existe ainda
- **O que Person 3 precisa**: Garantir que o backend tenha endpoints seguros antes de Pessoa 4 expô-los
- **Dependência**: Person 3 deve finalizar a segurança do CRUD de oportunidades antes de expor endpoints de inscrições

#### Pessoa 5 (Não especificada na tarefa atual)
- Provavelmente envolve relatórios ou dados adicionais
- Dependeria das bases estabelecidas por Person 3

---
## 5. Resultado da Auditoria

### 1. Estado do Git e Segurança dos Arquivos
- Branch `develop` sincronizada com `origin/develop`
- Nenhum merge pendente
- Última modificação relevante: `GIT-RECOVERY-REPORT.md` (arquivo de relatório, não código)
- Todos os arquivos de código-fonte estão no estado original do repositório remoto

### 2. Funcionalidades Já Implementadas

**Backend (Operações CRUD):**
- ✅ Listar oportunidades (com filtros por cidade, categoria, status)
- ✅ Buscar oportunidade por ID
- ✅ Criar nova oportunidade (valida ONG e Categoria no banco)
- ✅ Atualizar oportunidade existente (atualiza campos, mas sem validação de propriedade)
- ✅ Excluir oportunidade (remove se existir)
- ✅ ONGRepository com `findByUsuarioId`
- ✅ SecurityConfig com roles ONG e VOLUNTARIO
- ✅ DTOs de request e response estruturados

**Frontend (Operações CRUD):**
- ✅ `GerenciarOportunidades` lista apenas oportunidades da ONG logada (filtro local)
- ✅ `FormOportunidade` cria e edita oportunidades
- ✅ Validação de estado (2 letras maiúsculas) e dados obrigatórios
- ✅ `PainelOng` exibe indicadores e últimas 3 oportunidades
- ✅ `RotaONG` protege rotas da área administrativa
- ✅ Autenticação JWT via `localStorage` e `api()` wrapper

### 3. Falhas Confirmadas pelo Código

1. **⚠️ CRÍTICO - Sem validação de propriedade no backend**
   - `OportunidadeService.criar()` aceita `ongId` qualquer que venha do RequestDTO
   - `OportunidadeService.atualizar()` mesmo problema
   - Qualquer um pode tentar criar/editar passando `ongId` de outra ONG

2. **⚠️ CRÍTICO - Endpoints públicos em SecurityConfig**
   - `GET /oportunidades/**` configurados como `permitAll`
   - Significa que qualquer pessoa pode listar/buscar oportunidades sem login
   - O frontend contorna com filtro local, mas a segurança deveria ser no backend

3. **⚠️ MEDIUM - Ausência de validação cruzada usuário ONG ↔ oportunidade**
   - Não há verificação de que o `usuarioId` do JWT corresponde ao `usuarioId` da ONG da oportunidade
   - O sistema confia no `ongId` enviado pelo frontend

4. **⚠️ MEDIUM - Sem testes unitários**
   - Não há testes para OportunidadeService, controller ou repository
   - Falta cobertura para validar regras de negócio

### 4. Arquivos que Provavelmente Precisarão Ser Alterados

**Backend (Prioridade Alta):**
1. `OportunidadeService.java` - Adicionar método `validarPropriedadeOng()` e chamá-lo em `criar()` e `atualizar()`
2. `OportunidadeController.java` - Injetar `@AuthenticationPrincipal Usuario usuario` e validar antes de delegar ao service
3. `SecurityConfig.java` - Potencialmente restringir `GET /oportunidades/` ou adicionar regras baseadas em roles

**Frontend (Priority Media - para UX consistente):**
1. `FormOportunidade.jsx` - Já tem validação local, pode ser reforçada mas não é bloqueante
2. `GerenciarOportunidades.jsx` - Filtro local já existente, mantê-lo

### 5. Plano de Implementação em Pequenas Etapas

**Etapa 1 - Segurança Imediata (1-2 dias):**
1. Modificar `OportunidadeService.java`:
   - Injetar `OngRepository` ou `UsuarioService` (se necessário)
   - Adicionar método `validarPropriedadeOng(Integer oportunidadeId, Integer usuarioLogadoId)`
   - Chamar esse método no `criar()` e `atualizar()`

2. Modificar `OportunidadeController.java`:
   - Adicionar parâmetro `@AuthenticationPrincipal Usuario usuario` nos métodos `criar`, `atualizar`, `excluir`
   - Chamar validação de propriedade antes de delegar ao service

**Etapa 2 - Segurança de Endpoint (1 dia):**
1. Revisar `SecurityConfig.java`:
   - Avaliar se `GET /oportunidades/` deve permanecer `permitAll` ou ser restringido
   - Se restringir, adicionar `@PreAuthorize` ou validação no controller

**Etapa 3 - Testes e Validação (2-3 dias):**
1. Criar `OportunidadeServiceTest.java` com testes para:
   - Criar oportunidade com ongId válido vs. inválido
   - Editar oportunidade da própria ONG vs. OUTRA ONG
   - Excluir oportunidade da própria ONG vs. OUTRA ONG
   - Filtros de cidade, categoria, status

**Etapa 4 - Documentação e Dependências (1 dia):**
1. Atualizar contratos/endpoints se houver mudança de URL ou parâmetros
2. Verificar compatibilidade com o frontend já desenvolvido

### 6. Dependências das Outras Pessoas

#### Pessoa 2 (Autenticação)
- **Fornece**: Sistema completo de JWT, tokens, verificação de papel (ONG/VOLUNTARIO)
- **Dependência de Person 3**: Person 3 deve usar `getUsuario().getId()` ou `getUsuario().getTipo()` nos serviços/backend para validar propriedade. As mudanças não quebram o fluxo atual, apenas adicionam validações que antes eram apenas no frontend.

#### Pessoa 4 (Inscrições)
- **Fornece**: Frontend `VoluntariosInscritos.jsx` pronto, chamando API de inscrições
- **Dependência de Person 3**: Person 3 deve finalizar a segurança do CRUD de oportunidades PRIMEIRO. O endpoint `/oportunidades/{id}/inscricoes` depende de quem pode acessar oportunidades. Se Person 3 não adicionar a validação de propriedade, Pessoa 4 não pode expor o endpoint de inscrições de forma segura.

#### Pessoa 5 (Não especificada)
- Provavelmente trabalha com relatórios ou dashboards adicionais
- Depende das bases de dados estáveis fornecidas por Person 3

### 7. Testes Necessários

**Testes unitários essenciais para validar que "uma ONG só administra suas próprias oportunidades":**

1. **testCriarOportunidadeComOngAjudaDeOutraDeveFalhar**
   - Cenário: Tentativa de criar oportunidade passando `ongId` de ONG diferente da logada
   - Esperado: `ResourceNotFoundException` ou validação que corrige para ongId da logada

2. **testEditarOportunidadePrópriaONGDeveSuceder**
   - Cenário: ONG A edita oportunidade que ela mesma criou
   - Esperado: Sucesso, oportunidade atualizada

3. **testEditarOportunidadeOutraONGDeveFalhar**
   - Cenário: ONG A tenta editar oportunidade de ONG B
   - Esperado: `AccessDeniedException` ou `ResourceNotFoundException`

4. **testExcluirOportunidadePrópriaONGDeveSuceder**
   - Cenário: ONG A exclui oportunidade que ela mesma criou
   - Esperado: Sucesso, oportunidade removida

5. **testExcluirOportunidadeOutraONGDeveFalhar**
   - Cenário: ONG A tenta excluir oportunidade de ONG B
   - Esperado: `AccessDeniedException` ou `ResourceNotFoundException`

6. **testListarOportunidadesSemFiltroTrazTodas**
   - Cenário: `GET /oportunidades` sem parâmetros
   - Esperado: Lista todas as oportunidades do sistema (comportamento público esperado)

7. **testListarOportunidadesComFiltroOngIdTrazApenasDaONG**
   - Cenário: Novo endpoint método `findByOngId` ou filtro similar
   - Esperado: Apenas oportunidades da ONG especificada

8. **testBuscarPorIdDeOutraONGDeveRetornar404**
   - Cenário: Buscar oportunidade ID=X que pertence à ONG diferente da logada
   - Esperado: `ResourceNotFoundException` (ou acesso negado, dependendo da estratégia)

---

## Conclusão da Etapa 1

A auditoria técnica confirma que o ConecteBem tem o **funcionamento CRUD de oportunidades implementado**, mas **falta a validação de segurança crítica no backend** para garantir que cada ONG só pode administrar suas próprias oportunidades.

O fluxo atual funciona graças às validações no frontend (React), mas isso não é suficiente para produção, pois um usuário mal-intencionado poderia usar ferramentas como Postman ou curl para chamar os endpoints diretamente, burlando as validações visuais.

As mudanças necessárias focam em:
1. Adicionar validação no `OportunidadeService` usando o ID do usuário autenticado
2. Potencialmente restringir endpoints no `SecurityConfig`
3. Criar testes unitários para validar as regras de propriedade

Essas mudanças são de responsabilidade da Pessoa 3 e devem ser concluídas antes que as tarefas de Pessoa 4 (inscrições) possam ser implementadas com segurança.