$files = @(
  (Join-Path $PSScriptRoot '..\docs\01-business-architecture\functional-use-cases.md'),
  (Join-Path $PSScriptRoot '..\docs\02-system-blueprint\INFRATRACK_SYSTEM_BLUEPRINT_V1_SKELETON.md')
)

function Normalize-MarkdownContent {
  param([string]$Content)

  $nl = [Environment]::NewLine

  $Content = $Content -replace '&#x20;', ' '
  $Content = $Content.Replace('\---', '---')
  $Content = $Content.Replace('\*\*', '**')
  $Content = $Content.Replace('\####', '####')
  $Content = $Content.Replace('\###', '###')
  $Content = $Content.Replace('\##', '##')
  $Content = $Content.Replace('\#', '#')
  $Content = $Content.Replace('\*', '*')
  $Content = $Content.Replace('\_', '_')
  $Content = $Content.Replace('\&', '&')
  $Content = $Content.Replace('\-', '-')
  $Content = [regex]::Replace($Content, '(\d)\\\.', '${1}.')

  $Content = [regex]::Replace($Content, '```(\w*)\r?\n([\s\S]*?)```', {
    param($Match)
    $lang = $Match.Groups[1].Value
    $bodyLines = $Match.Groups[2].Value -split '\r?\n' | Where-Object { $_.Trim() -ne '' }
    '```' + $lang + $nl + ($bodyLines -join $nl) + $nl + '```'
  })

  $patterns = @(
    '(\|[^\r\n]*)\r?\n\r?\n(\|)',
    '(\* [^\r\n]*)\r?\n\r?\n(\* )',
    '(- [^\r\n]*)\r?\n\r?\n(- )',
    '(\d+\. [^\r\n]*)\r?\n\r?\n(\d+\. )'
  )

  foreach ($pattern in $patterns) {
    do {
      $prev = $Content
      $Content = [regex]::Replace($Content, $pattern, '$1' + $nl + '$2')
    } while ($Content -ne $prev)
  }

  $Content = $Content -replace '(\r?\n){3,}', ($nl + $nl)
  return ($Content.TrimEnd() + $nl)
}

foreach ($file in $files) {
  $resolved = Resolve-Path $file
  $original = Get-Content -Raw -Encoding UTF8 $resolved
  $normalized = Normalize-MarkdownContent $original
  [System.IO.File]::WriteAllText($resolved, $normalized, [System.Text.UTF8Encoding]::new($false))
  $name = Split-Path $resolved -Leaf
  Write-Output "Normalized $name"
}
