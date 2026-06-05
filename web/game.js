const API = "http://localhost:8080/api";

// ── Paleta de colores para fichas de jugadores (máx. 8 jugadores)
const COLORES_FICHA  = ["#f0c040","#ff5577","#40d080","#4d9fff","#c084fc","#fb923c","#34d399","#f472b6"];
// ── Íconos SVG para bots y jugadores humanos
const BOT_AVATARS    = ["<i class='fas fa-robot'></i>","<i class='fas fa-gamepad'></i>","<i class='fas fa-microchip'></i>","<i class='fas fa-brain'></i>","<i class='fas fa-cogs'></i>"];
const P_EMOJIS       = ["<i class='fas fa-smile'></i>","<i class='fas fa-glasses'></i>","<i class='fas fa-star-struck'></i>","<i class='fas fa-party-horn'></i>"];
const CONFETTI_COLS  = ["#f0c040","#ff5577","#40d080","#4d9fff","#c084fc","#fb923c"];

const CATEGORIAS_DISPONIBLES = [
    "Matematicas", "Geografia", "Literatura", "Deportes", "Entretenimiento"
];

// ── Rotaciones CSS 3D para cada cara del dado (1–6)
const DADO_ROTATIONS = {
  1: "rotateX(0deg) rotateY(0deg)",
  2: "rotateX(-90deg) rotateY(0deg)",
  3: "rotateX(0deg) rotateY(-90deg)",
  4: "rotateX(0deg) rotateY(90deg)",
  5: "rotateX(90deg) rotateY(0deg)",
  6: "rotateX(180deg) rotateY(0deg)"
};

// ── Mapa de escaleras: casilla origen → casilla destino (sube)
const ESCALERAS = {
  3: 19,  6: 27, 11: 41, 17: 33, 22: 37, 30: 44, 39: 48, 45: 50
};

// ── Mapa de serpientes: casilla origen → casilla destino (baja)
const SERPIENTES = {
  25: 9, 34: 20, 36: 13, 42: 28, 47: 32, 49: 38
};

// ── DOM ───────────────────────────────────────────────────
// Acceso rápido a elementos del DOM por ID
const $  = id => document.getElementById(id);
const screenInicio  = $("screen-inicio");
const screenJuego   = $("screen-juego");
const btnIniciar    = $("btn-iniciar");
const btnTirar      = $("btn-tirar");
const btnResponder  = $("btn-responder");
const btnReiniciar  = $("btn-reiniciar");
const tableroEl     = $("tablero");
const turnoBanner   = $("turno-banner");
const turnoNombre   = $("turno-nombre");
const turnoTag      = $("turno-tag");
const turnoSub      = $("turno-sub");
const turnoInner    = turnoBanner?.querySelector(".turno-banner-inner");
const miniDot       = $("mini-dot");
const miniTexto     = $("mini-texto");
const botOverlay    = $("bot-overlay");
const botNombreEl   = $("bot-nombre");
const botAcionEl    = $("bot-accion");
const botDadoEl     = $("bot-dado");
const dado3d        = $("dado-3d");
const modalReto     = $("modal-reto");
const modalGanador  = $("modal-ganador");
const modalFeedback = $("modal-feedback");

// ── Estado ────────────────────────────────────────────────
// Variables globales que mantienen el estado de la partida en curso
let estado          = null;       // Último estado recibido del servidor
let nombresHumanos  = [];         // Nombres de jugadores humanos (no bots)
let jugadoresOrden  = [];         // Orden de turno de todos los jugadores
let lastPositions   = {};         // Posición anterior de cada jugador (para animar movimiento)
let procesandoTurno = false;      // Semáforo: evita doble ejecución de turno
let cantJugadores   = 1;          // Cantidad de jugadores humanos seleccionados
let dadoResultadoActual = null;   // Valor del dado en el turno actual

// ═════════════════════════════════════════════════════════
//  PANTALLA INICIO
// ═════════════════════════════════════════════════════════

// Genera partículas decorativas flotantes en el fondo de la pantalla de inicio
(function generarParticulas() {
  const cont = $("particles");
  if (!cont) return;
  for (let i = 0; i < 25; i++) {
    const p = document.createElement("div");
    p.className = "particle";
    p.style.left   = Math.random() * 100 + "%";
    p.style.setProperty("--dur",  (6 + Math.random() * 8) + "s");
    p.style.setProperty("--del", -(Math.random() * 10) + "s");
    p.style.width = p.style.height = (3 + Math.random() * 4) + "px";
    const emojis = ["🎲","⚄","⚅","🪜","🐍","⭐","✦"];
    if (Math.random() > .6) { p.style.fontSize = "14px"; p.textContent = emojis[Math.floor(Math.random() * emojis.length)]; p.style.background = "none"; p.style.borderRadius = "0"; }
    cont.appendChild(p);
  }
})();

// Renderiza los campos de nombre según la cantidad de jugadores seleccionados
function renderInputs() {
  const cont = $("jugadores-inputs");
  cont.innerHTML = "";
  for (let i = 0; i < cantJugadores; i++) {
    const row = document.createElement("div");
    row.className = "player-input-row";
    row.innerHTML = `<span class="p-emoji">${P_EMOJIS[i]}</span>
      <input type="text" class="player-name-input" id="jug-${i}"
        placeholder="Nombre jugador ${i+1}" maxlength="20" autocomplete="off"/>`;
    cont.appendChild(row);
  }
  actualizarBots();
  $("jug-0")?.focus();
}

// Actualiza el selector de bots disponibles según cuántos jugadores humanos hay
// (máximo 5 jugadores en total entre humanos y bots)
function actualizarBots() {
  const sel = $("bots-select");
  sel.innerHTML = "";
  for (let b = 4; b >= 0; b--) {
    if (cantJugadores + b > 5) continue;
    const o = document.createElement("option");
    o.value = b;
    o.textContent = b === 0 ? "Sin bots" : b === 1 ? "1 bot" : `${b} bots`;
    sel.appendChild(o);
  }
}

