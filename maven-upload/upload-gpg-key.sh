#!/bin/bash

# Script to upload your GPG key to multiple keyservers
# This ensures Maven Central can verify your signatures

GPG_KEY_ID="9196540E"

echo "Uploading GPG key $GPG_KEY_ID to keyservers..."

# Upload to Ubuntu keyserver
echo "Uploading to keyserver.ubuntu.com..."
gpg --keyserver keyserver.ubuntu.com --send-keys "$GPG_KEY_ID"

# Upload to MIT keyserver
echo "Uploading to pgp.mit.edu..."
gpg --keyserver pgp.mit.edu --send-keys "$GPG_KEY_ID"

# Upload to OpenPGP keyserver
echo "Uploading to keys.openpgp.org..."
gpg --keyserver keys.openpgp.org --send-keys "$GPG_KEY_ID"

# Verify that the key is available
echo ""
echo "Verifying key availability..."
echo "This may take a moment as keyservers sync..."
sleep 5

# Try to receive the key from Ubuntu keyserver
echo "Checking keyserver.ubuntu.com..."
gpg --keyserver keyserver.ubuntu.com --recv-keys "$GPG_KEY_ID"

echo ""
echo "Key upload complete. Wait a few minutes for keyservers to sync before uploading to Maven Central."
echo "Next step: Run ./create-bundle.sh to create your Maven bundle with the proper signatures." 