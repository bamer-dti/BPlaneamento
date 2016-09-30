package objectos;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import sqlite.PreferenciasEmSQLite;
import utils.Constantes;
import utils.Singleton;
import utils.ValoresDefeito;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Locale;

///**
// * Created by miguel.silva on 08-06-2016.
// */
public class GridPaneCalendario extends GridPane {
    public static final int TIPO_GRELHA = 1;
    public static final int TIPO_TOPO = 2;
    private final int rows;
    private final int tipoCalendario;

    public GridPaneCalendario(int tipoCalendariotipoCalendario) {
        this.tipoCalendario = tipoCalendariotipoCalendario;
        this.rows = 1;
        construct();
    }

    private void construct() {
        this.setPadding(new Insets(0, 0, 0, 0));
        this.getStyleClass().add("game-grid");

        PreferenciasEmSQLite prefs = PreferenciasEmSQLite.getInstancia();
        int colunas = prefs.getInt(Constantes.PREF_AGENDA_NUMCOLS, ValoresDefeito.AGENDA_NUMCOLS);
        LocalDateTime data = LocalDateTime.now().minusDays(1);
        Singleton.getInstancia().dataInicioAgenda = data;

        for (int col = 0; col < colunas; col++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(20);
            getColumnConstraints().add(columnConstraints);
            for (int row = 0; row < rows; row++) {
                VBoxDia vBoxDia = new VBoxDia(row == 0 && col == 0 && tipoCalendario == TIPO_TOPO);
                vBoxDia.setColuna(col);
                String id = "vbx" + col + "|" + row;
//                out.println("BOX DIA id " + id);
                vBoxDia.setId(id);
                vBoxDia.getStyleClass().add("game-grid-cell");
                LocalDate localDate = data.toLocalDate().plusDays(col);
                if (row == 0) {
                    if (tipoCalendario == TIPO_TOPO
//                            || tipoCalendario == TIPO_GRELHA
                            ) {
                        vBoxDia.getStyleClass().add("first-row");
                        if (localDate.getDayOfWeek().equals(DayOfWeek.SATURDAY) || localDate.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                            vBoxDia.getStyleClass().add("fim-de-semana");
                        }
                        vBoxDia.setAlignment(Pos.CENTER);

                        String textoDiaDaSemana = localDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pt-PT"));
                        vBoxDia.setDataText(textoDiaDaSemana);

                        String textoDiaMes = String.format("%02d", localDate.getDayOfMonth()) + "." + String.format("%02d", localDate.getMonthValue());
                        vBoxDia.setTextoDiaMes(textoDiaMes);

                        TemporalField woy = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
                        int weekNumber = localDate.get(woy);

                        String textoSemana = String.format("%02d", weekNumber);
                        vBoxDia.setTextoDiaMes(textoDiaMes);

                        vBoxDia.setTextoSemana("S"+textoSemana);
                    } else {
                        vBoxDia.setManaged(false);
                    }

                } else {
                    if (localDate.getDayOfWeek().equals(DayOfWeek.SATURDAY) || localDate.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                        vBoxDia.getStyleClass().add("fim-de-semana");
                    }
                    GridPane.setVgrow(vBoxDia, Priority.ALWAYS);
                }
                this.add(vBoxDia, col, row);
            }
        }
    }
}
