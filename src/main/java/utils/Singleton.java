package utils;

import javafx.scene.control.ProgressIndicator;
import sqlite.PreferenciasEmSQLite;

import java.time.LocalDateTime;

///**
// * Created by miguel.silva on 08-06-2016.
// */
public class Singleton {
    private static Singleton singleton;
    public LocalDateTime dataInicioAgenda;
    private ProgressIndicator pi;

    public static Singleton getInstancia() {
        if (singleton == null) {
            singleton = new Singleton();
            PreferenciasEmSQLite prefs = PreferenciasEmSQLite.getInstancia();
        }
        return singleton;
    }

    public void setPi(ProgressIndicator pi) {
        pi.setProgress(100d);
        pi.setPrefWidth(30);
        this.pi = pi;
    }

    public ProgressIndicator getPi() {
        return pi;
    }
}
