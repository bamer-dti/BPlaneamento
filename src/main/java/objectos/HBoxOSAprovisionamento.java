package objectos;

import bamer.AppMain;
import com.couchbase.lite.CouchbaseLiteException;
import couchbase.ArtigoAprovisionamento;
import couchbase.ServicoCouchBase;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import utils.Funcoes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class HBoxOSAprovisionamento extends HBox {
    public static final int TIPO_APROVISIONAMENTO = 1;
    public static final int TIPO_ATRASADO = 2;
    private final LocalDateTime dtcortef;
    private final int tipo;
    private String seccao;
    private final String dtcliente;
    private final int obrano;
    private final String bostamp;
    private final String dtexpedi;
    private final String fref;
    private final String nmfref;
    private final String obs;
    private final ArtigoAprovisionamento artigoAprovisionamento;
    private int qtt = 0;
    private HBoxOSAprovisionamento contexto = this;
    private int linha;

    public HBoxOSAprovisionamento(ArtigoAprovisionamento artigo, int linha, int tipoRegisto) {
        this.artigoAprovisionamento = artigo;
        this.seccao = artigo.getSeccao();
        this.dtcliente = artigo.getDtcliente();
        this.obrano = artigo.getObrano();
        this.bostamp = artigo.getBostamp();
        this.dtexpedi = artigo.getDtexpedi();
        this.fref = artigo.getFref();
        this.nmfref = artigo.getNmfref();
        this.obs = artigo.getObs();
        this.linha = linha;
        this.dtcortef = artigo.getDtcortef();
        this.tipo = tipoRegisto;

        criarObjectos();

        configurarEventos();
    }

    private void criarObjectos() {
        try {
            this.qtt = ServicoCouchBase.getInstancia().getPecasPorOS(bostamp);
        } catch (IOException | CouchbaseLiteException e) {
            e.printStackTrace();
        }

        Label labelSeccao = new Label(seccao);
        labelSeccao.setWrapText(true);
        setMargin(labelSeccao, new Insets(2, 2, 2, 2));
        labelSeccao.setPrefWidth(100);
        this.getChildren().add(labelSeccao);

        Label labelOS = new Label("OS " + obrano);
        labelOS.setWrapText(true);
        setMargin(labelOS, new Insets(2, 2, 2, 20));
        this.getChildren().add(labelOS);

        Label labelDataCliente = new Label(Funcoes.dataBonita(dtcliente));
        labelDataCliente.setWrapText(true);
        setMargin(labelDataCliente, new Insets(2, 2, 2, 20));
        this.getChildren().add(labelDataCliente);
        Tooltip t = new Tooltip("Data de cliente");
        Tooltip.install(labelDataCliente, t);

        if (tipo == TIPO_ATRASADO) {
            Label labelDataCorte = new Label(Funcoes.dataBonita(Funcoes.dToCZeroHour(dtcortef)));
            labelDataCorte.setWrapText(true);
            setMargin(labelDataCorte, new Insets(2, 2, 2, 20));
            this.getChildren().add(labelDataCorte);
            t = new Tooltip("Data de Corte");
            Tooltip.install(labelDataCorte, t);
        } else {
            Label labelDataExped = new Label(Funcoes.dataBonita(dtexpedi));
            labelDataExped.setWrapText(true);
            setMargin(labelDataExped, new Insets(2, 2, 2, 20));
            this.getChildren().add(labelDataExped);
            t = new Tooltip("Data de Expedição");
            Tooltip.install(labelDataExped, t);
        }

        VBox vboxFref = new VBox();
        vboxFref.setPrefWidth(300);
        setMargin(vboxFref, new Insets(2, 2, 2, 20));

        Label labelFref = new Label(fref + " - " + nmfref);
        labelFref.setWrapText(true);
//        setMargin(labelFref, new Insets(2, 2, 2, 20));
        vboxFref.getChildren().add(labelFref);

        Label labelObs = new Label(obs);
        labelObs.setWrapText(true);
//        setMargin(labelObs, new Insets(2, 2, 2, 20));
        vboxFref.getChildren().add(labelObs);
        this.getChildren().add(vboxFref);

        VBox vBoxQtt = new VBox();
        setMargin(vBoxQtt, new Insets(2, 2, 2, 20));
        Label labelQtt = new Label("" + qtt);
        labelQtt.setWrapText(true);
        Font fontBase = labelQtt.getFont();
        labelQtt.setFont(Font.font(fontBase.getFamily(), FontWeight.BOLD, FontPosture.REGULAR, 16f));
        labelQtt.setStyle("-fx-text-fill: blue; -fx-background-insets: 0, 1 1 1 0 ;");
        vBoxQtt.setAlignment(Pos.CENTER);
//        setMargin(labelFref, new Insets(2, 2, 2, 20));
        vBoxQtt.getChildren().add(labelQtt);

        this.getChildren().add(vBoxQtt);
    }

    private void configurarEventos() {
        setOnDragDetected(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String seccaoPref = AppMain.getInstancia().getComboSeccao().getValue().toString();
                if (seccao.equals(seccaoPref)) {
                    Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                    Map<DataFormat, Object> map = new HashMap<>();
                    map.put(DataFormat.RTF, contexto);
                    dragboard.setContent(map);
                    setStyle("-fx-background-color: orange; -fx-background-insets: 0, 1 1 1 0 ;");
                    event.consume();
                } else {
                    Funcoes.alerta("Não pertence ao mesmo sector", Alert.AlertType.WARNING);
                }
            }
        });

        setOnDragDone(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if (linha != 0)
                    setStyle("-fx-background-color: white; -fx-background-insets: 0, 1 1 1 0 ;");
                else {
                    setStyle("-fx-background-color: rgb(220,220,220); -fx-background-insets: 0, 1 1 1 0 ;");
                }
                event.consume();
            }
        });
    }

    public ArtigoAprovisionamento getArtigoAprovisionamento() {
        return artigoAprovisionamento;
    }
}
