#!/usr/bin/env bash

set -Eeuo pipefail

run_tests=false
reset_database=false
postgres_password="${POSTGRES_PASSWORD:-local-dev-password}"

usage() {
  cat <<'EOF'
Usage: ./run.sh [options]

Options:
  --run-tests
      Run Maven tests before starting the application.

  --reset-database
      Remove existing containers and the PostgreSQL volume before startup.

  --postgres-password <password>
      Set the local PostgreSQL password.
      Default: POSTGRES_PASSWORD environment variable or local-dev-password.

  -h, --help
      Show this help message.
EOF
}

fail() {
  printf 'Error: %s\n' "$1" >&2
  exit 1
}

while (( $# > 0 )); do
  case "$1" in
    --run-tests)
      run_tests=true
      shift
      ;;

    --reset-database)
      reset_database=true
      shift
      ;;

    --postgres-password)
      if (( $# < 2 )); then
        fail "The --postgres-password option requires a value."
      fi

      postgres_password="$2"
      shift 2
      ;;

    -h|--help)
      usage
      exit 0
      ;;

    *)
      fail "Unknown option: $1. Use --help to see the supported options."
      ;;
  esac
done

if [[ ! -f "./mvnw" ]]; then
  fail "Maven Wrapper was not found: ./mvnw"
fi

if ! command -v docker > /dev/null 2>&1; then
  fail "Docker CLI was not found. Install or start Docker and try again."
fi

echo "Checking Docker Compose..."

if ! docker compose version > /dev/null 2>&1; then
  fail "Docker Compose is not available."
fi

export POSTGRES_PASSWORD="$postgres_password"

if [[ "$reset_database" == true ]]; then
  echo "Removing existing containers and PostgreSQL data..."

  docker compose down \
    --volumes \
    --remove-orphans
fi

maven_arguments=(
  --batch-mode
  --no-transfer-progress
  clean
  package
)

if [[ "$run_tests" == false ]]; then
  maven_arguments+=("-DskipTests")
fi

echo "Building the Spring Boot application..."

bash ./mvnw "${maven_arguments[@]}"

echo "Validating Docker Compose configuration..."

docker compose config > /dev/null

echo "Building the local image and starting the application..."

docker compose run \
  --rm \
  --build \
  app

echo
echo "Application exited successfully."
echo "PostgreSQL remains available with its local data."
echo "Use 'docker compose down' to stop the environment."
echo "Use './run.sh --reset-database' for a clean database."
