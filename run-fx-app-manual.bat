@echo off
echo JavaFX Uygulaması Başlatılıyor...

:: JavaFX SDK yolunu değiştirin (indirdikten sonra)
set JAVAFX_SDK="D:\javafx-sdk-24\lib"

cd music-app
java --module-path %JAVAFX_SDK% --add-modules javafx.controls,javafx.fxml,javafx.media -jar target/music-app-1.0-SNAPSHOT.jar

echo Uygulama Kapatıldı
pause 