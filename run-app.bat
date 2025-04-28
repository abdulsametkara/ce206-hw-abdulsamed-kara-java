@echo off
echo JavaFX Uygulaması Başlatılıyor...

cd music-app

:: Maven üzerinden derlemek için
echo Uygulama derleniyor...
call mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo Maven derleme hatası! Lütfen Maven'in doğru kurulduğundan emin olun.
    pause
    exit /b %ERRORLEVEL%
)

:: SQLite JDBC sürücüsünü kontrol et
if not exist "%USERPROFILE%\.m2\repository\org\xerial\sqlite-jdbc\3.43.0.0\sqlite-jdbc-3.43.0.0.jar" (
    echo SQLite JDBC sürücüsü indiriliyor...
    call mvn dependency:get -Dartifact=org.xerial:sqlite-jdbc:3.43.0.0
)

:: Uygulamayı çalıştır
echo Uygulama başlatılıyor...
java --module-path "%USERPROFILE%\.m2\repository\org\openjfx\javafx-controls\11.0.2;%USERPROFILE%\.m2\repository\org\openjfx\javafx-fxml\11.0.2;%USERPROFILE%\.m2\repository\org\openjfx\javafx-graphics\11.0.2;%USERPROFILE%\.m2\repository\org\openjfx\javafx-media\11.0.2" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.media -jar target/music-app-1.0-SNAPSHOT.jar

echo Uygulama kapatıldı
pause 