/**
 * Cola simple para el orden de turnos.
 */
public class Cola {

    // Arreglo para guardar jugadores.
    private Jugador[] elementos;
    // Inicio de la cola.
    private int frente;
    // Posicion final de la cola.
    private int finalCola;
    // Tamano maximo de la cola.
    private int capacidad;

    /**
     * Crea la cola con capacidad fija.
     */
    public Cola(int capacidad) {
        this.capacidad = capacidad;
        this.elementos = new Jugador[capacidad];
        this.frente = 0;
        this.finalCola = 0;
    }

    // Revisa si la cola esta vacia.
    public boolean estaVacia() {
        return frente == finalCola;
    }

    // Revisa si la cola ya se lleno.
    public boolean estaLlena() {
        return finalCola == capacidad;
    }

    /**
     * Agrega un jugador al final de la cola.
     */
    public void encolar(Jugador jugador) {
        if (estaLlena()) {
            System.out.println("La cola de turnos está llena");
            return;
        }

        elementos[finalCola] = jugador;
        finalCola++;
    }

    /**
     * Saca al jugador que va primero.
     */
    public Jugador desencolar() {
        if (estaVacia()) {
            return null;
        }

        // Sacamos al primero y recorremos los demas.
        Jugador jugador = elementos[frente];

        for (int i = frente; i < finalCola - 1; i++) {
            elementos[i] = elementos[i + 1];
        }

        finalCola--;
        elementos[finalCola] = null;
        return jugador;
    }
}