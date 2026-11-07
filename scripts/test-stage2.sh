#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
# The normal entry point includes every Stage 1 and Stage 2 suite exactly once.
exec sh scripts/test.sh
