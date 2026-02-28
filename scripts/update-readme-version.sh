#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
POM_FILE="$ROOT_DIR/pom.xml"
TEMPLATE_FILE="$ROOT_DIR/README.md.template"
README_FILE="$ROOT_DIR/README.md"

if [[ ! -f "$POM_FILE" ]]; then
  echo "pom.xml not found: $POM_FILE" >&2
  exit 1
fi

if [[ ! -f "$TEMPLATE_FILE" ]]; then
  echo "README template not found: $TEMPLATE_FILE" >&2
  exit 1
fi

VERSION="$(awk 'match($0, /<version>[^<]+<\/version>/) { print substr($0, RSTART + 9, RLENGTH - 19); exit }' "$POM_FILE")"

if [[ -z "$VERSION" ]]; then
  echo "Failed to extract project version from $POM_FILE" >&2
  exit 1
fi

sed "s/@PROJECT_VERSION@/$VERSION/g" "$TEMPLATE_FILE" > "$README_FILE"

echo "README.md updated with version: $VERSION"
