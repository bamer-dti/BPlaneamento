package objectos;

import bamer.AppMain;
import bamer.ControllerEditar;
import bamer.ControllerNotas;
import com.couchbase.lite.*;
import couchbase.ArtigoOSBO;
import couchbase.CamposCouch;
import couchbase.ServicoCouchBase;
import javafx.application.Platform;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import sql.BamerSqlServer;
import sqlite.PreferenciasEmSQLite;
import utils.Constantes;
import utils.Funcoes;
import utils.Singleton;
import utils.ValoresDefeito;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.lang.System.out;

public class VBoxOSBO extends VBox {
    private static final int COR_AZUL = 0;
    private static final int COR_AMARELO = 1;
    private static final int COR_VERMELHO = 2;
    private static final int COR_VERDE = 3;
    //    private final ArtigoOSBO artigoOSBO;
    private int coluna;
    private VBoxOSBO thisObject = this;
    private int linha;
    private SimpleObjectProperty<ArtigoOSBO> artigoOSBOProp = new SimpleObjectProperty<>();
    private SimpleStringProperty bostampProp = new SimpleStringProperty();
    private SimpleStringProperty frefProp = new SimpleStringProperty();
    private SimpleIntegerProperty obranoProp = new SimpleIntegerProperty();
    private SimpleStringProperty nmfrefProp = new SimpleStringProperty();
    private SimpleStringProperty estadoProp = new SimpleStringProperty();
    private SimpleStringProperty seccaoProp = new SimpleStringProperty();
    private SimpleStringProperty obsProp = new SimpleStringProperty();
    private SimpleObjectProperty<LocalDateTime> dtcortefProp = new SimpleObjectProperty<>();
    private SimpleObjectProperty<LocalDateTime> dttransfProp = new SimpleObjectProperty<>();
    private SimpleObjectProperty<LocalDateTime> dtembalaProp = new SimpleObjectProperty<>();
    private SimpleObjectProperty<LocalDateTime> dtexpediProp = new SimpleObjectProperty<>();
    private SimpleIntegerProperty qttProp = new SimpleIntegerProperty();
    private SimpleIntegerProperty qttProdProp = new SimpleIntegerProperty();
    private SimpleStringProperty notaProp = new SimpleStringProperty("");
    private SimpleIntegerProperty corProp = new SimpleIntegerProperty();
    private SimpleLongProperty tempoTotalProp = new SimpleLongProperty();

    private SimpleIntegerProperty ordemProp = new SimpleIntegerProperty();
    private ContextMenu contextMenu;
    private Label labelTempos;
    private SimpleLongProperty tempoParcialProp = new SimpleLongProperty();
    private Label labelCorte;
    private Label labelTransf;
    private Label labelEmbal;
    private Label labelExped;
    private Label labelProd;
    private Label labelResultado;
    private HBox hBoxNotificar;
    private ImageView imageNotas;
    private Timer timer;
    private Label labelQtt;

    public VBoxOSBO(ArtigoOSBO artigoOSBO) {
        setId(artigoOSBO.getBostamp());

        criarObjectos();

        configurarBinds();

        setArtigoOSBOProp(artigoOSBO);

        arranjinhos();

        colocarEmAgenda();

        configurarEventos();

        configurarContextMenu();
    }

    private void criarObjectos() {
        PreferenciasEmSQLite prefs = PreferenciasEmSQLite.getInstancia();
        int minWidth = prefs.getInt(Constantes.PREF_COMPRIMENTO_MINIMO, ValoresDefeito.COL_COMPRIMENTO);
        setPrefWidth(minWidth);
        setMinWidth(minWidth);

        hBoxNotificar = new HBox();
        setMargin(hBoxNotificar, new Insets(1));

        Image imageSource = Funcoes.imagemResource("notas.jpg", 16, 16);
        imageNotas = new ImageView(imageSource);
        imageNotas.setManaged(false);
        imageNotas.setVisible(false);
        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bindBidirectional(notaProp);
        Tooltip.install(imageNotas, tooltip);
        imageNotas.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    try {
                        View view = null;
                        try {
                            view = ServicoCouchBase.getInstancia().getViewNotas();
                            String bostamp = bostampProp.get();
                            Query query = view.createQuery();
                            query.setStartKey(bostamp);
                            query.setEndKey(bostamp);
                            QueryEnumerator queryEnumerator = query.run();
                            Document document;
                            if (queryEnumerator.getCount() > 0) {
                                document = queryEnumerator.next().getDocument();
                                if (document.getProperty(CamposCouch.FIELD_TEXTO) != null) {
                                    String textnota = document.getProperty(CamposCouch.FIELD_TEXTO).toString();
                                    if (!textnota.equals("")) {
                                        notaPropProperty().set(textnota);
                                    }
                                }
                            } else {
                                document = ServicoCouchBase.getInstancia().criarDocumentoNota(bostamp);
                            }
                            editarNota(document);
                        } catch (CouchbaseLiteException e) {
                            e.printStackTrace();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                event.consume();
            }
        });
        hBoxNotificar.getChildren().add(imageNotas);
        hBoxNotificar.setManaged(false);

