# kafka_project

A learning project: an order stream through Kafka with error handling via a Dead Letter Topic, persistence in Postgres, and logs in Loki/Grafana.

## Components

| Module | Purpose |
|---|---|
| `common` | Shared classes (`Order`, `OrderType`) |
| `order_producer` | Sends an order to the `orders` topic every 2.5 s (creates the topic on startup). Order ids are `ORD-<UUID>`, so they stay unique across restarts |
| `order_processor` | Reads `orders`, saves orders to Postgres (Liquibase), sends orders without `totalAmount` to `orders.DLT` |
| `dlq_handler` | Reads `orders.DLT` (consumer group `dlq-group`) |

Infrastructure from `docker-compose.yaml`: Postgres 17 (`shop`), Kafka in KRaft mode (`shop_kafka`), Loki (`loki_app`), Grafana.

## Requirements

- Docker with Docker Compose (enough to build and run everything)
- JDK 21 only if you run the applications from an IDE or build locally
- No need to install Gradle: the project uses the wrapper (`gradlew`, Gradle 9.8)

## Build and run in Docker

The applications are built inside Docker: the `Dockerfile` is multi-stage (a JDK 21 stage runs `bootJar`, then the jar is copied into a JRE 21 image). Neither a local JDK nor Gradle is needed for this.

1. Build the images and start all containers (from the project root):

   ```bash
   docker compose up -d --build
   ```

   The first build is slow because Gradle and the dependencies are downloaded. They are cached between builds, so later runs are faster.

2. Check the state and logs:

   ```bash
   docker compose ps
   docker logs -f order_processor
   ```

3. Stop (data volumes are kept):

   ```bash
   docker compose down
   ```

   To also delete the Postgres, Kafka, Loki and Grafana data, add `-v`.

After changing the code, just run `docker compose up -d --build` again. To rebuild and restart a single application:

```bash
docker compose up -d --build order_processor
```

A local build is only needed for running from an IDE or checking compilation: `./gradlew bootJar` (on Windows `.\gradlew.bat bootJar`); the result is in `<module>/build/libs/`.

## Addresses and ports

| Service | Address |
|---|---|
| Kafka (from the host) | `localhost:29092` |
| Kafka (inside the docker network) | `shop_kafka:9092` |
| Postgres (from the host) | `localhost:5434`, database, user and password `shop` |
| Loki | `http://localhost:3100` |
| Grafana | `http://localhost:3000` (login and password are in `docker-compose.yaml`) |

In Grafana, add a Loki data source with the URL `http://loki:3100` and filter logs by the `app` label (`order_producer`, `order_processor`, `dlq_handler`).

## Running the applications from an IDE

1. Start the infrastructure only:

   ```bash
   docker compose up -d shop shop_kafka loki_app grafana
   ```

2. Run the applications you need from the IDE. The defaults are set up for this case: Kafka at `localhost:29092`, Postgres at `localhost:5434`, Loki at `localhost:3100`.
3. The Postgres password is set with the `PG_PASS` environment variable (`shop`).

## Configuration via environment variables

| Variable | Default | Description |
|---|---|---|
| `KAFKA_BOOTSTRAP` | `localhost:29092` | Kafka address |
| `PG_HOST` / `PG_PORT` | `localhost` / `5434` | Postgres address (`order_processor`) |
| `PG_NAME` / `PG_USER` / `PG_PASS` | `shop` / `shop` / none | Database, user and password (`PG_PASS` is required) |
| `LOKI_URL` | `http://localhost:3100/loki/api/v1/push` | Loki address for logs |

In Docker these values are set by `docker-compose.yaml`.

## Error handling

- An order without `totalAmount` goes straight to `orders.DLT`, where `dlq_handler` reads it.
- Offsets are committed manually (`ack-mode: manual`) after an order is stored, so a restart does not re-read processed records. Orders are inserted with `ON CONFLICT DO NOTHING`, so a redelivered record is harmless.
- An order with `customerId=TEMP_ERROR` is retried 3 times at 1.5 s intervals. If the retries fail, the record is skipped and does not go to the DLT.

## Troubleshooting

- **Containers fail with `Connection to localhost:5434 refused`**: `order_processor` was started without `PG_HOST=shop` and `PG_PORT=5432`. They are already set in `docker-compose.yaml`.
- **The application cannot find Kafka when run from an IDE**: use `localhost:29092`, not `shop_kafka:9092`. That name exists only inside the docker network.
- **Docker runs an old version of the application**: run `docker compose up -d --build` (the build happens inside Docker, no local `bootJar` is needed).
- **The application fails on startup with `unsupported URI`**: the host name in `LOKI_URL` contains an underscore. Use the `loki` alias, not `loki_app`.
