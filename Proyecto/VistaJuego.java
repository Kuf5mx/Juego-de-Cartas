import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javafx.geometry.*;
import javafx.scene.Scene; 
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.application.Platform;

/**
 * AQUI DEBES ESTAR: tablero visual del juego.
 * Las cartas son genericas y de texto hasta que se agreguen imagenes PNG.
 */
public class VistaJuego {
    private static final String[] CARTAS_INICIALES = {
        "Bulbasaur", "Charmander", "Squirtle", "Pichu", "Oddish", "Magikarp",
        "Pidgey", "Clefairy", "Magmar", "Pocion", "Superpocion", "Pokeball",
        "Caramelo Raro"
    };

    private final Random random = new Random();
    private final Map<String, Carta> catalogo = new CatalogoCartas(random).obtenerCartas();
    private final List<String> mazoJugador1 = new ArrayList<>();
    private final List<String> mazoJugador2 = new ArrayList<>();
    private Stage stage;
    private Jugador jugador;
    private Jugador rival;
    private GestorPartida gestor;
    private AccionesGrafica acciones;
    private Jugador turno;
    private Jugador defensor;
    private boolean partidaTerminada;
    private int numeroTurno;
    private boolean energiaUsada;
    private int energiaDisponible;
    private boolean ataqueUsado;
    private Label estado;
    private HBox energiaVisual;
    private ComboBox<String> selectorEvolucion;
    private ComboBox<String> selectorRetirada;
    private String mensajeAccion = "";
    private VBox tablero;
    private VBox detalle;
    private Jugador jugadorSeleccion;
    private int indiceSeleccionado = -1;
    private int indiceManoSeleccionada = -1;

    

    public void mostrar(Stage stage) {
        this.stage = stage;
        stage.setMaximized(true);
        mostrarConfiguracion();
    }

    private void mostrarConfiguracion() {
        TextField nombre1 = new TextField("Jugador 1");
        TextField nombre2 = new TextField("Jugador 2");
        VBox editor1 = crearEditorMazo("Mazo de " + nombre1.getText(), mazoJugador1);
        VBox editor2 = crearEditorMazo("Mazo de " + nombre2.getText(), mazoJugador2);
        nombre1.textProperty().addListener((obs, anterior, nuevo) -> actualizarTituloEditor(editor1, "Mazo de " + nuevo));
        nombre2.textProperty().addListener((obs, anterior, nuevo) -> actualizarTituloEditor(editor2, "Mazo de " + nuevo));

        Button comenzar = new Button("Repartir y elegir activo");
        comenzar.setDefaultButton(true);
        comenzar.setOnAction(event -> iniciarPartida(nombre1.getText(), nombre2.getText()));

        Label etiquetaJugador1 = new Label("Nombre del jugador 1");
        Label etiquetaJugador2 = new Label("Nombre del jugador 2");
        etiquetaJugador1.setStyle("-fx-text-fill: white;");
        etiquetaJugador2.setStyle("-fx-text-fill: white;");
        VBox nombres = new VBox(8, etiquetaJugador1, nombre1, etiquetaJugador2, nombre2);
        HBox editores = new HBox(18, editor1, editor2);
        Label tituloPreparacion = titulo("Preparar partida");
        tituloPreparacion.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        VBox contenido = new VBox(18, tituloPreparacion, nombres, editores, comenzar);
        contenido.setAlignment(Pos.TOP_CENTER);
        contenido.setPadding(new Insets(30));
        java.net.URL portada = getClass().getResource("/imagenes/TecmiMon.png");
        ImageView cabecera = portada == null ? new ImageView() : new ImageView(new Image(portada.toExternalForm()));
        cabecera.setFitWidth(500);
        cabecera.setFitHeight(250);
        cabecera.setPreserveRatio(true);
        HBox encabezado = new HBox(cabecera);
        encabezado.setAlignment(Pos.CENTER);
        VBox raiz = new VBox(12, encabezado, contenido);
        raiz.setAlignment(Pos.TOP_CENTER);
        java.net.URL fondoPortada = getClass().getResource("/imagenes/Portada.png");
        raiz.setStyle(fondoPortada == null ? "-fx-background-color: #050505;"
            : "-fx-background-image: url('" + fondoPortada.toExternalForm()
                + "'); -fx-background-size: cover; -fx-background-position: center;");
        stage.setTitle("Juego de Cartas Pokemon");
        stage.setScene(new Scene(raiz));
        stage.show();
    }

