#!/bin/bash

# Generate keystore for Android app signing
# This script will be used in GitHub Actions

echo "Generating keystore for My Notes App..."

# Keystore details
KEYSTORE_FILE="my-notes-keystore.jks"
KEY_ALIAS="my-notes-key"
KEYSTORE_PASSWORD="MyNotesApp2024!"
KEY_PASSWORD="MyNotesApp2024!"

# Generate keystore
keytool -genkey -v \
    -keystore $KEYSTORE_FILE \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -alias $KEY_ALIAS \
    -storepass $KEYSTORE_PASSWORD \
    -keypass $KEY_PASSWORD \
    -dname "CN=My Notes App, OU=Development, O=Whymyhuman, L=Jakarta, ST=Jakarta, C=ID"

echo "Keystore generated successfully: $KEYSTORE_FILE"
echo "Key alias: $KEY_ALIAS"
echo "Store password: $KEYSTORE_PASSWORD"
echo "Key password: $KEY_PASSWORD"

# Convert to base64 for GitHub secrets
echo ""
echo "Base64 encoded keystore (for GitHub secrets):"
base64 -w 0 $KEYSTORE_FILE
echo ""