// Botones de selección de cantidad de jugadores (1–4)
document.querySelectorAll(".cnt-btn").forEach(btn => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".cnt-btn").forEach(b => b.classList.remove("active"));
    btn.classList.add("active");
    cantJugadores = parseInt(btn.dataset.count);
    renderInputs();
  });
});

renderInputs();

btnIniciar.addEventListener("click", iniciar);
// Permite iniciar el juego presionando Enter desde la pantalla de inicio
document.addEventListener("keydown", e => {
  if (e.key === "Enter" && screenInicio.classList.contains("active")) iniciar();
});

// Valida los nombres ingresados y llama al servidor para iniciar la partida
async function iniciar() {
  const nombres = [];
  for (let i = 0; i < cantJugadores; i++) {
    const inp = $(`jug-${i}`);
    const v = inp?.value.trim() || "";
    if (!v) { shake(inp); return; }
    if (nombres.includes(v)) { shake(inp); alert(`"${v}" ya está en uso`); return; }
    nombres.push(v);
  }
  const bots = parseInt($("bots-select").value);
  btnIniciar.disabled = true;
  btnIniciar.querySelector(".btn-play-text").textContent = "CARGANDO...";

  try {
    const data = await apiPost("/iniciar", { jugadores: nombres, bots });
    nombresHumanos = data.humanos || nombres;
    jugadoresOrden = data.jugadores.map(j => j.nombre);
    data.jugadores.forEach(j => { lastPositions[j.nombre] = j.posicion; });
    actualizarEstado(data);
    iniciarPantallaJuego();
  } catch (err) {
    alert("❌ No se pudo conectar con el servidor Java.\nAsegúrate de que GameServer está corriendo en el puerto 8080.\n\n" + err.message);
    btnIniciar.disabled = false;
    btnIniciar.querySelector(".btn-play-text").textContent = "¡JUGAR!";
  }
}

// ═════════════════════════════════════════════════════════
//  API helpers
// ═════════════════════════════════════════════════════════

// Realiza una petición GET al servidor Java y retorna el JSON de respuesta
async function apiGet(ep) {
  const r = await fetch(API + ep);
  if (!r.ok) throw new Error(await r.text());
  return r.json();
}

// Realiza una petición POST al servidor Java con un cuerpo JSON y retorna la respuesta
async function apiPost(ep, body = {}) {
  const r = await fetch(API + ep, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!r.ok) throw new Error(await r.text());
  return r.json();
}

// ═════════════════════════════════════════════════════════
//  PANTALLA JUEGO
// ═════════════════════════════════════════════════════════

// Transición de pantalla inicio → juego; renderiza tablero, jugadores y lanza el primer turno
function iniciarPantallaJuego() {
  screenInicio.classList.remove("active");
  screenJuego.classList.add("active");
  renderTablero();
  renderJugadores();
  renderRanking();
  mostrarBannerTurno(estado, () => {
    if (estado?.esBot) {
      setTimeout(() => procesarTurnoAutomatico(), 800);
    } else {
      habilitarBoton();
    }
  });
}

// Dispara el turno del jugador humano al hacer clic en el botón o en el dado 3D
btnTirar.addEventListener("click", () => {
  if (estado?.ganador || procesandoTurno || estado?.esBot) return;
  ejecutarTurno();
});

dado3d?.addEventListener("click", () => {
  if (estado?.ganador || procesandoTurno || estado?.esBot || btnTirar.disabled) return;
  ejecutarTurno();
});

// ═════════════════════════════════════════════════════════
//  ANIMACIÓN DEL DADO
// ═════════════════════════════════════════════════════════

const DADO_SPIN_MS = 1800; // Duración total del giro del dado en milisegundos

// Anima el dado 3D hasta mostrar el valor final recibido del servidor.
// Retorna una Promise que resuelve cuando la animación termina.
function animarDadoConResultado(valor) {
  return new Promise((resolve) => {
    // Limpia clases y estilos previos antes de iniciar la animación
    dado3d.classList.remove(
      "dado-listo", "dado-agrandado", "rolling-wait", "rolling-land", "rolling", "dado-bounce"
    );
    for (let i = 1; i <= 6; i++) dado3d.classList.remove(`val-${i}`);
    dado3d.style.cssText = "";

    dado3d.classList.add("rolling-wait"); // Fase 1: giro aleatorio

    setTimeout(() => {
      dado3d.classList.remove("rolling-wait");

      dado3d.classList.add("rolling-land"); // Fase 2: aterrizaje en el valor correcto
      dado3d.classList.add(`val-${valor}`);

      setTimeout(() => {
        dado3d.classList.remove("rolling-land");
        dado3d.classList.add("dado-agrandado"); // Fase 3: zoom de énfasis

        setTimeout(() => {
          dado3d.classList.remove("dado-agrandado");
          resolve();
        }, 500);
      }, 400);
    }, DADO_SPIN_MS);
  });
}

// ═════════════════════════════════════════════════════════
//  ANIMACIONES DE ESCALERA Y SERPIENTE
// ═════════════════════════════════════════════════════════