        labelTempos = new Label("...");
        labelTempos.setManaged(false);
        Font font = labelTempos.getFont();
        labelTempos.setFont(Font.font(font.getFamily(), FontWeight.BLACK, font.getSize()));
        hBoxNotificar.getChildren().add(labelTempos);

        getChildren().add(hBoxNotificar);

//        //SECCAO
//        Label labelSeccao = new Label();
//        labelSeccao.setWrapText(true);
//        labelSeccao.textProperty().bind(estadoPropProperty());
//        setMargin(labelSeccao, new Insets(2, 2, 2, 2));
//        this.getChildren().add(labelSeccao);

        //OBRA
        Label labelObra = new Label();
        labelObra.setWrapText(true);
        labelObra.textProperty().bind(frefPropProperty().concat(new SimpleStringProperty("-")).concat(nmfrefPropPorpery()));
        setMargin(labelObra, new Insets(2, 2, 2, 2));
        this.getChildren().add(labelObra);

        //OBRANO
        Label labelObrano = new Label();
        StringExpression textoObrano = new SimpleStringProperty("OS ").concat(obranoPropProperty()).concat(" (").concat(ordemProp.asString()).concat(")");
        labelObrano.textProperty().bind(textoObrano);
        setMargin(labelObrano, new Insets(2, 2, 2, 2));
        this.getChildren().add(labelObrano);


        //OBS
        Label labelObs = new Label();
        labelObs.textProperty().bind(obsPropProperty());
        labelObs.setWrapText(true);
        setMargin(labelObs, new Insets(2, 2, 2, 2));
        getChildren().add(labelObs);

        //GRELHA DAS DATAS
        GridPane grelhaDatas = new GridPane();
        tooltip = new Tooltip("(duplo clique para editar os dados)");
        Tooltip.install(grelhaDatas, tooltip);
        ColumnConstraints coluna1 = new ColumnConstraints();
        coluna1.setHalignment(HPos.RIGHT);
        grelhaDatas.getColumnConstraints().add(coluna1);

        grelhaDatas.getColumnConstraints().add(new ColumnConstraints());
        ColumnConstraints coluna3 = new ColumnConstraints(Double.MIN_VALUE, Double.MIN_VALUE, Double.MAX_VALUE);
        coluna3.setHgrow(Priority.SOMETIMES);
        grelhaDatas.getColumnConstraints().add(coluna3);

        //CORTE
        labelCorte = new Label("crt");
        GridPane.setMargin(labelCorte, new Insets(2, 2, 2, 2));
        GridPane.setHalignment(labelCorte, HPos.RIGHT);
        grelhaDatas.add(labelCorte, 0, 0);
        labelCorte = new Label();

        GridPane.setMargin(labelCorte, new Insets(2, 2, 2, 2));
        grelhaDatas.add(labelCorte, 1, 0);

        //TRANSFORMAÇÃO
        labelTransf = new Label("trf");
        GridPane.setMargin(labelTransf, new Insets(2, 2, 2, 2));
        GridPane.setHalignment(labelTransf, HPos.RIGHT);
        grelhaDatas.add(labelTransf, 0, 1);
        labelTransf = new Label();

        GridPane.setMargin(labelTransf, new Insets(2, 2, 2, 2));
        grelhaDatas.add(labelTransf, 1, 1);

        //EMBALAGEM
        labelEmbal = new Label("emb");
        GridPane.setMargin(labelEmbal, new Insets(2, 2, 2, 2));
        GridPane.setHalignment(labelEmbal, HPos.RIGHT);
        grelhaDatas.add(labelEmbal, 0, 2);
        labelEmbal = new Label();

        GridPane.setMargin(labelEmbal, new Insets(2, 2, 2, 2));
        grelhaDatas.add(labelEmbal, 1, 2);

        //EXPEDIÇÃO
        labelExped = new Label("exp");
        GridPane.setMargin(labelExped, new Insets(2, 2, 2, 2));
        GridPane.setHalignment(labelExped, HPos.RIGHT);
        grelhaDatas.add(labelExped, 0, 3);
        labelExped = new Label();

