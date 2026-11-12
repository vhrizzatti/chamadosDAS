# Sistema de Chamados - API REST

API em camadas (Controller → Service → Repository → Entity) com Java 21, Spring Boot, Spring Data JPA, H2 e Bean Validation.

## Como executar

```
./mvnw spring-boot:run      # macOS / Linux
.\mvnw spring-boot:run      # Windows PowerShell
./mvnw test                 # testes automatizados
```

- API: http://localhost:8080
- Console H2: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:chamadosdb`, usuário `sa`, senha em branco)
- O banco é em memória: os dados somem ao reiniciar.

## Rotas

| Recurso | Rotas | Sucesso |
|---|---|---|
| Clientes | `POST/GET /clientes`, `GET/PUT/DELETE /clientes/{id}`, `GET /clientes/{id}/chamados` | 201 / 200 / 204 |
| Categorias | `POST/GET /categorias`, `GET/PUT/DELETE /categorias/{id}` | 201 / 200 / 204 |
| Técnicos | `POST/GET /tecnicos`, `GET/PUT/DELETE /tecnicos/{id}` | 201 / 200 / 204 |
| Chamados | `POST/GET /chamados`, `GET/PUT/DELETE /chamados/{id}` | 201 / 200 / 204 |
| Ações | `PATCH /chamados/{id}/atribuir`, `PATCH /chamados/{id}/status` | 200 |
| Comentários | `POST/GET /chamados/{id}/comentarios` | 201 / 200 |

Erros: `400` validação/JSON inválido, `404` recurso inexistente, `409` regra de negócio violada.

## Regras de negócio

- Chamado nasce `ABERTO` e `dataAbertura` é preenchida automaticamente.
- Transições de status: `ABERTO → EM_ATENDIMENTO | FECHADO`; `EM_ATENDIMENTO → AGUARDANDO_CLIENTE | FECHADO`; `AGUARDANDO_CLIENTE → EM_ATENDIMENTO | FECHADO`; `FECHADO` é final.
- Ao fechar, `dataFechamento` é preenchida.
- Chamado fechado não pode ser alterado nem receber técnico (409).
- E-mail único (clientes e técnicos) e nome único (categorias).
- Cliente, categoria ou técnico com chamados não podem ser excluídos (409).

## Exemplos de consulta (filtros e paginação)

```
curl -i "http://localhost:8080/chamados?status=ABERTO"
curl -i "http://localhost:8080/chamados?status=ABERTO&prioridade=ALTA&clienteId=1"
curl -i "http://localhost:8080/chamados?page=0&size=5&sort=dataAbertura,desc"
curl -i "http://localhost:8080/clientes/1/chamados"
```

## Exemplos de escrita

```
curl -i -X POST http://localhost:8080/clientes -H "Content-Type: application/json" \
  -d '{"nome":"Ana Souza","email":"ana@exemplo.com","telefone":"47999999999"}'

curl -i -X POST http://localhost:8080/categorias -H "Content-Type: application/json" \
  -d '{"nome":"Hardware","descricao":"Problemas físicos"}'

curl -i -X POST http://localhost:8080/tecnicos -H "Content-Type: application/json" \
  -d '{"nome":"Carlos","email":"carlos@empresa.com","perfil":"TECNICO"}'

curl -i -X POST http://localhost:8080/chamados -H "Content-Type: application/json" \
  -d '{"titulo":"Impressora parada","descricao":"Não imprime","prioridade":"ALTA","clienteId":1,"categoriaId":1}'

curl -i -X PATCH http://localhost:8080/chamados/1/atribuir -H "Content-Type: application/json" \
  -d '{"tecnicoId":1}'

curl -i -X PATCH http://localhost:8080/chamados/1/status -H "Content-Type: application/json" \
  -d '{"status":"EM_ATENDIMENTO"}'

curl -i -X POST http://localhost:8080/chamados/1/comentarios -H "Content-Type: application/json" \
  -d '{"texto":"Estou verificando o cabo.","autor":"Carlos"}'
```
