package bamer;

import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

///**
// * Created by miguel.silva on 29-07-2016.
// */
public class ControllerEditar implements Initializable {
    public DatePicker dttransf;
    public Label fref;
    public Label os;
    public Label obs;
    public DatePicker dtembala;
    public DatePicker dtexpedi;
    public Button btgravar;
    public DatePicker dtcortef;
    public ComboBox combo_estado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setData(String estado) {

        combo_estado.getItems().clear();

        combo_estado.getItems().addAll(
                "00 - APROV.",
                "00 - PLANEAMENTO",
                "01 - CORTE",
                "02 - TRANSFORMAÇÃO",
                "021 - ORLAGEM",
                "03 - LACAGEM",
                "03 - TRATAMENTO EXT.",
                "04 - EMBALAGEM"
        );
        combo_estado.setValue(estado);
    }
}