        GridPane.setMargin(labelExped, new Insets(2, 2, 2, 2));
        grelhaDatas.add(labelExped, 1, 3);

        //QUANTIDADE
        labelQtt = new Label();
        Font fontBase = labelQtt.getFont();
        labelQtt.setFont(Font.font(fontBase.getFamily(), FontWeight.BOLD, FontPosture.REGULAR, 16f));
        labelQtt.setStyle("-fx-text-fill: blue; -fx-background-insets: 0, 1 1 1 0 ;");

        //QUANTIDADE PRODUZIDA
        labelProd = new Label();
        fontBase = labelProd.getFont();
        labelProd.setFont(Font.font(fontBase.getFamily(), FontWeight.BOLD, FontPosture.REGULAR, 16f));
        labelProd.setStyle("-fx-text-fill: red; -fx-background-insets: 0, 1 1 1 0 ;");

        labelResultado = new Label();
        fontBase = labelResultado.getFont();
        labelResultado.setFont(Font.font(fontBase.getFamily(), FontWeight.BOLD, FontPosture.REGULAR, 16f));
        labelResultado.setStyle("-fx-text-fill: green; -fx-background-insets: 0, 1 1 1 0 ;");

        HBox hBoxqtts = new HBox(labelQtt, labelProd, labelResultado);
        hBoxqtts.setAlignment(Pos.CENTER);
        HBox.setMargin(labelQtt, new Insets(2, 2, 2, 2));

        HBox.setMargin(labelProd, new Insets(2, 2, 2, 2));

        GridPane.setValignment(hBoxqtts, VPos.CENTER);
        GridPane.setHalignment(hBoxqtts, HPos.CENTER);
        grelhaDatas.add(hBoxqtts, 2, 0, 1, 4);
        grelhaDatas.setStyle("-fx-background-color: white; -fx-background-insets: 0, 1 1 1 0 ;");

        getChildren().add(grelhaDatas);
    }

    private void configurarBinds() {
//        out.println("TEMPO DO ARTIGO: " + artigoOSBOProp.get().getTempoTotal());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");

//        tempoTotalProp.set(artigoOSBOProp.get().getTempoTotal());

        corProp.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                String estilo = "game-grid-cell-" + newValue;
                Funcoes.colocarEstilo(thisObject, estilo);
            }
        });

        tempoTotalProp.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                String bostamp = bostampProp.get();

