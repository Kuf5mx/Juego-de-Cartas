# Diagramas del juego

Estos diagramas documentan el flujo que ejecuta el programa actualmente.

## Flujo de una partida

```mermaid
flowchart TD
    A[Iniciar Main] --> B[Crear catalogo]
    B --> C[Crear GestorPartida]
    C --> D[Crear mazos y jugadores]
    D --> E[Robar cartas iniciales]
    E --> F[Elegir activo y banca]
    F --> G[Tomar jugador de Cola]
    G --> H[Avanzar campo con ListaCircular cada 3 turnos]
    H --> I[Procesar estado y robar carta]
    I --> J{Accion del turno}
    J --> K[Asignar energia]
    J --> L[Jugar carta]
    J --> M[Evolucionar usando ArbolEvolucion]
    J --> N[Retirarse]
    J --> O[Atacar]
    J --> P[Terminar turno]
    K --> Q[Registrar evento en ListaDoble]
    L --> Q
    M --> Q
    N --> Q
    O --> Q
    P --> R{Partida terminada}
    Q --> R
    R -- No --> G
    R -- Si --> S[Consultar historial hacia adelante o atras]
```

## Arbol de evolucion

```mermaid
flowchart TD
    B[Bulbasaur] --> I[Ivysaur] --> V[Venusaur]
    C[Charmander] --> C2[Charmeleon] --> C3[Charizard]
    S[Squirtle] --> W[Wartortle] --> BL[Blastoise]
    P[Pichu] --> PI[Pikachu] --> R[Raichu]
    O[Oddish] --> G[Gloom] --> VI[Vileplume]
    M[Magikarp] --> GY[Gyarados]
```

## Estructuras utilizadas

- `Cola`: organiza el orden de los turnos.
- `ColaPrioridad`: procesa habilidades prioritarias.
- `Pila`: representa mazo y descarte.
- `ListaSimple`: almacena la mano.
- `ListaCircular`: rota el campo de batalla.
- `ListaDoble`: conserva el historial de eventos y cartas jugadas.
- `ArbolEvolucion`: resuelve la siguiente evolucion de cada familia.