// Muestra un overlay de pantalla completa con animación de subida por escalera.
// Recibe el nombre del jugador, la casilla de inicio y la casilla de destino.
function mostrarAnimacionEscalera(jugadorNombre, desde, hasta) {
  return new Promise((resolve) => {
    const overlay = document.createElement("div");
    overlay.className = "animacion-especial-overlay";
    overlay.innerHTML = `
      <div class="animacion-container escalera-animacion">
        <div class="animacion-fondo"></div>
        <div class="animacion-contenido">
          <div class="jugador-nombre">${jugadorNombre}</div>
          <div class="animacion-icono">🪜</div>
          <div class="animacion-escena">
            <div class="escalera-animada">
              <div class="escalera-peldaños">
                ${Array(8).fill('<div class="peldaño"></div>').join("")}
              </div>
              <div class="jugador-escalando">
                <div class="figura-jugador">🧗</div>
                <div class="particulas-subida"></div>
              </div>
            </div>
          </div>
          <div class="mensaje-especial">
            <span class="desde-casilla">${desde}</span>
            <span class="flecha">⬆️⬆️⬆️</span>
            <span class="hasta-casilla">${hasta}</span>
          </div>
          <div class="texto-accion">¡SUBIO POR UNA ESCALERA!</div>
        </div>
      </div>`;
    document.body.appendChild(overlay);
    setTimeout(() => overlay.classList.add("active"), 10);
    setTimeout(() => { overlay.querySelector(".jugador-escalando")?.classList.add("subiendo"); }, 500);
    // Cierra el overlay tras 4 segundos y resuelve la promesa
    setTimeout(() => {
      overlay.classList.remove("active");
      setTimeout(() => { overlay.remove(); resolve(); }, 800);
    }, 4000);
  });
}

// Muestra un overlay de pantalla completa con animación de bajada por serpiente.
// Recibe el nombre del jugador, la casilla de inicio y la casilla de destino.
function mostrarAnimacionSerpiente(jugadorNombre, desde, hasta) {
  return new Promise((resolve) => {
    const overlay = document.createElement("div");
    overlay.className = "animacion-especial-overlay";
    overlay.innerHTML = `
      <div class="animacion-container serpiente-animacion">
        <div class="animacion-fondo"></div>
        <div class="animacion-contenido">
          <div class="jugador-nombre">${jugadorNombre}</div>
          <div class="animacion-icono">🐍</div>
          <div class="animacion-escena">
            <div class="serpiente-animada">
              <div class="serpiente-cuerpo">
                ${Array(8).fill('<div class="segmento"></div>').join("")}
                <div class="cabeza-serpiente">🐍</div>
              </div>
              <div class="jugador-resbalando">
                <div class="figura-jugador-caida">😨</div>
                <div class="particulas-caida"></div>
              </div>
            </div>
          </div>
          <div class="mensaje-especial">
            <span class="desde-casilla">${desde}</span>
            <span class="flecha">⬇️⬇️⬇️</span>
            <span class="hasta-casilla">${hasta}</span>
          </div>
          <div class="texto-accion texto-peligro">¡CAYÓ EN UNA SERPIENTE!</div>
        </div>
      </div>`;
    document.body.appendChild(overlay);
    setTimeout(() => overlay.classList.add("active"), 10);
    setTimeout(() => { overlay.querySelector(".jugador-resbalando")?.classList.add("cayendo"); }, 500);
    // Cierra el overlay tras 4 segundos y resuelve la promesa
    setTimeout(() => {
      overlay.classList.remove("active");
      setTimeout(() => { overlay.remove(); resolve(); }, 800);
    }, 4000);
  });
}

// Orquesta el turno de un jugador humano:
// 1. Llama al servidor para tirar el dado
// 2. Anima el dado con el resultado real
// 3. Anima el movimiento de la ficha (con escalera/serpiente si aplica)
// 4. Evalúa el estado posterior: ganador, reto o siguiente turno
async function ejecutarTurno() {
  if (procesandoTurno) return;
  procesandoTurno = true;
  btnTirar.disabled = true;
  dado3d.style.cssText = "";
  dado3d.classList.remove("dado-listo");
  const hintEl = $("dado-hint");
  if (hintEl) hintEl.classList.add("oculto");

  // Guarda las posiciones actuales antes de mover (necesario para la animación)
  if (estado?.jugadores) estado.jugadores.forEach(j => { lastPositions[j.nombre] = j.posicion; });

  const jugadorActual = estado?.turnoActual;
  resaltarCasillaJugador(jugadorActual);

  try {
    const data = await apiPost("/tirar");

    const movimiento = data.ultimoMovimiento;
    const dadoVal = (movimiento && movimiento.dado > 0) ? movimiento.dado : 1;
    dadoResultadoActual = dadoVal;

    await animarDadoConResultado(dadoVal);
    await sleep(600);
    await animarMovimientoJugadores(data);
    actualizarEstado(data);

    // Caso 1: hay un ganador → mostrar pantalla de victoria
    if (data.ganador) {
      procesandoTurno = false;
      setTimeout(() => mostrarGanador(data), 1000);
      return;
    }

    // Caso 2: el jugador cayó en una casilla de reto → seleccionar categoría
    if (data.esperandoCategoria) {
      procesandoTurno = false;
      mostrarSeleccionCategoria(data);
      return;
    }

    // Caso 3: ya hay una pregunta activa esperando respuesta
    if (data.esperandoRespuesta) {
      procesandoTurno = false;
      return;
    }

    procesandoTurno = false;

    // Caso 4: el siguiente turno es de un bot → procesar automáticamente
    if (data.esBot) {
      setTimeout(() => procesarTurnoAutomatico(), 1000);
    } else {
      mostrarBannerTurno(data, () => habilitarBoton());
    }
  } catch (err) {
    console.error(err);
    procesandoTurno = false;
    habilitarBoton();
  }
}

// ═════════════════════════════════════════════════════════
//  SELECCIÓN DE CATEGORÍA
// ═════════════════════════════════════════════════════════

