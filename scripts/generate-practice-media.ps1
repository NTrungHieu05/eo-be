$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.Speech

$root = 'D:\DATN\eo-be\data'
$imgDir = Join-Path $root 'images'
$sndDir = Join-Path $root 'sounds'
New-Item -ItemType Directory -Force -Path $imgDir, $sndDir | Out-Null

function Save-Png([string]$path, [System.Drawing.Color]$bg, [scriptblock]$draw) {
  $bmp = New-Object System.Drawing.Bitmap 800, 600
  $g = [System.Drawing.Graphics]::FromImage($bmp)
  $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
  $g.Clear($bg)
  & $draw $g
  $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
  $g.Dispose()
  $bmp.Dispose()
}

function Draw-Person($g, $x, $y, $body, $head) {
  $g.FillEllipse((New-Object System.Drawing.SolidBrush $head), $x, $y, 50, 50)
  $g.FillRectangle((New-Object System.Drawing.SolidBrush $body), ($x + 8), ($y + 52), 34, 80)
}

$captionFont = New-Object System.Drawing.Font 'Segoe UI', 18, ([System.Drawing.FontStyle]::Bold)
$captionBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::White)
$shadowBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(160, 0, 0, 0))

# Photo 1: man at a desk
Save-Png (Join-Path $imgDir 'part1-1.png') ([System.Drawing.Color]::FromArgb(196, 216, 232)) {
  param($g)
  $g.FillRectangle((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(139, 105, 68))), 180, 360, 440, 28)
  $g.FillRectangle((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(92, 64, 40))), 200, 388, 400, 90)
  $g.FillRectangle((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(230, 230, 230))), 430, 250, 140, 90)
  Draw-Person $g 330 230 ([System.Drawing.Color]::FromArgb(36, 99, 160)) ([System.Drawing.Color]::FromArgb(241, 194, 125))
  $g.FillRectangle($shadowBrush, 18, 18, 210, 42)
  $g.DrawString('PHOTO 1  Desk', $captionFont, $captionBrush, 24, 22)
}

# Photo 2: people standing outdoors
Save-Png (Join-Path $imgDir 'part1-2.png') ([System.Drawing.Color]::FromArgb(126, 186, 116)) {
  param($g)
  $g.FillRectangle((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(135, 206, 235))), 0, 0, 800, 260)
  Draw-Person $g 280 250 ([System.Drawing.Color]::FromArgb(40, 120, 80)) ([System.Drawing.Color]::FromArgb(255, 224, 189))
  Draw-Person $g 430 260 ([System.Drawing.Color]::FromArgb(180, 70, 90)) ([System.Drawing.Color]::FromArgb(224, 172, 105))
  $g.FillRectangle($shadowBrush, 18, 18, 250, 42)
  $g.DrawString('PHOTO 2  People', $captionFont, $captionBrush, 24, 22)
}

# Photo 3: objects on a table
Save-Png (Join-Path $imgDir 'part1-3.png') ([System.Drawing.Color]::FromArgb(245, 236, 220)) {
  param($g)
  $g.FillRectangle((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(166, 124, 82))), 80, 330, 640, 40)
  $g.FillEllipse((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(200, 40, 40))), 180, 250, 90, 90)
  $g.FillRectangle((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(70, 130, 180))), 340, 210, 130, 120)
  $g.FillEllipse((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(240, 200, 80))), 530, 240, 80, 80)
  $g.FillRectangle($shadowBrush, 18, 18, 230, 42)
  $g.DrawString('PHOTO 3  Table', $captionFont, $captionBrush, 24, 22)
}

# Photo 4: woman holding a folder
Save-Png (Join-Path $imgDir 'part1-4.png') ([System.Drawing.Color]::FromArgb(232, 232, 236)) {
  param($g)
  Draw-Person $g 370 160 ([System.Drawing.Color]::FromArgb(150, 60, 110)) ([System.Drawing.Color]::FromArgb(255, 224, 189))
  $g.FillRectangle((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(230, 180, 70))), 330, 250, 70, 90)
  $g.FillRectangle($shadowBrush, 18, 18, 280, 42)
  $g.DrawString('PHOTO 4  Holding', $captionFont, $captionBrush, 24, 22)
}

