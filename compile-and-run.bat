@echo off
REM compile-and-run.bat — Windows
REM Compila el JuegoEscalera y lanza el servidor HTTP.

setlocal
set ROOT=%~dp0
set SRC=%ROOT%src
set BIN=%ROOT%bin

echo Compilando fuentes...
if not exist "%BIN%" mkdir "%BIN%"

javac -d "%BIN%" ^
  "%SRC%\estructuras\Nodo.java" ^
  "%SRC%\estructuras\ListaDoble.java" ^
  "%SRC%\estructuras\Cola.java" ^
  "%SRC%\estructuras\Pila.java" ^
  "%SRC%\estructuras\TablaHash.java" ^
  "%SRC%\estructuras\Grafo.java" ^
  "%SRC%\estructuras\ArbolBST.java" ^
  "%SRC%\modelo\Casilla.java" ^
  "%SRC%\modelo\Jugador.java" ^
  "%SRC%\modelo\Pregunta.java" ^
  "%SRC%\ordenamiento\MergeSort.java" ^
  "%SRC%\logica\Reglas.java" ^
  "%SRC%\logica\Validador.java" ^
  "%SRC%\logica\Turnos.java" ^
  "%SRC%\logica\Tablero.java" ^
  "%SRC%\logica\JuegoService.java" ^
  "%SRC%\servidor\GameServer.java"

if errorlevel 1 (
  echo ERROR en la compilacion.
  pause
  exit /b 1
)

echo Compilacion exitosa.
echo.
echo Iniciando servidor en http://localhost:8080
echo Abre esa URL en tu navegador para jugar.
echo (Ctrl+C para detener)
echo.

cd /d "%ROOT%"
java -cp "%BIN%" servidor.GameServer
pause