// Muestra el modal de selección de categoría cuando el jugador cae en una casilla de reto.
// Al elegir una categoría, solicita la pregunta al servidor y abre el modal de respuesta.
function mostrarSeleccionCategoria(data) {
  const stepCategoria = $("reto-step-categoria");
  const stepPregunta  = $("reto-step-pregunta");
  const categoriaBtns = $("categoria-btns");
  const retoJugadorLabel = $("reto-jugador-label-cat");

  if (!stepCategoria || !categoriaBtns) return;

  stepCategoria.classList.remove("hidden");
  if (stepPregunta) stepPregunta.classList.add("hidden");
  if (retoJugadorLabel) retoJugadorLabel.textContent = data.turnoActual ? `Reto para: ${data.turnoActual}` : "";

  // Genera un botón por cada categoría disponible
  categoriaBtns.innerHTML = "";
  CATEGORIAS_DISPONIBLES.forEach(cat => {
    const btn = document.createElement("button");
    btn.className = "categoria-btn";
    btn.textContent = cat;
    btn.onclick = async () => {
      document.querySelectorAll(".categoria-btn").forEach(b => b.disabled = true);
      try {
        const respuesta = await apiPost("/elegir-categoria", { categoria: cat });
        cerrarModal(modalReto);
        if (respuesta.esperandoRespuesta && respuesta.reto) {
          mostrarPreguntaReto(respuesta.reto, respuesta.turnoActual);
        } else {
          // Sin pregunta disponible para esa categoría → turno saltado
          actualizarEstado(respuesta);
          if (respuesta.esBot) {
            procesarTurnoAutomatico();
          } else {
            mostrarBannerTurno(respuesta, () => habilitarBoton());
          }
        }
      } catch (err) {
        console.error("Error al elegir categoría:", err);
        alert("Error al obtener la pregunta: " + err.message);
        cerrarModal(modalReto);
        const nuevoEstado = await apiGet("/estado");
        actualizarEstado(nuevoEstado);
        if (nuevoEstado.esBot) {
          procesarTurnoAutomatico();
        } else {
          mostrarBannerTurno(nuevoEstado, () => habilitarBoton());
        }
      }
    };
    categoriaBtns.appendChild(btn);
  });

  abrirModal(modalReto);
}

// Muestra el modal con la pregunta del reto, ocultando el paso de selección de categoría
function mostrarPreguntaReto(reto, jugador) {
  $("reto-cat").textContent      = reto.categoria;
  $("reto-nivel").textContent    = "Nivel " + reto.dificultad;
  $("reto-pregunta").textContent = reto.enunciado;
  $("reto-jugador-label").textContent = jugador ? `Reto para: ${jugador}` : "";
  $("reto-respuesta").value      = "";

  $("reto-step-categoria")?.classList.add("hidden");
  $("reto-step-pregunta")?.classList.remove("hidden");

  abrirModal(modalReto);
  setTimeout(() => $("reto-respuesta").focus(), 120);
}

// ═════════════════════════════════════════════════════════
//  ANIMACIÓN DE MOVIMIENTO
// ═════════════════════════════════════════════════════════

// Devuelve el color de ficha asignado a un jugador según su orden en la partida
function obtenerColorJugador(nombre) {
  const index = jugadoresOrden.indexOf(nombre);
  return COLORES_FICHA[index >= 0 ? index : 0];
}

// Resalta brevemente la casilla donde se encuentra el jugador activo
function resaltarCasillaJugador(nombre) {
  const jugador = estado?.jugadores?.find(j => j.nombre === nombre);
  if (jugador) {
    const casillaEl = document.getElementById(`cas-${jugador.posicion}`);
    if (casillaEl) {
      casillaEl.classList.add("casilla-activa");
      setTimeout(() => casillaEl.classList.remove("casilla-activa"), 2000);
    }
  }
}

// Construye la ruta de casillas entre dos posiciones para animar paso a paso
function calcularRuta(desde, hasta) {
  if (desde === hasta) return [desde];
  const ruta = [];
  const dir = hasta > desde ? 1 : -1;
  for (let i = desde; i !== hasta; i += dir) ruta.push(i);
  ruta.push(hasta);
  return ruta;
}

// Calcula el centro de una casilla del tablero en coordenadas relativas al contenedor
function getCasillaCentro(num) {
  const el = $("cas-" + num);
  if (!el) return null;
  const rect = el.getBoundingClientRect();
  const tableroRect = tableroEl.getBoundingClientRect();
  return {
    x: rect.left - tableroRect.left + rect.width / 2,
    y: rect.top  - tableroRect.top  + rect.height / 2
  };
}

// Anima una ficha flotante moviéndola casilla a casilla desde `desde` hasta `hasta`.
// La ficha es un elemento absolutamente posicionado que se crea y se elimina en cada animación.
async function animarFichaMovimiento(jugador, desde, hasta, colorIdx, color) {
  if (desde === hasta) return;

  const tableroRect = tableroEl.getBoundingClientRect();
  const colorFicha  = color || COLORES_FICHA[colorIdx] || COLORES_FICHA[0];

  // Crea el elemento visual de la ficha flotante
  const flotante = document.createElement("div");
  flotante.className = "ficha-flotante";
  flotante.style.cssText = `
    position:absolute;width:clamp(14px,2.2vw,20px);height:clamp(14px,2.2vw,20px);
    border-radius:50%;background:${colorFicha};border:3px solid white;
    box-shadow:0 0 12px ${colorFicha},0 3px 8px rgba(0,0,0,.6);
    pointer-events:none;z-index:100;transition:none;transform:translate(-50%,-50%);`;

  const posInicial = getCasillaCentro(desde);
  if (!posInicial) return;
  flotante.style.left = posInicial.x + "px";
  flotante.style.top  = posInicial.y + "px";

  const tableroWrap = tableroEl.parentElement;
  tableroWrap.style.position = "relative";
  tableroWrap.appendChild(flotante);

  const ruta   = calcularRuta(desde, hasta);
  const stepMs = 150; // Milisegundos por paso de casilla

  // Recorre cada paso de la ruta animando la posición con CSS transition
  for (let i = 1; i < ruta.length; i++) {
    const pos = getCasillaCentro(ruta[i]);
    if (!pos) continue;
    const wrapRect = tableroWrap.getBoundingClientRect();
    flotante.style.transition = `left ${stepMs}ms cubic-bezier(.4,0,.2,1),top ${stepMs}ms cubic-bezier(.4,0,.2,1)`;
    flotante.style.left = (pos.x + tableroRect.left - wrapRect.left) + "px";
    flotante.style.top  = (pos.y + tableroRect.top  - wrapRect.top)  + "px";
    const casEl = $("cas-" + ruta[i]);
    if (casEl) { casEl.classList.add("casilla-paso"); setTimeout(() => casEl.classList.remove("casilla-paso"), stepMs * 1.5); }
    await sleep(stepMs);
  }

  // Efecto de rebote al llegar a la casilla final
  flotante.style.transition = "transform .2s ease";
  flotante.style.transform  = "translate(-50%,-50%) scale(1.5)";
  await sleep(200);
  flotante.style.transform  = "translate(-50%,-50%) scale(1)";
  await sleep(200);
  flotante.remove();
}

