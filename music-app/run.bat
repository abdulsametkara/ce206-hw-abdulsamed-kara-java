@echo off
echo Baslaniyor: Music Library Organizer...

:: JavaFX modül yolunu ayarla - Maven dependency yolu
set JAVAFX_PATH=%USERPROFILE%\.m2\repository\org\openjfx

:: JavaFX modül adlarını belirle
set MODULES=javafx-controls;javafx-fxml;javafx-graphics;javafx-media

:: Versiyon bilgisini ayarla
set VERSION=17.0.2

:: Modül yolunu oluştur
set MODULE_PATH=%JAVAFX_PATH%\javafx-controls\%VERSION%\javafx-controls-%VERSION%.jar;%JAVAFX_PATH%\javafx-fxml\%VERSION%\javafx-fxml-%VERSION%.jar;%JAVAFX_PATH%\javafx-graphics\%VERSION%\javafx-graphics-%VERSION%.jar;%JAVAFX_PATH%\javafx-media\%VERSION%\javafx-media-%VERSION%.jar;%JAVAFX_PATH%\javafx-base\%VERSION%\javafx-base-%VERSION%.jar

:: Platform-spesifik dosyaları ekle
set PLATFORM_PATH=%JAVAFX_PATH%\javafx-controls\%VERSION%\javafx-controls-%VERSION%-win.jar;%JAVAFX_PATH%\javafx-fxml\%VERSION%\javafx-fxml-%VERSION%-win.jar;%JAVAFX_PATH%\javafx-graphics\%VERSION%\javafx-graphics-%VERSION%-win.jar;%JAVAFX_PATH%\javafx-media\%VERSION%\javafx-media-%VERSION%-win.jar;%JAVAFX_PATH%\javafx-base\%VERSION%\javafx-base-%VERSION%-win.jar

:: Tüm JAR dosyalarını içeren bir sınıf yolu oluştur
set CLASS_PATH=target\music-app-1.0-SNAPSHOT.jar;%MODULE_PATH%;%PLATFORM_PATH%

:: Java komutunu çalıştır
java --module-path "%MODULE_PATH%;%PLATFORM_PATH%" --add-modules=%MODULES% -cp %CLASS_PATH% com.samet.music.MusicAppFX

echo Uygulama kapandi.
pause 