# SchoolApplicationSpring

📘 **SchoolApplicationSpring** is a console-based Spring Boot application for managing academic groups, students, and courses. It is built using `Spring`, `JDBC`, `HikariCP` for connection pooling, `PostgreSQL` as the database, and `Flyway` for schema migrations.

## 📦 Features

The application allows you to:

- 🔍 Find all groups with a student count less than or equal to a given number
- 📚 List all students enrolled in a course by its name
- ➕ Add a new student
- ❌ Delete a student by ID
- 🔗 Assign a student to a course
- 🔗 Remove a student from a course

## 🐳 Dockerized Deployment

The application requires PostgreSQL and can be run in two ways:

1. Run the published image from Docker Hub without cloning the repository.
2. Build the application locally from the source code.

### Option 1: Run from Docker Hub

This option is intended for quickly trying the released application. The source repository is not required.

The commands below are intended for Bash or WSL.

#### 1. Pull the released application image

```bash
IMAGE=yuriikorolkov/school-application-on-spring:1.0.0

docker pull "$IMAGE"
```

The immutable version tag `1.0.0` is recommended for reproducible runs. The `latest` tag points to the most recently published release.

#### 2. Create a Docker network

```bash
docker network create school-app-demo
```

#### 3. Start PostgreSQL

```bash
docker run -d --rm \
  --name school-app-postgres \
  --network school-app-demo \
  -e POSTGRES_DB=school_console_app \
  -e POSTGRES_USER=school_demo \
  -e POSTGRES_PASSWORD=local-demo-password \
  --health-cmd="pg_isready -U school_demo -d school_console_app" \
  --health-interval=5s \
  --health-timeout=5s \
  --health-retries=10 \
  postgres:16
```

#### 4. Wait until PostgreSQL is ready

```bash
until docker inspect \
  -f '{{.State.Health.Status}}' \
  school-app-postgres \
  | grep -q '^healthy$'; do
  echo "Waiting for PostgreSQL..."
  sleep 2
done
```

#### 5. Run the application

```bash
docker run --rm -it \
  --name school-app \
  --network school-app-demo \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://school-app-postgres:5432/school_console_app \
  -e SPRING_DATASOURCE_USERNAME=school_demo \
  -e SPRING_DATASOURCE_PASSWORD=local-demo-password \
  "$IMAGE"
```

The application starts in interactive console mode. Select `q` to exit.

#### 6. Clean up the demo environment

```bash
docker stop school-app-postgres
docker network rm school-app-demo
```

The PostgreSQL container uses temporary storage in this demo, so its data is removed during cleanup.

---

### Option 2: Build and run locally

This option is intended for development and testing changes made to the source code.

#### Linux or WSL

```bash
./mvnw clean package
docker compose run --rm --build app
```

#### Windows PowerShell

```powershell
.\mvnw.cmd clean package
docker compose run --rm --build app
```

The first command builds the executable Spring Boot JAR. The second command builds the local application image, starts PostgreSQL, and runs the application interactively.

After changing the Java source code, rebuild the JAR before rebuilding the Docker image.

### Local environment management

Stop the local containers while preserving PostgreSQL data:

```bash
docker compose down
```

Stop the containers and remove the PostgreSQL volume:

```bash
docker compose down --volumes
```

Use the second command when you need a clean database.
