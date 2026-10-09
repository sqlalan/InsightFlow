# InsightFlow — execução e arquitetura do backend

## 1. O que o backend faz

O Spring Boot recebe as chamadas do Vue, autentica o administrador, cadastra
clientes e processa planilhas. O Python trata os dados e produz análises; o Java
persiste clientes, contratos, insights e eventos. O dashboard consulta o banco.

Stack: Java 21, Spring Boot 3, Spring Security, Jakarta Validation, Spring Data
JPA, Apache POI, PostgreSQL e H2 para desenvolvimento.

## 2. Preparar o Python

Abra o PowerShell na raiz `InsightFlow`:

```powershell
cd analytics-python
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
.\.venv\Scripts\python.exe -m unittest discover tests
cd ..
```

O ambiente virtual fica no projeto. Não é necessário ativá-lo nem mudar a
política de execução do PowerShell.

## 3. Iniciar a API

Na raiz `InsightFlow`, execute:

```powershell
$env:PYTHON_BIN = (Resolve-Path '.\analytics-python\.venv\Scripts\python.exe').Path
$env:AUTH_ADMIN_EMAIL = 'admin@exemplo.com'
$credencial = Get-Credential -UserName $env:AUTH_ADMIN_EMAIL -Message 'Defina a senha local (mínimo 8 caracteres)'
$env:AUTH_ADMIN_PASSWORD = $credencial.GetNetworkCredential().Password
cd backend-java
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=local'
```

Saída esperada: `Started CtiInsightsApplication`; API em `http://localhost:8080`.
O perfil local usa H2 em memória: clientes e contratos são apagados ao parar.
Para persistência, configure `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` e execute
sem o perfil local. O console H2 não foi liberado pela configuração de segurança.

Se já tem Maven instalado, pode usar `mvn` no lugar de `.\mvnw.cmd`. Em ambientes
que só permitem escrita no projeto, use:

```powershell
$env:MAVEN_USER_HOME = Join-Path (Get-Location) '.maven-cache'
.\mvnw.cmd '-Dmaven.repo.local=.maven-cache/repository' spring-boot:run '-Dspring-boot.run.profiles=local'
```

## 4. Iniciar o Vue

Em outro terminal, na raiz `InsightFlow`:

```powershell
cd frontend-vue
npm.cmd install
npm.cmd run dev
```

Abra o endereço mostrado pelo Vite, normalmente `http://localhost:5173`.
Entre com o e-mail e a senha definidos no terminal da API. Envie a planilha
`analytics-python/exemplos/CTI_Insights_modelo_upload_aula.xlsx` e consulte
dashboard e relatórios. `VITE_API_URL` permite mudar o endereço da API.

## 5. Testar autenticação e CRUD no PowerShell

Com a API iniciada, abra outro terminal:

```powershell
$base = 'http://localhost:8080'
Invoke-RestMethod "$base/api/health"
$credencial = Get-Credential -UserName 'admin@exemplo.com' -Message 'Informe a senha configurada na API'
$login = @{ email = $credencial.UserName; password = $credencial.GetNetworkCredential().Password; remember = $false } | ConvertTo-Json
$sessao = Invoke-RestMethod "$base/api/auth/login" -Method Post -ContentType 'application/json' -Body $login
$headers = @{ Authorization = "Bearer $($sessao.token)" }
$corpo = @{ codigoCti = 'CTI-001'; segmento = 'Industria'; nivel = 'A'; faturamentoAnual = 120000.50; consultor = 'Raphael' } | ConvertTo-Json
$cliente = Invoke-RestMethod "$base/api/clientes" -Method Post -Headers $headers -ContentType 'application/json' -Body $corpo
Invoke-RestMethod "$base/api/clientes/$($cliente.id)" -Headers $headers
Invoke-RestMethod "$base/api/clientes/$($cliente.id)" -Method Put -Headers $headers -ContentType 'application/json' -Body $corpo
Invoke-RestMethod "$base/api/clientes/$($cliente.id)/nivel" -Method Patch -Headers $headers -ContentType 'application/json' -Body '{"nivel":"B"}'
Invoke-RestMethod "$base/api/indicadores" -Headers $headers
Invoke-RestMethod "$base/api/clientes/$($cliente.id)" -Method Delete -Headers $headers
Invoke-RestMethod "$base/api/auth/logout" -Method Post -Headers $headers
```

Resultados esperados: saúde `UP`; login retorna token, e-mail e `expiresAt`;
cadastro retorna um ID e código `CTI-001`; PATCH retorna nível `B`; DELETE e
logout retornam 204 sem corpo. Repetir o cadastro retorna 409. Consultar a API
sem token ou depois do logout retorna 401. Nível inválido retorna 400.

## 6. Entender as camadas, passo a passo

