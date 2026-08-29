/**
 * Pila sencilla para guardar cartas.
 */
public class Pila {

    // Arreglo de cartas.
    private Carta[] elementos;
    // Posicion del tope.
    private int tope;
    // Capacidad maxima.
    private int capacidad;

    /**
     * Crea la pila con un tamaño fijo.
     */
    public Pila(int capacidad) {
        this.capacidad = capacidad;
        this.elementos = new Carta[capacidad];
        this.tope = 0;
    }

    // Revisa si la pila esta vacia.
    public boolean estaVacia() {
        return tope == 0;
    }

    // Revisa si la pila ya no cabe.
    public boolean estaLlena() {
        return tope == capacidad;
    }

    /**
     * Mete una carta en la pila.
     */
    public void apilar(Carta carta) {
        if (estaLlena()) {
            System.out.println("La pila está llena");
            return;
        }

        elementos[tope] = carta;
        tope++;
    }

    /**
     * Saca la carta de arriba.
     */
    public Carta desapilar() {
        if (estaVacia()) {
            return null;
        }

        // Bajamos el tope y sacamos la carta.
        tope--;
        Carta carta = elementos[tope];
        elementos[tope] = null;
        return carta;
    }

    /**
     * Mira la carta de arriba sin quitarla.
     */
    public Carta cima() {
        if (estaVacia()) {
            return null;
        }

        return elementos[tope - 1];
    }

    /**
     * Muestra la pila de arriba hacia abajo.
     */
    public void mostrar() {
        if (estaVacia()) {
            System.out.println("(vacia)");
            return;
        }

        // Mostramos de arriba hacia abajo.
        for (int i = tope - 1; i >= 0; i--) {
            System.out.println(elementos[i]);
        }
    }

    // Muestra la pila con numeros, para poder elegir una carta y editarla.
    public void mostrarConNumeros() {
        if (estaVacia()) {
            System.out.println("(vacio)");
            return;
        }

        for (int i = tope - 1; i >= 0; i--) {
            System.out.println((tope - i) + ". " + elementos[i]);
        }
    }

    // Quita la carta con el numero que se vio en mostrarConNumeros().
    public Carta quitarPorIndice(int numero) {
        int posicion = tope - numero;
        if (numero < 1 || posicion < 0 || posicion >= tope) {
            return null;
        }

        Carta carta = elementos[posicion];
        for (int i = posicion; i < tope - 1; i++) {
            elementos[i] = elementos[i + 1];
        }
        tope--;
        elementos[tope] = null;
        return carta;
    }
}