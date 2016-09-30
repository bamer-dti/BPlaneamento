package couchbase;

import bamer.AppMain;
import com.couchbase.lite.*;
import com.couchbase.lite.auth.Authenticator;
import com.couchbase.lite.auth.AuthenticatorFactory;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressIndicator;
import objectos.GridPaneAprovisionamentos;
import objectos.GridPaneAtrasados;
import objectos.GridPaneCalendario;
import objectos.VBoxOSBO;
import sql.BamerSqlServer;
import utils.Funcoes;
import utils.Singleton;
import utils.ValoresDefeito;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

import static com.couchbase.lite.Document.TAG;
import static java.lang.System.out;

public class ServicoCouchBase {
    private static final String VERSAO_DOS_MAPAS = "74";
    private static final String VIEW_CENTROS_TRABALHO = "view_centros_trabalho";
    private static final String VIEW_BOSTAMPS_POR_DATA_SECCAO = "view_bostamps_por_data_seccao";
    private static final String VIEW_DELETE_OSBI = "view_delete_osbi";
    private static final String VIEW_OS_PROD_LIVE = "view_os_prod_live";
    private static final String VIEW_DELETE_OSPROD = "view_delete_osprod";
    private static final String VIEW_DOCS_ATRASADOS = "view_docs_atrasados";
    private static final String VIEW_DOCS_POR_PLANEAR = "view_docs_por_planear";
    private static ServicoCouchBase servicoCouchBase;
    private final Manager manager;
    private final Database database;
    private final OnSyncProgressChangeObservable syncroProgress;
    private Replication pullReplication;
    private Replication pushReplication;

    private ArrayList<ArtigoOSBO> listaDocsOSBO;
    private boolean isBusy;

    private View viewOSBIcentrosTrabalho;
    private View viewOSBIpecasPorDossier;
    private View viewOSPRODpecasFeitasPorDossier;
    private View viewOSBIporBostamp;
    private View viewOSBIbostampsPorDataESeccao;
    private View viewLiveDeleteOSBI;
    private View viewLiveOSPROD;
    private View viewLiveDeleteOSPROD;
    private View viewOSAprov;
    private View viewOSAtrasos;
    private View viewNotas;

    private LiveQuery liveQueryOSPROD;
    public LiveQuery liveQueryAddChangeDocs;
    private LiveQuery liveQueryDeleteOSBI;
    private LiveQuery liveQueryDeleteOSPROD;
    private LiveQuery liveQueryAprovisionamentos;
    private LiveQuery liveQueryAtrasados;
    private LiveQuery liveQueryNotas;
    private LiveQuery liveQueryTempos;

    private ArrayList<QueryRow> listaAprovisionamento;
    private List<QueryRow> listaAtrasados;
    private View viewTempos;
    public View viewTemposPorDossier;
    public View viewOSPRODporBostamp;
    public View viewOSPRODQtdZero;
    public View viewOSPRODs;
    private View viewOSBO;
    public View viewOSBI;
    private View viewOSBOporData;

    private ServicoCouchBase() throws IOException, CouchbaseLiteException {
        Manager.enableLogging(Log.TAG_LISTENER, Log.ERROR);
        Manager.enableLogging(Log.TAG_SYNC_ASYNC_TASK, Log.ERROR);
        Manager.enableLogging(Log.TAG_SYNC, Log.ERROR);
        Manager.enableLogging(Log.TAG_QUERY, Log.ERROR);
        Manager.enableLogging(Log.TAG_VIEW, Log.ERROR);
        Manager.enableLogging(Log.TAG_DATABASE, Log.ERROR);
        Manager.enableLogging(Log.TAG_BATCHER, Log.ERROR);
        Manager.enableLogging(Log.TAG_ACTION, Log.ERROR);
        Manager.enableLogging(Log.TAG_DATABASE, Log.ERROR);
        Manager.enableLogging(Log.TAG, Log.ERROR);
        Manager.enableLogging(Log.TAG_BLOB_STORE, Log.ERROR);
        Manager.enableLogging(Log.TAG_CHANGE_TRACKER, Log.ERROR);
        Manager.enableLogging(Log.TAG_REMOTE_REQUEST, Log.ERROR);
        Manager.enableLogging(Log.TAG_ROUTER, Log.ERROR);
        Manager.enableLogging(Log.TAG_SYMMETRIC_KEY, Log.ERROR);


        String subdir = "dados";
        Context contexto = new JavaContext(subdir);
        manager = new Manager(contexto, Manager.DEFAULT_OPTIONS);
        database = manager.getDatabase(ValoresDefeito.COUCHBASE_BASE_DADOS);
        database.addChangeListener(new Database.ChangeListener() {
            @Override
            public void changed(Database.ChangeEvent event) {
                List<DocumentChange> changeList = event.getChanges();
                out.println(Funcoes.currentTimeStringStamp(Funcoes.FORMATO_h_m_s) + " -----" + ValoresDefeito.COUCHBASE_BASE_DADOS + " DATABASE CHANGED -----");
                ArrayList<ArtigoOSBO> lstDocsOSBO = new ArrayList<>();
                int cotador = 0;
                for (DocumentChange documentChange : changeList) {
                    cotador++;
//                    updateSyncProgress(
//                            cotador,
//                            changeList.size(),
//                            Replication.ReplicationStatus.REPLICATION_ACTIVE
//                    );

                    String _id = documentChange.getDocumentId();
                    Document document = database.getDocument(_id);
                    if (!document.isDeleted()) {
                        if (document.getProperty(CamposCouch.FIELD_TIPO).toString().equals(CamposCouch.DOCTYPE_OSBO)) {
                            String bostamp = (String) document.getProperty(CamposCouch.FIELD_BOSTAMP);
                            int obrano = (int) document.getProperty(CamposCouch.FIELD_OBRANO);
                            String fref = (String) document.getProperty(CamposCouch.FIELD_FREF);
                            String nmfref = (String) document.getProperty(CamposCouch.FIELD_NMFREF);
                            String estado = (String) document.getProperty(CamposCouch.FIELD_ESTADO);
                            String seccao = (String) document.getProperty(CamposCouch.FIELD_SECCAO);
                            String obs = (String) document.getProperty(CamposCouch.FIELD_OBS);
                            String dtcortef = (String) document.getProperty(CamposCouch.FIELD_DTCORTEF);
                            String dttransf = (String) document.getProperty(CamposCouch.FIELD_DTTRANSF);
                            String dtembala = (String) document.getProperty(CamposCouch.FIELD_DTEMBALA);
                            String dtexpedi = (String) document.getProperty(CamposCouch.FIELD_DTEXPEDI);
                            int ordem = (int) document.getProperty(CamposCouch.FIELD_ORDEM);
                            int cor = 0;
                            if (document.getProperty(CamposCouch.FIELD_COR) == null) {
                                try {
                                    getInstancia().updateCor(document, 0);
                                } catch (CouchbaseLiteException | IOException e) {
                                    e.printStackTrace();
                                }
                            } else
                                cor = (int) document.getProperty(CamposCouch.FIELD_COR);

                            ArtigoOSBO artigo = new ArtigoOSBO(bostamp, obrano, fref, nmfref, estado, seccao, obs, dtcortef, dttransf, dtembala, dtexpedi, ordem, cor);
                            lstDocsOSBO.add(artigo);
                        }
                    } else {
                        AppMain.getInstancia().eliminar(document.getId());
                    }
                }
                try {
                    AppMain.getInstancia().inserirOuActualizarOSBO(lstDocsOSBO);
                } catch (IOException | CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            }
        });

        setupViews();

        syncroProgress = new OnSyncProgressChangeObservable();
    }

    public static ServicoCouchBase getInstancia() throws IOException, CouchbaseLiteException {
        if (servicoCouchBase == null) {
            servicoCouchBase = new ServicoCouchBase();
        }
        return servicoCouchBase;
    }

    private void setupViews() {
        viewOSBI = database.getView("viewOSBI");
        Mapper mapperb = new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                String tipoDeDocumento = (String) document.get(CamposCouch.FIELD_TIPO);
                if (CamposCouch.DOCTYPE_OSBI.equals(tipoDeDocumento)) {
                    emitter.emit(document.get(CamposCouch.FIELD_BISTAMP), document);
                }
            }
        };
        viewOSBI.setMap(mapperb, VERSAO_DOS_MAPAS);