//                if (artigoOSBOProp.get().getObrano() == 162331) {
//                out.println("*****************************" + artigoOSBOProp.get().getBostamp() + ", " + notaProp.get() + ", " + newValue.longValue());
//                }
                long tempoCalculado = 0;
                try {
                    if (getPosicao(bostamp) == Constantes.STARTED) {
                        mostrarRegistoEmModoStarted(bostampProp.get());
                    } else {
                        if (timer != null) {
                            timer.cancel();
                            timer.purge();
                            timer = null;
                        }
                        tempoCalculado = ServicoCouchBase.getInstancia().getTempoTotal(bostamp);
                        if (tempoCalculado != 0) {
                            String textoTempo = Funcoes.milisegundos_em_HH_MM_SS(tempoCalculado * 1000);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    labelTempos.setText("" + textoTempo);
                                }
                            });

                        } else {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    labelTempos.setText("");
                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }

                boolean resultado = !(notaProp.get().equals("") && newValue.longValue() == 0);
                hBoxNotificar.setManaged(resultado);
                hBoxNotificar.setVisible(resultado);
                labelTempos.setManaged(newValue.longValue() != 0);
                labelTempos.setVisible(resultado);
            }

            private void mostrarRegistoEmModoStarted(String bostamp) throws IOException, CouchbaseLiteException {
                long tempoTotal = 0;
                long ultimoTempo = 0;
                try {
                    tempoTotal = ServicoCouchBase.getInstancia().getTempoTotal(bostamp);
                    ultimoTempo = ServicoCouchBase.getInstancia().getUltimoTempo(bostamp);

                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }

                final long finalTempoTotal = tempoTotal;
                final long finalUltimoTempo = ultimoTempo;
                TimerTask actualizarTempos = new TimerTask() {
                    @Override
                    public void run() {

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                long unixNow = System.currentTimeMillis() / 1000L;
                                long intervaloTempo = unixNow - finalUltimoTempo;
                                labelTempos.setText(
                                        "TT: " + Funcoes.milisegundos_em_HH_MM_SS(finalTempoTotal * 1000 + intervaloTempo * 1000)
                                                + " TP: " + Funcoes.milisegundos_em_HH_MM_SS(intervaloTempo * 1000)
                                );
                            }
                        });
                    }
                };
                if (timer != null) {
                    timer.cancel();
                    timer.purge();
                    timer = null;
                }
                timer = new Timer();
                timer.schedule(actualizarTempos, 1000, 1000);
            }
        });

        dtcortefProp.addListener(new ChangeListener<LocalDateTime>() {
            @Override
            public void changed(ObservableValue<? extends LocalDateTime> observable, LocalDateTime oldValue, LocalDateTime newValue) {
                labelCorte.setText(dtf.format(newValue));
            }
        });

        dttransfProp.addListener(new ChangeListener<LocalDateTime>() {
            @Override
            public void changed(ObservableValue<? extends LocalDateTime> observable, LocalDateTime oldValue, LocalDateTime newValue) {
                labelTransf.setText(dtf.format(newValue));
            }
        });

        dtembalaProp.addListener(new ChangeListener<LocalDateTime>() {
            @Override
            public void changed(ObservableValue<? extends LocalDateTime> observable, LocalDateTime oldValue, LocalDateTime newValue) {
                labelEmbal.setText(dtf.format(newValue));
            }
        });

        dtexpediProp.addListener(new ChangeListener<LocalDateTime>() {
            @Override
            public void changed(ObservableValue<? extends LocalDateTime> observable, LocalDateTime oldValue, LocalDateTime newValue) {
                labelExped.setText(dtf.format(newValue));
            }
        });

        qttProp.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                labelQtt.setText("" + newValue.intValue());
            }
        });

        qttProdProp.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.intValue() == 0) {
                    labelProd.setText("");
                    labelResultado.setText("");
                } else {
                    labelProd.setText("-" + newValue);
                    labelResultado.setText("=" + (qttProp.get() - qttProdProp.get()));
                }
                AppMain.getInstancia().actualizarTextoColunasZero(coluna);
            }
        });

        notaProp.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                boolean resultado = !(newValue.equals("") && tempoTotalProp.get() == 0);
                out.println("notaProp listener: " + resultado);
                hBoxNotificar.setManaged(resultado);
                hBoxNotificar.setVisible(resultado);
                imageNotas.setManaged(!newValue.equals(0));
                imageNotas.setVisible(true);
            }
        });
    }

    public static int getPosicao(String bostamp) throws IOException, CouchbaseLiteException {
        com.couchbase.lite.View view = ServicoCouchBase.getInstancia().viewTemposPorDossier;
        Query query = view.createQuery();
        String maxText = Long.MAX_VALUE + "";
        query.setStartKey(Arrays.asList(bostamp, "", ""));
        query.setEndKey(Arrays.asList(bostamp, maxText, maxText));
        int posicaoSQL = 0;
        try {
            QueryEnumerator queryEnumerator = query.run();
            Document document = null;
            while (queryEnumerator.hasNext()) {
                QueryRow queryRow = queryEnumerator.next();
                document = queryRow.getDocument();
            }
            if (document != null) {
                posicaoSQL = Integer.parseInt(document.getProperty(CamposCouch.FIELD_POSICAO).toString());
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        return posicaoSQL;
    }

    public void setArtigoOSBOProp(ArtigoOSBO artigoOSBO) {
        artigoOSBOProp.set(artigoOSBO);
        setBostampProp(artigoOSBO.getBostamp());
        frefProp.set(artigoOSBO.getFref());
        nmfrefProp.set(artigoOSBO.getNmfref());
        obranoProp.set(artigoOSBO.getObrano());
        estadoProp.set(artigoOSBO.getEstado());
        seccaoProp.set(artigoOSBO.getSeccao());
        obsProp.set(artigoOSBO.getObs());

        dtcortefProp.set(artigoOSBO.getDtcortef());
        dttransfProp.set(artigoOSBO.getDttransf());
        dtembalaProp.set(artigoOSBO.getDtembala());
        dtexpediProp.set(artigoOSBO.getDtexpedi());

        ordemProp.set(artigoOSBO.getOrdem());

        corProp.set(artigoOSBO.getCor());

        tempoTotalProp.set(artigoOSBO.getTempoTotal());
        tempoParcialProp.set(artigoOSBO.getTempoParcial());

        coluna = calcularColuna();

        if (artigoOSBO.getOrdem() == 0) {
            try {
                ServicoCouchBase.getInstancia().deleteOSBI(artigoOSBO);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }

        if (artigoOSBO.getSeccao().trim().equals("")) {
            try {
                ServicoCouchBase.getInstancia().deleteOSBI(artigoOSBO);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
    }

    private void arranjinhos() {
        String estilo = "game-grid-cell-" + getArtigoOSBOProp().getCor();
        Funcoes.colocarEstilo(this, estilo);
    }

    private void colocarEmAgenda() {
        if (coluna < 0)
            return;
        GridPaneCalendario calendario = AppMain.getInstancia().getCalendario();
        linha = getOrdemProp();
        VBoxOSBO contexto = this;

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
//                calendario.getChildren().remove()
                calendario.add(contexto, coluna, linha);
//                out.println("Coluna " + coluna + ", linha " + linha + ", rows: " + calendario.getRowConstraints().size());
            }
        });
    }

    private void configurarEventos() {
        VBoxOSBO source = this;
        setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    try {
                        abrirEdicao();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    event.consume();
                }
                if (event.isSecondaryButtonDown() && !contextMenu.isShowing()) {
                    contextMenu.show(source, event.getScreenX(), event.getScreenY());
                    event.consume();
                }
                if (event.isSecondaryButtonDown() && contextMenu.isShowing()) {
                    contextMenu.hide();
                    contextMenu.show(source, event.getScreenX(), event.getScreenY());
                    event.consume();
                }
                if (event.isPrimaryButtonDown() && contextMenu.isShowing()) {
                    contextMenu.hide();
                }
            }
        });

        this.setOnDragDetected(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                Map<DataFormat, Object> map = new HashMap<>();
                map.put(DataFormat.RTF, thisObject);
                dragboard.setContent(map);
                event.consume();
            }
        });

        this.setOnDragEntered(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Object object = event.getDragboard().getContent(DataFormat.RTF);
                if (object instanceof VBoxOSBO) {
                    Dragboard dragboard = event.getDragboard();
                    VBoxOSBO vBoxOSBO = (VBoxOSBO) dragboard.getContent(DataFormat.RTF);
                    ArtigoOSBO artigoOSBO = vBoxOSBO.getArtigoOSBOProp();
                    if (!artigoOSBO.getBostamp().equals(thisObject.getId())) {
                        String idToFind = "#header0" + coluna;
                        Text mText = (Text) AppMain.getInstancia().getCalendario().lookup(idToFind);
                        Font font = mText.getFont();
                        mText.setFill(Color.INDIANRED);
                        mText.setFont(Font.font(font.getFamily(), FontWeight.BOLD, font.getSize()));
                    }
                    event.consume();
                }
            }
        });

        this.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Object object = event.getDragboard().getContent(DataFormat.RTF);
                if (object instanceof VBoxOSBO) {
                    Dragboard dragboard = event.getDragboard();
                    VBoxOSBO vBoxOSBOemDrag = (VBoxOSBO) dragboard.getContent(DataFormat.RTF);
                    ArtigoOSBO artigoOSBO = vBoxOSBOemDrag.getArtigoOSBOProp();
                    if (artigoOSBO.getBostamp().equals(thisObject.getId())) {
                        event.acceptTransferModes(TransferMode.NONE);
                    } else {
                        event.acceptTransferModes(TransferMode.MOVE);
                        disableEnabledObjects(thisObject, false);
                    }
                    event.consume();
                }
            }
        });

        this.setOnDragExited(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Object object = event.getDragboard().getContent(DataFormat.RTF);
                if (object instanceof VBoxOSBO) {
                    Dragboard dragboard = event.getDragboard();
                    VBoxOSBO vBoxOSBO = (VBoxOSBO) dragboard.getContent(DataFormat.RTF);
                    ArtigoOSBO artigoOSBO = vBoxOSBO.getArtigoOSBOProp();
                    if (artigoOSBO.getBostamp().equals(thisObject.getId())) {
                        event.acceptTransferModes(TransferMode.NONE);
                    } else {
                        String idToFind = "#header0" + coluna;
                        Text mText = (Text) AppMain.getInstancia().getCalendario().lookup(idToFind);
                        Font font = mText.getFont();
                        mText.setFill(Color.BLACK);
                        mText.setFont(Font.font(font.getFamily(), FontWeight.NORMAL, font.getSize()));
                        disableEnabledObjects(thisObject, true);
                    }
                    event.consume();
                }
            }
        });

        this.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Object object = event.getDragboard().getContent(DataFormat.RTF);
                if (object instanceof VBoxOSBO) {
                    Dragboard dragboard = event.getDragboard();
                    VBoxOSBO vboxEmDRAG = (VBoxOSBO) dragboard.getContent(DataFormat.RTF);
                    ArtigoOSBO artigoOSBOemDRAG = vboxEmDRAG.getArtigoOSBOProp();
                    if (artigoOSBOemDRAG.getBostamp().equals(getId())) {
                        event.consume();
                        return;
                    }
                    int ordemNova = getOrdemProp();
                    int ordemAnterior = artigoOSBOemDRAG.getOrdem();
                    LocalDateTime dataNova = getDtcortefProp();
                    LocalDateTime dataAnterior = artigoOSBOemDRAG.getDtcortef();
                    long days = dataNova.until(dataAnterior, ChronoUnit.DAYS);
                    GridPane gridPane = (GridPane) getParent();
                    ArrayList<VBoxOSBO> listaDeAlteracoes = new ArrayList<>();

                    if (days == 0) {//ALTEROU NO MESMO DIA
                        if (ordemNova > ordemAnterior) {
                            for (int i = ordemAnterior; i <= ordemNova; i++) {
                                Node node = Funcoes.getNodeByRowColumnIndex(i, getColuna(), gridPane);
                                if (!(node instanceof VBoxOSBO)) {
                                } else {
                                    VBoxOSBO vBoxOSBO = (VBoxOSBO) node;
                                    ArtigoOSBO artigoOSBO = vBoxOSBO.getArtigoOSBOProp();
                                    //É o objecto em DRAG
                                    if (artigoOSBO == artigoOSBOemDRAG) {
                                        artigoOSBO.setOrdem(ordemNova);
                                    } else {
                                        artigoOSBO.setOrdem(artigoOSBO.getOrdem() - 1);
                                    }
                                    vBoxOSBO.setOrdemProp(artigoOSBO.getOrdem());
                                    listaDeAlteracoes.add(vBoxOSBO);
                                }
                            }
                        }

                        if (ordemNova < ordemAnterior) {
                            for (int i = ordemNova; i <= ordemAnterior; i++) {
                                Node node = Funcoes.getNodeByRowColumnIndex(i, getColuna(), gridPane);
                                if (!(node instanceof VBoxOSBO)) {
                                } else {
                                    VBoxOSBO vBoxOSBO = (VBoxOSBO) node;
                                    ArtigoOSBO artigoOSBO = vBoxOSBO.getArtigoOSBOProp();
                                    //É o objecto em DRAG
                                    if (artigoOSBO == artigoOSBOemDRAG) {
                                        artigoOSBO.setOrdem(ordemNova);
                                    } else {
                                        artigoOSBO.setOrdem(artigoOSBO.getOrdem() + 1);
                                    }
                                    vBoxOSBO.setOrdemProp(artigoOSBO.getOrdem());
                                    listaDeAlteracoes.add(vBoxOSBO);
                                }
                            }
                        }
                    }

                    if (days != 0) {
                        ordemNova = getOrdemProp();
                        ordemAnterior = vboxEmDRAG.getOrdemProp();
                        int colunaAnterior = vboxEmDRAG.getColuna();
                        vboxEmDRAG.getArtigoOSBOProp().setDtcortef(getArtigoOSBOProp().getDtcortef());
                        vboxEmDRAG.setDtcortefProp(getDtcortefProp());
                        vboxEmDRAG.setOrdemProp(ordemNova);
                        vboxEmDRAG.setColuna(coluna);
                        listaDeAlteracoes.add(vboxEmDRAG);

                        //Na nova coluna
                        for (int i = 0; i < 200; i++) {
                            Node node = Funcoes.getNodeByRowColumnIndex(i, getColuna(), gridPane);
                            if (!(node instanceof VBoxOSBO)) {

                            } else {
                                VBoxOSBO vBoxOSBO = (VBoxOSBO) node;
                                ArtigoOSBO artigoOSBO = vBoxOSBO.getArtigoOSBOProp();
                                if (artigoOSBO != artigoOSBOemDRAG && artigoOSBO.getOrdem() >= ordemNova) {
                                    vBoxOSBO.setOrdemProp(vBoxOSBO.getOrdemProp() + 1);
                                    listaDeAlteracoes.add(vBoxOSBO);
                                }
                            }
                        }

                        //Na coluna anterior
                        for (int i = 0; i < 200; i++) {
                            Node node = Funcoes.getNodeByRowColumnIndex(i, colunaAnterior, gridPane);
                            if (!(node instanceof VBoxOSBO)) {

                            } else {
                                VBoxOSBO vBoxOSBO = (VBoxOSBO) node;
                                ArtigoOSBO artigoOSBO = vBoxOSBO.getArtigoOSBOProp();
                                if (artigoOSBO != artigoOSBOemDRAG && artigoOSBO.getOrdem() >= ordemAnterior) {
                                    vBoxOSBO.setOrdemProp(vBoxOSBO.getOrdemProp() - 1);
                                    listaDeAlteracoes.add(vBoxOSBO);
                                }
                            }
                        }

                    }

                    for (VBoxOSBO vBoxOSBO : listaDeAlteracoes) {
                        GridPane.setConstraints(vBoxOSBO, vBoxOSBO.getColuna(), vBoxOSBO.getOrdemProp());
                        try {
                            ServicoCouchBase.getInstancia().actualizarOrdem(vBoxOSBO);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (CouchbaseLiteException e) {
                            e.printStackTrace();
                        }
                    }
                    event.consume();
                }
            }
        });
    }

    private void configurarContextMenu() {
        contextMenu = new ContextMenu();

        Menu menuCor = new Menu("colorir");
        MenuItem menuItemAmarelo = new MenuItem("amarelo");
        menuItemAmarelo.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                pintar(COR_AMARELO);
            }
        });
        menuCor.getItems().add(menuItemAmarelo);

        MenuItem menuItemVermelho = new MenuItem("vermelho");
        menuItemVermelho.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                pintar(COR_VERMELHO);
            }
        });
        menuCor.getItems().add(menuItemVermelho);

        MenuItem menuItemAzul = new MenuItem("azul");
        menuItemAzul.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                pintar(COR_AZUL);
            }
        });
        menuCor.getItems().add(menuItemAzul);

        MenuItem menuItemVerde = new MenuItem("verde");
        menuItemVerde.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                pintar(COR_VERDE);
            }
        });
        menuCor.getItems().add(menuItemVerde);

        Menu menuAccoes = new Menu("tools");
        MenuItem itemNota = new MenuItem("notas");
        itemNota.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String bostamp = bostampProp.get();
                ServicoCouchBase instancia;
                try {
                    instancia = ServicoCouchBase.getInstancia();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                    return;
                }
                View view;
                view = instancia.getViewNotas();
                Query query = view.createQuery();
                query.setStartKey(bostamp);
                query.setEndKey(bostamp);
                QueryEnumerator queryEnumerator = null;
                try {
                    queryEnumerator = query.run();
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                    return;
                }
                out.println("Nº de notas encontradas: " + queryEnumerator.getCount());
                Document document;
                if (queryEnumerator.getCount() == 0) {
                    try {
                        document = instancia.criarDocumentoNota(bostamp);
                    } catch (CouchbaseLiteException e) {
                        e.printStackTrace();
                        return;
                    }
                } else {
                    document = queryEnumerator.next().getDocument();
                }
                try {
                    editarNota(document);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        menuAccoes.getItems().add(itemNota);

        contextMenu.getItems().addAll(menuCor, menuAccoes);
    }

    private void editarNota(Document document) throws IOException {
        URL location = ClassLoader.getSystemResource("editarNota.fxml");
        FXMLLoader loader = new FXMLLoader(location);
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Notas");
        stage.setScene(new Scene(root));
        ControllerNotas controller = loader.getController();
        controller.areaDoTexto.textProperty().bindBidirectional(notaPropProperty());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.show();

        stage.setOnHiding(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                try {
                    guardarNota(document, controller.areaDoTexto.getText());
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                    Funcoes.alerta("Erro ao gravar nota!", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void guardarNota(Document document, String text) throws CouchbaseLiteException {
        Map map = new HashMap<String, Object>();
        map.putAll(document.getProperties());
        map.put(CamposCouch.FIELD_TEXTO, text);
        document.putProperties(map);
    }

    private void pintar(int cor) {
        String estilo = "game-grid-cell-" + cor;
        Funcoes.colocarEstilo(this, estilo);
        if (cor != getArtigoOSBOProp().getCor()) {
            try {
                BamerSqlServer.getInstancia().actualizarCor(bostampProp.get(), cor);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void abrirEdicao() throws IOException {
        URL location = ClassLoader.getSystemResource("editarCompromisso.fxml");
        FXMLLoader loader = new FXMLLoader(location);
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Editar");
        stage.setScene(new Scene(root));
        ControllerEditar controller = loader.getController();

        StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        };

        controller.fref.setText(frefPropProperty().get() + "-" + nmfrefPropPorpery().get());
        controller.os.setText(obranoPropProperty().get() + "");
        controller.obs.setText(obsPropProperty().get());

        DatePicker dateObj = controller.dtcortef;
        dateObj.setConverter(converter);
        dateObj.setValue(dtcortefProp.get().toLocalDate());
        dateObj.setEditable(false);

        dateObj = controller.dttransf;
        dateObj.setConverter(converter);
        dateObj.setValue(dttransfPropProperty().get().toLocalDate());
        dateObj.setEditable(false);

        dateObj = controller.dtembala;
        dateObj.setConverter(converter);
        dateObj.setValue(dtembalaPropProperty().get().toLocalDate());
        dateObj.setEditable(false);

        dateObj = controller.dtexpedi;
        dateObj.setConverter(converter);
        LocalDate dexped = dtexpediPropProperty().get().toLocalDate();
        dateObj.setValue(dexped);
        out.print("Expedição = 1900? " + dexped.equals(LocalDate.of(1900, Month.JANUARY, 1)));
        dateObj.setEditable(false);
        dateObj.setDisable(dexped.equals(LocalDate.of(1900, Month.JANUARY, 1)));
//        dateObj.setStyle("-fx-opacity: 1");
//        dateObj.getEditor().setStyle("-fx-opacity: 1");

        Button botao_gravar = controller.btgravar;
        botao_gravar.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String dtcortef = Funcoes.dToSQL(controller.dtcortef.getValue());
                String dttransf = Funcoes.dToSQL(controller.dttransf.getValue());
                String dtembala = Funcoes.dToSQL(controller.dtembala.getValue());
                String dtexpedi = Funcoes.dToSQL(controller.dtexpedi.getValue());
                String estado = controller.combo_estado.getValue().toString();

                int rows = 0;
                try {
                    rows = BamerSqlServer.getInstancia().editarEactualizarDatas(bostampProp.get(), dtcortef, dttransf, dtembala, dtexpedi, estado);
                } catch (SQLException e) {
                    e.printStackTrace();
                    Funcoes.alerta(e.getLocalizedMessage(), Alert.AlertType.ERROR);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                if (rows > 0) {
                    dtcortefProp.set(controller.dtcortef.getValue().atStartOfDay());
                    dttransfPropProperty().set(controller.dttransf.getValue().atStartOfDay());
                    dtembalaProp.set(controller.dtembala.getValue().atStartOfDay());
                }

                stage.close();
            }
        });

        controller.setData(estadoProp.getValue());

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.show();
    }

    private void disableEnabledObjects(VBoxOSBO context, Boolean visible) {
        ObservableList<Node> childs = context.getChildren();
        for (Node node : childs) {
            if (node instanceof Label) {
                node.setVisible(visible);
            }
            if (node instanceof GridPane) {
                node.setVisible(visible);
            }
        }
    }

    private SimpleStringProperty frefPropProperty() {
        return frefProp;
    }

    private SimpleStringProperty nmfrefPropPorpery() {
        return nmfrefProp;
    }

    private SimpleStringProperty obsPropProperty() {
        return obsProp;
    }

    private int calcularColuna() {
        return (int) Singleton.getInstancia().dataInicioAgenda.toLocalDate().until(getDtcortefProp().toLocalDate(), ChronoUnit.DAYS);
    }

    public void setBostampProp(String bostamp) {
        try {
            int qtt = ServicoCouchBase.getInstancia().getPecasPorOS(bostamp);
            int qttProd = ServicoCouchBase.getInstancia().getPecasFeitasPorOS(bostamp);
            setQttProp(qtt);
            setQttProdProp(qttProd);
        } catch (IOException | CouchbaseLiteException e) {
            e.printStackTrace();
        }

        this.bostampProp.set(bostamp);
    }

    public SimpleIntegerProperty obranoPropProperty() {
        return obranoProp;
    }

    public void setQttProp(int qtt) {
        this.qttProp.set(qtt);
    }

    public void setQttProdProp(int qtt) {
        this.qttProdProp.set(qtt);
    }


    public LocalDateTime getDtcortefProp() {
        return dtcortefProp.get();
    }

    public void setDtcortefProp(LocalDateTime dtcorte) {
        dtcortefProp.set(dtcorte);
    }

    public SimpleObjectProperty<LocalDateTime> dttransfPropProperty() {
        return dttransfProp;
    }

    public SimpleObjectProperty<LocalDateTime> dtembalaPropProperty() {
        return dtembalaProp;
    }

    public SimpleObjectProperty<LocalDateTime> dtexpediPropProperty() {
        return dtexpediProp;
    }


    public int getOrdemProp() {
        return ordemProp.get();
    }

    public void setOrdemProp(int ordem) {
        getArtigoOSBOProp().setOrdem(ordem);
        ordemProp.set(ordem);
    }

    public ArtigoOSBO getArtigoOSBOProp() {
        return artigoOSBOProp.get();
    }

    public int getColuna() {
        return coluna;
    }

    public void setColuna(int coluna) {
        this.coluna = coluna;
    }

    public SimpleStringProperty notaPropProperty() {
        return notaProp;
    }

    public void setNotaProp(String notaProp) {
        this.notaProp.set(notaProp);
    }

    public SimpleLongProperty tempoTotalPropProperty() {
        return tempoTotalProp;
    }
}
