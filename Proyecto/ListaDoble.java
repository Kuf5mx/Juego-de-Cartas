/**
 * Lista doblemente enlazada para llevar el historial de jugadas
 */
public class ListaDoble {

    private Nodo inicio;
    private Nodo fin;

    private class Nodo {
        Carta carta;
        Nodo anterior;
        Nodo siguiente;

        public Nodo(Carta carta) {
            this.carta = carta;
            this.anterior = null;
            this.siguiente = null;
        }
    }

    public boolean estaVacia() {
        return inicio == null;
    }

    /**
     * Agrega una carta al historial en O(1)
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
        nuevo.anterior = fin;
        fin = nuevo;
    }

    public void mostrarAdelante() {
        if (estaVacia()) {
            System.out.println("El historial esta vacio");
            return;
        }

        Nodo actual = inicio;
        while (actual != null) {
            System.out.println(actual.carta);
            actual = actual.siguiente;
        }
    }

    public void mostrarAtras() {
        if (estaVacia()) {
            System.out.println("El historial esta vacio");
            return;
        }

        Nodo actual = fin;
        while (actual != null) {
            System.out.println(actual.carta);
            actual = actual.anterior;
        }
    }
}