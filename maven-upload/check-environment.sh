#!/bin/bash

# Script to check the environment for Maven Central publishing

echo "===== GPG Key Check ====="
if command -v gpg &> /dev/null; then
  echo "GPG is installed ✓"
  
  # List available keys
  echo ""
  echo "Available GPG keys:"
  gpg --list-keys
  
  # Ask for key ID
  echo ""
  echo "Enter the key ID you want to use for signing (last 8 characters):"
  read KEY_ID
  
  if [ -n "$KEY_ID" ]; then
    # Check if the key exists
    if gpg --list-keys "$KEY_ID" &> /dev/null; then
      echo "Key $KEY_ID exists locally ✓"
      
      # Check if key is on keyserver
      echo ""
      echo "Checking if your key is available on keyserver..."
      if gpg --keyserver keyserver.ubuntu.com --recv-keys "$KEY_ID" &> /dev/null; then
        echo "Key $KEY_ID is available on keyserver ✓"
      else
        echo "⚠️ Key $KEY_ID is not available on keyserver or could not be retrieved"
        echo "Please upload your key with:"
        echo "gpg --keyserver keyserver.ubuntu.com --send-keys $KEY_ID"
      fi
      
      # Test signing
      echo ""
      echo "Testing signature creation..."
      echo "test" > test-file.txt
      if gpg --armor --detach-sign --default-key "$KEY_ID" test-file.txt &> /dev/null; then
        echo "Signature created successfully ✓"
        
        # Verify signature
        if gpg --verify test-file.txt.asc test-file.txt &> /dev/null; then
          echo "Signature verified successfully ✓"
        else
          echo "⚠️ Signature verification failed"
        fi
      else
        echo "⚠️ Failed to create signature"
      fi
      
      # Clean up
      rm -f test-file.txt test-file.txt.asc
    else
      echo "⚠️ Key $KEY_ID not found"
    fi
  else
    echo "⚠️ No key ID provided"
  fi
else
  echo "⚠️ GPG is not installed"
  echo "Please install GPG before continuing"
  exit 1
fi

echo ""
echo "===== Gradle Properties Check ====="
GROUP=$(grep "GROUP=" ../gradle.properties | cut -d'=' -f2)
VERSION=$(grep "VERSION_NAME=" ../gradle.properties | cut -d'=' -f2)
ARTIFACT_ID=$(grep "POM_ARTIFACT_ID=" ../gradle.properties | cut -d'=' -f2)

echo "Group ID: $GROUP"
echo "Artifact ID: $ARTIFACT_ID"
echo "Version: $VERSION"

# Check Group ID format
if [[ "$GROUP" == io.github.* ]]; then
  echo "Group ID format is valid for GitHub users ✓"
elif [[ "$GROUP" =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$ ]]; then
  echo "Group ID appears to be a domain name format"
  echo "Make sure you own this domain or have permission to use it"
else
  echo "⚠️ Group ID format may not be accepted by Sonatype"
  echo "Consider using io.github.username format if you don't own a domain"
fi

echo ""
echo "===== File Check ====="
if [ -f "flow-visualiser-1.0.0.aar" ]; then
  echo "AAR file exists ✓"
else
  echo "⚠️ AAR file not found in the current directory"
fi

echo ""
echo "===== Sonatype Account Check ====="
echo "Do you have a Sonatype OSSRH account? (y/n)"
read HAS_ACCOUNT

if [ "$HAS_ACCOUNT" = "y" ]; then
  echo "Have you created a New Project ticket for your Group ID? (y/n)"
  read HAS_TICKET
  
  if [ "$HAS_TICKET" = "y" ]; then
    echo "Has your Group ID been approved? (y/n)"
    read IS_APPROVED
    
    if [ "$IS_APPROVED" = "y" ]; then
      echo "Sonatype account and Group ID setup complete ✓"
    else
      echo "⚠️ You need to wait for Group ID approval before uploading"
    fi
  else
    echo "⚠️ You need to create a New Project ticket to register your Group ID"
    echo "Visit: https://issues.sonatype.org/secure/CreateIssue.jspa?issuetype=21&pid=10134"
  fi
else
  echo "⚠️ You need a Sonatype OSSRH account"
  echo "Sign up at: https://issues.sonatype.org/secure/Signup!default.jspa"
fi

echo ""
echo "===== Summary ====="
echo "Fix any warnings marked with ⚠️ before proceeding with the upload" 