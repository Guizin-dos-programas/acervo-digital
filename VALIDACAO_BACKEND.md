# Validação do backend — Acervo Digital

## Escopo implementado

- **Acervo:** entidades de Livro, Autor e Categoria; DTOs; repositórios; serviços; controllers; criação, consulta, atualização e exclusão de livros; consulta avançada por título, autor e ISBN; controle de estoque total e disponível.
- **Empréstimos:** associação ao usuário autenticado; baixa de estoque; prazos por perfil (ALUNO=7, PROFESSOR=14 dias); bloqueio de usuário inativo/bloqueado, empréstimos atrasados e multas não quitadas.
- **Devolução:** devolução do estoque, registro da data, cálculo de multa por atraso (padrão provisório de R$ 2,00/dia) e impedimento de dupla devolução.
- **Renovação:** somente dono do empréstimo, antes do vencimento, até 2 renovações por padrão (configurável).
- **Histórico:** empréstimos do usuário autenticado e consulta administrativa por ID.
- **Segurança:** consulta pública do acervo; alterações administrativas exigem perfil BIBLIOTECARIO; cadastro público não permite autoatribuir BIBLIOTECARIO; autenticação HTTP Basic.
- **Banco:** Flyway V1 e V2 existentes; PostgreSQL 18.6 conectado no perfil local; busca pública em PostgreSQL confirmada após correção de parâmetros opcionais do LivroRepository.

## Evidências de testes

Comando executado na pasta raiz:

```powershell
mvn test
```

**Resultado da última execução em 21/10/2026: 17 testes, 0 falhas, 0 erros, BUILD SUCCESS.**

Composição:
- 9 testes de integração (MockMvc, Spring Security, Hibernate/JPA, H2 isolado);
- 4 testes unitários LivroService;
- 3 testes unitários EmprestimoService;
- 1 teste de inicialização do contexto Spring.

Cenários integrados: CRUD e busca pública de livros, regras de bibliotecário, cadastro de autor/categoria, rejeição de cadastro de administrador público, empréstimo aluno/professor, devolução e estoque, histórico próprio, segurança do histórico de terceiros, multa, quitação, renovação, estoque zerado, ISBN duplicado, conta bloqueada, proteção de histórico de empréstimo.

**Observação:** os testes integrados usam H2 em memória em uma transação revertida ao fim de cada teste, sem inserir dados de exemplo no PostgreSQL. São cenários automatizados do backend, não uma homologação de concorrência ou carga em produção.

## Endpoints principais

| Método | Rota | Acesso |
|---|---|---|
| GET | /api/livros | Público |
| GET | /api/livros/{id} | Público |
| POST | /api/livros | Bibliotecário |
| PUT | /api/livros/{id} | Bibliotecário |
| DELETE | /api/livros/{id} | Bibliotecário |
| GET | /api/autores | Público |
| POST | /api/autores | Bibliotecário |
| GET | /api/categorias | Público |
| POST | /api/categorias | Bibliotecário |
| POST | /api/usuarios | Público (somente aluno/professor) |
| POST | /api/emprestimos/livros/{livroId} | Autenticado; elegibilidade validada |
| POST | /api/emprestimos/{id}/devolver | Bibliotecário |
| POST | /api/emprestimos/{id}/renovar | Autenticado; somente dono |
| POST | /api/emprestimos/{id}/quitar-multa | Bibliotecário |
| GET | /api/emprestimos/me | Autenticado |
| GET | /api/emprestimos/usuarios/{usuarioId} | Bibliotecário |

## Execução local

Configurar no PowerShell com as credenciais corretas da base de desenvolvimento:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/biblioteca_db"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$senha = Read-Host "Senha PostgreSQL" -AsSecureString
$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($senha)
try { $env:SPRING_DATASOURCE_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
$env:SERVER_PORT = "8082"
mvn spring-boot:run '-Dspring-boot.run.profiles=local'
```

```powershell
Invoke-WebRequest http://localhost:8082/api/livros -UseBasicParsing
```

## Validação em PostgreSQL local já realizada

- Spring Boot iniciou na porta 8082.
- A aplicação conectou ao PostgreSQL 18.6 e validou duas migrações Flyway.
- GET /api/livros, /api/autores e /api/categorias: HTTP 200.
- GET /api/emprestimos/me sem autenticação: HTTP 401 (esperado).

## Pendências antes da entrega/homologação final

1. Executar também o fluxo de escrita em uma **base PostgreSQL exclusiva de homologação** com credenciais administrativas de teste, preservando a base atual; os cenários de escrita já passaram no H2.
2. Aprovar oficialmente multa diária e limite de renovações (valores atuais são provisórios).
3. Revisar o alinhamento das migrações Flyway com o modelo JPA. O perfil local ainda usa `ddl-auto=update`; antes de produção, migrar o esquema pelo Flyway e usar `validate`.
4. Corrigir o aviso de serialização da paginação Spring Data para obter um contrato JSON estável, se houver frontend consumindo a API.
5. Se houve alguma senha real anteriormente versionada, **revogá-la/trocá-la**; a configuração local agora usa variáveis de ambiente, mas arquivos antigos podem permanecer no histórico Git.
6. Revisar com a equipe a documentação de requisitos, disponibilidade de contas administrativas e checklist da entrega.

As alterações estão na branch local `fix/compilacao-lombok`; nenhum commit ou push foi realizado.