// Aplica una animación CSS temporal a la casilla especial (escalera o serpiente)
async function animarEspecial(casilla, tipo) {
  const el = $("cas-" + casilla);
  if (!el) return;
  const cls = tipo === "ESCALERA" ? "casilla-escalera-anim" : "casilla-serpiente-anim";
  el.classList.add(cls);
  await sleep(800);
  el.classList.remove(cls);
}

// Compara posiciones anteriores y nuevas de los jugadores para determinar qué fichas
// deben moverse. Si hay un evento especial (escalera/serpiente), muestra la animación
// correspondiente antes de mover la ficha al destino final.
async function animarMovimientoJugadores(data) {
  if (!data?.jugadores) return;

  const movimiento = data.ultimoMovimiento;
  const movimientos = [];

  // Detecta qué jugadores cambiaron de posición comparando con lastPositions
  data.jugadores.forEach((j, i) => {
    const prev = lastPositions[j.nombre];
    if (prev !== undefined && prev !== j.posicion) {
      const ci = jugadoresOrden.indexOf(j.nombre);
      movimientos.push({ jugador: j, desde: prev, hasta: j.posicion, colorIdx: ci >= 0 ? ci : i });
    }
  });

  for (const { jugador, desde, hasta, colorIdx } of movimientos) {
    const color   = COLORES_FICHA[colorIdx];
    let especial  = null;
    let casillaTrigger   = desde;
    let destinoEspecial  = hasta;

    // Determina si el movimiento involucra una escalera o serpiente
    if (movimiento && movimiento.jugador === jugador.nombre) {
      if      (movimiento.evento === "escalera")  { especial = "ESCALERA"; casillaTrigger = movimiento.desde; destinoEspecial = movimiento.hasta; }
      else if (movimiento.evento === "serpiente") { especial = "SERPIENTE"; casillaTrigger = movimiento.desde; destinoEspecial = movimiento.hasta; }
    } else {
      if      (ESCALERAS[desde] === hasta)  { especial = "ESCALERA"; }
      else if (SERPIENTES[desde] === hasta) { especial = "SERPIENTE"; }
    }

    if (especial) {
      // Movimiento en dos fases: llegar a la casilla trigger, luego mostrar animación y mover al destino
      await animarFichaMovimiento(jugador.nombre, desde, casillaTrigger !== desde ? casillaTrigger : desde, colorIdx, color);
      if (especial === "ESCALERA") await mostrarAnimacionEscalera(jugador.nombre, casillaTrigger, destinoEspecial);
      else                          await mostrarAnimacionSerpiente(jugador.nombre, casillaTrigger, destinoEspecial);
      await animarEspecial(casillaTrigger, especial);
      await sleep(500);
      await animarFichaMovimiento(jugador.nombre, casillaTrigger, hasta, colorIdx, color);
    } else {
      await animarFichaMovimiento(jugador.nombre, desde, hasta, colorIdx, color);
    }

    lastPositions[jugador.nombre] = hasta;
    await sleep(200);
  }
}

// ═════════════════════════════════════════════════════════
//  CICLO DE BOTS
// ═════════════════════════════════════════════════════════

// Ejecuta turnos automáticos consecutivos mientras el turno corresponda a un bot.
// Muestra el overlay de bot con el nombre, ícono y resultado del dado.
// El ciclo se detiene cuando: hay un ganador, toca un humano, o se necesita una respuesta.
async function procesarTurnoAutomatico() {
  while (estado?.esBot && !estado?.ganador && !estado?.esperandoRespuesta && !estado?.esperandoCategoria) {
    if (procesandoTurno) return;
    procesandoTurno = true;

    const nombre   = estado.turnoActual;
    const botIdx   = jugadoresOrden.indexOf(nombre) - nombresHumanos.length;
    const icono    = BOT_AVATARS[Math.max(0, botIdx)];
    const colorJugador = obtenerColorJugador(nombre);

    botNombreEl.innerHTML = icono + " " + nombre;
    botNombreEl.style.color = colorJugador;
    botAcionEl.textContent  = "Pensando...";
    botDadoEl.innerHTML     = "🎲";
    botDadoEl.classList.remove("girar");
    botOverlay.classList.remove("hidden");
    await sleep(800);

    botAcionEl.textContent = "Tirando el dado...";
    botDadoEl.classList.add("girar");

    try {
      // Guarda posiciones antes de la tirada para poder animar el movimiento
      const prev = {};
      estado.jugadores?.forEach(j => { prev[j.nombre] = j.posicion; });
      Object.assign(lastPositions, prev);

      const data = await apiPost("/tirar");

      const movimiento = data.ultimoMovimiento;
      const dadoVal    = (movimiento && movimiento.dado > 0) ? movimiento.dado : 1;

      // Muestra el resultado del dado en el overlay del bot
      botDadoEl.classList.remove("girar");
      const dadosEmojis = ["","⚀","⚁","⚂","⚃","⚄","⚅"];
      botDadoEl.textContent = dadosEmojis[dadoVal];
      botDadoEl.classList.add("bot-dado-animacion");
      setTimeout(() => botDadoEl.classList.remove("bot-dado-animacion"), 500);

      botAcionEl.textContent = descMovimiento(data, nombre, prev);

      await sleep(800);
      await animarMovimientoJugadores(data);

      actualizarEstado(data);
      procesandoTurno = false;
      await sleep(1000);

      if (data.ganador) {
        botOverlay.classList.add("hidden");
        setTimeout(() => mostrarGanador(data), 500);
        return;
      }

    } catch (err) {
      console.error(err);
      procesandoTurno = false;
      botOverlay.classList.add("hidden");
      return;
    }
  }

  // Fin del ciclo de bots: ocultar overlay y pasar el turno al humano
  botOverlay.classList.add("hidden");
  procesandoTurno = false;
  if (!estado || estado.ganador) return;

  mostrarBannerTurno(estado, () => habilitarBoton());
}

