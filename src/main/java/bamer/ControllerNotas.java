package bamer;

import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;

///**
// * Created by miguel.silva on 05-08-2016.
// */
public class ControllerNotas implements Initializable {
    public TextArea areaDoTexto;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void update(String texto) {
        areaDoTexto.setText(texto);
    }
}
