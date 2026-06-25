$ErrorActionPreference = 'Stop'
$docsRoot = (Join-Path $PSScriptRoot '..\docs' | Resolve-Path).Path
$issues = @()
$checked = @{}

function Test-DocReference {
  param(
    [string]$SourceFile,
    [string]$Reference
  )

  $ref = $Reference.Trim()
  if ($ref -match '^https?://') { return }
  if ($ref -match '^#') { return }

  $ref = $ref -replace '^\./', ''
  $ref = ($ref -split '#')[0]
  if ([string]::IsNullOrWhiteSpace($ref)) { return }

  $sourceDir = Split-Path $SourceFile -Parent
  $target = Join-Path $sourceDir $ref
  if (-not (Test-Path $target)) {
    $target = Join-Path $docsRoot $ref
  }

  $key = "$SourceFile|$Reference"
  if ($checked.ContainsKey($key)) { return }
  $checked[$key] = $true

  if (-not (Test-Path $target)) {
    $script:issues += "Broken reference in '$SourceFile': '$Reference' (resolved to '$target')"
  }
}

Get-ChildItem -Path $docsRoot -Recurse -Filter '*.md' | ForEach-Object {
  $content = Get-Content -Raw -Encoding UTF8 $_.FullName
  $content = [regex]::Replace($content, '```[\s\S]*?```', '')

  foreach ($match in [regex]::Matches($content, '\[[^\]]+\]\(([^)]+)\)')) {
    Test-DocReference -SourceFile $_.FullName -Reference $match.Groups[1].Value
  }

  foreach ($match in [regex]::Matches($content, '(?<!\()\b([0-9]{2}-[a-z0-9-]+\.md)\b')) {
    Test-DocReference -SourceFile $_.FullName -Reference $match.Groups[1].Value
  }

  foreach ($match in [regex]::Matches($content, '\*\*([0-9]{2}-[a-z0-9-]+\.md)\*\*')) {
    Test-DocReference -SourceFile $_.FullName -Reference $match.Groups[1].Value
  }
}

if ($issues.Count -eq 0) {
  Write-Output 'All cross-references valid.'
  exit 0
}

$issues | ForEach-Object { Write-Output $_ }
exit 1
