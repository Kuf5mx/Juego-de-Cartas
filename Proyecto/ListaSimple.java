/**
 * Lista simple para guardar el catalogo de cartas.
 */
public class ListaSimple {

    // Inicio de la lista.
    private Nodo inicio;

    // Nodo interno de la lista.
    private class Nodo {
        // Carta guardada.
        Carta carta;
        // Siguiente nodo.
        Nodo siguiente;

        /**
         * Nodo basico para la lista.
         */
        public Nodo(Carta carta) {
            this.carta = carta;
            this.siguiente = null;
        }
    }

    /**
     * Agrega una carta al final.
     */
    public void agregar(Carta carta) {
        Nodo nuevo = new Nodo(carta);

        if (inicio == null) {
            inicio = nuevo;
            return;
        }

        // Nos movemos hasta el final.
        Nodo actual = inicio;
        while (actual.siguiente != null) {
            actual = actual.siguiente;
        }

        actual.siguiente = nuevo;
    }

    /**
     * Elimina una carta por nombre.
     */
    public void eliminar(String nombre) {
        if (inicio == null) {
            return;
        }

        if (inicio.carta.getNombre().equals(nombre)) {
            inicio = inicio.siguiente;
            return;
        }

        // Buscamos la carta por nombre.
        Nodo actual = inicio;
        while (actual.siguiente != null && !actual.siguiente.carta.getNombre().equals(nombre)) {
            actual = actual.siguiente;
        }

        if (actual.siguiente != null) {
            actual.siguiente = actual.siguiente.siguiente;
        }
    }

    /**
     * Muestra todas las cartas de la lista.
     */
    public void mostrar() {
        Nodo actual = inicio;
        while (actual != null) {
            System.out.println(actual.carta);
            actual = actual.siguiente;
        }
    }
}