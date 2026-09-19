# Handoff para IA / compañero

## Objetivo

Continuar con la parte gráfica del juego sin perder la lógica ya implementada.

## Punto de entrada

- [Proyecto/Main.java](Proyecto/Main.java)
- [Proyecto/Partida.java](Proyecto/Partida.java)

## Lógica central

El modelo funciona en consola, pero está preparado para ser usado por una vista gráfica.

### Entidades clave

- `Carta`: representa cualquier carta y almacena nombre, vida, energía, estado, tipo, fase, ataque y habilidad.
- `Jugador`: tiene mano, activo, banca, puntos, energía y funciones para jugar, evolucionar, atacar y aplicar objetos.
- `Partida`: controla la partida, turnos, reglas, resumen y fin del juego.

### Relaciones

- `Partida` crea y gestiona a los `Jugador`.
- `Jugador` contiene la mano, mazo, descarte, activo y banca.
- `Carta` es la representación básica de cada carta del juego.

## Lo que ya está hecho

- seleccionar jugadores
- personalizar mazos
- robar cartas
- elegir activo y banca inicial
- turno por turno
- energía
- objetos
- evoluciones
- ataques
- estados alterados
- cálculo de ventaja/desventaja según terreno
- fin de la partida
- resumen narrado

## Lo que falta para la GUI

- sprites del Pokémon activo y de la banca
- imágenes de los terrenos
- indicadores de ventaja/desventaja visuales
- animación de ataques
- cartas con nombre, energía, tipo y ataque visibles
- interfaz de mano y selección
- botones para evolucionar, atacar, jugar objeto, etc.

## Recomendación importante

No conviertan la lógica en una GUI completa desde cero. Lo ideal es:

1. usar el modelo actual como fuente de verdad,
2. construir una vista que lea ese estado,
3. y enviar acciones de usuario a los métodos ya implementados.

## Claves para continuar

Cualquier IA o compañero que trabaje con el proyecto puede comenzar revisando:

- [Proyecto/Jugador.java](Proyecto/Jugador.java)
- [Proyecto/Carta.java](Proyecto/Carta.java)
- [Proyecto/Partida.java](Proyecto/Partida.java)

y luego construir la capa visual encima de ellos.

## Compilación verificada

Se compiló correctamente con:

```bash
javac Proyecto/*.java
```

El proyecto quedó en estado de ejecución funcional.
