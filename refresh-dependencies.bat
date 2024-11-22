@echo off
echo Refreshing Gradle dependencies...

REM Clean Gradle cache for JGraphT
echo Cleaning Gradle cache for JGraphT...
rmdir /s /q "%USERPROFILE%\.gradle\caches\modules-2\files-2.1\org.jgrapht" 2>nul

REM Run Gradle refresh
echo Running Gradle refresh...
call gradlew --refresh-dependencies clean build

echo Done! Please refresh your IDE project.
