@echo off
echo Initializing Gradle project...

:: Download Gradle binary
curl -L -o gradle.zip https://services.gradle.org/distributions/gradle-7.6.1-bin.zip
echo Extracting Gradle...
powershell -command "Expand-Archive -Force gradle.zip ."

:: Create Gradle wrapper
gradle-7.6.1\bin\gradle wrapper

:: Clean up
del gradle.zip
rmdir /s /q gradle-7.6.1

echo Project initialization complete!
pause
