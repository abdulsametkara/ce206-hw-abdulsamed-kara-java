@echo off
@setlocal enableextensions
@cd /d "%~dp0"

echo Running Application
java --module-path "D:\javafx-sdk-24\lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base -jar music-app\target\music-app-1.0-SNAPSHOT.jar

echo Operation Completed!
pause