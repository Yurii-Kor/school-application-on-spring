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

The project is fully containerized using Docker and Docker Compose.

### 🔧 Available Commands

- `.un.ps1`  
  Builds the application JAR, starts the PostgreSQL container, and runs the app container interactively.

- `docker-compose down`  
  Stops all running containers without removing persistent volume data.

- `docker-compose down -v`  
  Stops all containers and removes volumes (used to reset the PostgreSQL database).

- `docker-compose up --build`  
  Rebuilds the application image and starts all containers.

- `docker-compose run --rm app`  
  Runs the app container interactively and removes it after execution (useful for development).