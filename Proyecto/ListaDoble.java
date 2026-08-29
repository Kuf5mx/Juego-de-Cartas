/**
 * Lista doble para llevar el historial de jugadas.
 */
public class ListaDoble {

    // Inicio de la lista.
    private Nodo inicio;
    // Final de la lista.
    private Nodo fin;

    // Nodo interno de la lista doble.
    private class Nodo {
        // Carta de la jugada.
        Carta carta;
        // Nodo anterior.
        Nodo anterior;
        // Nodo siguiente.
        Nodo siguiente;

        /**
         * Nodo para recorrer hacia adelante y hacia atras.
         */
        public Nodo(Carta carta) {
            this.carta = carta;
            this.anterior = null;
            this.siguiente = null;
        }
    }

    /**
     * Agrega una carta al historial.
     */
    public void agregar(Carta carta) {
        Nodo nuevo = new Nodo(carta);

        if (inicio == null) {
            inicio = nuevo;
            fin = nuevo;
            return;
        }

        // Pegamos el nodo al final.
        fin.siguiente = nuevo;
        nuevo.anterior = fin;
        fin = nuevo;
    }

    /**
     * Muestra el historial de inicio a fin.
     */
    public void mostrarAdelante() {
        Nodo actual = inicio;
        while (actual != null) {
            System.out.println(actual.carta);
            actual = actual.siguiente;
        }
    }

    /**
     * Muestra el historial de fin a inicio.
     */
    public void mostrarAtras() {
        Nodo actual = fin;
        while (actual != null) {
            System.out.println(actual.carta);
            actual = actual.anterior;
        }
    }
}