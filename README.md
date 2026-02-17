# Photopia Backend

Backend API for the Photopia social network.

## Technology Stack
- **Framework**: Spring Boot 3.5
- **Database**: PostgreSQL
- **Cache**: Redis (Upstash)
- **Storage**: Cloudflare R2 / AWS S3
- **Authentication**: JWT & Firebase

## Local Development

### Prerequisites
- Docker & Docker Compose
- Java 17+

### Running the App
1. Start the infrastructure (Postgres + Redis):
   ```bash
   docker-compose up -d
   ```
2. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## Deployment (Render & Upstash)

This project is configured for deployment on [Render](https://render.com) with Redis on [Upstash](https://upstash.com).

### 1. Redis Setup (Upstash)
1. Creates a database on Upstash (Serverless Redis).
2. Get the "Java (Spring)" connection details or simply the host/port/password.
3. You will need:
   - Endpoint (Host)
   - Port (usually 6379)
   - Password
   - **Enable SSL**: Upstash requires SSL, so set `REDIS_SSL_ENABLED=true`.

### 2. Render Deployment
Environment Variables to configure in Render:

- `DB_URL`: JDBC URL for PostgreSQL.
- `DB_USER_USERNAME` / `DB_USER_PASSWORD`
- `REDIS_HOST`: Your Upstash endpoint (e.g., `us1-choice-tuna-32456.upstash.io`)
- `REDIS_PORT`: `6379`
- `REDIS_PASSWORD`: Your Upstash password
- `REDIS_SSL_ENABLED`: `true`
- `R2_...`: Object storage config.
- `JWT_SECRET`: Security token.
