import java.util.ArrayList;
import java.util.List;

public class ArbolEvolucion {

    public static class Nodo {
        private Carta carta;
        private List<Nodo> hijos = new ArrayList<>();

        public Nodo(Carta carta) { this.carta = carta; }
    }

    private Nodo raiz;
    private List<Nodo> raices = new ArrayList<>();

    public ArbolEvolucion(Carta cartaInicial) {
        raiz = new Nodo(cartaInicial);
        raices.add(raiz);
    }

    public void agregarRaiz(Carta carta) {
        if (carta != null) raices.add(new Nodo(carta));
    }

    public void agregarEvolucion(String nombreAnterior, Carta evolucion) {
        Nodo anterior = buscarEnRaices(nombreAnterior);
        if (anterior != null && evolucion != null) anterior.hijos.add(new Nodo(evolucion));
    }

    public Carta buscarSiguiente(String nombreActual) {
        Nodo actual = buscarEnRaices(nombreActual);
        if (actual == null || actual.hijos.isEmpty()) return null;
        return actual.hijos.get(0).carta;
    }

    public void recorrer() {
        for (Nodo raizActual : raices) recorrer(raizActual, 0);
    }

    private void recorrer(Nodo nodo, int nivel) {
        if (nodo == null) return;
        for (int i = 0; i < nivel; i++) System.out.print("  ");
        System.out.println(nodo.carta.getNombre());
        for (Nodo hijo : nodo.hijos) recorrer(hijo, nivel + 1);
    }

    private Nodo buscar(Nodo nodo, String nombre) {
        if (nodo == null) return null;
        if (nodo.carta.esCarta(nombre)) return nodo;
        for (Nodo hijo : nodo.hijos) {
            Nodo encontrado = buscar(hijo, nombre);
            if (encontrado != null) return encontrado;
        }
        return null;
    }

    private Nodo buscarEnRaices(String nombre) {
        for (Nodo raizActual : raices) {
            Nodo encontrado = buscar(raizActual, nombre);
            if (encontrado != null) return encontrado;
        }
        return null;
    }
}