// ═════════════════════════════════════════════════════════
//  RESPONDER RETO (HUMANO)
// ═════════════════════════════════════════════════════════

btnResponder?.addEventListener("click", responder);
// Permite enviar la respuesta con Enter además del botón
$("reto-respuesta")?.addEventListener("keydown", e => { if (e.key === "Enter") responder(); });

// Envía la respuesta del jugador humano al servidor, muestra el feedback
// y determina el siguiente paso (turno extra, turno siguiente o bot)
async function responder() {
  const v = $("reto-respuesta").value.trim();
  if (!v) { shake($("reto-respuesta")); return; }
  btnResponder.disabled = true;

  try {
    const data = await apiPost("/responder", { respuesta: v });
    cerrarModal(modalReto);
    $("reto-respuesta").value = "";

    const ok = data.evento === "correcto";

    // Muestra un feedback visual de acierto o error con la respuesta correcta
    mostrarFeedback(
      ok ? "✅" : "❌",
      ok ? "¡Correcto! Tiras de nuevo." : `Incorrecto. Era: "${data.respuestaCorrecta}"`,
      ok ? "correcto" : "incorrecto"
    );

    estado?.jugadores?.forEach(j => { lastPositions[j.nombre] = j.posicion; });
    await sleep(500);
    await animarMovimientoJugadores(data);
    actualizarEstado(data);

    if (data.ganador) {
      setTimeout(() => mostrarGanador(data), 1800);
      return;
    }

    setTimeout(() => {
      if (data.esBot) {
        // El siguiente turno es de un bot
        procesarTurnoAutomatico();
      } else if (ok) {
        // ACIERTO: el mismo jugador humano repite
        mostrarBannerTurnoExtra(data.turnoActual, () => habilitarBoton());
      } else {
        // Fallo: turno al siguiente
        mostrarBannerTurno(data, () => habilitarBoton());
      }
    }, 2000);

  } catch (err) {
    alert("Error: " + err.message);
  } finally {
    btnResponder.disabled = false;
  }
}

// ═════════════════════════════════════════════════════════
//  REINICIAR
// ═════════════════════════════════════════════════════════

// Reinicia la partida: notifica al servidor, limpia el estado local y vuelve a la pantalla de inicio
btnReiniciar?.addEventListener("click", async () => {
  try { await apiPost("/reiniciar"); } catch (_) {}
  cerrarModal(modalGanador);
  screenJuego.classList.remove("active");
  screenInicio.classList.add("active");
  btnIniciar.disabled = false;
  btnIniciar.querySelector(".btn-play-text").textContent = "¡JUGAR!";
  tableroEl.innerHTML = "";
  estado = null;
  lastPositions = {};
  procesandoTurno = false;
  botOverlay.classList.add("hidden");
  turnoBannerOcultar();
  dado3d.style.cssText = "";
  dado3d.className = "dado-3d val-1";
});

// ═════════════════════════════════════════════════════════
//  ESTADO GENERAL
// ═════════════════════════════════════════════════════════

// Actualiza el estado global y re-renderiza todos los componentes visuales.
// También abre automáticamente los modales de reto si el servidor lo indica.
function actualizarEstado(data) {
  estado = data;
  if (data.humanos) nombresHumanos = data.humanos;
  renderTablero();
  renderJugadores();
  renderRanking();
  actualizarHdr();

  if (data.esperandoCategoria) {
    mostrarSeleccionCategoria(data);
  } else if (data.esperandoRespuesta && data.reto) {
    mostrarPreguntaReto(data.reto, data.turnoActual);
  }
}

function esHumano(n) { return nombresHumanos.includes(n); }

// Habilita el dado y el botón de tirar para el jugador humano activo,
// aplica el color del jugador y muestra el hint de interacción
function habilitarBoton() {
  btnTirar.disabled = false;
  dado3d.style.cssText = "";
  dado3d.classList.remove("dado-listo", "rolling-wait", "rolling-land", "dado-agrandado");
  void dado3d.offsetWidth; // Fuerza reflow para reiniciar la animación CSS
  dado3d.classList.add("dado-listo");

  const jugadorActual = estado?.turnoActual;
  const colorJugador  = obtenerColorJugador(jugadorActual);
  const hintEl = $("dado-hint");
  if (hintEl) {
    hintEl.style.color = colorJugador;
    hintEl.textContent = estado?.turnoActual && !estado.esBot
      ? `¡${estado.turnoActual}, haz clic al dado!`
      : "¡Haz clic al dado!";
    hintEl.classList.remove("oculto");
  }
}

// Actualiza el indicador de turno en el header (mini-dot y nombre del jugador)
function actualizarHdr() {
  if (!estado) return;
  const nombre = estado.turnoActual || "";
  const bot    = estado.esBot;
  const ganado = !!estado.ganador;
  const colorJugador = obtenerColorJugador(nombre);

  miniDot.className = "mini-dot" + (ganado ? " espera" : bot ? " bot" : "");
  miniTexto.innerHTML = ganado
    ? `🏆 ${estado.ganador}`
    : `Turno: <span style="color:${colorJugador};font-weight:bold;">${nombre}</span>`;
}

// ═════════════════════════════════════════════════════════
//  BANNERS DE TURNO
// ═════════════════════════════════════════════════════════

