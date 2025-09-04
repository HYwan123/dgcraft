# GitHub Actions Workflows

This repository includes two GitHub Actions workflows for automating builds and releases.

## Workflows

### 1. Build and Release (`release.yml`)

**Trigger:** Automatically runs when code is pushed to the `main` branch, or can be triggered manually.

**What it does:**
- Sets up Java 17 environment (required for Minecraft Forge 1.18.2)
- Builds the project using Gradle
- Extracts the project version from `build.gradle`
- Creates a GitHub release with version tag (e.g., `v0.0.1-dev5`)
- Uploads all JAR files from the build output to the release

**Released Artifacts:**
- Main mod JAR: `wan_try-{version}.jar`
- JarJar build (if applicable): `wan_try-1.18.2-{version}.jar`
- Any other JAR files generated during build

### 2. Build Check (`build.yml`)

**Trigger:** Runs on pull requests to `main` branch, or can be triggered manually.

**What it does:**
- Builds the project to ensure code compiles
- Archives build artifacts for review
- Provides CI feedback on pull requests

## Setup Requirements

For the workflows to work properly, ensure:

1. **Gradle Wrapper**: The `gradlew` script must be executable and present
2. **Dependencies**: All Gradle dependencies and repositories must be properly configured
3. **Permissions**: The repository must have Actions enabled with appropriate permissions

## Repository Configuration

The workflows are configured for this Minecraft Forge mod project:
- **Java Version**: 17 (as specified in `build.gradle`)
- **Minecraft Version**: 1.18.2
- **Forge Version**: 40.2.21
- **Archive Base Name**: `wan_try`

## Manual Release

To create a release manually:
1. Go to the "Actions" tab in GitHub
2. Select "Build and Release" workflow
3. Click "Run workflow"
4. Choose the branch (usually `main`)
5. Click "Run workflow"

## Troubleshooting

If builds fail, common issues include:
- Gradle dependency resolution problems
- Missing or incorrect repository configurations
- Version conflicts
- Network connectivity to Maven repositories

Check the Actions logs for detailed error messages and stack traces.