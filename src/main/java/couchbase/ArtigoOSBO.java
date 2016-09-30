package couchbase;


import com.couchbase.lite.CouchbaseLiteException;
import utils.Funcoes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ArtigoOSBO {
    private final String bostamp;
    private final int obrano;
    private final String fref;
    private final String nmfref;
    private final String estado;
    private final String seccao;
    private final String obs;
    private final int cor;
    private long tempoParcial = 0L;
    private long tempoTotal = 0L;
    private LocalDateTime dtcortef;
    private final LocalDateTime dttransf;
    private final LocalDateTime dtembala;
    private final LocalDateTime dtexpedi;
    private int ordem;

    public ArtigoOSBO(String bostamp, int obrano, String fref, String nmfref, String estado, String seccao, String obs, String dtcortef, String dttransf, String dtembala, String dtexpedi, int ordem, int cor) {
        this.bostamp = bostamp;
        this.obrano = obrano;
        this.fref = fref;
        this.nmfref = nmfref;
        this.estado = estado;
        this.seccao = seccao;
        this.obs = obs;
        this.dtcortef = Funcoes.cToT(dtcortef);
        this.dttransf = Funcoes.cToT(dttransf);
        this.dtembala = Funcoes.cToT(dtembala);
        this.dtexpedi = Funcoes.cToT(dtexpedi);
        this.ordem = ordem;
        this.cor = cor;
        try {
            this.tempoTotal = ServicoCouchBase.getInstancia().getTempoTotal(bostamp);
        } catch (IOException | CouchbaseLiteException e) {
            e.printStackTrace();
        }
        try {
            this.tempoParcial = ServicoCouchBase.getInstancia().getUltimoTempo(bostamp);
        } catch (IOException | CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public String getBostamp() {
        return bostamp;
    }

    public int getObrano() {
        return obrano;
    }

    public String getFref() {
        return fref;
    }

    public String getNmfref() {
        return nmfref;
    }

    public String getEstado() {
        return estado;
    }

    public String getSeccao() {
        return seccao;
    }

    public String getObs() {
        return obs;
    }

    public LocalDateTime getDtcortef() {
        return dtcortef.truncatedTo(ChronoUnit.DAYS);
    }

    public LocalDateTime getDttransf() {
        return dttransf.truncatedTo(ChronoUnit.DAYS);
    }

    public LocalDateTime getDtembala() {
        return dtembala.truncatedTo(ChronoUnit.DAYS);
    }

    public LocalDateTime getDtexpedi() {
        return dtexpedi.truncatedTo(ChronoUnit.DAYS);
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }

    public void setDtcortef(LocalDateTime dtcorte) {
        this.dtcortef = dtcorte;
    }

    public int getCor() {
        return cor;
    }

    public long getTempoParcial() {
        return tempoParcial;
    }

    public long getTempoTotal() {
        return tempoTotal;
    }
}
