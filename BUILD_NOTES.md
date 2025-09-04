# Build Configuration Notes

## Current Issue

The project currently has a build configuration issue with the MinecraftForge Gradle plugin. The plugin `net.minecraftforge.gradle` version `5.1.+` cannot be resolved from the configured repositories.

## Potential Solutions

1. **Update Plugin Version**: Try using a specific version that's known to work:
   ```gradle
   id 'net.minecraftforge.gradle' version '5.1.48'
   ```

2. **Alternative Repository**: The ForgeGradle plugin might have moved to a different repository. Check the official MinecraftForge documentation for the current setup.

3. **Gradle Version Compatibility**: Ensure the Gradle version (7.5.1) is compatible with the ForgeGradle version being used.

4. **Legacy Plugin Syntax**: As a fallback, you could try using the legacy plugin application syntax in `build.gradle`:
   ```gradle
   buildscript {
       repositories {
           maven { url = 'https://maven.minecraftforge.net' }
           mavenCentral()
       }
       dependencies {
           classpath group: 'net.minecraftforge.gradle', name: 'ForgeGradle', version: '5.1.+', changing: true
       }
   }
   apply plugin: 'net.minecraftforge.gradle'
   ```

## Workflow Status

The GitHub Actions workflows are correctly configured and will work once the Gradle build is fixed. The workflows will:

- ✅ Trigger on main branch updates
- ✅ Set up Java 17 environment
- ✅ Build the project using Gradle
- ✅ Create GitHub releases with proper versioning
- ✅ Upload JAR artifacts to releases

## Next Steps

1. Fix the Gradle plugin resolution issue
2. Test the build locally: `./gradlew clean build`
3. Test the workflow by pushing to main branch or running manually
4. Verify that releases are created with correct artifacts