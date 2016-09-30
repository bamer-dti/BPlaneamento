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
import javafx.scene.layout.GridPane;
import utils.Funcoes;
import utils.Singleton;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

///**
// * Created by miguel.silva on 28-07-2016.
// */
public class GridPaneAtrasados extends GridPane {
    public GridPaneAtrasados() {
        construct();
    }

    private void construct() {
        this.setPadding(new Insets(0, 5, 0, 15));
        this.getStyleClass().add("game-grid");
        try {
            ServicoCouchBase.getInstancia().liveAtrasados();
        } catch (IOException | CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public static void alimentarLista(List<QueryRow> lista) {
        GridPaneAtrasados gridPaneAtrasados = new GridPaneAtrasados();
        if (lista == null)
            return;

        Label labelTotRecs = AppMain.getInstancia().getLabelTotRecsAtrasados();
        if (labelTotRecs != null)
            labelTotRecs.setText("0");

        int linha = 0;
        int recsTotal = 0;
        for (QueryRow queryRow : lista) {
            Document document = queryRow.getDocument();
            String seccao = document.getProperty(CamposCouch.FIELD_SECCAO).toString();
            int obrano = (int) document.getProperty(CamposCouch.FIELD_OBRANO);
            String textoDtCorte = document.getProperty(CamposCouch.FIELD_DTCORTEF).toString();
            String dtcliente = document.getProperty(CamposCouch.FIELD_DTCLIENTE).toString();
            String bostamp = document.getProperty(CamposCouch.FIELD_BOSTAMP).toString();
            String dtexpedi = document.getProperty(CamposCouch.FIELD_DTEXPEDI).toString();
            String fref = document.getProperty(CamposCouch.FIELD_FREF).toString();
            String nmfref = document.getProperty(CamposCouch.FIELD_NMFREF).toString();
            String obs = document.getProperty(CamposCouch.FIELD_OBS).toString();
            LocalDateTime dtcortef;
            if (textoDtCorte.equals("1900-01-01 00:00:00")) {
                dtcortef = LocalDateTime.of(1900, Month.JANUARY, 1, 0, 0);
            } else {
                dtcortef = Funcoes.sToT(textoDtCorte);
            }

            LocalDateTime datainicio = Singleton.getInstancia().dataInicioAgenda.minusDays(1);
            if (!dtcortef.isBefore(datainicio)) {
                continue;
            }
            recsTotal++;

            String filtroFref = AppMain.getInstancia().getTextFieldFiltroFrefAtrasados().getText();
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
            artigo.setDtcortef(dtcortef);

            HBoxOSAprovisionamento hBoxOSAprovisionamento = new HBoxOSAprovisionamento(artigo, linha, HBoxOSAprovisionamento.TIPO_ATRASADO);
            hBoxOSAprovisionamento.setId(bostamp);
            if (linha % 2 == 0)
                hBoxOSAprovisionamento.setStyle("-fx-background-color: rgb(220,220,220); -fx-background-insets: 0, 1 1 1 0 ;");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    gridPaneAtrasados.getChildren().add(hBoxOSAprovisionamento);
                }
            });
            GridPane.setConstraints(hBoxOSAprovisionamento, 0, linha);
            linha++;
            if (labelTotRecs != null) {
                int finalLinha = linha;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        labelTotRecs.setText("" + finalLinha);
                    }
                });
            }
        }

        int finalRecsTotal = recsTotal;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                AppMain.getInstancia().getBut_atrasados().setText("atrasos (" + finalRecsTotal + ")");
            }
        });

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                AppMain.getInstancia().borderPaneAtrasados.setCenter(gridPaneAtrasados);
            }
        });

    }
}
