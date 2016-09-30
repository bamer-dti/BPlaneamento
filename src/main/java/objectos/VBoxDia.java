package objectos;

import bamer.AppMain;
import com.jfoenix.controls.JFXButton;
import couchbase.ArtigoAprovisionamento;
import couchbase.ArtigoOSBO;
import couchbase.ServicoCouchBase;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import sql.BamerSqlServer;
import sqlite.PreferenciasEmSQLite;
import utils.Constantes;
import utils.Funcoes;
import utils.Singleton;
import utils.ValoresDefeito;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static java.lang.System.out;

public class VBoxDia extends VBox {

    private int coluna;
    private Text textDiaDaSemana;
    private Text textDiaMes;
    private Text textQtd;
    private Text textQtdFeita;
    private boolean mostraResize;
    private Text textSemana;

    VBoxDia(Boolean mostraResizee) {
        this.mostraResize = mostraResizee;
        objectos();
        configurarEventos();
        resize();
    }

    public void resize() {
        PreferenciasEmSQLite prefs = PreferenciasEmSQLite.getInstancia();
        int minWidth = prefs.getInt(Constantes.PREF_COMPRIMENTO_MINIMO, ValoresDefeito.COL_COMPRIMENTO);
        setPrefWidth(minWidth);
        setMinWidth(minWidth);
    }

    private void objectos() {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);

        if (mostraResize) {
            VBox vBoxPlusMinus = new VBox();

            JFXButton btplus = new JFXButton("+");
            btplus.getStyleClass().add("button-raised-bamer-sizecolumn");
            btplus.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    PreferenciasEmSQLite prefs = PreferenciasEmSQLite.getInstancia();
                    int tamanho = prefs.getInt(Constantes.PREF_COMPRIMENTO_MINIMO, ValoresDefeito.COL_COMPRIMENTO);
                    tamanho = tamanho + 1;
                    prefs.putInt(Constantes.PREF_COMPRIMENTO_MINIMO, tamanho);
                    alterarTamanhos(tamanho);
                    out.println("Plus clicked! Novo tamanho = " + tamanho);
                    event.consume();
                }
            });
            vBoxPlusMinus.getChildren().add(btplus);

            JFXButton btminus = new JFXButton("-");
            btminus.getStyleClass().add("button-raised-bamer-sizecolumn");
            btminus.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    out.println("Minus clicked!");
                    PreferenciasEmSQLite prefs = PreferenciasEmSQLite.getInstancia();
                    int tamanho = prefs.getInt(Constantes.PREF_COMPRIMENTO_MINIMO, ValoresDefeito.COL_COMPRIMENTO);
                    tamanho = tamanho - 1;
                    prefs.putInt(Constantes.PREF_COMPRIMENTO_MINIMO, tamanho);
                    alterarTamanhos(tamanho);
                    out.println("Plus clicked! Novo tamanho = " + tamanho);
                    event.consume();
                }
            });
            vBoxPlusMinus.getChildren().add(btminus);

            hBox.getChildren().add(vBoxPlusMinus);
        }

        textDiaDaSemana = new Text();
        textDiaDaSemana.setFont(Font.font(textDiaDaSemana.getFont().getSize() * 1.5));
        HBox.setMargin(textDiaDaSemana, new Insets(2, 2, 2, 2));
        hBox.getChildren().add(textDiaDaSemana);

        VBox vBoxDiaMesSemana = new VBox();

        textDiaMes = new Text();
