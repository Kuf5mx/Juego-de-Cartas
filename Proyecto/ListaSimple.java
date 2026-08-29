/**
 * Lista simplemente enlazada para guardar el catalogo de cartas
 */
public class ListaSimple {

    private Nodo inicio;
    private Nodo fin;

    private class Nodo {
        Carta carta;
        Nodo siguiente;

        public Nodo(Carta carta) {
            this.carta = carta;
            this.siguiente = null;
        }
    }

    public boolean estaVacia() {
        return inicio == null;
    }

    /**
     * Agrega una carta al final en O(1) usando la referencia fin
     */
    public void agregar(Carta carta) {
        if (carta == null) {
            return;
        }

        Nodo nuevo = new Nodo(carta);

        if (inicio == null) {
            inicio = nuevo;
            fin = nuevo;
            return;
        }

        fin.siguiente = nuevo;
        fin = nuevo;
    }

    /**
     * Elimina una carta por nombre de forma segura sin riesgo de NullPointerException
     */
    public void eliminar(String nombre) {
        if (inicio == null || nombre == null) {
            return;
        }

        if (inicio.carta != null && nombre.equalsIgnoreCase(inicio.carta.getNombre())) {
            inicio = inicio.siguiente;
            if (inicio == null) {
                fin = null;
            }
            return;
        }

        Nodo actual = inicio;
        while (actual.siguiente != null) {
            Carta cartaSiguiente = actual.siguiente.carta;
            if (cartaSiguiente != null && nombre.equalsIgnoreCase(cartaSiguiente.getNombre())) {
                actual.siguiente = actual.siguiente.siguiente;
                if (actual.siguiente == null) {
                    fin = actual;
                }
                return;
            }
            actual = actual.siguiente;
        }
    }

    public void mostrar() {
        if (estaVacia()) {
            System.out.println("El catalogo esta vacio");
            return;
        }

        Nodo actual = inicio;
        while (actual != null) {
            System.out.println(actual.carta);
            actual = actual.siguiente;
        }
    }
}