# Photo 5: weather / rain
Save-Png (Join-Path $imgDir 'part1-5.png') ([System.Drawing.Color]::FromArgb(90, 110, 140)) {
  param($g)
  $g.FillEllipse((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(180, 190, 200))), 80, 40, 220, 90)
  $g.FillEllipse((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(180, 190, 200))), 200, 20, 260, 110)
  $pen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(170, 200, 230)), 4
  for ($i = 80; $i -le 720; $i += 40) {
    $g.DrawLine($pen, $i, 180, ($i - 20), 320)
  }
  $g.FillRectangle($shadowBrush, 18, 18, 250, 42)
  $g.DrawString('PHOTO 5  Weather', $captionFont, $captionBrush, 24, 22)
}

# Photo 6: building with a tree
Save-Png (Join-Path $imgDir 'part1-6.png') ([System.Drawing.Color]::FromArgb(170, 210, 245)) {
  param($g)
  $g.FillRectangle((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(96, 125, 139))), 180, 140, 280, 360)
  $g.FillRectangle((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(255, 236, 179))), 220, 180, 50, 50)
  $g.FillRectangle((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(255, 236, 179))), 320, 180, 50, 50)
  $g.FillRectangle((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(62, 39, 35))), 560, 320, 28, 180)
  $g.FillEllipse((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(46, 125, 50))), 500, 210, 150, 150)
  $g.FillRectangle($shadowBrush, 18, 18, 270, 42)
  $g.DrawString('PHOTO 6  Building', $captionFont, $captionBrush, 24, 22)
}

function Save-Speech([string]$path, [string]$text) {
  $synth = New-Object System.Speech.Synthesis.SpeechSynthesizer
  $synth.Rate = -1
  $synth.SetOutputToWaveFile($path)
  $synth.Speak($text)
  $synth.Dispose()
}

$bankPath = 'D:\DATN\eo-be\src\main\resources\practice\practice-questions.json'
$bank = Get-Content -Raw -Encoding UTF8 $bankPath | ConvertFrom-Json
$targets = @(
  @{ Exam = $bank.exams[0]; Dir = $sndDir },
  @{ Exam = $bank.exams[1]; Dir = (Join-Path $sndDir 'exam2') },
  @{ Exam = $bank.exams[2]; Dir = (Join-Path $sndDir 'exam3') }
)

function Join-SpokenSingle([string]$prefix, $item) {
  $letters = @('A', 'B', 'C', 'D')
  $pieces = New-Object System.Collections.Generic.List[string]
  [void]$pieces.Add($prefix + $item.text)
  for ($i = 0; $i -lt $item.options.Count; $i++) {
    [void]$pieces.Add(('{0}. {1}' -f $letters[$i], $item.options[$i]))
  }
  return ($pieces -join ' ')
}

function Join-SpokenGroup($group) {
  $pieces = New-Object System.Collections.Generic.List[string]
  [void]$pieces.Add([string]$group.passage)
  $number = 1
  foreach ($question in $group.questions) {
    [void]$pieces.Add(('Number {0}. {1}' -f $number, $question.text))
    $number++
  }
  return ($pieces -join ' ')
}

foreach ($target in $targets) {
  New-Item -ItemType Directory -Force -Path $target.Dir | Out-Null
  $exam = $target.Exam
  for ($i = 0; $i -lt $exam.part1.Count; $i++) {
    $spoken = Join-SpokenSingle ("Look at the picture. Number {0}. " -f ($i + 1)) $exam.part1[$i]
    Save-Speech (Join-Path $target.Dir ("part1-{0}.wav" -f ($i + 1))) $spoken
  }
  for ($i = 0; $i -lt $exam.part2.Count; $i++) {
    $spoken = Join-SpokenSingle ("Question {0}. " -f ($i + 1)) $exam.part2[$i]
    Save-Speech (Join-Path $target.Dir ("part2-{0}.wav" -f ($i + 1))) $spoken
  }
  for ($i = 0; $i -lt $exam.part3.Count; $i++) {
    Save-Speech (Join-Path $target.Dir ("part3-{0}.wav" -f ($i + 1))) (Join-SpokenGroup $exam.part3[$i])
  }
  for ($i = 0; $i -lt $exam.part4.Count; $i++) {
    Save-Speech (Join-Path $target.Dir ("part4-{0}.wav" -f ($i + 1))) (Join-SpokenGroup $exam.part4[$i])
  }
}

Write-Output 'Practice media generated.'
