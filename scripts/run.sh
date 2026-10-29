#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
sh scripts/build.sh
exec java -cp out/main restaurantrush.Main "$@"
