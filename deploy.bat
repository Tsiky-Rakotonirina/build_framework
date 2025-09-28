@echo off
rem deploy.bat - copy build-framework jar to use_framework WEB-INF/lib
setlocal enabledelayedexpansion

rem dossier du script (chemin absolu se termine par backslash)
set SCRIPT_DIR=%~dp0

rem trouve le jar dans target
set JAR=
for %%f in ("%SCRIPT_DIR%target\framework-sprint-*.jar") do (
    set JAR=%%~f
)

if "%JAR%"=="" (
    echo [deploy.bat] Aucun jar trouve dans "%SCRIPT_DIR%target".
    echo Compile d'abord : mvn clean package
    pause
    exit /b 1
)

rem destination relatif : ../use_framework/src/main/webapp/WEB-INF/lib
set DEST_LIB=%SCRIPT_DIR%..\use_framework\src\main\webapp\WEB-INF\lib

rem normaliser chemin (supprime quotes)
call :norm "%DEST_LIB%" DEST_LIB
if not exist "%DEST_LIB%" (
    echo [deploy.bat] Création du dossier %DEST_LIB%
    mkdir "%DEST_LIB%"
    if errorlevel 1 (
        echo [deploy.bat] Erreur: impossible de créer %DEST_LIB%
        pause
        exit /b 1
    )
)

echo [deploy.bat] Copie de "%JAR%" vers "%DEST_LIB%"
copy /Y "%JAR%" "%DEST_LIB%" >nul
if errorlevel 1 (
    echo [deploy.bat] Echec de la copie.
    pause
    exit /b 1
)

echo [deploy.bat] Deployment reussi.
endlocal
exit /b 0

:norm
rem helper pour enlever les guillemets passées
set "%2=%~1"
goto :eof