//        textDiaMes.setId("header0" + coluna);
        VBox.setMargin(textDiaMes, new Insets(0, 2, 0, 2));
        vBoxDiaMesSemana.getChildren().add(textDiaMes);

        textSemana = new Text();
        Font fonte = textDiaDaSemana.getFont();
        textSemana.setFont(Font.font(fonte.getFamily(), FontPosture.ITALIC, 12f));
        textSemana.setFill(Color.DARKBLUE);
        VBox.setMargin(textSemana, new Insets(0, 2, 0, 2));
        vBoxDiaMesSemana.getChildren().add(textSemana);
        vBoxDiaMesSemana.setAlignment(Pos.CENTER);

        hBox.getChildren().add(vBoxDiaMesSemana);

        textQtd = new Text();
        Font font = textQtd.getFont();
        textQtd.setFont(Font.font(font.getFamily(), FontWeight.BOLD, font.getSize() * 1.5));
        textQtd.setFill(Color.web("#548045"));
        HBox.setMargin(textQtd, new Insets(2, 2, 2, 10));

        textQtdFeita = new Text();
        font = textQtdFeita.getFont();
        textQtdFeita.setFont(Font.font(font.getFamily(), FontWeight.BOLD, font.getSize() * 1.5));
        textQtdFeita.setFill(Color.web("#E31751"));
        HBox.setMargin(textQtdFeita, new Insets(2, 2, 2, 10));

        hBox.getChildren().addAll(textQtd, textQtdFeita);

        getChildren().add(hBox);
    }

    private void alterarTamanhos(int tamanho) {
        ObservableList<Node> childs = AppMain.getInstancia().getCalendario().getChildren();
        for (Node node : childs) {
            if (node instanceof VBoxOSBO) {
                VBoxOSBO vb = (VBoxOSBO) node;
                vb.setPrefWidth(tamanho);
                vb.setMinWidth(tamanho);
            }
        }
    }

    private void configurarEventos() {
        this.setOnDragEntered(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Object object = event.getDragboard().getContent(DataFormat.RTF);
//                out.println("OnDragEntered: " + object.getClass().getSimpleName());
                if (object instanceof VBoxOSBO) {
                    Dragboard dragboard = event.getDragboard();
                    VBoxOSBO vBoxOSBO = (VBoxOSBO) dragboard.getContent(DataFormat.RTF);
                    if (vBoxOSBO.getColuna() == coluna) {
                        return;
                    }
                    Font font = textDiaDaSemana.getFont();
                    textDiaDaSemana.setFill(Color.INDIANRED);
                    textDiaDaSemana.setFont(Font.font(font.getFamily(), FontWeight.BOLD, font.getSize()));
                    event.consume();
                }

                if (object instanceof HBoxOSAprovisionamento) {
                    Font font = textDiaDaSemana.getFont();
                    textDiaDaSemana.setFill(Color.INDIANRED);
                    textDiaDaSemana.setFont(Font.font(font.getFamily(), FontWeight.BOLD, font.getSize()));
                    event.consume();
                }
            }
        });

        this.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Object object = event.getDragboard().getContent(DataFormat.RTF);
//                out.println("OnDragOver: " + object.getClass().getSimpleName());
                if (object instanceof VBoxOSBO) {
                    Dragboard dragboard = event.getDragboard();
                    VBoxOSBO vBoxOSBO = (VBoxOSBO) dragboard.getContent(DataFormat.RTF);
                    if (vBoxOSBO.getColuna() == coluna) {
                        event.acceptTransferModes(TransferMode.NONE);
                    } else {
                        event.acceptTransferModes(TransferMode.MOVE);
                    }
                    event.consume();
                }

                if (object instanceof HBoxOSAprovisionamento) {
                    event.acceptTransferModes(TransferMode.MOVE);
                    event.consume();
                }
            }
        });

        this.setOnDragExited(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Object object = event.getDragboard().getContent(DataFormat.RTF);
//                out.println("OnDragExited: " + object.getClass().getSimpleName());
                if (object instanceof VBoxOSBO) {
                    Dragboard dragboard = event.getDragboard();
                    VBoxOSBO vBoxOSBO = (VBoxOSBO) dragboard.getContent(DataFormat.RTF);
                    if (vBoxOSBO.getColuna() == coluna) {
                        return;
                    }
                    Font font = textDiaDaSemana.getFont();
                    textDiaDaSemana.setFill(Color.BLACK);
                    textDiaDaSemana.setFont(Font.font(font.getFamily(), FontWeight.NORMAL, font.getSize()));
                    event.consume();
                }

                if (object instanceof HBoxOSAprovisionamento) {
                    Font font = textDiaDaSemana.getFont();
                    textDiaDaSemana.setFill(Color.BLACK);
                    textDiaDaSemana.setFont(Font.font(font.getFamily(), FontWeight.NORMAL, font.getSize()));
                    event.consume();
                }
            }
        });

        this.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Object object = event.getDragboard().getContent(DataFormat.RTF);
