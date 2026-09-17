param([switch]$SkipBuild)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$envFile = Join-Path $projectRoot '.env'
if (-not (Test-Path -LiteralPath $envFile)) { throw 'Run ./scripts/init-env.ps1 first.' }
foreach ($line in Get-Content -LiteralPath $envFile) {
    if ($line -match '^([A-Z][A-Z0-9_]*)=(.*)$') {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process')
    }
}
Push-Location (Join-Path $projectRoot 'backend')
try {
    if (-not $SkipBuild) {
        & ./mvnw.cmd -B -ntp verify
        if ($LASTEXITCODE -ne 0) { throw 'Backend build failed.' }
    }
    & java -jar target/corpedia-backend-0.0.1-SNAPSHOT.jar
    if ($LASTEXITCODE -ne 0) { throw 'Backend stopped with an error.' }
} finally { Pop-Location }
