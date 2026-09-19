# Juego de Cartas Pokémon

Este proyecto es una versión de juego de cartas estilo Pokémon implementada en Java puro. La lógica principal del juego ya está funcionando en consola y está separada para facilitar una transición a una interfaz gráfica.

## Cómo ejecutar

Desde la carpeta principal del proyecto:

```bash
javac Proyecto/*.java
java -cp Proyecto Main
```

## Estructura principal

- [Proyecto/Main.java](Proyecto/Main.java): punto de entrada mínimo.
- [Proyecto/Partida.java](Proyecto/Partida.java): lógica completa de la partida.
- [Proyecto/Jugador.java](Proyecto/Jugador.java): jugador, tablero, mano, energía, ataques, evoluciones y objetos.
- [Proyecto/Carta.java](Proyecto/Carta.java): entidad central de cada carta y estado del Pokémon.
- [Proyecto/Pila.java](Proyecto/Pila.java): mazo y descartes.
- [Proyecto/ListaSimple.java](Proyecto/ListaSimple.java): lista simple para mano.
- [Proyecto/ListaDoble.java](Proyecto/ListaDoble.java): historial doblemente enlazado.
- [Proyecto/ListaCircular.java](Proyecto/ListaCircular.java): estructura disponible, aunque no es la lógica activa del juego.
- [Proyecto/Cola.java](Proyecto/Cola.java): cola para turnos.
- [Proyecto/ColaPrioridad.java](Proyecto/ColaPrioridad.java): cola para habilidades prioritarias.
- [Proyecto/ArbolEvolucion.java](Proyecto/ArbolEvolucion.java): árbol de evolución disponible como referencia o extensión.

## Idea de diseño

La lógica del juego se debe tratar como modelo, no como interfaz. Esto permite reutilizarla desde una app gráfica.

La separación actual es:

- Modelo / reglas: `Partida`, `Jugador`, `Carta`, `Pila`, listas y colas.
- Entrada/salida de consola: `Main` y `Partida` muestran mensajes por terminal.
- UI futura: se puede conectar una capa gráfica sobre el modelo sin reescribir toda la lógica del juego.

## Flujo de partida

1. Se crean dos jugadores.
2. Se arma el mazo de cada uno.
3. Cada jugador elige activo y banca inicial.
4. Se decide el orden de turno.
5. El juego entra al ciclo principal por turnos.
6. En cada turno se puede:
   - robar carta,
   - asignar energía,
   - jugar objeto,
   - evolucionar Pokémon,
   - atacar,
   - retirar activo,
   - terminar turno.
7. Cuando se cumple la condición de victoria, termina la partida.

## Puntos clave para la GUI

La lógica del juego ya expone la información necesaria para una interfaz gráfica:

- `Jugador.getActivo()`
- `Jugador.getPokemonBanca(int indice)`
- `Jugador.getTamanoMano()`
- `Jugador.obtenerCartaDeMano(int indice)`
- `Carta.getNombre()`, `getTipo()`, `getFase()`, `getVida()`, `getVidaMaxima()`, `getEnergias()`, `getEstado()`, `getAtaque()`
- `Carta.esPokemon()`, `Carta.esPocion()`

Esto permite dibujar:

- sprite del Pokémon activo,
- cartas de la mano,
- estado de ventaja/desventaja del terreno,
- flechas o indicadores visuales,
- energía, daño, costo de retirada, nombres y habilidades,
- Pokémon de banca con indicadores de bufos o efectos.

## Recomendación para UI

La capa gráfica debe leer el estado del modelo y dibujarlo, sin modificar reglas internas. Idealmente:

- vista del tablero,
- vista de la mano,
- vista de estadísticas del Pokémon,
- evento de click para seleccionar carta, energía, evolución o ataque.

## Cuidado con la parte gráfica

Para no romper la lógica del juego, la UI no debería reescribir estados directamente. Lo correcto es:

- pedir al modelo la información actual,
- mostrarla visualmente,
- y enviar la acción seleccionada al método correspondiente en `Jugador` o `Partida`.

## Estado actual

La lógica funcional del juego está en consola y compila correctamente. El proyecto está listo para que otra IA o un compañero continúe con la parte visual y la integración de sprites, cartas, indicadores y estados del tablero.
