param([switch]$Pruebas, [switch]$SinColor)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $javac = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/javac.exe' } else { (Get-Command javac).Source }
    $java = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/java.exe' } else { (Get-Command java).Source }
    New-Item -ItemType Directory -Force build/classes | Out-Null
    $fuentes = @(Get-ChildItem src/main/java -Recurse -Filter *.java | ForEach-Object FullName)
    if ($Pruebas) {
        $fuentes += @(Get-ChildItem src/test/java -Recurse -Filter *.java | ForEach-Object FullName)
    }
    & $javac -encoding UTF-8 --release 21 -d build/classes $fuentes
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo compilar el proyecto' }
    if ($Pruebas) {
        & $java '-Dfile.encoding=UTF-8' -cp build/classes taberna.Pruebas
    } elseif ($SinColor) {
        & $java '-Dfile.encoding=UTF-8' -cp build/classes taberna.Main --sin-color
    } else {
        & $java '-Dfile.encoding=UTF-8' -cp build/classes taberna.Main
    }
    if ($LASTEXITCODE -ne 0) { throw 'La ejecución terminó con errores' }
} finally {
    Pop-Location
}
