#!/usr/bin/env bash
#
# update-from-upstream.sh
#
# Sync Signal-Enterprise with the upstream signalapp/Signal-Android repository.
#
# What it does:
#   1. Ensures an `upstream` remote pointing at signalapp/Signal-Android exists.
#   2. Fetches the latest upstream history.
#   3. Reports how far behind/ahead the current branch is.
#   4. Merges upstream/<branch> into the current branch.
#   5. If conflicts occur, lists them and (by default) leaves the merge in
#      progress so you can resolve the small Enterprise customization surface
#      by hand, then `git commit`.
#
# The Enterprise customization surface is intentionally tiny and lives in a
# handful of files (see docs/ENTERPRISE.md). Conflicts almost always land there.
#
# Usage:
#   scripts/update-from-upstream.sh              # merge upstream/main into current branch
#   scripts/update-from-upstream.sh --branch main
#   scripts/update-from-upstream.sh --abort-on-conflict   # roll back if it can't auto-merge
#   scripts/update-from-upstream.sh --dry-run    # fetch + report divergence only, no merge
#
set -euo pipefail

UPSTREAM_URL="https://github.com/signalapp/Signal-Android.git"
UPSTREAM_REMOTE="upstream"
UPSTREAM_BRANCH="main"
DRY_RUN=false
ABORT_ON_CONFLICT=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --branch) UPSTREAM_BRANCH="$2"; shift 2 ;;
    --dry-run) DRY_RUN=true; shift ;;
    --abort-on-conflict) ABORT_ON_CONFLICT=true; shift ;;
    -h|--help) sed -n '2,32p' "$0"; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; exit 2 ;;
  esac
done

# Run from repo root regardless of where the script was invoked.
cd "$(git rev-parse --show-toplevel)"

info()  { printf '\033[1;34m==>\033[0m %s\n' "$*"; }
warn()  { printf '\033[1;33m!!\033[0m %s\n'  "$*"; }
error() { printf '\033[1;31mxx\033[0m %s\n'  "$*" >&2; }

# --- Preconditions -----------------------------------------------------------
if ! git diff --quiet || ! git diff --cached --quiet; then
  error "Working tree is not clean. Commit or stash your changes first."
  exit 1
fi

CURRENT_BRANCH="$(git rev-parse --abbrev-ref HEAD)"
info "Current branch: ${CURRENT_BRANCH}"

# --- Ensure upstream remote --------------------------------------------------
if ! git remote get-url "${UPSTREAM_REMOTE}" >/dev/null 2>&1; then
  info "Adding '${UPSTREAM_REMOTE}' remote -> ${UPSTREAM_URL}"
  git remote add "${UPSTREAM_REMOTE}" "${UPSTREAM_URL}"
fi

info "Fetching ${UPSTREAM_REMOTE}/${UPSTREAM_BRANCH} ..."
git fetch --no-tags "${UPSTREAM_REMOTE}" "${UPSTREAM_BRANCH}"

# --- Report divergence -------------------------------------------------------
BEHIND="$(git rev-list --count "HEAD..${UPSTREAM_REMOTE}/${UPSTREAM_BRANCH}")"
AHEAD="$(git rev-list --count "${UPSTREAM_REMOTE}/${UPSTREAM_BRANCH}..HEAD")"
info "Behind upstream by ${BEHIND} commit(s); ahead by ${AHEAD} commit(s) (Enterprise changes)."

if [[ "${BEHIND}" -eq 0 ]]; then
  info "Already up to date with ${UPSTREAM_REMOTE}/${UPSTREAM_BRANCH}. Nothing to do."
  exit 0
fi

if [[ "${DRY_RUN}" == true ]]; then
  info "Dry run — files upstream changed that Enterprise also touches (likely conflict points):"
  MERGE_BASE="$(git merge-base HEAD "${UPSTREAM_REMOTE}/${UPSTREAM_BRANCH}")"
  comm -12 \
    <(git diff --name-only "${MERGE_BASE}" HEAD | sort -u) \
    <(git diff --name-only "${MERGE_BASE}" "${UPSTREAM_REMOTE}/${UPSTREAM_BRANCH}" | sort -u) \
    | sed 's/^/    /' || true
  exit 0
fi

# --- Merge -------------------------------------------------------------------
info "Merging ${UPSTREAM_REMOTE}/${UPSTREAM_BRANCH} into ${CURRENT_BRANCH} ..."
if git merge --no-edit "${UPSTREAM_REMOTE}/${UPSTREAM_BRANCH}"; then
  info "Merge completed cleanly. Now run CI locally: ./gradlew qa"
  exit 0
fi

# --- Conflict handling -------------------------------------------------------
warn "Merge hit conflicts in the following files:"
git diff --name-only --diff-filter=U | sed 's/^/    /'

if [[ "${ABORT_ON_CONFLICT}" == true ]]; then
  warn "Aborting merge (--abort-on-conflict)."
  git merge --abort
  exit 1
fi

cat <<'EOF'

Resolve the conflicts above (they are almost always in the small Enterprise
customization surface documented in docs/ENTERPRISE.md), then:

    git add <resolved files>
    git commit            # completes the merge

To throw the merge away instead:

    git merge --abort
EOF
exit 1
