/**
 * Cola circular eficiente para el orden de turnos
 */
public class Cola {

    private Jugador[] elementos;
    private int frente;
    private int finalCola;
    private int capacidad;
    private int tamano;

    /**
     * Crea la cola con capacidad fija
     */
    public Cola(int capacidad) {
        this.capacidad = capacidad;
        this.elementos = new Jugador[capacidad];
        this.frente = 0;
        this.finalCola = 0;
        this.tamano = 0;
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    public boolean estaLlena() {
        return tamano == capacidad;
    }

    /**
     * Agrega un jugador al final de la cola en O(1)
     */
    public void encolar(Jugador jugador) {
        if (estaLlena()) {
            System.out.println("La cola de turnos esta llena");
            return;
        }

        elementos[finalCola] = jugador;
        finalCola = (finalCola + 1) % capacidad;
        tamano++;
    }

    /**
     * Saca al primer jugador en O(1) sin recorrer el arreglo
     */
    public Jugador desencolar() {
        if (estaVacia()) {
            return null;
        }

        Jugador jugador = elementos[frente];
        elementos[frente] = null;
        frente = (frente + 1) % capacidad;
        tamano--;
        return jugador;
    }
}