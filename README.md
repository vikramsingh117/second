# GitHub Repository Searcher

Spring Boot application that searches GitHub repositories and saves them to PostgreSQL database. Search by keywords/language, auto-saves results, provides filtering and sorting.

## Technology Stack

- **Java 17** + **Spring Boot 3.2.1**
- **PostgreSQL** for data storage
- **Maven** for build management
- **Docker** for database setup
- **Lombok** for reducing boilerplate code
- **H2** for testing

## Quick Setup

### 1. Database Setup
```bash
# Start PostgreSQL with Docker
docker run --name github-repos-postgres \
  -e POSTGRES_DB=github_repos \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:15-alpine
```

### 2. Run Application
```bash
mvn clean compile
mvn spring-boot:run
```

Application starts on `http://localhost:8080`

## API Endpoints

### Search & Save Repositories
`POST /api/github/search`
- Searches GitHub repositories
- Saves results to database
- Supports query, language, sort parameters

### Get Saved Repositories  
`GET /api/github/repositories`
- Returns saved repositories
- Optional filters: language, minStars, sort

## API Examples

**See `api-examples.http` for complete examples with curl commands and request samples.**

Quick test:
```bash
curl http://localhost:8080/api/github/repositories
```

## Database

PostgreSQL stores repository data with fields:
- Repository ID, name, description, owner
- Programming language, stars, forks
- Last updated, created/updated timestamps

Connect to database:
```bash
docker exec -it github-repos-postgres psql -U postgres -d github_repos
```

## Docker Setup

### Using Docker Compose
```bash
docker-compose up -d    # Start everything
docker-compose down     # Stop everything
```

### Manual Setup
Use the docker run command from Quick Setup section.

## Configuration

Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/github_repos
    username: postgres
    password: postgres

github:
  api:
    base-url: https://api.github.com
    timeout: 30000
```

## Developer Notes

**Lombok**: Reduces boilerplate code. Uses @Data, @NoArgsConstructor for auto-generating getters/setters/constructors. Install IDE plugin if compilation errors.

**H2 Testing**: Tests use in-memory H2 instead of PostgreSQL. Auto-configured in test profile.

**Commands**:
```bash
mvn test              # Run tests with H2
mvn clean compile     # Check compilation
mvn spring-boot:run   # Start with PostgreSQL
```

## Common Issues

- **Connection refused**: Check if PostgreSQL is running with `docker ps`
- **Port conflicts**: Verify ports 5432 and 8080 are available
- **GitHub rate limits**: 60 requests/hour for unauthenticated requests

## Export Data

```bash
# Export to CSV
docker exec -it github-repos-postgres psql -U postgres -d github_repos -c "
COPY (SELECT name, owner_name, programming_language, stars_count, forks_count 
      FROM repositories ORDER BY stars_count DESC) 
TO STDOUT WITH CSV HEADER" > repositories.csv
```

## Next Steps

1. Check `api-examples.http` for detailed API usage
2. Search for repositories by different topics/languages  
3. Use database queries to analyze collected data
4. Export data for external analysis 