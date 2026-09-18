$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$envFile = Join-Path $projectRoot '.env'
if (Test-Path -LiteralPath $envFile) { Write-Output '.env already exists; preserved.'; exit 0 }
$content = Get-Content -LiteralPath (Join-Path $projectRoot '.env.example') -Raw
foreach ($name in @('DB_PASSWORD','MYSQL_ROOT_PASSWORD','JWT_SECRET','SEED_PASSWORD','MINIO_ROOT_PASSWORD')) {
    $bytes = [byte[]]::new(32)
    [Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
    $secret = [Convert]::ToHexString($bytes).ToLowerInvariant()
    $content = [regex]::Replace($content, '(?m)^' + $name + '=.*$', $name + '=' + $secret)
}
[IO.File]::WriteAllText($envFile, $content, [Text.UTF8Encoding]::new($false))
Write-Output 'Created .env with local credentials. Read SEED_PASSWORD there to log in; do not commit this file.'