let bannerTimer = null;

// Muestra el banner central con el nombre del jugador cuyo turno comienza.
// Se oculta automáticamente tras 2–2.5 segundos y ejecuta el callback.
function mostrarBannerTurno(data, callback) {
  if (!data || data.ganador || data.esperandoRespuesta || data.esperandoCategoria) { callback?.(); return; }
  const nombre = data.turnoActual;
  const bot    = data.esBot;
  const colorJugador = obtenerColorJugador(nombre);

  turnoTag.textContent    = bot ? "TURNO DEL BOT" : "TURNO DE";
  turnoNombre.textContent = nombre.toUpperCase();
  turnoNombre.style.color = colorJugador;
  turnoNombre.style.textShadow = `0 0 20px ${colorJugador}`;
  turnoSub.textContent    = bot ? "Procesando..." : "Presiona el dado para tirar";
  turnoNombre.className   = "turno-banner-nombre" + (bot ? " bot-turn" : "");

  turnoInner?.classList.remove("saliendo");
  turnoBannerMostrar();

  if (bannerTimer) clearTimeout(bannerTimer);
  bannerTimer = setTimeout(() => {
    ocultarBannerConAnimacion(() => { callback?.(); });
  }, bot ? 2000 : 2500);
}

// Muestra el banner de turno extra cuando el jugador acierta un reto
// (el mismo jugador vuelve a tirar)
function mostrarBannerTurnoExtra(nombre, callback) {
  const colorJugador = obtenerColorJugador(nombre);

  turnoTag.textContent    = "🎉 ¡TURNO EXTRA!";
  turnoNombre.textContent = nombre.toUpperCase();
  turnoNombre.style.color = colorJugador;
  turnoNombre.style.textShadow = `0 0 20px ${colorJugador}`;
  turnoSub.textContent    = "¡Respondiste bien! Tira de nuevo.";
  turnoNombre.className   = "turno-banner-nombre";

  turnoInner?.classList.remove("saliendo");
  turnoBannerMostrar();

  if (bannerTimer) clearTimeout(bannerTimer);
  bannerTimer = setTimeout(() => {
    ocultarBannerConAnimacion(() => { callback?.(); });
  }, 2500);
}

// Inicia la animación de salida del banner y ejecuta el callback al terminar
function ocultarBannerConAnimacion(cb) {
  turnoInner?.classList.add("saliendo");
  setTimeout(() => { turnoBannerOcultar(); cb?.(); }, 400);
}

function turnoBannerMostrar() { turnoBanner.classList.remove("hidden"); }
function turnoBannerOcultar() { turnoBanner.classList.add("hidden"); turnoInner?.classList.remove("saliendo"); }

// ═════════════════════════════════════════════════════════
//  TABLERO
// ═════════════════════════════════════════════════════════

// Renderiza el tablero de 50 casillas con el patrón serpentina (izq→der / der→izq).
// Coloca las fichas de los jugadores en sus casillas actuales.
function renderTablero() {
  if (!estado?.tablero) return;
  // Agrupa los índices de jugadores por posición para dibujar fichas apiladas
  const posJ = {};
  estado.jugadores?.forEach((j, i) => { (posJ[j.posicion] ||= []).push(i); });

  tableroEl.innerHTML = "";
  for (let fila = 4; fila >= 0; fila--) {
    const base = fila * 10;
    const ltr  = fila % 2 === 0; // Fila par: izquierda a derecha; impar: derecha a izquierda
    for (let col = 0; col < 10; col++) {
      const colR = ltr ? col : (9 - col);
      const n    = base + colR + 1;
      const d    = estado.tablero[n - 1];
      const div  = document.createElement("div");
      div.className = "casilla tipo-" + d.tipo + (n === 50 ? " meta" : "");
      div.id = "cas-" + n;

      const icons = { ESCALERA:"🪜", SERPIENTE:"🐍", RETO:"❓" };
      div.innerHTML = `<span class="num">${n}</span>`
        + (d.tipo !== "NORMAL" ? `<span class="icono">${icons[d.tipo]||""}</span>` : "")
        + (n === 50 ? `<span class="icono">🏁</span>` : "");

      // Si hay jugadores en esta casilla, dibuja sus fichas
      if (posJ[n]) {
        const fd = document.createElement("div");
        fd.className = "fichas";
        posJ[n].forEach(idx => {
          const f  = document.createElement("div");
          const ci = jugadoresOrden.indexOf(estado.jugadores[idx].nombre);
          f.className = "ficha ficha-" + (ci >= 0 ? ci : idx);
          f.title = estado.jugadores[idx].nombre;
          fd.appendChild(f);
        });
        div.appendChild(fd);
      }
      tableroEl.appendChild(div);
    }
  }
}

// ═════════════════════════════════════════════════════════
//  PANEL JUGADORES
// ═════════════════════════════════════════════════════════

// Renderiza las tarjetas de jugadores en el panel lateral con:
// posición actual, barra de progreso, aciertos y fallos
function renderJugadores() {
  if (!estado?.jugadores) return;
  const cont = $("lista-jugadores");
  cont.innerHTML = "";
  estado.jugadores.forEach((j, i) => {
    const ci    = jugadoresOrden.indexOf(j.nombre);
    const color = COLORES_FICHA[ci >= 0 ? ci : i];
    const activo = j.nombre === estado.turnoActual;
    const hum    = esHumano(j.nombre);
    const progreso = Math.round((j.posicion / 50) * 100);

    const card = document.createElement("div");
    card.className = "jugador-card" + (activo ? " activo" + (!hum ? " bot-activo" : "") : "");
    card.style.borderLeft = activo ? `4px solid ${color}` : "";
    card.innerHTML = `
      <div class="j-top">
        <span class="j-ficha" style="background:${color};box-shadow:0 0 6px ${color}80"></span>
        <span class="j-nombre" style="color:${color}">${j.nombre}</span>
        <span class="j-badge ${hum ? 'hum' : 'bot'}">${hum ? "👤" : "🤖"}</span>
      </div>
      <div class="j-pos">Casilla ${j.posicion} / 50</div>
      <div class="j-bar-bg"><div class="j-bar" style="width:${progreso}%;background:${color}"></div></div>
      <div class="j-stats">
        <span class="j-stat ok">✓ ${j.aciertos}</span>
        <span class="j-stat bad">✗ ${j.fallos}</span>
      </div>`;
    cont.appendChild(card);
  });
  actualizarHistorial();
}

