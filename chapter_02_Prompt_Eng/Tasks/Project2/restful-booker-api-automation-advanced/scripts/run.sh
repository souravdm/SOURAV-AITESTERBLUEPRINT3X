#!/usr/bin/env bash
# run.sh — Cross-platform launcher for the Restful-Booker test suite.
# Usage: ./scripts/run.sh [suite] [env] [threads]
#   suite   : smoke | sanity | regression | e2e   (default: regression)
#   env     : qa | stage | prod                    (default: qa)
#   threads : integer                              (default: 4)

set -euo pipefail

SUITE="${1:-regression}"
ENV="${2:-qa}"
THREADS="${3:-4}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "========================================================"
echo "  Restful-Booker API Test Runner"
echo "  Suite   : $SUITE"
echo "  Env     : $ENV"
echo "  Threads : $THREADS"
echo "========================================================"

cd "$PROJECT_DIR"

mvn clean test \
    -Denv="$ENV" \
    -DsuiteXmlFile="testsuites/$SUITE.xml" \
    -Dparallel.thread.count="$THREADS" \
    --no-transfer-progress

echo ""
echo "Report: $PROJECT_DIR/target/extent-report/index.html"
