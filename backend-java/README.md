# backend-java — API Spring Boot

API REST do InsightFlow CTI: recebe a planilha, valida, aciona o módulo
Python de tratamento, grava no PostgreSQL e serve os dados do dashboard.

## Rodando local

Requisito único: **JDK 21 ou superior**. O Maven não precisa estar instalado —
o wrapper (`mvnw`) baixa a versão correta na primeira execução.

```bash
# banco em memória (não precisa do Azure)
export AUTH_ADMIN_EMAIL="admin@exemplo.com"
export AUTH_ADMIN_PASSWORD="<sua senha com pelo menos 8 caracteres>"
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# apontando para o PostgreSQL no Azure
export DB_URL="jdbc:postgresql://<servidor>.postgres.database.azure.com:5432/<base>?sslmode=require"
export DB_USERNAME="..."
export DB_PASSWORD="..."
./mvnw spring-boot:run
```

No Windows (PowerShell/cmd), troque `./mvnw` por `mvnw.cmd`.

### Banco no Azure

O banco de produção é o **Azure Database for PostgreSQL (Flexible Server)**. A
API só precisa das três variáveis acima; nada no código depende do provedor.

- `sslmode=require` na URL: o Azure recusa conexão sem SSL.
- Em **Rede**, liberar no firewall o IP de quem acessa o banco (a máquina de
  desenvolvimento e o servidor onde a API estiver publicada).
- As tabelas são criadas pelo Hibernate na primeira execução
  (`ddl-auto=update`); `database/schema.sql` documenta o mesmo modelo.

O `AnaliseService` procura o interpretador Python testando `python3`, `python` e
`py` nessa ordem — não é preciso configurar nada no caso comum. Para apontar um
interpretador específico (um virtualenv, por exemplo), use `PYTHON_BIN`.

## Variáveis de ambiente

| Variável | Padrão | Para que serve |
| -------- | ------ | -------------- |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | PostgreSQL local | conexão com o PostgreSQL no Azure |
| `CORS_ORIGINS` | `http://localhost:5173,http://localhost:4173` | domínios liberados (Vercel em produção) |
| `PYTHON_BIN` | detectado | forca um interpretador Python especifico |
| `ANALYTICS_DIR` | `../analytics-python` | pasta do módulo de Ciência de Dados |
| `UPLOAD_DIR` | `uploads` | onde as planilhas recebidas são gravadas |
| `PORT` | `8080` | porta HTTP (o Render define automaticamente) |
| `AUTH_ADMIN_EMAIL` | vazio | e-mail da conta administrativa, criada ao iniciar a API; vazio = ninguém aprova contas novas |
| `AUTH_ADMIN_PASSWORD` | vazio | senha administrativa de pelo menos 8 caracteres |

## Endpoints

| Método | Rota | Resposta |
| ------ | ---- | -------- |
| `GET` | `/api/health` | 200 — verificação pública de disponibilidade HTTP |
| `POST` | `/api/auth/cadastro` | 201 — pedido de conta, nasce `PENDENTE` / 409 se o e-mail já existe |
| `POST` | `/api/auth/login` | 200 — token, e-mail, nome, perfil e validade / 401 / 403 se pendente ou bloqueada |
| `GET` | `/api/auth/me` | 200 — e-mail da sessão / 401 |
| `POST` | `/api/auth/logout` | 204 — revoga o token / 401 |
| `POST` | `/api/planilhas` | 200 — resumo do processamento e primeiros insights |
| `GET` | `/api/clientes?segmento=` | 200 — lista da carteira |
| `GET` | `/api/clientes/{id}` | 200 / 404 |
| `POST` | `/api/clientes` | 201 + `Location` / 409 se código repetido |
| `PUT` | `/api/clientes/{id}` | 200 — substitui os dados do cliente |
| `PATCH` | `/api/clientes/{id}/nivel` | 200 — reclassifica A/B/C |
| `DELETE` | `/api/clientes/{id}` | 204 |
| `GET` | `/api/indicadores` | 200 — números e séries do dashboard |
| `GET` | `/api/insights` | 200 — insights com o texto de `gerarResumo()` |
| `GET` | `/api/telemetria` | 200 — últimos 20 eventos de processamento |
| `GET` | `/api/usuarios` | 200 — todas as contas, mais recentes primeiro (só `ADMIN`) |
| `PATCH` | `/api/usuarios/{id}/status` | 200 — `ATIVO` aprova/desbloqueia, `BLOQUEADO` recusa/bloqueia (só `ADMIN`) |

## Exceções tratadas

Exceto saúde, cadastro e login, todas as rotas exigem `Authorization: Bearer <token>`.
O login recebe `{ "email": "...", "password": "...", "remember": false }`.
Sessões duram 8 horas ou 7 dias com `remember: true`, e desaparecem ao reiniciar
a API. O navegador usa sessionStorage ou localStorage conforme essa opção.

Contas: qualquer pessoa pede uma conta em `/api/auth/cadastro`, mas ela nasce
`CONSULTOR` + `PENDENTE` e o login responde 403 até o administrador aprovar.
O consultor aprovado acessa tudo, menos `/api/usuarios`. Bloquear uma conta
derruba as sessões abertas dela; o admin pode desbloquear depois. A conta do
admin não pode ser bloqueada. Não inclui SSO nem recuperação de senha. Use
HTTPS na publicação.

Veja [EXECUCAO_E_ARQUITETURA.md](EXECUCAO_E_ARQUITETURA.md) para os comandos em
PowerShell, exemplos de CRUD e explicação passo a passo.

Todas passam pelo `GlobalExceptionHandler` e chegam ao Front-end com mensagem
legível, nunca como stack trace:

| Exceção | HTTP | Quando acontece |
| ------- | ---- | --------------- |
| `ExcelInvalidoException` | 400 | arquivo fora do formato, vazio ou ilegível |
| `ColunaObrigatoriaException` | 422 | falta coluna esperada (a lista vai em `detalhes`) |
| `ClienteDuplicadoException` | 409 | código CTI já cadastrado |
| `RecursoNaoEncontradoException` | 404 | id inexistente |
| `AnaliseException` | 503 | o script Python falhou ou estourou o tempo |

## Estrutura

```
src/main/java/br/senai/ctiinsights/
├── config/       CorsConfig, AnalyticsProperties
├── controller/   ClienteController, UploadController, DashboardController
├── domain/       Cliente, Consultor, Servico, Contrato, Insight (+3 subtipos), Telemetria
├── dto/          contratos de entrada e saída da API
├── exception/    exceções próprias + @RestControllerAdvice
├── repository/   Spring Data JPA
└── service/      ClienteService, PlanilhaService, AnaliseService, ImportacaoService,
                  DashboardService, TelemetriaService
```

## Conceitos de POO no modelo

| Conceito | Onde aparece |
| -------- | ------------ |
| Abstração | `Cliente.calcularFaixaFaturamento()` esconde os cortes de faixa |
| Encapsulamento | atributos privados com getters/setters em todas as entidades |
| Herança | `InsightSegmento`, `InsightFaturamento` e `InsightServico` estendem `Insight` |
| Polimorfismo | `gerarResumo()` é reescrito em cada subtipo de `Insight` |
| Relacionamentos | Consultor 1–N Cliente; Cliente 1–N Contrato; Contrato N–1 Servico; Cliente 1–N Insight |
