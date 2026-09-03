/**
 * Lista doblemente enlazada para llevar el historial de jugadas
 */
public class ListaDoble {

    private Nodo inicio;
    private Nodo fin;
    private int tamano;

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

    public int size() {
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
            tamano = 1;
            return;
        }

        fin.siguiente = nuevo;
        nuevo.anterior = fin;
        fin = nuevo;
        tamano++;
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