1. `Cliente.java` é a entidade persistida. Cada objeto representa um cliente;
   o ID é gerado pelo banco e o código CTI é único. Consultor, contratos e
   insights são relacionamentos JPA.
2. `ClienteRequest` define o JSON de entrada e valida tamanho, nível e valor
   monetário. `NivelRequest` exige um nível válido no PATCH. `ClienteResponse`
   define a saída e evita expor relacionamentos internos do JPA.
3. `ClienteRepository` executa consultas Spring Data. O service usa essas
   consultas para localizar clientes e detectar códigos duplicados.
4. `ClienteService` concentra regras e transações. Remove espaços do código
   antes da consulta de duplicidade e grava alterações no banco.
5. `ClienteController` converte verbos HTTP em operações: GET consulta,
   POST cria, PUT substitui, PATCH reclassifica e DELETE exclui.
6. `GlobalExceptionHandler` traduz falhas em JSON com status, código, mensagem
   e detalhes que o Vue consegue apresentar.
7. `AuthService` verifica a conta administrativa usando BCrypt e gera tokens
   aleatórios. `SecurityConfig` valida o Bearer em cada requisição.
8. `UploadController` recebe multipart no campo `arquivo`. `PlanilhaService`
   valida o Excel; `AnaliseService` executa Python com limite de tempo e pasta
   exclusiva por execução; `ImportacaoService` persiste os resultados.
9. `DashboardService` calcula indicadores do banco. `TelemetriaService` grava
   eventos em transação separada para manter os erros mesmo após rollback.
10. No Vue, `api.js` envia o token e trata erros; `session.js` guarda a sessão;
    o router exige login antes de abrir as telas do painel.

## 7. Rodar os testes

Dentro de `backend-java`:

```powershell
.\mvnw.cmd test
```

Os testes cobrem validação de Excel, faixas de faturamento, login, revogação,
proteção das rotas, CRUD, duplicidade, JSON inválido e CORS com PATCH.
O teste com o Python real é opcional e precisa das dependências instaladas:

```powershell
$env:INSIGHTFLOW_PYTHON_TEST = (Resolve-Path '..\analytics-python\.venv\Scripts\python.exe').Path
.\mvnw.cmd test
```

Esse teste importa a planilha modelo, consulta indicadores e reimporta para
confirmar que não duplica clientes. O teste usa banco e pastas de teste.
Também são verificados o encerramento de um processo lento e o isolamento das
saídas de duas execuções Python.

Dentro de `frontend-vue`, `npm.cmd run build` valida o build de produção.

## 8. Erros comuns e soluções

| Erro | Solução |
| --- | --- |
| API não conecta ao PostgreSQL | Use o perfil `local` ou configure as três variáveis `DB_*`. |
| Login retorna 401 | Configure `AUTH_ADMIN_EMAIL` e `AUTH_ADMIN_PASSWORD` antes de iniciar a API. |
| Sessão deixou de funcionar | Entre novamente; reiniciar a API invalida os tokens. |
| Python não encontra pandas | Instale requirements no virtualenv e configure `PYTHON_BIN` com seu executável. |
| PowerShell bloqueia npm.ps1 | Use `npm.cmd`. |
| Frontend bloqueado por CORS | Configure `CORS_ORIGINS` com a origem completa do Vue. |
| Código já cadastrado | Use outro código ou altere o existente via PUT. Espaços não evitam a duplicidade. |
| Planilha incompleta | Use os cabeçalhos do modelo; a resposta 422 lista as colunas faltantes. |
| Upload excede o tempo | Verifique Python e volume de dados; a API interrompe a execução e retorna 503. |

## 9. Limites e publicação

A implementação oferece uma conta administrativa. Não inclui cadastro de
usuários, SSO, recuperação de senha ou segundo fator. Os tokens ficam em memória:
uma implantação com várias instâncias precisa de um armazenamento compartilhado.
O navegador guarda o token em sessionStorage, ou localStorage com “manter conectado”.
As pastas `uploads` e `output/execucao-*` preservam arquivos para diagnóstico;
defina uma política de remoção conforme a necessidade da empresa.

O `render.yaml` usa `/api/health`, público e sem dados comerciais. Ele informa
disponibilidade HTTP, sem testar o Python ou a conexão ao banco em cada chamada.
Configure credenciais, CORS, banco e HTTPS no ambiente de publicação.

## 10. Checklist de entrega

- [ ] API inicia e responde em `/api/health`.
- [ ] Login correto funciona e senha errada retorna 401.
- [ ] Rotas de negócio exigem autenticação.
- [ ] CRUD e reclassificação funcionam.
- [ ] Upload da planilha modelo gera clientes e insights.
- [ ] Dashboard e histórico exibem resultados.
- [ ] Testes Java e Python passam; Vue compila.
- [ ] Credenciais e planilhas reais não foram incluídas no Git.
- [ ] Limites da autenticação e do perfil local estão registrados na entrega.
