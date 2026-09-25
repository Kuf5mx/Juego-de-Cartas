public class Carta {

    private String nombre;
    private int vida;
    private int vidaMaxima;
    private int danio;
    private boolean esPocion;
    private String tipoCarta;
    private String tipo;
    private int fase;
    private String ataque;
    private boolean ataqueElemental;
    private String habilidad;
    private String estado;
    private int energias;
    private int turnoEntrada;
    private int turnoEvolucion;

    // Constructor para Pokemon
    public Carta(String nombre, int vida, int danio) {
        this(nombre, vida, danio, false, "Normal", 1, "Mordisco", false, null);
    }

    // Constructor general
    public Carta(String nombre, int vida, int danio, boolean esPocion) {
        this(nombre, vida, danio, esPocion, "Normal", 1, "Objeto", false, null);
    }

    public Carta(String nombre, int vida, int danio, boolean esPocion, String tipo,
            int fase, String ataque, boolean ataqueElemental, String habilidad) {
        this.nombre = nombre;
        this.vida = Math.max(0, vida);
        this.vidaMaxima = this.vida;
        this.danio = Math.max(0, danio);
        this.esPocion = esPocion;
        this.tipoCarta = esPocion ? "OBJETO" : "POKEMON";
        this.tipo = tipo;
        this.fase = fase;
        this.ataque = ataque;
        this.ataqueElemental = ataqueElemental;
        this.habilidad = habilidad;
        this.estado = "Ninguno";
        this.energias = 0;
        this.turnoEntrada = 0;
        this.turnoEvolucion = 0;
    }

    public String getNombre() {
        return nombre;
    }

    public int getVida() {
        return vida;
    }

    public int getDanio() {
        return danio;
    }

    public int getVidaMaxima() {
        return vidaMaxima;
    }

    public boolean esPocion() {
        return esPocion;
    }

    public boolean esPokemon() { return !esPocion; }
    public String getTipoCarta() { return tipoCarta; }
    public String getTipo() { return tipo; }
    public int getFase() { return fase; }
    public String getAtaque() { return ataque; }
    public boolean tieneAtaqueElemental() { return ataqueElemental; }
    public String getHabilidad() { return habilidad; }
    public String getEstado() { return estado; }
    public int getEnergias() { return energias; }
    public int getTurnoEntrada() { return turnoEntrada; }
    public int getTurnoEvolucion() { return turnoEvolucion; }
    public boolean esCarta(String nombreCarta) { return nombre.equalsIgnoreCase(nombreCarta); }

    public void prepararParaTablero(int turno) {
        turnoEntrada = turno;
        turnoEvolucion = turno;
    }

    public void agregarEnergia() { energias++; }

    public boolean quitarEnergia(int cantidad) {
        if (cantidad <= 0) return true;
        if (energias < cantidad) return false;
        energias -= cantidad;
        return true;
    }

    public void establecerEnergia(int cantidad) { energias = Math.max(0, cantidad); }
    public boolean puedeEvolucionar(int turnoActual) { return turnoActual > turnoEvolucion + 2; }
    public void registrarEvolucion(int turno) { turnoEvolucion = turno; estado = "Ninguno"; }
    public boolean tieneEstado() { return !"Ninguno".equals(estado); }
    public void aplicarEstado(String nuevoEstado) { if (!tieneEstado()) estado = nuevoEstado; }
    public void curarEstado() { estado = "Ninguno"; }

    public void recibirDanio(int cantidad) {
        if (esPocion || cantidad <= 0) return;
        vida = Math.max(0, vida - cantidad);
    }

    public boolean estaFueraDeCombate() {
        return !esPocion && vida <= 0;
    }

    public void curar(int cantidad) {
        if (esPocion || cantidad <= 0 || estaFueraDeCombate()) return;
        vida = Math.min(vidaMaxima, vida + cantidad);
    }

    public void recibirDanioDeEstado() { recibirDanio(10); }

    @Override
    public String toString() {
        if (esPocion) {
            return "Carta{nombre='" + nombre + "', efecto=" + descripcionObjeto() + "}";
        }
        return "Carta{nombre='" + nombre + "', vida=" + vida + "/" + vidaMaxima
            + ", daño=" + danio + ", tipo=" + tipo + ", fase=" + fase
            + ", energia=" + energias + ", estado=" + estado + "}";
    }

    private String descripcionObjeto() {
        if (nombre.equals("Pocion")) return "cura 20 de vida";
        if (nombre.equals("Superpocion")) return "cura 40 y tiene 50% de quitar 1 energia";
        if (nombre.equals("Pokeball")) return "busca un Pokemon de primera fase";
        if (nombre.equals("Caramelo Raro")) return "permite evolucionar una fase sin esperar";
        return "objeto especial";
    }
}