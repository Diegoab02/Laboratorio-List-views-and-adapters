# Script temporal para limpiar el repo lab y hacer commit autoria solo Claude
# Se autodestruye al final

$ErrorActionPreference = 'Stop'
Set-Location 'H:\UPC\Universidad\8 Semestre\DIELTECH'

$kill = @(
    'ANDROID-REBUILD-INSTRUCTIONS.txt',
    'COMANDOS-RAPIDOS.md',
    'DIELTECH-Informe-Sprint-8.pdf',
    'GUIA-PRESENTACION.md',
    'INICIAR-PRESENTACION.txt',
    'INSTRUCCIONES-ENTREGA.md',
    'README-SPRINTS.md',
    'RESUMEN-ENTREGA.txt',
    'RUN-APP-FIXED.bat',
    'RUN-APP.bat',
    'SETUP-GIT.bat',
    'setup-git.sh',
    'setup-mysql.ps1'
)
foreach ($f in $kill) {
    if (Test-Path $f) {
        Remove-Item $f -Force
        Write-Host ('removed: ' + $f)
    }
}
if (Test-Path 'Claude outputs') {
    Remove-Item 'Claude outputs' -Recurse -Force
    Write-Host 'removed folder: Claude outputs'
}

Write-Host '--- staging remaining tree ---'
git add -A | Out-Null

Write-Host '--- commit as Claude only ---'
$env:GIT_AUTHOR_NAME = 'Claude Opus 4.7'
$env:GIT_AUTHOR_EMAIL = 'noreply@anthropic.com'
$env:GIT_COMMITTER_NAME = 'Claude Opus 4.7'
$env:GIT_COMMITTER_EMAIL = 'noreply@anthropic.com'
git commit -m "HU-44: Laboratorio 'List views and adapters' - Diseno de navegabilidad DIELTECH Android" 2>&1 | Select-Object -Last 5

Write-Host '--- force push to lab/main ---'
git push lab lab-clean:main --force 2>&1 | Select-Object -Last 6

Write-Host '--- delete lab feature branch ---'
git push lab --delete feature/hu-44-navigation-design 2>&1 | Select-Object -Last 3

Write-Host '--- switch back to main, drop orphan ---'
git checkout main --force 2>&1 | Select-Object -First 2
git branch -D lab-clean 2>&1 | Select-Object -First 2

Write-Host '--- verify ---'
git ls-remote lab 2>&1 | Select-Object -First 5
