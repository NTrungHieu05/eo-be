<#
  Chay thu toan bo stack (Mongo + BE + FE), build image, kiem tra nhanh.
  Dung:  cd D:\DATN\eo-be ; .\scripts\docker-demo.ps1            (build + chay + kiem tra)
         .\scripts\docker-demo.ps1 -Save                         (them: luu image ra D:\DATN\images\*.tar)
         .\scripts\docker-demo.ps1 -Down                         (dung stack)
#>
param([switch]$Save, [switch]$Down)
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
$base = 'http://localhost:3000'
$script:fail = 0

function Step($m) { Write-Host "`n== $m" -ForegroundColor Cyan }
function Ok($m)   { Write-Host "  [OK]   $m" -ForegroundColor Green }
function Bad($m)  { Write-Host "  [LOI]  $m" -ForegroundColor Red; $script:fail++ }

if ($Down) { docker compose down; exit 0 }

Step '1. Kiem tra moi truong'
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { Bad 'Chua cai Docker'; exit 1 }
docker info *> $null
if ($LASTEXITCODE -ne 0) { Bad 'Docker Desktop chua chay. Mo Docker Desktop, doi trang thai Running roi chay lai.'; exit 1 }
Ok 'Docker dang chay'
if (-not (Test-Path ..\eo-fe\Dockerfile)) { Bad 'Khong thay ..\eo-fe (eo-fe phai nam canh eo-be)'; exit 1 }
Ok 'Tim thay eo-fe canh eo-be'
if (-not (Test-Path .env)) { Copy-Item .env.example .env; Write-Host '  Da tao .env tu .env.example (nen doi JWT_SECRET)' -ForegroundColor Yellow }
Ok 'Co file .env'
$busy = Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue
if ($busy -and -not (docker compose ps -q fe 2>$null)) { Bad 'Cong 3000 dang bi chuong trinh khac dung. Tat no hoac doi cong.'; exit 1 }
Ok 'Cong 3000 san sang'

Step '2. Build image (lan dau mat vai phut)'
docker compose build
if ($LASTEXITCODE -ne 0) { Bad 'Build that bai (xem log phia tren)'; exit 1 }
Ok 'Build xong'

Step '3. Khoi dong Mongo + BE + FE'
docker compose up -d
if ($LASTEXITCODE -ne 0) { Bad 'docker compose up that bai'; exit 1 }

Step '4. Cho he thong san sang (toi da 4 phut)'
$ready = $false
for ($i = 0; $i -lt 48; $i++) {
  try {
    $h = Invoke-WebRequest "$base/api/actuator/health" -UseBasicParsing -TimeoutSec 5
    if ($h.Content -match '"status"\s*:\s*"UP"') { $ready = $true; break }
  } catch { }
  Start-Sleep -Seconds 5
}
if ($ready) { Ok 'BE health = UP (qua FE proxy /api)' }
else { Bad 'BE chua len sau 4 phut'; docker compose logs --tail 60 be mongo; exit 1 }

Step '5. Kiem tra nhanh'
foreach ($p in '/welcome','/login','/signup') {
  try { $r = Invoke-WebRequest "$base$p" -UseBasicParsing -TimeoutSec 15; Ok "$p -> $($r.StatusCode)" }
  catch { Bad "$p -> $($_.Exception.Message)" }
}
try {
  $body = @{ email = 'admin@toeic.local'; password = 'Admin@123' } | ConvertTo-Json
  $r = Invoke-WebRequest "$base/api/app/login" -Method Post -Body $body -ContentType 'application/json' -UseBasicParsing -TimeoutSec 15
  if ($r.Content.Length -gt 20) { Ok "Dang nhap admin -> $($r.StatusCode)" } else { Bad 'Dang nhap tra ve rong' }
} catch { Bad "Dang nhap admin that bai: $($_.Exception.Message)" }
try {
  $r = Invoke-WebRequest "$base/data/sounds/part1-1.wav" -Method Head -UseBasicParsing -TimeoutSec 15
  Ok "Audio mau /data/sounds/part1-1.wav -> $($r.StatusCode)"
} catch { Bad "Khong lay duoc audio mau: $($_.Exception.Message)" }
$n = docker compose exec -T mongo mongosh --quiet --eval "db.getSiblingDB('exam_online').getCollectionNames().length" 2>$null
if ([int]$n -gt 0) { Ok "MongoDB co database exam_online ($n collection)" } else { Bad 'MongoDB chua co du lieu seed' }

Step '6. Gan tag image'
$sha = (git rev-parse --short HEAD 2>$null); if (-not $sha) { $sha = 'local' }
$beImg = docker compose images be -q | Select-Object -First 1
$feImg = docker compose images fe -q | Select-Object -First 1
if ($beImg) { docker tag $beImg eo-be:latest; docker tag $beImg "eo-be:$sha" }
if ($feImg) { docker tag $feImg eo-fe:latest; docker tag $feImg "eo-fe:$sha" }
docker images --format 'table {{.Repository}}\t{{.Tag}}\t{{.Size}}' | Select-String '^(REPOSITORY|eo-)'

if ($Save) {
  Step '7. Luu image ra file'
  $out = Resolve-Path ..\ | ForEach-Object { Join-Path $_ 'images' }
  New-Item -ItemType Directory -Force $out | Out-Null
  docker save eo-be:latest -o (Join-Path $out 'eo-be.tar')
  docker save eo-fe:latest -o (Join-Path $out 'eo-fe.tar')
  Ok "Da luu vao $out"
}

Write-Host ''
if ($script:fail -eq 0) { Write-Host "TAT CA OK. Mo $base de demo." -ForegroundColor Green }
else { Write-Host "$($script:fail) muc loi. Gui log phia tren cho Claude." -ForegroundColor Red; docker compose logs --tail 40 be fe }
