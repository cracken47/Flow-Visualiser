# Maven Central Publishing

This directory contains scripts and configuration for publishing the Flow Visualiser library to Maven Central.

## Maven Central Artifact

The Flow Visualiser is now available on Maven Central:

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.github.cracken47:flow-visualiser:1.0.0")
}
```

Or in Groovy:

```groovy
// build.gradle
dependencies {
    implementation 'io.github.cracken47:flow-visualiser:1.0.0'
}
```

## Scripts Overview

The following scripts are included to help with the Maven Central publishing process:

- `create-bundle.sh` - Creates a properly structured bundle for Maven Central upload
- `upload-gpg-key.sh` - Uploads GPG key to public keyservers (required for Maven Central)
- `sign-file.sh` - Helper script to sign individual files for testing
- `check-environment.sh` - Verifies the local environment for Maven Central publishing

## Publishing Instructions

To publish a new version:

1. Update the version number in `gradle.properties`
2. Place the AAR file in the maven-upload directory
3. Upload your GPG key to keyservers
4. Create the bundle using the provided scripts
5. Upload to Sonatype Nexus Repository Manager

For detailed instructions, see the full README.md included in this directory.

## Note to Contributors

The actual AAR files, build artifacts, and GPG keys should not be committed to the repository. These files are listed in the `.gitignore` file. 