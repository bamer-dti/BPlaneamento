package objectos;

import bamer.AppMain;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.QueryRow;
import couchbase.ArtigoAprovisionamento;
import couchbase.CamposCouch;
import couchbase.ServicoCouchBase;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.util.List;

///**
// * Created by miguel.silva on 28-07-2016.
// */
public class GridPaneAprovisionamentos extends GridPane {
    public GridPaneAprovisionamentos() {
        construct();
    }

    private void construct() {
        this.setPadding(new Insets(0, 5, 0, 15));
        this.getStyleClass().add("game-grid");
        try {
            ServicoCouchBase.getInstancia().liveAprovisionamento();
        } catch (IOException | CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public static void alimentarLista(List<QueryRow> lista) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                AppMain.getInstancia().getBut_aprovisionamento().setText("planeamento (" + lista.size() + ")");
            }
        });
        GridPaneAprovisionamentos gridPaneAprovados = new GridPaneAprovisionamentos();

        Label labelTotRecs = AppMain.getInstancia().getLabelTotRecsAprovisionamento();
        labelTotRecs.setText("0");
        if (lista == null)
            return;
        int linha = 0;
        for (QueryRow queryRow : lista) {
            Document document = queryRow.getDocument();
            String seccao = document.getProperty(CamposCouch.FIELD_SECCAO).toString();
            int obrano = (int) document.getProperty(CamposCouch.FIELD_OBRANO);
            String dtcliente = document.getProperty(CamposCouch.FIELD_DTCLIENTE).toString();
            String bostamp = document.getProperty(CamposCouch.FIELD_BOSTAMP).toString();
            String dtexpedi = document.getProperty(CamposCouch.FIELD_DTEXPEDI).toString();
            String fref = document.getProperty(CamposCouch.FIELD_FREF).toString();
            String nmfref = document.getProperty(CamposCouch.FIELD_NMFREF).toString();
            String obs = document.getProperty(CamposCouch.FIELD_OBS).toString();

            String filtroFref = AppMain.getInstancia().getTextFieldFiltroFrefAprovisionamento().getText();
            if (!filtroFref.equals("")) {
                if (!fref.startsWith(filtroFref)) {
                    continue;
                }
            }

            ArtigoAprovisionamento artigo = new ArtigoAprovisionamento.Builder()
                    .bostamp(bostamp)
                    .seccao(seccao)
                    .dtcliente(dtcliente)
                    .obrano(obrano)
                    .dtexpedi(dtexpedi)
                    .fref(fref)
                    .nmfref(nmfref)
                    .obs(obs)
                    .build();

            HBoxOSAprovisionamento hBoxOSAprovisionamento = new HBoxOSAprovisionamento(artigo, linha, HBoxOSAprovisionamento.TIPO_APROVISIONAMENTO);
            hBoxOSAprovisionamento.setId(bostamp);
            if (linha % 2 == 0)
                hBoxOSAprovisionamento.setStyle("-fx-background-color: rgb(220,220,220); -fx-background-insets: 0, 1 1 1 0 ;");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    gridPaneAprovados.getChildren().add(hBoxOSAprovisionamento);
                }
            });
            GridPane.setConstraints(hBoxOSAprovisionamento, 0, linha);
            linha++;
            labelTotRecs.setText("" + linha);
        }



        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ScrollPane scrollPane = new ScrollPane(gridPaneAprovados);
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(true);
                AppMain.getInstancia().borderPaneAprovisionamento.setCenter(scrollPane);
            }
        });

    }
}
