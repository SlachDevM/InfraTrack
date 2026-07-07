$base = Join-Path $PSScriptRoot '..\docs' | Resolve-Path

$dirs = @(
  '00-business-discovery',
  '01-business-architecture',
  '02-system-blueprint',
  '03-architecture',
  '04-api',
  '05-deployment'
)
foreach ($d in $dirs) {
  New-Item -ItemType Directory -Force -Path (Join-Path $base $d) | Out-Null
}

$business = @(
  '00-development-philosophy.md','01-project-context.md','02-domain-overview.md',
  '03-council-organisation.md','04-actors-responsibilities.md','05-asset-operational-lifecycle.md',
  '06-business-triggers.md','07-inspection-lifecycle.md','08-operational-decisions.md',
  '09-business-rules.md','10-ubiquitous-language.md','11-asset-status-model.md',
  '12-notification-lifecycle.md','13-department-collaboration.md'
)
$bd = Join-Path $base '00-business-discovery'
foreach ($f in $business) {
  $src = Join-Path $base $f
  if (Test-Path $src) {
    Move-Item -Force $src (Join-Path $bd $f)
  }
}

$srcFunc = Join-Path $base 'functional-use-case.md'
$dstFunc = Join-Path (Join-Path $base '01-business-architecture') 'functional-use-cases.md'
if (Test-Path $srcFunc) { Move-Item -Force $srcFunc $dstFunc }

$srcBp = Join-Path $base 'INFRATRACK_SYSTEM_BLUEPRINT.md'
$dstBp = Join-Path (Join-Path $base '02-system-blueprint') 'INFRATRACK_SYSTEM_BLUEPRINT.md'
if (Test-Path $srcBp) { Move-Item -Force $srcBp $dstBp }

foreach ($d in @('03-architecture','04-api','05-deployment')) {
  $keep = Join-Path (Join-Path $base $d) '.gitkeep'
  if (-not (Test-Path $keep)) { New-Item -ItemType File -Path $keep | Out-Null }
}

Write-Output 'Reorganisation complete'
