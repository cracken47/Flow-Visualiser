#!/bin/bash

# Simple script to sign a single file for troubleshooting

# GPG signing information
GPG_KEY_ID="9196540E"
GPG_PASSWORD="Apple@9878"

if [ $# -ne 1 ]; then
  echo "Usage: $0 <file_to_sign>"
  exit 1
fi

FILE_TO_SIGN=$1

if [ ! -f "$FILE_TO_SIGN" ]; then
  echo "Error: File $FILE_TO_SIGN not found"
  exit 1
fi

echo "Signing file: $FILE_TO_SIGN with key $GPG_KEY_ID"

# Method 1: Using passphrase-fd
echo "Method 1: Using passphrase-fd"
echo "$GPG_PASSWORD" | gpg --batch --yes --passphrase-fd 0 \
    --default-key "$GPG_KEY_ID" \
    --armor --detach-sign "$FILE_TO_SIGN"

# Check if signature was created
if [ -f "$FILE_TO_SIGN.asc" ]; then
  echo "✓ Signature created: $FILE_TO_SIGN.asc"
  
  # Verify the signature
  if gpg --verify "$FILE_TO_SIGN.asc" "$FILE_TO_SIGN" 2>/dev/null; then
    echo "✓ Signature verified successfully"
  else
    echo "✗ Signature verification failed"
  fi
else
  echo "✗ Failed to create signature"
  
  # Try alternate method
  echo "Trying alternate method..."
  
  # Method 2: Using pinentry program
  gpg --pinentry-mode loopback --passphrase "$GPG_PASSWORD" \
      --default-key "$GPG_KEY_ID" \
      --armor --detach-sign "$FILE_TO_SIGN"
  
  if [ -f "$FILE_TO_SIGN.asc" ]; then
    echo "✓ Signature created with alternate method"
  else
    echo "✗ Both signature methods failed"
  fi
fi

# Display file list to confirm
ls -la "$FILE_TO_SIGN"* 