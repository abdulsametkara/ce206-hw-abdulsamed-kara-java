@echo off
cd /d %~dp0
mvn test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass=com.samet.music.dao.RunImprovedPlaylistDAOTest
pause 