    private VBox crearEditorMazo(String encabezado, List<String> mazo) {
        ComboBox<String> selector = new ComboBox<>();
        List<String> opciones = new ArrayList<>(catalogo.keySet());
        ordenarNombresPorInsercion(opciones);
        opciones.addAll(List.of("Pocion", "Superpocion", "Pokeball", "Caramelo Raro"));
        selector.getItems().addAll(opciones);
        selector.getSelectionModel().selectFirst();
        ListView<String> lista = new ListView<>();
        lista.setPrefHeight(250);
        Label tituloMazo = new Label(encabezado);
        Button agregar = new Button("Agregar");
        Button quitar = new Button("Quitar");
        Button base = new Button("Mazo base");
        base.setOnAction(event -> {
            mazo.clear();
            for (int i = 0; i < 8; i++) mazo.add(CARTAS_INICIALES[i]);
            for (int i = 0; i < 3; i++) mazo.add("Pocion");
            mazo.add("Superpocion");
            mazo.add("Pokeball");
            mazo.add("Caramelo Raro");
            actualizarListaMazo(lista, mazo);
        });
        agregar.setOnAction(event -> {
            if (mazo.size() < 15) {
                mazo.add(selector.getValue());
                actualizarListaMazo(lista, mazo);
            }
        });
        quitar.setOnAction(event -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice >= 0) {
                mazo.remove(indice);
                actualizarListaMazo(lista, mazo);
            }
        });
        base.fire();
        VBox editor = new VBox(8, tituloMazo, selector, new HBox(6, agregar, quitar, base), lista);
        editor.setPrefWidth(500);
        return editor;
    }

    private void ordenarNombresPorInsercion(List<String> nombres) {
        for (int indiceActual = 1; indiceActual < nombres.size(); indiceActual++) {
            String nombreActual = nombres.get(indiceActual);
            int indiceAnterior = indiceActual - 1;
            while (indiceAnterior >= 0 && nombres.get(indiceAnterior).compareTo(nombreActual) > 0) {
                nombres.set(indiceAnterior + 1, nombres.get(indiceAnterior));
                indiceAnterior--;
            }
            nombres.set(indiceAnterior + 1, nombreActual);
        }
    }

    private void actualizarTituloEditor(VBox editor, String texto) {
        if (!editor.getChildren().isEmpty() && editor.getChildren().get(0) instanceof Label) {
            ((Label) editor.getChildren().get(0)).setText(texto);
        }
    }

    private void iniciarPartida(String nombre1, String nombre2) {
        if (!mazoValido(mazoJugador1) || !mazoValido(mazoJugador2)) {
            alerta("Cada jugador necesita 15 cartas, al menos 4 Pokemon y un Pokemon basico.");
            return;
        }
        jugador = crearJugador(nombre1.trim().isEmpty() ? "Jugador 1" : nombre1.trim());
        rival = crearJugador(nombre2.trim().isEmpty() ? "Jugador 2" : nombre2.trim());
        gestor = new GestorPartida(catalogo);
        acciones = new AccionesGrafica(gestor, random);
        cargarMazo(jugador, mazoJugador1);
        cargarMazo(rival, mazoJugador2);
        jugador.robarCartasIniciales(4);
        rival.robarCartasIniciales(4);
        mostrarSeleccionInicial(jugador);
    }

    private Jugador crearJugador(String nombre) {
        return new Jugador(nombre, new Pila(30));
    }

    private void cargarMazo(Jugador destino, List<String> mazoElegido) {
        List<String> cartas = new ArrayList<>(mazoElegido);
        Collections.shuffle(cartas, random);
        for (String nombre : cartas) destino.getMazo().apilar(clonar(cartaPorNombre(nombre)));
    }

    private Carta clonar(Carta carta) {
        if (carta == null) return new Carta("Carta", 0, 0, true);
        return new Carta(carta.getNombre(), carta.getVidaMaxima(), carta.getDanio(), carta.esPocion(),
                carta.getTipo(), carta.getFase(), carta.getAtaque(), carta.tieneAtaqueElemental(), carta.getHabilidad());
    }

    private void prepararCampo(Jugador destino, int turnoInicial) {
        Carta activo = quitarPrimeraBasicaDeMano(destino);
        if (activo != null) {
            activo.prepararParaTablero(turnoInicial);
            destino.ponerCartaEnActivo(activo);
        }
        for (int i = 0; i < 3; i++) {
            Carta banca = quitarPrimeraBasicaDeMano(destino);
            if (banca == null) break;
            banca.prepararParaTablero(turnoInicial);
            destino.ponerCartaEnBanca(banca, i);
        }
    }

    private boolean mazoValido(List<String> mazo) {
        return mazo.size() == 15 && contarPokemones(mazo) >= 4 && contarBasicos(mazo) >= 1;
    }

    private int contarBasicos(List<String> mazo) {
        int total = 0;
        for (String nombre : mazo) {
            Carta carta = cartaPorNombre(nombre);
            if (carta.esPokemon() && carta.getFase() == 1) total++;
        }
        return total;
    }

    private void mostrarSeleccionInicial(Jugador jugadorActual) {
        jugadorSeleccion = jugadorActual;
        indiceSeleccionado = -1;
        VBox raiz = new VBox(14);
        raiz.setPadding(new Insets(24));
        raiz.setAlignment(Pos.TOP_CENTER);
        raiz.setStyle("-fx-background-color: #183642;");
        Label instruccion = new Label(jugadorActual.getNombre()
                + ": elige un Pokemon activo y hasta 3 Pokemon para tu banca.");
        instruccion.setStyle("-fx-text-fill: #f4f1de; -fx-font-size: 18px;");
        ListView<String> mano = new ListView<>();
        mano.setPrefHeight(260);
        mano.getSelectionModel().selectedIndexProperty().addListener((obs, anterior, nuevo) -> {
            indiceSeleccionado = nuevo.intValue();
        });
        Button activo = new Button("Poner como activo");
        Button banca = new Button("Poner en banca");
        Button terminar = new Button("Terminar seleccion");
        Label resumen = new Label();
        activo.setOnAction(event -> ponerSeleccionado(mano, resumen, true));
        banca.setOnAction(event -> ponerSeleccionado(mano, resumen, false));
        terminar.setOnAction(event -> terminarSeleccionInicial());
        raiz.getChildren().addAll(titulo("Preparar campo"), instruccion, mano,
                new HBox(8, activo, banca, terminar), resumen);
        actualizarManoSeleccion(mano, resumen);
        stage.setScene(new Scene(raiz));
    }

    private void actualizarManoSeleccion(ListView<String> mano, Label resumen) {
        mano.getItems().clear();
        for (int i = 0; i < jugadorSeleccion.getTamanoMano(); i++) {
            Carta carta = jugadorSeleccion.obtenerCartaDeMano(i);
            mano.getItems().add((i + 1) + ". Carta " + carta.getNombre()
                    + (carta.esPokemon() ? " | " + carta.getTipo() : " | Objeto"));
        }
        resumen.setText("Activo: " + nombreCarta(jugadorSeleccion.getActivo())
                + " | Banca: " + contarBanca(jugadorSeleccion) + "/3");
    }

    private void ponerSeleccionado(ListView<String> mano, Label resumen, boolean activo) {
        if (indiceSeleccionado < 0) return;
        Carta carta = jugadorSeleccion.obtenerCartaDeMano(indiceSeleccionado);
        if (carta == null || !carta.esPokemon() || carta.getFase() != 1) {
            resumen.setText("Solo puedes elegir Pokemon basicos.");
            return;
        }
        if (activo && jugadorSeleccion.getActivo() != null) {
            resumen.setText("Ya tienes un Pokemon activo.");
            return;
        }
        if (!activo && contarBanca(jugadorSeleccion) >= 3) {
            resumen.setText("La banca ya esta llena.");
            return;
        }
        carta = jugadorSeleccion.sacarCartaDeMano(indiceSeleccionado);
        carta.prepararParaTablero(1);
        boolean colocado = activo ? jugadorSeleccion.ponerCartaEnActivo(carta)
                : colocarEnPrimeraBancaLibre(carta);
        if (!colocado) {
            jugadorSeleccion.getMazo().apilar(carta);
            resumen.setText(activo ? "Ya tienes un activo." : "La banca esta llena.");
        }
        indiceSeleccionado = -1;
        actualizarManoSeleccion(mano, resumen);
    }

    private boolean colocarEnPrimeraBancaLibre(Carta carta) {
        for (int i = 0; i < 3; i++) if (jugadorSeleccion.getPokemonBanca(i) == null) {
            return jugadorSeleccion.ponerCartaEnBanca(carta, i);
        }
        return false;
    }

    private void terminarSeleccionInicial() {
        if (jugadorSeleccion.getActivo() == null) {
            alerta("Debes elegir un Pokemon activo.");
            return;
        }
        if (jugadorSeleccion == jugador) {
            mostrarSeleccionInicial(rival);
        } else {
            turno = jugador;
            defensor = rival;
            indiceManoSeleccionada = -1;
            numeroTurno = 1;
            energiaUsada = false;
            energiaDisponible = 1;
            ataqueUsado = false;
            partidaTerminada = false;
            construirTablero();
        }
    }

    private Carta quitarPrimeraBasicaDeMano(Jugador destino) {
        for (int i = 0; i < destino.getTamanoMano(); i++) {
            Carta carta = destino.obtenerCartaDeMano(i);
            if (carta != null && carta.esPokemon() && carta.getFase() == 1) {
                return destino.sacarCartaDeMano(i);
            }
        }
        return null;
    }

    private void construirTablero() {
        BorderPane raiz = new BorderPane();
        raiz.setPadding(new Insets(14));
        raiz.setStyle("-fx-background-color: #183642;");
        tablero = new VBox(10);
        detalle = new VBox(8);
        detalle.setPadding(new Insets(12));
        detalle.setPrefWidth(350);
        StackPane panelDetalle = crearPanelPokedex(detalle, "/imagenes/Pokedex-2.png");
        estado = new Label();
        estado.setStyle("-fx-text-fill: #f4f1de; -fx-font-size: 14px;");
        ScrollPane tableroDesplazable = new ScrollPane(tablero);
        tableroDesplazable.setFitToWidth(true);
        tableroDesplazable.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tableroDesplazable.setStyle("-fx-background: #183642; -fx-background-color: #183642;");
        raiz.setLeft(crearAcciones());
        raiz.setCenter(tableroDesplazable);
        raiz.setRight(panelDetalle);
        mensajeAccion = acciones.procesarHabilidadesInicioTurno(turno, defensor);
        refrescar();
        stage.setScene(new Scene(raiz));
        Platform.runLater(this::mostrarNotificacionesEstado);
    }

    private void refrescar() {
        actualizarFondoTerreno();
        tablero.getChildren().clear();
        Region separador = new Region();
        separador.setPrefHeight(6);
        separador.setMaxWidth(Double.MAX_VALUE);
        separador.setStyle("-fx-background-color: #000000;");
        tablero.getChildren().addAll(titulo("Campo: " + gestor.campoActual()),
            crearZonaJugador(defensor, false), separador, crearZonaJugador(turno, true), estado);
        estado.setText("Turno " + numeroTurno + " de " + turno.getNombre()
            + " | Energia: " + energiaDisponible
                + " | Ataque: " + (ataqueUsado ? "usado" : "disponible")
                + (mensajeAccion.isEmpty() ? "" : " | " + mensajeAccion));
        actualizarEnergiaVisual();
    }

    private void actualizarFondoTerreno() {
        String archivo = switch (gestor.campoActual()) {
            case "Llanura de Fuego" -> "Llanura de fuego.png";
            case "Bosque de Pasto" -> "Bosque de pasto.png";
            case "Oceano de Agua" -> "Oceano de agua.png";
            case "Valle de Rayo" -> "Valle de rayo.png";
            default -> null;
        };
        java.net.URL recurso = archivo == null ? null
                : getClass().getResource("/imagenes/terrenos/" + archivo);
        tablero.setPadding(new Insets(16));
        tablero.setStyle(recurso == null ? "-fx-background-color: #183642;"
                : "-fx-background-image: url('" + recurso.toExternalForm()
                        + "'); -fx-background-size: cover; -fx-background-position: center;");
    }

