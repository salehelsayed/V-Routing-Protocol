@echo off
if not exist "gradle\wrapper" mkdir "gradle\wrapper"
powershell -Command "Invoke-WebRequest -Uri https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar -OutFile gradle/wrapper/gradle-wrapper.jar"
