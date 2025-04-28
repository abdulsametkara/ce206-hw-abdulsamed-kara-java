@echo off
echo JavaFX App Starting...

cd music-app
mvn clean javafx:run -DskipTests

echo App Closed!
pause 