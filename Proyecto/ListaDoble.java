import java.util.ArrayList;
import java.util.List;

/**
 * Lista doblemente enlazada para llevar el historial de jugadas
 */
public class ListaDoble {

    private Nodo inicio;
    private Nodo fin;
    private int tamano;

    private class Nodo {
        Carta carta;
        String evento;
        Nodo anterior;
        Nodo siguiente;

        public Nodo(Carta carta) {
            this.carta = carta;
            this.evento = null;
            this.anterior = null;
            this.siguiente = null;
        }

        public Nodo(String evento) {
            this.carta = null;
            this.evento = evento;
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

    public List<String> obtenerElementos() {
        List<String> elementos = new ArrayList<>();
        Nodo actual = inicio;
        while (actual != null) {
            elementos.add(actual.evento != null ? actual.evento : actual.carta.toString());
            actual = actual.siguiente;
        }
        return elementos;
    }

    public void agregar(Carta carta) {
        if (carta == null) {
            return;
        }

        agregarNodo(new Nodo(carta));
    }

    public void agregarEvento(String evento) {
        if (evento == null || evento.trim().isEmpty()) return;
        agregarNodo(new Nodo(evento));
    }

    private void agregarNodo(Nodo nuevo) {

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
            imprimir(actual);
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
            imprimir(actual);
            actual = actual.anterior;
        }
    }

    private void imprimir(Nodo nodo) {
        System.out.println(nodo.evento != null ? nodo.evento : nodo.carta);
    }
}