private VBox crearZonaJugador(Jugador jugadorVista, boolean esJugador) {
    VBox zona = new VBox(6);
    zona.setPadding(new Insets(8));
    zona.setStyle("-fx-background-color: transparent;");

    Label nombre = new Label(
            jugadorVista.getNombre() + " | Puntos: " + jugadorVista.getPuntos()
    );
    nombre.setStyle(
            "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;"
    );
        Label indicadorTerreno = crearIndicadorTerreno(jugadorVista);
        Label indicadorVentaja = crearIndicadorVentaja(jugadorVista);

    HBox banca = new HBox(14);
    banca.setAlignment(Pos.CENTER);

    for (int i = 0; i < 3; i++) {
        banca.getChildren().add(
                crearCartaTablero(jugadorVista.getPokemonBanca(i))
        );
    }
    HBox conexionesBanca = crearConexionesBanca(jugadorVista);

    HBox activo = new HBox(crearEspacioActivo(jugadorVista));
    activo.setAlignment(Pos.CENTER);

    if (esJugador) {
        // JUGADOR 1:
        // Activo arriba y banca abajo.
        zona.getChildren().addAll(
                nombre,
            indicadorTerreno,
                indicadorVentaja,
                new Label("ACTIVO"),
                activo,
                conexionesBanca,
                new Label("BANCA"),
                banca
        );

        // La mano del jugador queda debajo de su campo.
        zona.getChildren().add(crearMano());

    } else {
        // JUGADOR 2:
        // Banca arriba y activo abajo.
        zona.getChildren().addAll(
                nombre,
            indicadorTerreno,
                indicadorVentaja,
                new Label("BANCA"),
                banca,
                conexionesBanca,
                new Label("ACTIVO"),
                activo
        );
    }

    return zona;
}

    private HBox crearConexionesBanca(Jugador jugadorVista) {
        HBox conexiones = new HBox(14);
        conexiones.setAlignment(Pos.CENTER);
        for (int i = 0; i < 3; i++) {
            StackPane espacio = new StackPane();
            espacio.setPrefSize(155, 24);
            if (acciones.bancaPotenciaActivo(jugadorVista, i)) {
                Region linea = new Region();
                linea.setPrefSize(5, 24);
                linea.setMaxSize(5, 24);
                linea.setStyle("-fx-background-color: #4cff79;"
                        + "-fx-effect: dropshadow(gaussian, #4cff79, 12, 0.8, 0, 0);");
                espacio.getChildren().add(linea);
            }
            conexiones.getChildren().add(espacio);
        }
        return conexiones;
    }

    private Label crearIndicadorTerreno(Jugador jugadorVista) {
        Carta activo = jugadorVista.getActivo();
        if (activo == null) return new Label();

        String campo = gestor.campoActual();
        boolean favorable = (campo.contains("Fuego") && "Fuego".equals(activo.getTipo()))
                || (campo.contains("Pasto") && "Planta".equals(activo.getTipo()))
                || (campo.contains("Agua") && "Agua".equals(activo.getTipo()))
                || (campo.contains("Rayo") && "Rayo".equals(activo.getTipo()));
        boolean desfavorable = (campo.contains("Fuego") && "Planta".equals(activo.getTipo()))
                || (campo.contains("Pasto") && "Agua".equals(activo.getTipo()))
                || (campo.contains("Agua") && "Fuego".equals(activo.getTipo()))
                || (campo.contains("Rayo") && "Agua".equals(activo.getTipo()));
        if (favorable) {
            Label indicador = new Label("↑ Terreno favorable: +10 daño");
            indicador.setStyle("-fx-text-fill: #4cff79; -fx-font-weight: bold;");
            return indicador;
        }
        if (desfavorable) {
            Label indicador = new Label("↓ Terreno desfavorable: rival +10 daño");
            indicador.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
            return indicador;
        }
        return new Label();
    }

    private Label crearIndicadorVentaja(Jugador jugadorVista) {
        Jugador oponente = jugadorVista == turno ? defensor : turno;
        Carta activo = jugadorVista.getActivo();
        Carta activoRival = oponente == null ? null : oponente.getActivo();
        if (activo == null || activoRival == null) return new Label();

        boolean ventaja = ("Planta".equals(activo.getTipo()) && "Agua".equals(activoRival.getTipo()))
                || ("Agua".equals(activo.getTipo()) && "Fuego".equals(activoRival.getTipo()))
                || ("Fuego".equals(activo.getTipo()) && "Planta".equals(activoRival.getTipo()))
                || ("Rayo".equals(activo.getTipo()) && "Agua".equals(activoRival.getTipo()));
        if (!ventaja) return new Label();

        Label indicador = new Label("↑ Ventaja elemental: +10 daño");
        indicador.setStyle("-fx-text-fill: #4cff79; -fx-font-weight: bold;");
        return indicador;
    }

    private VBox crearEspacioActivo(Jugador propietario) {
        Carta carta = propietario.getActivo();
        VBox espacio = new VBox(4);
        espacio.setAlignment(Pos.CENTER);
        if (carta != null) espacio.getChildren().add(crearSprite(carta));
        espacio.getChildren().add(cartaVisual(carta, 190, 170));
        Label etiqueta = new Label("ACTIVO");
        etiqueta.setStyle("-fx-text-fill: #ffd166; -fx-font-weight: bold;");
        espacio.getChildren().add(0, etiqueta);
        if (carta != null) {
            espacio.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    mostrarDatosCompletos(carta);
                } else {
                    mostrarDetalle(carta);
                }
            });
        }
        return espacio;
    }

