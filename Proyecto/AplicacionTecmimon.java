import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class AplicacionTecmimon extends Application {

    @Override
    public void start(Stage stage) {
        Platform.setImplicitExit(true);
        stage.setOnCloseRequest(event -> Platform.exit());
        VistaJuego vista = new VistaJuego();
        vista.mostrar(stage);
    }

    @Override
    public void stop() {
        System.exit(0);
    }
}
