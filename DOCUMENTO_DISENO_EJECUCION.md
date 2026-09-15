# Documento de diseno y ejecucion

## 1. Organizacion general

`Main` inicia la partida y coordina los turnos. `GestorPartida` concentra el estado global que puede reutilizar una futura interfaz grafica: terrenos, evoluciones e historial. `Carta` y `Jugador` conservan las reglas de cartas, vida, energia, banca y combate.

## 2. Cola de prioridad

`ColaPrioridad` almacena las cartas con habilidad especial del Pokemon activo. Al comenzar el turno, la habilidad se encola con prioridad y se procesa antes de mostrar las acciones del jugador mediante `procesarHabilidades`.

## 3. Tabla hash

`crearCatalogo()` usa `HashMap<String, Carta>` para indexar las cartas por nombre. El catalogo se utiliza para construir los mazos y para construir el `ArbolEvolucion` sin recorrer una lista de cartas para localizar cada Pokemon.

## 4. Arbol y recursividad

`ArbolEvolucion` tiene nodos propios, referencias a hijos y varias raices para representar las familias disponibles. `GestorPartida` agrega estas cadenas:

- Bulbasaur -> Ivysaur -> Venusaur
- Charmander -> Charmeleon -> Charizard
- Squirtle -> Wartortle -> Blastoise
- Pichu -> Pikachu -> Raichu
- Oddish -> Gloom -> Vileplume
- Magikarp -> Gyarados

La busqueda `buscar` se llama a si misma para revisar los hijos. El recorrido `recorrer` tambien es recursivo y visita todas las raices y sus descendientes.

## 5. Lista circular

`GestorPartida` crea una `ListaCircular` con los terrenos. El campo cambia al comenzar los turnos 1, 4, 7 y siguientes mediante `siguienteCampo()`. El campo actual se consulta con `campoActual()` y se usa en las reglas de ataque.

## 6. Historial interactivo

`ListaDoble` conserva los eventos globales de la partida en orden normal e inverso. Durante cada turno existe la opcion `7. Consultar historial de la partida`; consultar el historial no termina el turno y permite volver al menu de acciones. Al finalizar tambien se puede abrir el historial y revisar las cartas jugadas por cada jugador.

## 7. Ejecucion

Desde la carpeta raiz del proyecto:

```text
javac -source 8 -target 8 Proyecto\*.java
java -cp Proyecto Main
```

El primer comando se recomienda porque el entorno de ejecucion usado en la entrega puede ser Java 8.

## 8. Capturas requeridas

Agregar capturas reales de estas ejecuciones antes de entregar:

1. Inicio de la partida y seleccion de activo y banca.
2. Menu de turno mostrando la opcion de consultar historial.
3. Consulta del historial hacia adelante y hacia atras.
4. Evolucion de una familia distinta de Bulbasaur, por ejemplo Charmander -> Charmeleon.
5. Cambio de campo al avanzar tres turnos.
6. Procesamiento de una habilidad especial mediante la cola de prioridad.