//                out.println("OnDragDropped: " + object.getClass().getSimpleName());
                if (object instanceof VBoxOSBO) {
                    Dragboard dragboard = event.getDragboard();
                    VBoxOSBO vboxEmDRAG = (VBoxOSBO) dragboard.getContent(DataFormat.RTF);
                    if (vboxEmDRAG.getColuna() == coluna) {
                        return;
                    }

                    int colunaAnterior = vboxEmDRAG.getColuna();
                    int ordemAnterior = vboxEmDRAG.getOrdemProp();
                    ArtigoOSBO artigoOSBOemDRAG = vboxEmDRAG.getArtigoOSBOProp();

                    LocalDateTime dataNova = Singleton.getInstancia().dataInicioAgenda.plusDays(coluna);
                    GridPane gridPane = (GridPane) getParent();
                    ArrayList<VBoxOSBO> listaDeAlteracoes = new ArrayList<>();

                    int countOrdem = 1;
                    for (int i = 0; i <= 200; i++) {
                        Node node = Funcoes.getNodeByRowColumnIndex(i, coluna, gridPane);
                        if (node instanceof VBoxOSBO) {
                            countOrdem = countOrdem + 1;
                        }
                    }

                    artigoOSBOemDRAG.setOrdem(countOrdem);
                    vboxEmDRAG.setOrdemProp(countOrdem);
                    vboxEmDRAG.setColuna(coluna);
                    artigoOSBOemDRAG.setDtcortef(dataNova);
                    vboxEmDRAG.setDtcortefProp(dataNova);
                    listaDeAlteracoes.add(vboxEmDRAG);

                    //NA COLUNA DE ORIGEM DO DRAG
                    for (int i = 0; i < 200; i++) {
                        Node node = Funcoes.getNodeByRowColumnIndex(i, colunaAnterior, gridPane);
                        if (node instanceof VBoxOSBO) {
                            VBoxOSBO vBoxOSBO = (VBoxOSBO) node;
                            ArtigoOSBO artigoOSBO = vBoxOSBO.getArtigoOSBOProp();
                            if (artigoOSBO != artigoOSBOemDRAG && artigoOSBO.getOrdem() >= ordemAnterior) {
                                vBoxOSBO.setOrdemProp(vBoxOSBO.getOrdemProp() - 1);
                                listaDeAlteracoes.add(vBoxOSBO);
                            }
                        }
                    }

                    for (VBoxOSBO vBoxOSBO : listaDeAlteracoes) {
                        GridPane.setConstraints(vBoxOSBO, vBoxOSBO.getColuna(), vBoxOSBO.getOrdemProp());
                        try {
                            ServicoCouchBase.getInstancia().actualizarOrdem(vBoxOSBO);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    event.consume();
                }

                if (object instanceof HBoxOSAprovisionamento) {
                    Dragboard dragboard = event.getDragboard();
                    HBoxOSAprovisionamento hBoxOSAprovisionamento = (HBoxOSAprovisionamento) dragboard.getContent(DataFormat.RTF);
                    LocalDateTime dataDeCorte = Singleton.getInstancia().dataInicioAgenda.plusDays(coluna);
                    out.println("Colocar o aprovisionamento " + hBoxOSAprovisionamento.getId() + " em CORTE na data " + Funcoes.dToCZeroHour(dataDeCorte));
                    ArtigoAprovisionamento artigoAprovisionamento = hBoxOSAprovisionamento.getArtigoAprovisionamento();
                    artigoAprovisionamento.setDtcortef(dataDeCorte);
                    try {
                        BamerSqlServer.getInstancia().actualizar_De_Aprovisionamento_para_Corte(artigoAprovisionamento);
                    } catch (SQLException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    event.consume();
                }
            }
        });
    }

    void setColuna(int coluna) {
        this.coluna = coluna;
        textDiaDaSemana.setId("header0" + coluna);
        textQtd.setId("qtttot" + coluna);
        textQtdFeita.setId("qttfeita" + coluna);
    }

    public void setDataText(String dataText) {
        textDiaDaSemana.setText(dataText);
    }

    public void setTextoDiaMes(String textoDiaMes) {
        textDiaMes.setText(textoDiaMes);
    }

    public void setTextoSemana(String textoSemana) {
        textSemana.setText(textoSemana);
    }
}
