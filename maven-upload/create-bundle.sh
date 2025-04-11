#!/bin/bash

# Script to create a Maven Central bundle zip file from the AAR

# Read values from gradle.properties
GROUP=$(grep "GROUP=" ../gradle.properties | cut -d'=' -f2)
VERSION=$(grep "VERSION_NAME=" ../gradle.properties | cut -d'=' -f2)
ARTIFACT_ID=$(grep "POM_ARTIFACT_ID=" ../gradle.properties | cut -d'=' -f2)

# GPG signing information
GPG_KEY_ID="9196540E"
GPG_PASSWORD="Apple@9878"
GPG_KEY_RING="/Users/starvader/secring.gpg"

# Convert dots to slashes in the group ID
GROUP_PATH=$(echo $GROUP | tr '.' '/')

echo "Creating Maven Central bundle for $GROUP:$ARTIFACT_ID:$VERSION"
echo "Group path: $GROUP_PATH"
echo "Using GPG key: $GPG_KEY_ID"

# Create directories
BUNDLE_DIR="maven-bundle"
rm -rf "$BUNDLE_DIR"  # Clean up any existing bundle directory
mkdir -p "$BUNDLE_DIR/$GROUP_PATH/$ARTIFACT_ID/$VERSION"
TARGET_DIR="$BUNDLE_DIR/$GROUP_PATH/$ARTIFACT_ID/$VERSION"

# Copy AAR file
cp flow-visualiser-1.0.0.aar "$TARGET_DIR/$ARTIFACT_ID-$VERSION.aar"

# Create empty javadoc and sources JARs (required by Maven Central)
jar -cf "$TARGET_DIR/$ARTIFACT_ID-$VERSION-javadoc.jar" -C /tmp .
jar -cf "$TARGET_DIR/$ARTIFACT_ID-$VERSION-sources.jar" -C /tmp .

# Generate POM file
cat > "$TARGET_DIR/$ARTIFACT_ID-$VERSION.pom" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>$GROUP</groupId>
  <artifactId>$ARTIFACT_ID</artifactId>
  <version>$VERSION</version>
  <packaging>aar</packaging>
  <name>Flow Visualiser</name>
  <description>A powerful debugging and visualization tool for Kotlin Flow, StateFlow, LiveData, and other reactive streams in Android applications</description>
  <url>https://github.com/cracken47/Flow-Visualiser</url>
  <licenses>
    <license>
      <name>MIT License</name>
      <url>https://opensource.org/licenses/MIT</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <developers>
    <developer>
      <id>cracken47</id>
      <name>YourName</name>
      <url>https://github.com/cracken47</url>
    </developer>
  </developers>
  <scm>
    <connection>scm:git:git://github.com/cracken47/Flow-Visualiser.git</connection>
    <developerConnection>scm:git:ssh://git@github.com/cracken47/Flow-Visualiser.git</developerConnection>
    <url>https://github.com/cracken47/Flow-Visualiser</url>
  </scm>
</project>
EOF

# Generate MD5 and SHA1 checksums for all files
echo "Generating checksums..."
for file in "$TARGET_DIR"/*.{jar,aar,pom}; do
  if [ -f "$file" ]; then
    # MD5
    if command -v md5sum &> /dev/null; then
      md5sum "$file" | cut -d ' ' -f 1 > "$file.md5"
    elif command -v md5 &> /dev/null; then
      md5 -q "$file" > "$file.md5"
    else
      echo "Warning: md5sum or md5 command not found. MD5 checksums will not be generated."
    fi
    
    # SHA1
    if command -v sha1sum &> /dev/null; then
      sha1sum "$file" | cut -d ' ' -f 1 > "$file.sha1"
    elif command -v shasum &> /dev/null; then
      shasum -a 1 "$file" | cut -d ' ' -f 1 > "$file.sha1"
    else
      echo "Warning: sha1sum or shasum command not found. SHA1 checksums will not be generated."
    fi
  fi
done

# Simple manual signing approach - create detached signatures for all required files
echo "Signing files manually..."
cd "$TARGET_DIR"
for file in *.jar *.aar *.pom; do
  if [ -f "$file" ]; then
    echo "Signing $file"
    # Create a detached signature using standard GPG format
    echo "$GPG_PASSWORD" | gpg --batch --yes --passphrase-fd 0 \
        --default-key "$GPG_KEY_ID" \
        --armor --detach-sign "$file"
    
    # Verify signature was created
    if [ -f "$file.asc" ]; then
      echo "✓ Signature created for $file"
    else
      echo "✗ Failed to create signature for $file"
    fi
  fi
done
cd - > /dev/null

# Create zip bundle
rm -f maven-central-bundle.zip  # Remove any existing zip file
(cd "$BUNDLE_DIR" && zip -r "../maven-central-bundle.zip" .)

echo "Bundle created: maven-central-bundle.zip"
echo "You can now upload this bundle to Sonatype Nexus at https://s01.oss.sonatype.org/"
echo ""
echo "Make sure your GPG key $GPG_KEY_ID is uploaded to a keyserver:"
echo "gpg --keyserver keyserver.ubuntu.com --send-keys $GPG_KEY_ID" 