# Serpientes & Escaleras

Juego de Serpientes y Escaleras desarrollado en Java con arquitectura cliente-servidor. Incluye multijugador, bots, preguntas de reto y estructuras de datos implementadas manualmente.

## Requisitos

* Java 11 o superior
* Navegador web moderno

Verificar instalación:

```bash
java -version
```

## Ejecución

### Windows

```bat
.\compile-and-run.bat
```


### Ejecución manual

Compilar:

```bash
find src -name "*.java" > sources.txt
javac -d bin @sources.txt
```

Ejecutar:

```bash
java -cp bin servidor.GameServer
```

Abrir en el navegador:

```text
http://localhost:8080
```

## Estructura principal

```text
src/
├── servidor/
│   └── GameServer.java
├── logica/
│   ├── JuegoService.java
│   ├── Tablero.java
│   ├── Turnos.java
│   └── Reglas.java
├── estructuras/
│   ├── ArbolBST.java
│   ├── Cola.java
│   ├── Grafo.java
│   ├── Pila.java
│   └── TablaHash.java
├── modelo/
└── ordenamiento/

web/
├── index.html
├── game.js
└── style.css
```

## Flujo general

1. El navegador carga la interfaz desde `web/`.
2. El frontend se comunica con el servidor mediante solicitudes HTTP.
3. `GameServer` recibe las peticiones y delega la lógica a `JuegoService`.
4. Las reglas del juego actualizan posiciones, turnos, retos y ranking.
5. El estado actualizado se envía nuevamente al frontend.

## Estructuras de datos utilizadas

| Estructura    | Función                  |
| ------------- | ------------------------ |
| Árbol BST     | Gestión de preguntas     |
| Cola circular | Control de turnos        |
| Pila          | Historial de eventos     |
| Tabla Hash    | Búsqueda de jugadores    |
| Grafo         | Escaleras y serpientes   |
| MergeSort     | Ordenamiento del ranking |

## Funcionalidades principales

* Partidas de 1 a 4 jugadores
* Soporte para bots
* Escaleras y serpientes
* Sistema de preguntas y retos
* Historial de movimientos
* Ranking de jugadores
* Determinación automática del ganador

## Punto de entrada

La aplicación inicia desde:

```java
servidor.GameServer
```

Este componente levanta el servidor HTTP, sirve la interfaz web y expone los endpoints necesarios para el juego.