private StackPane crearSprite(Carta carta) {
    StackPane contenedor = new StackPane();
    contenedor.setPrefSize(180, 125);

    if (carta == null) {
        return contenedor;
    }

    String nombrePokemon = carta.getNombre();

    // Nombre exacto del archivo PNG correspondiente a cada Pokemon
    String archivoSprite;

    switch (nombrePokemon) {
        case "Bulbasaur":
            archivoSprite = "Bulbasaur.png";
            break;

        case "Ivysaur":
            archivoSprite = "Ivisaur.png";
            break;

        case "Venusaur":
            archivoSprite = "Venasaur.png";
            break;

        case "Charmander":
            archivoSprite = "Charmander.png";
            break;

        case "Charmeleon":
            archivoSprite = "Charmeleon.png";
            break;

        case "Charizard":
            archivoSprite = "Charizard.png";
            break;

        case "Squirtle":
            archivoSprite = "Squirtle.png";
            break;

        case "Wartortle":
            archivoSprite = "Wartortle.png";
            break;

        case "Blastoise":
            archivoSprite = "Blastois.png";
            break;

        case "Pichu":
            archivoSprite = "Pichu.png";
            break;

        case "Pikachu":
            archivoSprite = "Pikachu.png";
            break;

        case "Raichu":
            archivoSprite = "Raichu.png";
            break;

        case "Oddish":
            archivoSprite = "Odysh.png";
            break;

        case "Gloom":
            archivoSprite = "Gloom.png";
            break;

        case "Vileplume":
            archivoSprite = "Vileplume.png";
            break;

        case "Magikarp":
            archivoSprite = "Magikarp.png";
            break;

        case "Gyarados":
            archivoSprite = "Gyarados.png";
            break;

        case "Pidgey":
            archivoSprite = "Pidgey.png";
            break;

        case "Clefairy":
            archivoSprite = "Clefa.png";
            break;

        case "Magmar":
            archivoSprite = "Magmar.png";
            break;

        default:
            archivoSprite = null;
            break;
    }

    if (archivoSprite == null) {
        Label sustituto = new Label("SPRITE\n" + nombrePokemon);
        sustituto.setAlignment(Pos.CENTER);
        sustituto.setStyle(
                "-fx-text-fill: #f4f1de;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-color: #355070;" +
                "-fx-background-radius: 10;"
        );
        sustituto.setPrefSize(160, 105);
        contenedor.getChildren().add(sustituto);
        return contenedor;
    }

    String ruta = "/imagenes/sprites/" + archivoSprite;

    java.net.URL recurso = getClass().getResource(ruta);

    if (recurso == null) {
        Label sustituto = new Label("SPRITE\n" + nombrePokemon);
        sustituto.setAlignment(Pos.CENTER);
        sustituto.setStyle(
                "-fx-text-fill: #f4f1de;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-color: #355070;" +
                "-fx-background-radius: 10;"
        );
        sustituto.setPrefSize(160, 105);
        contenedor.getChildren().add(sustituto);
        return contenedor;
    }

    ImageView imagen = new ImageView(new Image(recurso.toExternalForm()));
    imagen.setFitWidth(175);
    imagen.setFitHeight(120);
    imagen.setPreserveRatio(true);

    contenedor.getChildren().add(imagen);

    return contenedor;
}

    private VBox crearCartaTablero(Carta carta) {
        VBox visual = cartaVisual(carta, 155, 150);
        if (carta != null && carta == turno.getActivo()) {
            visual.setStyle(visual.getStyle() + "-fx-border-color: #ffd166; -fx-border-width: 4;");
        }
        if (carta != null) {
            visual.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    mostrarDatosCompletos(carta);
                } else {
                    mostrarDetalle(carta);
                }
            });
        }
        return visual;
    }

    private HBox crearMano() {
        HBox mano = new HBox(8);
        mano.setAlignment(Pos.CENTER);
        for (int i = 0; i < turno.getTamanoMano(); i++) {
            final int indice = i;
            Carta carta = turno.obtenerCartaDeMano(i);
            VBox visual = cartaVisual(carta, 125, 115);
            if (indice == indiceManoSeleccionada) {
                visual.setStyle(visual.getStyle() + "-fx-border-color: #ffd166; -fx-border-width: 4;");
            }
            visual.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    mostrarDatosCompletos(carta);
                } else {
                    indiceManoSeleccionada = indice;
                    visual.setStyle(visual.getStyle()
                            + "-fx-border-color: #ffd166; -fx-border-width: 4;");
                    mostrarDetalle(carta);
                }
            });
            mano.getChildren().add(visual);
        }
        return mano;
    }

    private VBox cartaVisual(Carta carta, double ancho, double alto) {
        VBox visual = new VBox(4);
        visual.setAlignment(Pos.CENTER);
        visual.setPrefSize(ancho, alto);
        visual.setPadding(new Insets(7));
        visual.setStyle("-fx-background-color: #f4f1de; -fx-background-radius: 7;"
                + "-fx-border-color: #283044; -fx-border-width: 2; -fx-border-radius: 7;");
        if (carta == null) {
            visual.getChildren().add(new Label("Vacio"));
            return visual;
        }
        java.net.URL recursoCarta = getClass().getResource("/imagenes/cartas/" + nombreArchivoCarta(carta));
        if (recursoCarta != null) {
            ImageView imagenCarta = new ImageView(new Image(recursoCarta.toExternalForm()));
            imagenCarta.setFitWidth(ancho - 8);
            imagenCarta.setFitHeight(alto - 8);
            imagenCarta.setPreserveRatio(true);
            StackPane cartaConEnergia = new StackPane(imagenCarta);
            if (carta.esPokemon() && carta.getEnergias() > 0) {
                HBox energias = new HBox(2);
                energias.setPadding(new Insets(4));
                for (int i = 0; i < carta.getEnergias(); i++) {
                    energias.getChildren().add(crearIconoEnergia(20));
                }
                cartaConEnergia.getChildren().add(energias);
                StackPane.setAlignment(energias, Pos.BOTTOM_RIGHT);
            }
            if (carta.estaFueraDeCombate()) {
                java.net.URL recursoMuerto = getClass().getResource("/imagenes/Muerto.png");
                if (recursoMuerto != null) {
                    ImageView marcaMuerto = new ImageView(new Image(recursoMuerto.toExternalForm()));
                    marcaMuerto.setFitWidth(ancho * 0.65);
                    marcaMuerto.setFitHeight(alto * 0.65);
                    marcaMuerto.setPreserveRatio(true);
                    cartaConEnergia.getChildren().add(marcaMuerto);
                }
            }
            visual.getChildren().add(cartaConEnergia);
            return visual;
        }
        Label nombre = new Label("Carta\n" + carta.getNombre());
        nombre.setWrapText(true);
        nombre.setAlignment(Pos.CENTER);
        nombre.setStyle("-fx-font-weight: bold; -fx-text-alignment: center;");
        Label datos = new Label(carta.esPokemon()
                ? carta.getTipo() + " | Vida " + carta.getVida() + "/" + carta.getVidaMaxima()
                : "Objeto");
        visual.getChildren().addAll(nombre, datos);
        if (carta.esPokemon()) {
            HBox energias = new HBox(2);
            energias.setAlignment(Pos.CENTER);
            for (int i = 0; i < carta.getEnergias(); i++) {
                energias.getChildren().add(crearIconoEnergia(20));
            }
            visual.getChildren().add(energias);
        }
        return visual;
    }

    private String nombreArchivoCarta(Carta carta) {
        return switch (carta.getNombre()) {
            case "Ivysaur" -> "Carta Ivysaur.png";
            case "Venusaur" -> "Carta Venasaur.png";
            case "Blastoise" -> "Carta Blastois.png";
            case "Oddish" -> "Carta Oddysh.png";
            case "Clefairy" -> "Carta Clefayri.png";
            default -> "Carta " + carta.getNombre() + ".png";
        };
    }

    private StackPane crearAcciones() {
        Button energiaActivo = new Button("Energia al activo", crearIconoEnergia(24));
        Button energiaBanca1 = new Button("Energia a banca 1", crearIconoEnergia(24));
        Button energiaBanca2 = new Button("Energia a banca 2", crearIconoEnergia(24));
        Button energiaBanca3 = new Button("Energia a banca 3", crearIconoEnergia(24));
        Button jugar = new Button("Jugar carta seleccionada");
        selectorEvolucion = new ComboBox<>();
        selectorEvolucion.getItems().addAll("Activo", "Banca 1", "Banca 2", "Banca 3");
        selectorEvolucion.getSelectionModel().selectFirst();
        Button evolucionar = new Button("Evolucionar objetivo");
        selectorRetirada = new ComboBox<>();
        selectorRetirada.getItems().addAll("Banca 1", "Banca 2", "Banca 3");
        selectorRetirada.getSelectionModel().selectFirst();
        Button retirar = new Button("Retirar activo");
        Button atacar = new Button("Atacar");
        Button terminar = new Button("Terminar turno");
        Button reiniciar = new Button("Nueva partida");
        Button historial = new Button("Historial de partida");

        energiaActivo.setOnAction(event -> asignarEnergia(-1));
    energiaBanca1.setOnAction(event -> asignarEnergia(0));
    energiaBanca2.setOnAction(event -> asignarEnergia(1));
    energiaBanca3.setOnAction(event -> asignarEnergia(2));
        jugar.setOnAction(event -> jugarPrimeraCarta());
        evolucionar.setOnAction(event -> evolucionarObjetivo());
        retirar.setOnAction(event -> ejecutarRetirada());
        atacar.setOnAction(event -> ejecutarAtaque());
        terminar.setOnAction(event -> terminarTurno());
        reiniciar.setOnAction(event -> mostrarConfiguracion());
        historial.setOnAction(event -> mostrarHistorial());

    energiaVisual = new HBox(6);
    energiaVisual.setAlignment(Pos.CENTER);

    GridPane botones = new GridPane();
    botones.setHgap(8);
    botones.setVgap(8);
    botones.add(energiaActivo, 0, 0);
    botones.add(energiaBanca1, 0, 1);
    botones.add(energiaBanca2, 0, 2);
    botones.add(energiaBanca3, 0, 3);
    botones.add(jugar, 0, 4);
    botones.add(selectorEvolucion, 0, 5);
    botones.add(evolucionar, 0, 6);
    botones.add(selectorRetirada, 0, 7);
    botones.add(retirar, 0, 8);
    botones.add(atacar, 0, 9);
    botones.add(terminar, 0, 10);
    botones.add(reiniciar, 0, 11);

    for (Button boton : new Button[] {energiaActivo, energiaBanca1, energiaBanca2,
        energiaBanca3, jugar, evolucionar, retirar, atacar, terminar, reiniciar, historial}) {
        boton.setMaxWidth(Double.MAX_VALUE);
    }
    GridPane.setHgrow(energiaActivo, Priority.ALWAYS);

    Region espacioHistorial = new Region();
    espacioHistorial.setPrefHeight(18);
    VBox controles = new VBox(10, new Label("Acciones"), energiaVisual, botones, espacioHistorial, historial);
    controles.setAlignment(Pos.TOP_CENTER);
    controles.setPadding(new Insets(10));
    controles.setPrefWidth(300);
    return crearPanelPokedex(controles, "/imagenes/Pokedex-1.png");
    }

    private StackPane crearPanelPokedex(Region contenido, String ruta) {
        StackPane panel = new StackPane();
        java.net.URL recurso = getClass().getResource(ruta);
        if (recurso != null) {
            ImageView pokedex = new ImageView(new Image(recurso.toExternalForm()));
            pokedex.setManaged(false);
            pokedex.fitWidthProperty().bind(panel.widthProperty());
            pokedex.fitHeightProperty().bind(panel.heightProperty());
            pokedex.setPreserveRatio(false);
            panel.getChildren().add(pokedex);
        } else {
            panel.setStyle("-fx-background-color: #f4f1de;");
        }
        contenido.setStyle("-fx-background-color: transparent;");
        panel.getChildren().add(contenido);
        StackPane.setAlignment(contenido, Pos.TOP_CENTER);
        return panel;
    }

    private void actualizarEnergiaVisual() {
        if (energiaVisual == null) return;
        energiaVisual.getChildren().clear();
        for (int i = 0; i < energiaDisponible; i++) {
            energiaVisual.getChildren().add(crearIconoEnergia(32));
        }
        if (energiaDisponible == 0) energiaVisual.getChildren().add(new Label("Sin energia disponible"));
    }

    private ImageView crearIconoEnergia(double tamano) {
        java.net.URL recurso = getClass().getResource("/imagenes/energia.png");
        ImageView icono;
        if (recurso != null) {
            icono = new ImageView(new Image(recurso.toExternalForm()));
            icono.setPreserveRatio(true);
            icono.setFitWidth(tamano);
            icono.setFitHeight(tamano);
        } else {
            icono = new ImageView();
            icono.setFitWidth(tamano);
            icono.setFitHeight(tamano);
            icono.setStyle("-fx-background-color: #f4f1de; -fx-border-color: #283044;"
                    + "-fx-border-radius: 50; -fx-background-radius: 50;");
        }
        return icono;
    }

    private void asignarEnergia(int posicion) {
        if (partidaTerminada) return;
        if (energiaDisponible <= 0 || !acciones.asignarEnergia(turno, posicion)) {
            estado.setText("No se puede asignar energia en ese espacio.");
            return;
        }
        energiaUsada = true;
        energiaDisponible--;
        refrescar();
    }

    private void jugarPrimeraCarta() {
        if (partidaTerminada) return;
        if (indiceManoSeleccionada < 0 || indiceManoSeleccionada >= turno.getTamanoMano()) {
            estado.setText("Selecciona una carta de tu mano primero.");
            return;
        }
        int indice = indiceManoSeleccionada;
        Carta carta = turno.obtenerCartaDeMano(indice);
        try {
            if (acciones.jugarPokemon(turno, indice, numeroTurno)) {
                estado.setText(carta.getNombre() + " entro al tablero.");
            } else if (carta.esCarta("Pocion") && acciones.usarPocion(turno, indice)) {
                estado.setText("Pocion usada.");
            } else if (carta.esCarta("Superpocion") && acciones.usarSuperPocion(turno, indice)) {
                estado.setText("Superpocion usada.");
            } else if (carta.esCarta("Pokeball") && acciones.usarPokeball(turno, indice)) {
                estado.setText("Pokeball usada.");
            } else if (carta.esCarta("Caramelo Raro")) {
                usarCarameloRaro(indice);
            } else {
                estado.setText("La primera carta no se puede jugar ahora.");
            }
        } catch (ListaVaciaException exception) {
            estado.setText(exception.getMessage());
        }
        indiceManoSeleccionada = -1;
        refrescar();
    }

    private void usarCarameloRaro(int indiceCaramelo) {
        int posicion = posicionEvolucionSeleccionada();
        Carta objetivo = pokemonEnPosicion(posicion);
        Carta siguiente = acciones.siguienteEvolucion(objetivo);
        if (siguiente == null) {
            estado.setText("El objetivo seleccionado no tiene una evolucion disponible.");
            return;
        }
        for (int i = 0; i < turno.getTamanoMano(); i++) {
            Carta carta = turno.obtenerCartaDeMano(i);
            if (carta.esCarta(siguiente.getNombre())) {
                Carta anterior = acciones.usarCarameloRaro(turno, indiceCaramelo, i, posicion, numeroTurno);
                estado.setText(anterior == null
                        ? "No se pudo usar Caramelo Raro en este Pokemon."
                        : anterior.getNombre() + " evoluciono a " + carta.getNombre() + ".");
                return;
            }
        }
        estado.setText("Necesitas tener a " + siguiente.getNombre() + " en la mano.");
    }

    private void evolucionarObjetivo() {
        if (partidaTerminada) return;
        int posicion = posicionEvolucionSeleccionada();
        Carta objetivo = pokemonEnPosicion(posicion);
        if (objetivo == null) {
            estado.setText("No hay un Pokemon en el objetivo seleccionado.");
            return;
        }
        Carta siguiente = acciones.siguienteEvolucion(objetivo);
        if (siguiente == null) return;
        for (int i = 0; i < turno.getTamanoMano(); i++) {
            Carta carta = turno.obtenerCartaDeMano(i);
            if (carta.esCarta(siguiente.getNombre())) {
                Carta anterior = acciones.evolucionar(turno, posicion, carta, numeroTurno, false);
                if (anterior != null) {
                    turno.sacarCartaDeMano(i);
                    estado.setText(anterior.getNombre() + " evoluciono a " + carta.getNombre() + ".");
                }
                refrescar();
                return;
            }
        }
        estado.setText("No tienes la siguiente evolucion en la mano.");
    }

    private int posicionEvolucionSeleccionada() {
        return selectorEvolucion == null ? -1 : selectorEvolucion.getSelectionModel().getSelectedIndex() - 1;
    }

    private Carta pokemonEnPosicion(int posicion) {
        return posicion < 0 ? turno.getActivo() : turno.getPokemonBanca(posicion);
    }

    private void ejecutarRetirada() {
        if (partidaTerminada) return;
        int bancaDestino = selectorRetirada == null ? -1 : selectorRetirada.getSelectionModel().getSelectedIndex();
        if (acciones.retirar(turno, bancaDestino)) estado.setText("Retirada realizada.");
        else estado.setText("Necesitas banca y energia suficiente para retirarte.");
        refrescar();
    }

    private void ejecutarAtaque() {
        if (partidaTerminada) return;
        Carta atacante = turno.getActivo();
        Carta objetivo = defensor.getActivo();
        if (ataqueUsado || atacante == null || objetivo == null) return;
        int danio = acciones.atacar(turno, defensor);
        if (danio < 0) {
            mensajeAccion = acciones.getUltimoResumenAtaque();
            refrescar();
            return;
        }
        ataqueUsado = true;
        mensajeAccion = acciones.getUltimoResumenAtaque();
        if (objetivo.estaFueraDeCombate()) {
            turno.sumarPunto();
            mensajeAccion += " Punto para " + turno.getNombre() + ".";
            mostrarNotificacionesEstado();
            resolverDerrotaVisual(defensor, turno);
            return;
        }
        refrescar();
        mostrarNotificacionesEstado();
    }

    private void resolverDerrotaVisual(Jugador derrotado, Jugador ganador) {
        Carta caido = derrotado.sacarActivo();
        if (caido != null) derrotado.getDescarte().apilar(caido);
        if (ganador.getPuntos() >= 3) {
            finalizarPartida(ganador, "al conseguir tres puntos");
            return;
        }

        List<String> opciones = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Carta carta = derrotado.getPokemonBanca(i);
            if (carta != null) opciones.add("Banca " + (i + 1) + ": " + carta.getNombre());
        }
        for (int i = 0; i < derrotado.getTamanoMano(); i++) {
            Carta carta = derrotado.obtenerCartaDeMano(i);
            if (carta != null && carta.esPokemon() && carta.getFase() == 1) {
                opciones.add("Mano " + (i + 1) + ": " + carta.getNombre());
            }
        }

        if (opciones.isEmpty()) {
            if (derrotado.promoverBasicoDelMazo(numeroTurno)) {
                mensajeAccion = derrotado.getNombre() + " puso un Pokemon basico del mazo como activo.";
                refrescar();
            } else {
                finalizarPartida(ganador, derrotado.getNombre() + " no tiene Pokemon para reemplazar al activo");
            }
            return;
        }

        ChoiceDialog<String> dialogo = new ChoiceDialog<>(opciones.get(0), opciones);
        dialogo.setTitle("Pokemon derrotado");
        dialogo.setHeaderText(derrotado.getNombre() + ": elige un nuevo Pokemon activo.");
        dialogo.setContentText("Reemplazo:");
        String eleccion = dialogo.showAndWait().orElse(opciones.get(0));
        boolean promovido;
        if (eleccion.startsWith("Banca ")) {
            int indice = Integer.parseInt(eleccion.substring(6, 7)) - 1;
            promovido = derrotado.promoverDesdeBanca(indice);
        } else {
            int finIndice = eleccion.indexOf(':');
            int indice = Integer.parseInt(eleccion.substring(5, finIndice)) - 1;
            promovido = derrotado.promoverDesdeMano(indice, numeroTurno);
        }
        if (!promovido) {
            finalizarPartida(ganador, derrotado.getNombre() + " no pudo reemplazar al activo");
            return;
        }
        mensajeAccion = derrotado.getNombre() + " eligio un nuevo Pokemon activo.";
        refrescar();
    }

    private void finalizarPartida(Jugador ganador, String razon) {
        partidaTerminada = true;
        mensajeAccion = "Fin de la partida: " + ganador.getNombre() + " gana " + razon + ".";
        alerta(mensajeAccion);
        refrescar();
    }

    private void terminarTurno() {
        if (partidaTerminada) return;
        Jugador temporal = turno;
        turno = defensor;
        defensor = temporal;
        numeroTurno++;
        gestor.avanzarCampoAlComenzarTurno(numeroTurno);
        energiaUsada = false;
        energiaDisponible = 1;
        ataqueUsado = false;
        boolean roboCarta = turno.robarCartaAMano();
        String efectoEstado = turno.procesarEstadoInicioTurno(random);
        if (turno.activoFueraDeCombate()) {
            mensajeAccion = (roboCarta ? "Robo una carta. " : "") + efectoEstado;
            resolverDerrotaVisual(turno, defensor);
            return;
        }
        String habilidades = acciones.procesarHabilidadesInicioTurno(turno, defensor);
        mensajeAccion = (roboCarta ? "Robo una carta. " : "") + efectoEstado
            + (habilidades.isEmpty() ? "" : " " + habilidades + ".");
        refrescar();
        mostrarNotificacionesEstado();
    }

    private void mostrarNotificacionesEstado() {
        for (String notificacion : acciones.consumirNotificacionesEstado()) {
            Alert alertaEstado = new Alert(Alert.AlertType.INFORMATION);
            alertaEstado.setTitle("Estado alterado");
            alertaEstado.setHeaderText("Un Pokemon recibio un estado alterado");
            alertaEstado.setContentText(notificacion);
            alertaEstado.showAndWait();
        }
    }

    private void mostrarDetalle(Carta carta) {
        detalle.getChildren().clear();
        detalle.setAlignment(Pos.CENTER);
        detalle.setFillWidth(false);
        Label encabezado = titulo("Detalle de carta");
        encabezado.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #111111;");
        detalle.getChildren().add(encabezado);
        detalle.getChildren().addAll(new Label("Nombre: Carta " + carta.getNombre()),
                new Label("Tipo: " + carta.getTipo()),
                new Label("Vida: " + carta.getVida() + "/" + carta.getVidaMaxima()),
                new Label("Ataque: " + carta.getAtaque()),
                new Label("Daño: " + carta.getDanio()),
                new Label("Ataque elemental: " + (carta.tieneAtaqueElemental() ? "si" : "no")),
                new Label("Tiene habilidad: " + (carta.getHabilidad() == null ? "No" : "Si")),
                new Label("Efecto de habilidad: " + descripcionHabilidad(carta)),
                new Label("Energia actual: " + carta.getEnergias()),
                new Label("Energia para atacar: " + carta.getFase()),
                new Label("Costo de retirada: " + carta.getFase()),
                new Label("Estado: " + carta.getEstado()));
    }

    private String descripcionHabilidad(Carta carta) {
        if (carta.getHabilidad() == null) return "No tiene habilidad.";
        return switch (carta.getNombre()) {
            case "Venusaur" -> "Al iniciar el turno cura 10 de vida.";
            case "Vileplume" -> "Al iniciar el turno aplica Veneno al activo rival.";
            case "Clefairy" -> "Al iniciar el turno cura 10 de vida y elimina su estado.";
            case "Charizard" -> "Sus ataques hacen 10 de daño adicional.";
            case "Blastoise" -> "Con al menos una energia, sus ataques hacen 10 de daño adicional.";
            case "Pikachu" -> "Tiene 50% de probabilidad de hacer 10 de daño adicional.";
            case "Raichu" -> "Sus ataques hacen 15 de daño adicional.";
            case "Gyarados" -> "Con vida a la mitad o menos, sus ataques hacen 15 de daño adicional.";
            default -> carta.getHabilidad();
        };
    }

    private void mostrarDatosCompletos(Carta carta) {
        VBox contenido = new VBox(7);
        contenido.setPadding(new Insets(8));
        contenido.getChildren().addAll(
                new Label("Nombre: " + carta.getNombre()),
                new Label("Clase: " + carta.getTipoCarta()),
                new Label("Tipo: " + carta.getTipo()));

        if (carta.esPokemon()) {
            contenido.getChildren().addAll(
                    new Label("Fase: " + carta.getFase()),
                    new Label("Vida: " + carta.getVida() + "/" + carta.getVidaMaxima()),
                    new Label("Ataque: " + carta.getAtaque()),
                    new Label("Daño base: " + carta.getDanio()),
                    new Label("Ataque elemental: " + (carta.tieneAtaqueElemental() ? "Si" : "No")),
                    new Label("Tiene habilidad: " + (carta.getHabilidad() == null ? "No" : "Si")),
                    new Label("Efecto de habilidad: " + descripcionHabilidad(carta)),
                    new Label("Energias asignadas: " + carta.getEnergias()),
                    new Label("Estado: " + carta.getEstado()),
                    new Label("Turno de entrada: " + carta.getTurnoEntrada()),
                    new Label("Turno de evolucion: " + carta.getTurnoEvolucion()));
        } else {
            contenido.getChildren().add(new Label("Efecto: " + descripcionObjeto(carta)));
        }

        Alert ventana = new Alert(Alert.AlertType.INFORMATION);
        ventana.setTitle("Datos completos de la carta");
        ventana.setHeaderText(carta.getNombre());
        ventana.getDialogPane().setContent(contenido);
        ventana.showAndWait();
    }

    private String descripcionObjeto(Carta carta) {
        return switch (carta.getNombre()) {
            case "Pocion" -> "Cura 20 puntos de vida al Pokemon activo.";
            case "Superpocion" -> "Cura 40 puntos de vida y puede retirar una energia.";
            case "Pokeball" -> "Busca un Pokemon basico en el mazo.";
            case "Caramelo Raro" -> "Permite evolucionar sin esperar un turno.";
            default -> "Objeto especial.";
        };
    }

        private void mostrarHistorial() {
        List<String> eventos = acciones.historial().obtenerElementos();
        TextArea resumen = new TextArea(eventos.isEmpty()
            ? "Aun no hay jugadas registradas."
            : String.join(System.lineSeparator(), eventos));
        resumen.setEditable(false);
        resumen.setWrapText(true);
        resumen.setPrefSize(520, 360);
        Alert ventana = new Alert(Alert.AlertType.INFORMATION);
        ventana.setTitle("Historial de partida");
        ventana.setHeaderText("Resumen de jugadas");
        ventana.getDialogPane().setContent(resumen);
        ventana.showAndWait();
        }

    private Label titulo(String texto) {
        Label label = new Label(texto);
        label.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #f4f1de;");
        return label;
    }

    private void actualizarListaMazo(ListView<String> lista, List<String> mazo) {
        lista.getItems().setAll(mazo);
    }

    private int contarPokemones(List<String> mazo) {
        int total = 0;
        for (String nombre : mazo) if (cartaPorNombre(nombre).esPokemon()) total++;
        return total;
    }

    private String nombreCarta(Carta carta) {
        return carta == null ? "ninguno" : "Carta " + carta.getNombre();
    }

    private int contarBanca(Jugador propietario) {
        int total = 0;
        for (int i = 0; i < 3; i++) if (propietario.getPokemonBanca(i) != null) total++;
        return total;
    }

    private Carta cartaPorNombre(String nombre) {
        Carta carta = catalogo.get(nombre);
        if (carta != null) return carta;
        if ("Pocion".equals(nombre)) return new Carta("Pocion", 20, 0, true);
        if ("Superpocion".equals(nombre)) return new Carta("Superpocion", 40, 0, true);
        if ("Pokeball".equals(nombre)) return new Carta("Pokeball", 0, 0, true);
        if ("Caramelo Raro".equals(nombre)) return new Carta("Caramelo Raro", 0, 0, true);
        return new Carta("Carta", 0, 0, true);
    }

    private void alerta(String mensaje) {
        new Alert(Alert.AlertType.WARNING, mensaje).showAndWait();
    }
}
