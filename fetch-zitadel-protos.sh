#!/bin/bash
set -e

PROTO_DIR="src/main/proto"

echo "Cleaning existing repos and protos..."
rm -rf tmp
rm -rf "$PROTO_DIR"
mkdir -p "$PROTO_DIR"

echo "Cloning Zitadel repo..."
git clone --depth=1 https://github.com/zitadel/zitadel.git tmp/zitadel-repo
cp -r tmp/zitadel-repo/proto/zitadel "$PROTO_DIR"
cp -r tmp/zitadel-repo/proto/zitadel/protoc_gen_zitadel "$PROTO_DIR/zitadel/"
cp -r tmp/zitadel-repo/proto/zitadel/webkey "$PROTO_DIR/zitadel/"

echo "Cloning protoc-gen-validate repo..."
git clone --depth=1 https://github.com/envoyproxy/protoc-gen-validate.git tmp/validate-repo
mkdir -p "$PROTO_DIR/validate"
cp -r tmp/validate-repo/validate/*.proto "$PROTO_DIR/validate/"

echo "Cleaning temp folders..."
rm -rf tmp

echo "✅ Proto files downloaded to $PROTO_DIR"