# orders-service

Microserviço Spring Boot 3 (Java 17, Maven) da demo `order-events`: recebe pedidos por REST (`POST /api/orders`), grava no MongoDB e publica `OrderCreatedEvent` no Kafka. Porta **8081**.

## Ambiente

- **Runtime**: JDK 17 (`java.version` no `pom.xml`); a imagem Docker compila com Maven 3.9 + Temurin **21**. `source "$PROJECTS_ROOT/workspace/scripts/env.sh"` para o JDK na sessão.
- **Variáveis** (`.env`, ignorado pelo git; chaves esperadas): `SPRING_DATA_MONGODB_URI`, `SPRING_KAFKA_BOOTSTRAP_SERVERS`, `SSL_STORE_PASSWORD`, `KAFKA_CA_PEM`, `KAFKA_CERT_PEM`, `KAFKA_KEY_PEM` (Kafka com TLS). Valores: fora do git.
- **Comandos**: `mvn spring-boot:run` · `mvn test` (sem Kafka/Mongo reais) · `docker build -t orders-service .`.
- O `docker-compose.yml` que orquestra os 3 serviços fica em `../` (pasta-pai, **fora do git**), junto dos certificados TLS.

## Regras

- Sem caminhos absolutos de máquina em arquivos versionados; regras comuns em `$PROJECTS_ROOT/CLAUDE.md`.
- Segredos e `.env` **não vêm no `git clone`** — ver `workspace/docs/segredos-e-arquivos-fora-do-git.md`.
- Commit/push só quando o usuário pedir.
