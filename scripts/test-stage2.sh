#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
sh scripts/test.sh
failed=0
for suite in Stage2CustomerTests Stage2OrderTests Stage2StaffTests Stage2ControllerTests; do
    if java -cp out/main:out/test "restaurantrush.$suite"; then
        printf '%s\n' "PASS: $suite"
    else
        printf '%s\n' "PENDING/FAILED: $suite"
        failed=1
    fi
done
if [ "$failed" -ne 0 ]; then
    printf '%s\n' 'Stage 2 is NOT complete. See docs/STAGE2_ACCEPTANCE.md.'
    exit 1
fi
printf '%s\n' 'Stage 2 automated suites passed. Review the manual acceptance evidence before tagging.'
