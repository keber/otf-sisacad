<#
.SYNOPSIS
  Create a git worktree for a refactor work package and seed its .claude/settings.json.

.EXAMPLE
  .\.claude\refactor\setup-worktree.ps1 -Branch refactor/wp3-domain -Base dev

.EXAMPLE
  .\.claude\refactor\setup-worktree.ps1 -Branch refactor/wp5-use-cases -Base refactor/wp4-repository-port
#>
param(
    [Parameter(Mandatory = $true)][string]$Branch,
    [string]$Base = "dev",
    [string]$Path
)
$ErrorActionPreference = "Stop"

$repoRoot = (git rev-parse --show-toplevel).Trim()
if (-not $Path) {
    $leaf = $Branch.Split("/")[-1]
    $Path = Join-Path (Split-Path $repoRoot -Parent) "otf-sisacad-$leaf"
}

git fetch --all --prune
git worktree add -b $Branch $Path $Base

$claudeDir = Join-Path $Path ".claude"
New-Item -ItemType Directory -Force -Path $claudeDir | Out-Null
Copy-Item (Join-Path $repoRoot ".claude/refactor/settings.template.json") (Join-Path $claudeDir "settings.json") -Force

Write-Host "Worktree ready:"
Write-Host "  path   : $Path"
Write-Host "  branch : $Branch (from $Base)"
Write-Host "  .claude/settings.json seeded (untracked, attribution disabled)"
