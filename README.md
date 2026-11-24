# Monitoramento de Máquinas — CloudTech Solutions

API REST em Java 21 e Spring Boot para monitorar máquinas industriais: cadastro, consulta, atualização e exclusão de máquinas, e registro do histórico de produção e consumo de energia (kW).

## Estrutura

```
src/main/java/br/com/cloudtech/
├── Application.java
├── models/        Maquinas, CadastroMaquina, DadosProducao, Producao
└── controller/    MaquinaControllers
src/test/java/...  ApplicationTest (JUnit 5 + MockMvc)
tests/             requisições para o Thunder Client
.github/workflows/pipeline.yml   CI com GitHub Actions
Dockerfile
```

## Executar

Requisitos: JDK 21 e Maven 3.9.

```
mvn clean verify
mvn spring-boot:run
```

Acesse http://localhost:8080. A porta pode ser trocada com a variável `PORT` (é assim que o Railway define a porta). Os dados ficam em memória: ao reiniciar, são apagados. Não há autenticação.

## Endpoints

| Método | Caminho | Descrição | HTTP |
| --- | --- | --- | --- |
| GET | `/` | Página inicial | 200 |
| GET | `/status` | Status da aplicação | 200 |
| GET | `/versao` | Versão da aplicação | 200 |
| GET | `/maquinas` | Lista as máquinas | 200 |
| POST | `/maquinas/cadastro` | Cadastra uma máquina | 201 |
| PUT | `/maquinas/{id}` | Atualiza uma máquina | 200 / 404 |
| DELETE | `/maquinas/{id}` | Exclui uma máquina | 204 / 404 |
| POST | `/producao` | Registra produção e consumo | 201 / 404 |
| GET | `/producao` | Lista o histórico (filtro opcional `?maquinaId=1`) | 200 / 404 |
| PUT | `/producao/{id}` | Atualiza um registro de produção | 200 / 404 |
| DELETE | `/producao/{id}` | Exclui um registro de produção | 204 / 404 |

Dados inválidos (campos vazios, consumo negativo, horário fora do formato `HH:mm`) retornam 400. IDs inexistentes retornam 404.

### Exemplos de corpo

Máquina:

```json
{ "nome": "Máquina CNC 01", "tipo": "CNC", "setor": "Usinagem", "status": "em operação" }
```

Produção:

```json
{ "maquinaId": 1, "consumoKw": 12.5, "horarioAcionamento": "08:30", "pecasProduzidas": 150 }
```

## Testes

- Automatizados: `mvn test` (JUnit 5 + MockMvc), cobrindo todos os endpoints e os códigos HTTP.
- Manuais: as requisições estão em `tests/requisicoes.http`, na ordem de execução, para reproduzir no Thunder Client.

## CI/CD

O workflow `.github/workflows/pipeline.yml` roda a cada push e pull request: baixa o código, configura o Java 21, executa `mvn clean verify` (compila, testa e gera o `.jar`) e guarda o jar como artefato.

## Docker

```
docker build -t cloudtech-monitoramento .
docker run --rm -p 8080:8080 cloudtech-monitoramento
```
