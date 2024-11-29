@echo off
title CestaFacil
echo Iniciando CestaFacil...

:: Verifica se o Java está instalado
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Erro: Java nao encontrado. Por favor, instale o Java.
    pause
    exit /b 1
)

:: Verifica se existem os diretórios necessários
if not exist "data" mkdir data
if not exist "logs" mkdir logs
if not exist "pdf" mkdir pdf
if not exist "backup" mkdir backup

:: Executa o programa
java -jar CestaFacil.jar

:: Se houver erro, mostra mensagem
if %errorlevel% neq 0 (
    echo.
    echo Erro ao executar o programa.
    pause
)

exit /b 0