@echo off
REM Script pour exécuter les tests d'intégration sur Windows

setlocal enabledelayedexpansion

echo.
echo ========================================
echo  PFE Exams - Backend Tests
echo ========================================
echo.

REM Vérifier si Docker est disponible
docker --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker n'est pas installé ou n'est pas dans le PATH
    echo [INFO] Veuillez installer Docker Desktop: https://www.docker.com/products/docker-desktop
    exit /b 1
)

echo [OK] Docker détecté
docker --version
echo.

REM Demander quel service tester
echo Sélectionnez un service à tester:
echo 1. answer-service (MongoDB)
echo 2. course-service (MySQL)
echo 3. user-service (H2)
echo 4. exam-service (H2)
echo 5. Tous les services
echo.
set /p choice="Entrez votre choix (1-5): "

setlocal enabledelayedexpansion

if "!choice!"=="1" (
    set service=answer-service
) else if "!choice!"=="2" (
    set service=course-service
) else if "!choice!"=="3" (
    set service=user-service
) else if "!choice!"=="4" (
    set service=exam-service
) else if "!choice!"=="5" (
    set service=all
) else (
    echo [ERROR] Choix invalide
    exit /b 1
)

echo.
echo [INFO] Démarrage des services de test...
echo.

if "!service!"=="answer-service" (
    cd answer-service
    echo [INFO] Démarrage de MongoDB pour answer-service...
    docker-compose up -d
    echo [INFO] Attente du démarrage de MongoDB (5 secondes)...
    timeout /t 5 /nobreak
    echo [INFO] Exécution des tests...
    call mvn clean test -Dspring.profiles.active=test-ci
    echo [INFO] Arrêt des services...
    docker-compose down
    cd ..
) else if "!service!"=="course-service" (
    cd course-service
    echo [INFO] Démarrage de MySQL pour course-service...
    docker-compose up -d
    echo [INFO] Attente du démarrage de MySQL (5 secondes)...
    timeout /t 5 /nobreak
    echo [INFO] Exécution des tests...
    call mvn clean test -Dspring.profiles.active=test-ci
    echo [INFO] Arrêt des services...
    docker-compose down
    cd ..
) else if "!service!"=="user-service" (
    cd user-service
    echo [INFO] Exécution des tests (H2 in-memory)...
    call mvn clean test -Dspring.profiles.active=test-ci
    cd ..
) else if "!service!"=="exam-service" (
    cd exam-service
    echo [INFO] Exécution des tests (H2 in-memory)...
    call mvn clean test -Dspring.profiles.active=test-ci
    cd ..
) else if "!service!"=="all" (
    echo [INFO] Exécution de tous les tests...
    cd answer-service
    docker-compose up -d
    timeout /t 5 /nobreak
    cd ..
    cd course-service
    docker-compose up -d
    timeout /t 5 /nobreak
    cd ..
    
    for /r . %%f in (pom.xml) do (
        cd %%~dpf
        if exist "pom.xml" (
            for %%A in (.) do set "dirname=%%~nxA"
            echo [INFO] Testant !dirname!...
            call mvn clean test -Dspring.profiles.active=test-ci
        )
    )
    
    cd ..\answer-service
    docker-compose down
    cd ..\course-service
    docker-compose down
    cd ..
)

echo.
echo ========================================
echo  Tests terminés!
echo ========================================
echo.