// Obtiene del servidor los últimos 10 eventos del historial y los muestra en el panel
async function actualizarHistorial() {
  try {
    const data = await apiGet("/historial");
    const list = $("historial-list");
    list.innerHTML = "";
    data.historial?.slice(0, 10).forEach(e => {
      const li = document.createElement("li");
      li.textContent = e;
      list.appendChild(li);
    });
  } catch (_) {}
}

// Obtiene el ranking actual del servidor y lo renderiza con medallas para los primeros 3
async function renderRanking() {
  try {
    const data = await apiGet("/ranking");
    const list = $("ranking-list");
    list.innerHTML = "";
    const med = ["🥇","🥈","🥉"];
    data.ranking?.forEach((j, i) => {
      const colorJugador = obtenerColorJugador(j.nombre);
      const li = document.createElement("li");
      li.innerHTML = `<span class="r-pos">${med[i]||(i+1)}</span>
        <span class="r-nombre" style="color:${colorJugador}">${j.nombre}</span>
        <span class="r-casilla">${j.posicion}</span>`;
      list.appendChild(li);
    });
  } catch (_) {}
}

// ═════════════════════════════════════════════════════════
//  MODALES
// ═════════════════════════════════════════════════════════

// Muestra el modal de feedback (acierto/error) y lo cierra automáticamente tras 2 segundos
function mostrarFeedback(emoji, msg, tipo) {
  $("feedback-emoji").textContent = emoji;
  $("feedback-msg").textContent   = msg;
  const card = modalFeedback.querySelector(".feedback-card");
  card.className = "feedback-card " + tipo;
  abrirModal(modalFeedback);
  setTimeout(() => cerrarModal(modalFeedback), 2000);
}

// Muestra el modal de ganador con el ranking final y lanza la animación de confeti
async function mostrarGanador(data) {
  $("ganador-nombre").textContent = data.ganador.toUpperCase();
  const colorGanador = obtenerColorJugador(data.ganador);
  $("ganador-nombre").style.color = colorGanador;
  $("ganador-nombre").style.textShadow = `0 0 20px ${colorGanador}`;

  try {
    const rk = await apiGet("/ranking");
    const rf = $("ranking-final");
    rf.innerHTML = "";
    const ol  = document.createElement("ol");
    ol.style.listStyle = "none";
    const med = ["🥇","🥈","🥉"];
    rk.ranking?.forEach((j, i) => {
      const colorJugador = obtenerColorJugador(j.nombre);
      const li = document.createElement("li");
      li.innerHTML = `<span class="rf-pos">${med[i]||(i+1)}</span>
        <span class="rf-nombre" style="color:${colorJugador}">${j.nombre}</span>
        <span class="rf-casilla">Casilla ${j.posicion}</span>`;
      ol.appendChild(li);
    });
    rf.appendChild(ol);
  } catch (_) {}
  abrirModal(modalGanador);
  lanzarConfetti();
}

// Genera 50 piezas de confeti con colores, tamaños y tiempos aleatorios
function lanzarConfetti() {
  const area = $("confetti-area");
  area.innerHTML = "";
  for (let i = 0; i < 50; i++) {
    const p = document.createElement("div");
    p.className = "confetti-piece";
    p.style.left       = Math.random() * 100 + "%";
    p.style.background = CONFETTI_COLS[i % CONFETTI_COLS.length];
    p.style.width      = (5 + Math.random() * 7) + "px";
    p.style.height     = (5 + Math.random() * 7) + "px";
    p.style.setProperty("--dur", (1.5 + Math.random() * 2.5) + "s");
    p.style.setProperty("--del", (Math.random() * 1.5) + "s");
    area.appendChild(p);
  }
}

// Muestra un modal quitándole la clase "hidden"
function abrirModal(m)  { m.classList.remove("hidden"); }
// Oculta un modal añadiéndole la clase "hidden"
function cerrarModal(m) { m.classList.add("hidden"); }

// Permite cerrar el modal de reto haciendo clic en el fondo oscuro
modalReto?.addEventListener("click", e => { if (e.target === modalReto) cerrarModal(modalReto); });

// ═════════════════════════════════════════════════════════
//  HELPERS
// ═════════════════════════════════════════════════════════

// Genera una descripción textual del movimiento de un bot para mostrar en el overlay
function descMovimiento(data, nombre, prev) {
  const j = data.jugadores?.find(j => j.nombre === nombre);
  if (!j) return "Movió su ficha";
  const p = prev[nombre] || 0;
  const c = j.posicion;
  if (ESCALERAS[p]  === c) return `¡Escalera! Subió a la casilla ${c} 🪜`;
  if (SERPIENTES[p] === c) return `¡Serpiente! Bajó a la casilla ${c} 🐍`;
  if (c > p) return `Avanzó a la casilla ${c}`;
  if (c < p) return `Retrocedió a la casilla ${c}`;
  return `Se quedó en la casilla ${c}`;
}

// Pausa la ejecución asíncrona por `ms` milisegundos
function sleep(ms) { return new Promise(r => setTimeout(r, ms)); }

// Aplica una animación de sacudida a un input inválido para indicar error al usuario
function shake(el) {
  if (!el) return;
  el.style.animation = "none";
  el.offsetWidth; // Fuerza reflow para reiniciar la animación
  el.style.animation = "shake .4s ease";
}