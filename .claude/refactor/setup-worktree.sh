#!/usr/bin/env bash
# Create a git worktree for a refactor work package and seed its .claude/settings.json.
#
# Usage:
#   .claude/refactor/setup-worktree.sh <branch> [base] [path]
#
# Examples:
#   .claude/refactor/setup-worktree.sh refactor/wp3-domain dev
#   .claude/refactor/setup-worktree.sh refactor/wp5-use-cases refactor/wp4-repository-port
set -euo pipefail

branch="${1:?usage: setup-worktree.sh <branch> [base] [path]}"
base="${2:-dev}"
# Default worktree path: sibling folder named after the branch's last segment.
repo_root="$(git rev-parse --show-toplevel)"
default_path="$(dirname "$repo_root")/otf-sisacad-${branch##*/}"
path="${3:-$default_path}"

git fetch --all --prune
git worktree add -b "$branch" "$path" "$base"

mkdir -p "$path/.claude"
cp "$repo_root/.claude/refactor/settings.template.json" "$path/.claude/settings.json"

echo "Worktree ready:"
echo "  path   : $path"
echo "  branch : $branch (from $base)"
echo "  .claude/settings.json seeded (untracked, attribution disabled)"
