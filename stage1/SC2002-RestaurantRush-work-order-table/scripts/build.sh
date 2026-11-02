#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
mkdir -p out/main
find src/main/java -name '*.java' -print | sort > out/main-sources.txt
if command -v javac >/dev/null 2>&1; then
    javac --release 17 -encoding UTF-8 -Xlint:all -Werror -d out/main @out/main-sources.txt
else
    java -m jdk.compiler/com.sun.tools.javac.Main --release 17 -encoding UTF-8 -Xlint:all -Werror -d out/main @out/main-sources.txt
fi