        viewOSPRODs = database.getView("viewOSPRODs");
        mapperb = new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                String tipoDeDocumento = (String) document.get(CamposCouch.FIELD_TIPO);
                if (CamposCouch.DOCTYPE_OSPROD.equals(tipoDeDocumento)) {
                    emitter.emit(document.get(CamposCouch.FIELD_TIPO), document);
                }
            }
        };
        viewOSPRODs.setMap(mapperb, VERSAO_DOS_MAPAS);

        viewTemposPorDossier = database.getView("view_tempos_dossiers");
        mapperb = new Mapper() {
            public void map(Map<String, Object> document, Emitter emitter) {
                Log.i(TAG, "" + document.get(CamposCouch.FIELD_TIPO + " - " + document.get(CamposCouch.FIELD_ESTADO)));
                if (CamposCouch.DOCTYPE_TEMPOS.equals(document.get(CamposCouch.FIELD_TIPO))
                        && CamposCouch.ESTADO_01_CORTE.equals(document.get(CamposCouch.FIELD_ESTADO))
                        ) {
                    emitter.emit(Arrays.asList(
                            document.get(CamposCouch.FIELD_BOSTAMP)
                            , document.get(CamposCouch.FIELD_UNIXTIME)
                            , document.get(CamposCouch.FIELD_LASTTIME)
                    ), document);
                }
            }
        };
        viewTemposPorDossier.setMap(mapperb, VERSAO_DOS_MAPAS);


        viewTempos = database.getView("viewTempos");
        Mapper mappera = new Mapper() {
            public void map(Map<String, Object> document, Emitter emitter) {
                if (CamposCouch.DOCTYPE_TEMPOS.equals(document.get(CamposCouch.FIELD_TIPO))) {
                    emitter.emit(document.get(CamposCouch.FIELD_BOSTAMP), document);
                }
            }
        };
        viewTempos.setMap(mappera, VERSAO_DOS_MAPAS);

        viewNotas = database.getView("viewNotas");
        Mapper mapper = new Mapper() {
            public void map(Map<String, Object> document, Emitter emitter) {
                String tipoDoDocumento = (String) document.get(CamposCouch.FIELD_TIPO);
                if (CamposCouch.DOCTYPE_NOTAS.equals(tipoDoDocumento)) {
                    emitter.emit(document.get(CamposCouch.FIELD_BOSTAMP), document);
                }
            }
        };
        viewNotas.setMap(mapper, VERSAO_DOS_MAPAS);

        viewOSBO = database.getView("view_OSBO_");
        mapper = new Mapper() {
            public void map(Map<String, Object> document, Emitter emitter) {
                String tipoDoDocumento = (String) document.get(CamposCouch.FIELD_TIPO);
                if (CamposCouch.DOCTYPE_OSBO.equals(tipoDoDocumento)) {
                    emitter.emit(document.get(CamposCouch.FIELD_BOSTAMP), document);
                }
            }
        };
        viewOSBO.setMap(mapper, VERSAO_DOS_MAPAS);

        viewLiveDeleteOSBI = database.getView(VIEW_DELETE_OSBI);
        mapper = new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                String tipoDeDocumento = (String) document.get(CamposCouch.FIELD_TIPO);
                if (CamposCouch.DOCTYPE_DEL_OSBI.equals(tipoDeDocumento)) {
                    emitter.emit(document.get(CamposCouch.FIELD_BOSTAMP), document);
                }
            }
        };
        viewLiveDeleteOSBI.setMap(mapper, VERSAO_DOS_MAPAS);

        viewLiveDeleteOSPROD = database.getView(VIEW_DELETE_OSPROD);
        mapper = new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                String tipoDeDocumento = (String) document.get(CamposCouch.FIELD_TIPO);
                if (CamposCouch.DOCTYPE_DEL_OSPROD.equals(tipoDeDocumento)) {
                    emitter.emit(document.get(CamposCouch.FIELD_BOSTAMP), document);
                }
            }
        };
        viewLiveDeleteOSPROD.setMap(mapper, VERSAO_DOS_MAPAS);

        viewLiveOSPROD = database.getView(VIEW_OS_PROD_LIVE);
        mapper = new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                String tipoDeDocumento = (String) document.get(CamposCouch.FIELD_TIPO);
                if (CamposCouch.DOCTYPE_OSPROD.equals(tipoDeDocumento)) {
                    emitter.emit(document.get(CamposCouch.FIELD_BOSTAMP), document);
                }
            }
        };
        viewLiveOSPROD.setMap(mapper, VERSAO_DOS_MAPAS);

        viewOSBIporBostamp = database.getView("view_OSBI_por_bostamp");
        mapper = new Mapper() {
            public void map(Map<String, Object> document, Emitter emitter) {
                String tipoDoDocumento = (String) document.get(CamposCouch.FIELD_TIPO);
                if (CamposCouch.DOCTYPE_OSBI.equals(tipoDoDocumento)) {
                    emitter.emit(document.get(CamposCouch.FIELD_BOSTAMP), document);
                }
            }
        };
        viewOSBIporBostamp.setMap(mapper, VERSAO_DOS_MAPAS);

        viewOSPRODporBostamp = database.getView("view_osprod_por_bostamp");
        mapper = new Mapper() {
            public void map(Map<String, Object> document, Emitter emitter) {
                String tipoDoDocumento = (String) document.get(CamposCouch.FIELD_TIPO);
                if (CamposCouch.DOCTYPE_OSPROD.equals(tipoDoDocumento)) {
                    emitter.emit(
                            document.get(CamposCouch.FIELD_BOSTAMP)
                            , document);
                }
            }
        };
        viewOSPRODporBostamp.setMap(mapper, VERSAO_DOS_MAPAS);

        viewOSPRODQtdZero = database.getView("view_osprod_zero");
        mapper = new Mapper() {
            public void map(Map<String, Object> document, Emitter emitter) {
                String tipoDoDocumento = (String) document.get(CamposCouch.FIELD_TIPO);
                int qtt = (int) document.get(CamposCouch.FIELD_QTT);
                if (CamposCouch.DOCTYPE_OSPROD.equals(tipoDoDocumento) && qtt == 0) {
                    emitter.emit(
                            document.get(CamposCouch.FIELD_BOSTAMP)
                            , document);
                }
            }
        };
        viewOSPRODQtdZero.setMap(mapper, VERSAO_DOS_MAPAS);

        viewOSAprov = database.getView(VIEW_DOCS_POR_PLANEAR);
        mapper = new Mapper() {
            public void map(Map<String, Object> document, Emitter emitter) {
                String tipoDoDocumento = (String) document.get(CamposCouch.FIELD_TIPO);
                String estado = (String) document.get((CamposCouch.FIELD_ESTADO));
                String seccao = (String) document.get((CamposCouch.FIELD_SECCAO));
                if (CamposCouch.DOCTYPE_OSBO.equals(tipoDoDocumento)
                        &&
                        (seccao.equals(CamposCouch.SECCAO_ACOS)
                                || seccao.equals(CamposCouch.SECCAO_CARPINTARIA)
                                || seccao.equals(CamposCouch.SECCAO_ALUMINIOS)
                        )
                        &&
                        (CamposCouch.ESTADO_00_APROV.equals(estado)
                                || CamposCouch.ESTADO_00_PLANEAMENTO.equals(estado)
                        )
                        ) {
                    emitter.emit(Arrays.asList(document.get(CamposCouch.FIELD_SECCAO)
                            , document.get(CamposCouch.FIELD_OBRANO)
                            , document.get(CamposCouch.FIELD_BOSTAMP)), document);
                }
            }
        };
        viewOSAprov.setMap(mapper, VERSAO_DOS_MAPAS);

        viewOSAtrasos = database.getView(VIEW_DOCS_ATRASADOS);
        mapper = new Mapper() {
            public void map(Map<String, Object> document, Emitter emitter) {
                String tipoDoDocumento = (String) document.get(CamposCouch.FIELD_TIPO);
                String estado = (String) document.get((CamposCouch.FIELD_ESTADO));
                if (CamposCouch.DOCTYPE_OSBO.equals(tipoDoDocumento)
                        && CamposCouch.ESTADO_01_CORTE.equals(estado)
                        ) {
                    emitter.emit(Arrays.asList(document.get(CamposCouch.FIELD_SECCAO)
                            , document.get(CamposCouch.FIELD_OBRANO)
                            , document.get(CamposCouch.FIELD_BOSTAMP)), document);
                }
            }
        };
        viewOSAtrasos.setMap(mapper, VERSAO_DOS_MAPAS);
        viewOSBIcentrosTrabalho = database.getView(VIEW_CENTROS_TRABALHO);
        mapper = new Mapper() {
            public void map(Map<String, Object> document, Emitter emitter) {
                String tipoDoDocumento = (String) document.get(CamposCouch.FIELD_TIPO);
                if (CamposCouch.DOCTYPE_OSBO.equals(tipoDoDocumento)) {
                    emitter.emit(document.get(CamposCouch.FIELD_SECCAO), document.get(CamposCouch.FIELD_SECCAO));
                }
            }
        };
        viewOSBIcentrosTrabalho.setMap(mapper, VERSAO_DOS_MAPAS);

        viewOSBIpecasPorDossier = database.getView("pecasPorDossier");
        viewOSBIpecasPorDossier.setMapReduce(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                String tipoDoDocumento = (String) document.get(CamposCouch.FIELD_TIPO);
                if (CamposCouch.DOCTYPE_OSBI.equals(tipoDoDocumento)) {
                    emitter.emit(document.get(CamposCouch.FIELD_BOSTAMP), document.get(CamposCouch.FIELD_QTT));
                }
            }
        }, new Reducer() {
            @Override
            public Object reduce(List<Object> keys, List<Object> values, boolean rereduce) {
                int total = 0;
                for (Object value : values) {
                    int newVal;
                    if (value instanceof Double) {
                        newVal = ((Double) value).intValue();
                    } else {
                        newVal = (int) value;
                    }

                    total += newVal;
                }
                return total;
            }
        }, VERSAO_DOS_MAPAS);

        viewOSPRODpecasFeitasPorDossier = database.getView("pecasFeitasPorDossier");
        viewOSPRODpecasFeitasPorDossier.setMapReduce(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                if (CamposCouch.DOCTYPE_OSPROD.equals(document.get(CamposCouch.FIELD_TIPO))) {
                    emitter.emit(document.get(CamposCouch.FIELD_BOSTAMP), document.get(CamposCouch.FIELD_QTT));
                }
            }
        }, new Reducer() {
            @Override
            public Object reduce(List<Object> keys, List<Object> values, boolean rereduce) {
                int total = 0;
                for (Object value : values) {
                    int newVal;
                    if (value instanceof Double) {
                        newVal = ((Double) value).intValue();
                    } else {
                        newVal = (int) value;
                    }
                    total += newVal;
                }
                return total;
            }
        }, VERSAO_DOS_MAPAS);

        viewOSBIbostampsPorDataESeccao = database.getView(VIEW_BOSTAMPS_POR_DATA_SECCAO);
        mapper = new Mapper() {
            public void map(Map<String, Object> document, Emitter emitter) {
                String tipoDoDocumento = (String) document.get(CamposCouch.FIELD_TIPO);
                String estado = (String) document.get((CamposCouch.FIELD_ESTADO));
                if (CamposCouch.DOCTYPE_OSBI.equals(tipoDoDocumento)
                        && CamposCouch.ESTADO_01_CORTE.equals(estado)
                        ) {
                    emitter.emit(Arrays.asList(
                            document.get(CamposCouch.FIELD_DTCORTEF)
                            , document.get(CamposCouch.FIELD_SECCAO)
                            , document.get(CamposCouch.FIELD_BOSTAMP)
                    ), null);
                }
            }
        };
        viewOSBIbostampsPorDataESeccao.setMap(mapper, VERSAO_DOS_MAPAS);

        viewOSBOporData = database.getView("viesOSBOporData");
        mapper = new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                String tipoDoDocumento = (String) document.get(CamposCouch.FIELD_TIPO);
                if (CamposCouch.DOCTYPE_OSBO.equals(tipoDoDocumento)
                        && document.get(CamposCouch.FIELD_ESTADO).equals(CamposCouch.ESTADO_01_CORTE)
                        ) {
                    emitter.emit(Arrays.asList(
                            document.get(CamposCouch.FIELD_DTCORTEF)
                            , document.get(CamposCouch.FIELD_SECCAO)
                    ), document.get(CamposCouch.FIELD_BOSTAMP));
                }
            }
        };
        viewOSBOporData.setMap(mapper, VERSAO_DOS_MAPAS);

    }

    private void liveDeleteOSBI() {
        Query query = viewLiveDeleteOSBI.createQuery();
        liveQueryDeleteOSBI = query.toLiveQuery();
        liveQueryDeleteOSBI.addChangeListener(new LiveQuery.ChangeListener() {
            @Override
            public void changed(LiveQuery.ChangeEvent event) {
                for (int i = 0; i < event.getRows().getCount(); i++) {
                    if (i == 0) {
                        out.println(Funcoes.currentTimeStringStamp(Funcoes.FORMATO_h_m_s) + " ----- liveDeleteOSBI ----");
                    }
                    QueryRow row = event.getRows().getRow(i);
                    Document document = row.getDocument();
                    if (document.isDeleted()) {
                        continue;
                    }
                    String bostamp = row.getKey().toString();
                    eliminarOSBI(bostamp);
                    eliminarOSBO(bostamp);
                    try {
                        document.delete();
                        out.println("Eliminado o documento " + CamposCouch.DOCTYPE_DEL_OSBI + " id " + document.getId());
                    } catch (CouchbaseLiteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        liveQueryDeleteOSBI.start();
    }

    private void liveDeleteOSPROD() {
        Query query = viewLiveDeleteOSPROD.createQuery();
        liveQueryDeleteOSPROD = query.toLiveQuery();
        liveQueryDeleteOSPROD.addChangeListener(new LiveQuery.ChangeListener() {
            @Override
            public void changed(LiveQuery.ChangeEvent event) {
                for (int i = 0; i < event.getRows().getCount(); i++) {
                    if (i == 0) {
                        out.println(Funcoes.currentTimeStringStamp(Funcoes.FORMATO_h_m_s) + " ----- liveDeleteOSPROD ----");
                    }
                    QueryRow row = event.getRows().getRow(i);
                    Document document = row.getDocument();
                    if (document.isDeleted()) {
                        continue;
                    }
                    String bostamp = row.getKey().toString();
                    eliminarOSPROD(bostamp);
                    try {
                        document.delete();
                        out.println("Eliminado o documento " + CamposCouch.DOCTYPE_DEL_OSPROD + " id " + document.getId());
                    } catch (CouchbaseLiteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        liveQueryDeleteOSPROD.start();
    }

    private void liveOSPROD() {
        Query query = viewLiveOSPROD.createQuery();
        liveQueryOSPROD = query.toLiveQuery();
        liveQueryOSPROD.addChangeListener(new LiveQuery.ChangeListener() {
            @Override
            public void changed(LiveQuery.ChangeEvent event) {
                for (int i = 0; i < event.getRows().getCount(); i++) {
                    if (i == 0) {
                        out.println(Funcoes.currentTimeStringStamp(Funcoes.FORMATO_h_m_s) + " ----- liveOSPROD ----");
                    }
                    QueryRow row = event.getRows().getRow(i);
                    Document document = row.getDocument();
                    if (document.isDeleted()) {
                        continue;
                    }
                    String bostamp = row.getKey().toString();
                    AppMain.getInstancia().actualizarOSPRODqtt(bostamp);
                }
            }
        });
        liveQueryOSPROD.start();
    }

    private void liveAddChangeOSBI() {
        Query query = viewOSBO.createQuery();
        liveQueryAddChangeDocs = query.toLiveQuery();

        liveQueryAddChangeDocs.addChangeListener(new LiveQuery.ChangeListener() {
            @Override
            public void changed(LiveQuery.ChangeEvent event) {
                ArrayList<ArtigoOSBO> lstDocsOSBO = new ArrayList<>();
                for (int i = 0; i < event.getRows().getCount(); i++) {
                    if (i == 0) {
                        out.println(Funcoes.currentTimeStringStamp(Funcoes.FORMATO_h_m_s) + " ----- liveAddChangeOSBI ----");
                    }
                    QueryRow queryRow = event.getRows().getRow(i);
                    Document document = queryRow.getDocument();
                    @SuppressWarnings("unchecked")
                    String dtcortef = document.getProperty(CamposCouch.FIELD_DTCORTEF).toString();
                    Object ordem = document.getProperty(CamposCouch.FIELD_ORDEM);
                    String dttransf = document.getProperty(CamposCouch.FIELD_DTTRANSF).toString();
                    String dtembala = document.getProperty(CamposCouch.FIELD_DTEMBALA).toString();
                    String dtexpedi = document.getProperty(CamposCouch.FIELD_DTEXPEDI).toString();
                    String bostamp = document.getProperty(CamposCouch.FIELD_BOSTAMP).toString();
                    Object obrano = document.getProperty(CamposCouch.FIELD_OBRANO);
                    String fref = document.getProperty(CamposCouch.FIELD_FREF).toString();
                    String nmfref = document.getProperty(CamposCouch.FIELD_NMFREF).toString();
                    String estado = document.getProperty(CamposCouch.FIELD_ESTADO).toString();
                    String seccao = document.getProperty(CamposCouch.FIELD_SECCAO).toString();
                    String obs = document.getProperty(CamposCouch.FIELD_OBS).toString();
                    Object cor = document.getProperty(CamposCouch.FIELD_COR);
//                    Object ordem = calcularOrdem(bostamp, dtcortef, seccao);
                    ArtigoOSBO artigoOSBO = new ArtigoOSBO(bostamp, (int) obrano, fref, nmfref, estado, seccao, obs, dtcortef, dttransf, dtembala, dtexpedi, (int) ordem, (int) cor);
                    lstDocsOSBO.add(artigoOSBO);
                }
                if (lstDocsOSBO.size() > 0) {
                    listaDocsOSBO = lstDocsOSBO;
                    try {
                        AppMain.getInstancia().inserirOuActualizarOSBO(lstDocsOSBO);
                    } catch (IOException | CouchbaseLiteException e) {
                        e.printStackTrace();
                    }
                }
//                liveQueryAddChangeDocs.stop();
            }
        });

        liveQueryAddChangeDocs.start();
    }

    private void liveNotas() {
        liveQueryNotas = viewNotas.createQuery().toLiveQuery();
        liveQueryNotas.addChangeListener(new LiveQuery.ChangeListener() {
            @Override
            public void changed(LiveQuery.ChangeEvent event) {
                QueryEnumerator queryEnumerator = event.getRows();
                while (queryEnumerator.hasNext()) {
                    QueryRow queryRow = queryEnumerator.next();
                    Document document = queryRow.getDocument();
                    AppMain.getInstancia().actualizarNota(document);
                }
            }
        });
        liveQueryNotas.start();
    }

    private void liveTempos() {
        liveQueryTempos = viewTempos.createQuery().toLiveQuery();
        liveQueryTempos.addChangeListener(new LiveQuery.ChangeListener() {
            @Override
            public void changed(LiveQuery.ChangeEvent event) {
                out.println(Funcoes.currentTimeStringStamp(Funcoes.FORMATO_h_m_s) + " ----- liveTempos ----");
                QueryEnumerator queryEnumerator = event.getRows();
                while (queryEnumerator.hasNext()) {
                    QueryRow queryRow = queryEnumerator.next();
                    Document document = queryRow.getDocument();
                    String bostamp = document.getProperty(CamposCouch.FIELD_BOSTAMP).toString();
                    GridPaneCalendario cla = AppMain.getInstancia().getCalendario();
                    VBoxOSBO vBoxOSBO = (VBoxOSBO) cla.lookup("#" + bostamp);
                    if (vBoxOSBO != null) {
                        vBoxOSBO.tempoTotalPropProperty().set(Long.parseLong(document.getProperty(CamposCouch.FIELD_UNIXTIME).toString()));
                    }
                }
            }
        });
        liveQueryTempos.start();
    }

    public void liveAprovisionamento() {
        if (liveQueryAprovisionamentos == null) {
            out.println("liveAprovisionamento");
            Query query = viewOSAprov.createQuery();
            liveQueryAprovisionamentos = query.toLiveQuery();
            liveQueryAprovisionamentos.addChangeListener(new LiveQuery.ChangeListener() {
                @Override
                public void changed(LiveQuery.ChangeEvent event) {
                    out.println(Funcoes.currentTimeStringStamp(Funcoes.FORMATO_h_m_s) + " ----- liveQueryAprovisionamentos ----");
                    listaAprovisionamento = new ArrayList<>();
                    QueryEnumerator queryEnumerator = event.getRows();
                    while (queryEnumerator.hasNext()) {
                        QueryRow queryRow = queryEnumerator.next();
                        listaAprovisionamento.add(queryRow);
                    }
                    GridPaneAprovisionamentos.alimentarLista(listaAprovisionamento);
                }
            });
            liveQueryAprovisionamentos.start();
        }
    }

    public void liveAtrasados() {
        out.println("liveAtrasados");
        if (liveQueryAtrasados == null) {
            Query query = viewOSAtrasos.createQuery();
            LocalDateTime localData = Singleton.getInstancia().dataInicioAgenda.minusDays(1);
            int datafinal = Integer.parseInt(Funcoes.dToSQL(localData.toLocalDate()));
            out.println("DATA PARA ATRASOS: " + datafinal);
            liveQueryAtrasados = query.toLiveQuery();
            liveQueryAtrasados.addChangeListener(new LiveQuery.ChangeListener() {
                @Override
                public void changed(LiveQuery.ChangeEvent event) {
                    out.println(Funcoes.currentTimeStringStamp(Funcoes.FORMATO_h_m_s) + " ----- liveQueryAtrasados ----");
                    listaAtrasados = new ArrayList<>();
                    QueryEnumerator queryEnumerator = event.getRows();
                    out.println("REGISTOS ATRASADOS: " + queryEnumerator.getCount());
                    while (queryEnumerator.hasNext()) {
                        QueryRow queryRow = queryEnumerator.next();
                        Document document = queryRow.getDocument();
                        String data = document.getProperty(CamposCouch.FIELD_DTCORTEF).toString();
                        String dataSql = Funcoes.cToSQL(data);
                        int dataRow = Integer.parseInt(dataSql);
                        boolean podeColocar = true;
                        String seccao = document.getProperty(CamposCouch.FIELD_SECCAO).toString();
                        if (dataRow > datafinal)
                            podeColocar = false;

                        if (!seccao.equals(CamposCouch.SECCAO_ACOS)
                                && !seccao.equals(CamposCouch.SECCAO_ALUMINIOS)
                                && !seccao.equals(CamposCouch.SECCAO_CARPINTARIA)
                                )
                            podeColocar = false;

                        if (podeColocar)
                            listaAtrasados.add(queryRow);

                    }
                    GridPaneAtrasados.alimentarLista(listaAtrasados);
                }
            });
        }
        liveQueryAtrasados.start();
    }

    private void eliminarOSPROD(String bostamp) {
        Query query = viewOSPRODporBostamp.createQuery();
        query.setStartKey(bostamp);
        query.setEndKey(bostamp);
        try {
            QueryEnumerator queryEnumerator = query.run();
            while (queryEnumerator.hasNext()) {
                QueryRow row = queryEnumerator.next();
                Document document = row.getDocument();
                out.println("A ELIMINAR O REGISTO OSPROD " + document.getId());
                AppMain.getInstancia().actualizarOSPRODqtt(bostamp);
                AppMain.getInstancia().actualizarTextoColunasZero();
                document.delete();
            }

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    private void eliminarOSBI(String bostamp) {
        out.println("Tentar eliminar OSBI com bostamp = " + bostamp);
        Query query = viewOSBIporBostamp.createQuery();
        query.setStartKey(bostamp);
        query.setEndKey(bostamp);
        try {
            QueryEnumerator queryEnumerator = query.run();
            while (queryEnumerator.hasNext()) {
                QueryRow row = queryEnumerator.next();
                Document document = row.getDocument();
                out.println("A ELIMINAR O REGISTO OSBI " + document.getId());
                document.delete();
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    private void eliminarOSBO(String bostamp) {
        out.println("Tentar eliminar OSBO com bostamp = " + bostamp);
        Query query = viewOSBO.createQuery();
        query.setStartKey(bostamp);
        query.setEndKey(bostamp);
        try {
            QueryEnumerator queryEnumerator = query.run();
            while (queryEnumerator.hasNext()) {
                QueryRow row = queryEnumerator.next();
                Document document = row.getDocument();
                out.println("A ELIMINAR O REGISTO OSBO " + document.getId());
                document.delete();
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    // TODO: 28-09-2016 SYNCRO
    private synchronized void updateSyncProgress(int completedCount, int totalCount, Replication.ReplicationStatus status) {
        syncroProgress.notifyChanges(completedCount, totalCount, status);
        isBusy = (completedCount != totalCount);
        ProgressIndicator pi = Singleton.getInstancia().getPi();
        double percentagem = totalCount == 0 ? 100f : completedCount * 1f / totalCount * 1f * 100f;
        out.println("SYNCRO " + completedCount + " / " + totalCount + " = " + percentagem);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (totalCount != completedCount) {
                    pi.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                } else {
                    pi.setProgress(100f);
                }
            }
        });
    }

    private void iniciarReplicadorPull(Boolean continuous) throws MalformedURLException {
        Authenticator basicAuthenticator = AuthenticatorFactory.createBasicAuthenticator(ValoresDefeito.NOSQL_SYNC_USERID, ValoresDefeito.NOSQL_SYNC_USER_PASSWORD);

        String urlString = ValoresDefeito.COUCHBASE_SERVER_URL
                + "/"
                + ValoresDefeito.COUCHBASE_BASE_DADOS
                + "/";


        URL syncUrl = new URL(urlString);

        pullReplication = database.createPullReplication(syncUrl);
        pullReplication.setAuthenticator(basicAuthenticator);
        pullReplication.setContinuous(continuous);

        Replication.ChangeListener changeListenerPull = new Replication.ChangeListener() {
            @Override
            public void changed(Replication.ChangeEvent event) {
                Replication replication = event.getSource();
                updateSyncProgress(
                        replication.getCompletedChangesCount(),
                        replication.getChangesCount(),
                        replication.getStatus()
                );
            }
        };

        pullReplication.addChangeListener(changeListenerPull);

        List<String> canais = new ArrayList<>();
        canais.add(CamposCouch.ESTADO_00_APROV);
        canais.add(CamposCouch.ESTADO_00_PLANEAMENTO);
        canais.add(CamposCouch.ESTADO_01_CORTE);
        canais.add(CamposCouch.DOCTYPE_OSPROD);
        canais.add(CamposCouch.DOCTYPE_TEMPOS);
        pullReplication.setChannels(canais);

        pullReplication.start();
    }

    private void iniciarReplicadorPush(Boolean continuous) throws MalformedURLException {
        Authenticator basicAuthenticator = AuthenticatorFactory.createBasicAuthenticator(ValoresDefeito.NOSQL_SYNC_USERID, ValoresDefeito.NOSQL_SYNC_USER_PASSWORD);

        String urlString = ValoresDefeito.NOSQL_COMPLETE_SERVER_URL;


        URL syncUrl = new URL(urlString);

        pushReplication = database.createPushReplication(syncUrl);
        pushReplication.setAuthenticator(basicAuthenticator);
        pushReplication.setContinuous(continuous);
        Replication.ChangeListener changeListener = new Replication.ChangeListener() {
            @Override
            public void changed(Replication.ChangeEvent event) {
                Replication replication = event.getSource();
                updateSyncProgress(
                        replication.getCompletedChangesCount(),
                        replication.getChangesCount(),
                        replication.getStatus()
                );
            }
        };
        pushReplication.addChangeListener(changeListener);
        pushReplication.start();
    }

    public void stopServicosCouchBase() {
        pushReplication.stop();
        pullReplication.stop();
        liveQueryAddChangeDocs.stop();
        liveQueryDeleteOSBI.stop();
        liveQueryDeleteOSPROD.stop();
        liveQueryOSPROD.stop();
        liveQueryNotas.stop();
        liveQueryAtrasados.stop();
        liveQueryAprovisionamentos.stop();
        liveQueryTempos.stop();
        database.close();
        manager.close();
        servicoCouchBase = null;
    }

    public void startNoSQL() {
        try {
            getInstancia().iniciarReplicadorPull(true);
            getInstancia().iniciarReplicadorPush(true);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        getInstancia().liveDeleteOSBI();
                        getInstancia().liveDeleteOSPROD();
                        getInstancia().liveAddChangeOSBI();
                        getInstancia().liveOSPROD();
                        getInstancia().liveNotas();
                        getInstancia().liveTempos();
                    } catch (IOException | CouchbaseLiteException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (IOException | CouchbaseLiteException e) {
            e.printStackTrace();
        }

    }

//    }

    public ArrayList<ArtigoOSBO> getListaDocsOSBO() {
        return listaDocsOSBO;
    }

    public View getViewOSBIcentrosTrabalho() {
        return viewOSBIcentrosTrabalho;
    }

    public int getPecasPorOS(String bostamp) {
        Query query = viewOSBIpecasPorDossier.createQuery();
        query.setStartKey(bostamp);
        query.setEndKey(bostamp);
        int somaQtt = 0;
        try {
            QueryEnumerator queryEnumerator = query.run();
            while (queryEnumerator.hasNext()) {
                QueryRow queryRow = queryEnumerator.next();
                somaQtt = (int) queryRow.getValue();
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        return somaQtt;
    }

    public int getPecasFeitasPorOS(String bostamp) {
        Query query = viewOSPRODpecasFeitasPorDossier.createQuery();
        query.setStartKey(bostamp);
        query.setEndKey(bostamp);
        int somaQtt = 0;
        try {
            QueryEnumerator queryEnumerator = query.run();
            while (queryEnumerator.hasNext()) {
                QueryRow queryRow = queryEnumerator.next();
                somaQtt = (int) queryRow.getValue();
//                out.println("QTT Feita por OS: " + somaQtt + " bostamp = " + bostamp);
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        return somaQtt;
    }

    public int getPecasPorData(String data) {
        String seccao = AppMain.getInstancia().getComboSeccao().getValue().toString();
        Query queryPorData = viewOSBOporData.createQuery();
        queryPorData.setStartKey(Arrays.asList(data, seccao));
        queryPorData.setEndKey(Arrays.asList(data, seccao));
        int somaQtt = 0;
        try {
            QueryEnumerator queryEnumerator = queryPorData.run();
            while (queryEnumerator.hasNext()) {
                QueryRow queryRow = queryEnumerator.next();
                String bostamp = queryRow.getValue().toString();
                somaQtt += ServicoCouchBase.getInstancia().getPecasPorOS(bostamp);
            }
        } catch (CouchbaseLiteException | IOException e) {
            e.printStackTrace();
        }
        return somaQtt;
    }

    public int getPecasFeitasPorData(String data) {
//        PreferenciasEmSQLite prefs = PreferenciasEmSQLite.getInstancia();
//        String seccao = prefs.get(Constantes.PREF_SECCAO, ValoresDefeito.SECCAO);
        String seccao = AppMain.getInstancia().getComboSeccao().getValue().toString();
        Query queryBostamps = viewOSBIbostampsPorDataESeccao.createQuery();
        queryBostamps.setStartKey(Arrays.asList(data, seccao, ""));
        queryBostamps.setEndKey(Arrays.asList(data, seccao, "Z"));
        queryBostamps.setGroupLevel(3);
        int somaQtt = 0;
        try {
            QueryEnumerator queryRows = queryBostamps.run();
            for (int i = 0; i < queryRows.getCount(); i++) {
                QueryRow row = queryRows.getRow(i);
                @SuppressWarnings("unchecked")
                List<Object> listaKeys = (List<Object>) row.getKey();
                String bostamp = listaKeys.get(2).toString();

                Query queryPorDossier = viewOSPRODpecasFeitasPorDossier.createQuery();
                queryPorDossier.setStartKey(bostamp);
                queryPorDossier.setEndKey(bostamp);
                QueryEnumerator queryEnumerator = queryPorDossier.run();
                while (queryEnumerator.hasNext()) {
                    QueryRow queryRow = queryEnumerator.next();
                    somaQtt += (int) queryRow.getValue();
//                    out.println(queryBostamps.getStartKey() + " -> (" + queryRows.getCount() + " dossiers); i: " + i + " = " + bostamp + " qttfeita = " + somaQtt);
                }
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        return somaQtt;
    }

    public void actualizarOrdem(VBoxOSBO vBoxOSBO) {
        ArtigoOSBO artigoOSBO = vBoxOSBO.getArtigoOSBOProp();
        try {
            BamerSqlServer.getInstancia().actualizarDataCorte(artigoOSBO);
        } catch (Exception e) {
            e.printStackTrace();
            Funcoes.alerta(e.getLocalizedMessage(), Alert.AlertType.ERROR);
        }
    }

    public void deleteOSBI(ArtigoOSBO artigoOSBO) {
        Query query = viewOSBIporBostamp.createQuery();
        query.setStartKey(artigoOSBO.getBostamp());
        query.setEndKey(artigoOSBO.getBostamp());
        try {
            QueryEnumerator queryEnumerator = query.run();
            while (queryEnumerator.hasNext()) {
                QueryRow row = queryEnumerator.next();
                Document document = row.getDocument();
                out.println("A ELIMINAR O REGISTO " + document.getId());
                document.delete();
            }

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public boolean isBusy() {
        return isBusy;
    }

    public View getViewNotas() {
        return viewNotas;
    }

    public Document criarDocumentoNota(String bostamp) throws CouchbaseLiteException {
        Map<String, Object> mapa = new HashMap<>();
        mapa.put(CamposCouch.FIELD_TIPO, CamposCouch.DOCTYPE_NOTAS);
        mapa.put(CamposCouch.FIELD_BOSTAMP, bostamp);
        mapa.put(CamposCouch.FIELD_TEXTO, "");
        mapa.put(CamposCouch.FIELD_UPDATED_AT, Funcoes.currentTimeStringStamp(Funcoes.FORMATO_A_M_DTh_m_s_sssZ));

        Document document = new Document(database, "NOTA_" + bostamp);
        UnsavedRevision revision = document.createRevision();
        revision.setUserProperties(mapa);
        revision.save();

        return document;
    }

    public View getViewTempos() {
        return viewTempos;
    }

    public long getUltimoTempo(String bostamp) throws IOException, CouchbaseLiteException {
        com.couchbase.lite.View view = ServicoCouchBase.getInstancia().viewTemposPorDossier;
        Query query = view.createQuery();
        String maxText = Long.MAX_VALUE + "";
        query.setStartKey(Arrays.asList(bostamp, "", ""));
        query.setEndKey(Arrays.asList(bostamp, maxText, maxText));
        long tempoUnix = 0;
        try {
            QueryEnumerator queryEnumerator = query.run();
            Document document;
            int lastPosition = 0;
            while (queryEnumerator.hasNext()) {
                QueryRow queryRow = queryEnumerator.next();
                document = queryRow.getDocument();
                Log.w(TAG, "tempoUnix: " + document.getProperty(CamposCouch.FIELD_UNIXTIME));
                tempoUnix = Long.parseLong(document.getProperty(CamposCouch.FIELD_UNIXTIME).toString());
                lastPosition = Integer.parseInt(document.getProperty(CamposCouch.FIELD_POSICAO).toString());
            }
            if (lastPosition != 1) {
                tempoUnix = 0;
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        return tempoUnix;
    }

    public long getTempoTotal(String bostamp) throws IOException, CouchbaseLiteException {
        com.couchbase.lite.View view = ServicoCouchBase.getInstancia().viewTemposPorDossier;
        Query query = view.createQuery();
        String maxText = Long.MAX_VALUE + "";
        query.setStartKey(Arrays.asList(bostamp, "", ""));
        query.setEndKey(Arrays.asList(bostamp, maxText, maxText));
        long tempoCalculado = 0;
        try {
            QueryEnumerator queryEnumerator = query.run();
            Document document;
            while (queryEnumerator.hasNext()) {
                QueryRow queryRow = queryEnumerator.next();
                document = queryRow.getDocument();
                if (Integer.parseInt(document.getProperty(CamposCouch.FIELD_POSICAO).toString()) == 2) {
                    long inicio = Long.parseLong(document.getProperty(CamposCouch.FIELD_LASTTIME).toString());
                    long fim = Long.parseLong(document.getProperty(CamposCouch.FIELD_UNIXTIME).toString());
                    tempoCalculado = tempoCalculado + (fim - inicio);
                }
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        return tempoCalculado;
    }

    public static class OnSyncProgressChangeObservable extends Observable {
        private void notifyChanges(int completedCount, int totalCount, Replication.ReplicationStatus status) {
            SyncProgress progress = new SyncProgress();
            progress.completedCount = completedCount;
            progress.totalCount = totalCount;
            progress.status = status;
            setChanged();
            notifyObservers(progress);
        }
    }

    private static class SyncProgress {
        int completedCount;
        int totalCount;
        Replication.ReplicationStatus status;
    }

    private void updateCor(Document document, int cor) throws CouchbaseLiteException {
        out.println("UPDATE cor " + cor + ", ID " + document.getId());
        Map<String, Object> propertiesMap = new HashMap<>();
        propertiesMap.putAll(document.getProperties());
        propertiesMap.put(CamposCouch.FIELD_COR, cor);
        document.putProperties(propertiesMap);
    }

    public ArrayList getListaAprovisionamento() {
        return listaAprovisionamento;
    }

    public List<QueryRow> getListaAtrasados() {
        return listaAtrasados;
    }

}
