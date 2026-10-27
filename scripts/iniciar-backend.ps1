$projectRoot = Split-Path -Parent $PSScriptRoot
$envPath = Join-Path $projectRoot '.env'
if (-not (Test-Path $envPath)) {
    throw 'Primero copia .env.example como .env y configura tus datos.'
}
Get-Content $envPath | ForEach-Object {
    $line = $_.Trim()
    if ($line -and -not $line.StartsWith('#')) {
        $pair = $line.Split('=', 2)
        if ($pair.Length -eq 2) {
            [Environment]::SetEnvironmentVariable($pair[0].Trim(), $pair[1], 'Process')
        }
    }
}
if (-not $env:JWT_SECRET_BASE64) {
    throw 'Falta JWT_SECRET_BASE64 en .env. Ejecuta scripts/generar-secret.ps1.'
}
Push-Location $projectRoot
try {
    & mvn spring-boot:run
} finally {
    Pop-Location
}
