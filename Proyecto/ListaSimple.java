/**
 * Lista simplemente enlazada para guardar cartas
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

    public int size() {
        int tamano = 0;
        Nodo actual = inicio;
        while (actual != null) {
            tamano++;
            actual = actual.siguiente;
        }
        return tamano;
    }

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

    public Carta obtenerPorIndice(int indice) {
        if (estaVacia()) {
            return null;
        }

        if (indice < 0 || indice >= size()) {
            return null;
        }

        Nodo actual = inicio;
        for (int i = 0; i < indice; i++) {
            actual = actual.siguiente;
        }
        return actual.carta;
    }

    public Carta quitarPorIndice(int indice) {
        if (estaVacia()) {
            throw new ListaVaciaException("No se puede sacar una carta de una lista vacia.");
        }

        if (indice < 0 || indice >= size()) {
            throw new IndexOutOfBoundsException("Indice fuera de rango.");
        }

        if (indice == 0) {
            Carta carta = inicio.carta;
            inicio = inicio.siguiente;
            if (inicio == null) {
                fin = null;
            }
            return carta;
        }

        Nodo actual = inicio;
        for (int i = 0; i < indice - 1; i++) {
            actual = actual.siguiente;
        }

        Carta carta = actual.siguiente.carta;
        actual.siguiente = actual.siguiente.siguiente;

        if (actual.siguiente == null) {
            fin = actual;
        }

        return carta;
    }

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