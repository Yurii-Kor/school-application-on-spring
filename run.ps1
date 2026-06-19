[CmdletBinding()]
param(
    [switch]$RunTests,
    [switch]$ResetDatabase,
    [string]$PostgresPassword = "local-dev-password"
)

$ErrorActionPreference = "Stop"

$hadPostgresPassword = Test-Path Env:POSTGRES_PASSWORD
$previousPostgresPassword = $env:POSTGRES_PASSWORD

try {
    if (-not (Test-Path ".\mvnw.cmd")) {
        throw "Maven Wrapper was not found: .\mvnw.cmd"
    }

    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        throw "Docker CLI was not found. Start Docker Desktop and try again."
    }

    Write-Host "Checking Docker Compose..."
    docker compose version | Out-Null

    if ($LASTEXITCODE -ne 0) {
        throw "Docker Compose is not available."
    }

    $env:POSTGRES_PASSWORD = $PostgresPassword

    if ($ResetDatabase) {
        Write-Host "Removing existing containers and PostgreSQL data..."

        docker compose down --volumes --remove-orphans

        if ($LASTEXITCODE -ne 0) {
            throw "Failed to reset the local Docker environment."
        }
    }

    $mavenArguments = @(
        "--batch-mode"
        "--no-transfer-progress"
        "clean"
        "package"
    )

    if (-not $RunTests) {
        $mavenArguments += "-DskipTests"
    }

    Write-Host "Building the Spring Boot application..."

    & .\mvnw.cmd @mavenArguments

    if ($LASTEXITCODE -ne 0) {
        throw "Maven build failed with exit code $LASTEXITCODE."
    }

    Write-Host "Validating Docker Compose configuration..."

    docker compose config | Out-Null

    if ($LASTEXITCODE -ne 0) {
        throw "Docker Compose configuration is invalid."
    }

    Write-Host "Building the local image and starting the application..."

    docker compose run --rm --build app

    if ($LASTEXITCODE -ne 0) {
        throw "Application container exited with code $LASTEXITCODE."
    }

    Write-Host ""
    Write-Host "Application exited successfully."
    Write-Host "PostgreSQL remains available with its local data."
    Write-Host "Use 'docker compose down' to stop the environment."
    Write-Host "Use '.\run.ps1 -ResetDatabase' for a clean database."
}
finally {
    if ($hadPostgresPassword) {
        $env:POSTGRES_PASSWORD = $previousPostgresPassword
    }
    else {
        Remove-Item Env:POSTGRES_PASSWORD -ErrorAction SilentlyContinue
    }
}
