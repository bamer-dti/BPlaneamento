package utils;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import static java.lang.System.out;

@SuppressWarnings("unused")
public class Funcoes {

    public static String FORMATO_A_M_DTh_m_s_sssZ = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static String FORMATO_h_m_s = "HH:mm:ss";

//    public static String textoEmUTF8(String texto) {
//        byte ptext[] = texto.getBytes(ISO_8859_1);
//        return new String(ptext, UTF_8);
//    }

    public static String dToCZeroHour(LocalDateTime data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00");
        String formattedDateTime = data.format(formatter);
        return formattedDateTime;
    }

    public static String dToC(LocalDateTime data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDateTime = data.format(formatter);
        return formattedDateTime;
    }

    public static Image iconeBamer() {
        URL icoURL = ClassLoader.getSystemResource("bamer.jpg");
        Image ico = new Image(icoURL.toExternalForm());
        out.println(ico.getWidth() + " x " + ico.getHeight());
        return ico;
    }

    public static Image imagemResource(String imagem, int largura, int altura) {
        URL icoURL = ClassLoader.getSystemResource(imagem);
        Image image = new Image(icoURL.toExternalForm(), largura, altura, true, true);
        return image;
    }

    public static String currentTimeStringStamp(String formato) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(formato);
        Calendar calendar = GregorianCalendar.getInstance();
        return dateFormatter.format(calendar.getTime());
    }

    public static LocalDateTime cToT(String datastring) {
        LocalDateTime data = Timestamp.valueOf(datastring).toLocalDateTime().truncatedTo(ChronoUnit.DAYS);
        return data;
    }

    public static LocalDateTime sToT(String datastring) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDate localDate = LocalDate.parse(datastring, formatter);
        LocalDateTime localDateTime = localDate.atStartOfDay();
        return localDateTime;
    }

    public static Node getNodeByRowColumnIndex(final int row, final int column, GridPane gridPane) {
        Node result = null;
        ObservableList<Node> childrens = gridPane.getChildren();
        for (Node node : childrens) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                result = node;
                break;
            }
        }
        return result;
    }

    public static String dToSQL(LocalDate value) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String valor = value.format(formatter);
        return valor;
    }

    public static String cToSQL(String data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(data, formatter);
        //Em sql
        formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String sqlData = dateTime.format(formatter);
        return sqlData;
    }

    public static String dataBonita(String data) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDateTime local = cToT(data);
        return dtf.format(local);
    }

    public static void alerta(String mensagem, Alert.AlertType tipoAlerta) {
        String titulo = "";
        switch (tipoAlerta) {
            case ERROR:
                titulo = "Erro";
                break;
            case INFORMATION:
                titulo = "Nota";
                break;
            case CONFIRMATION:
                titulo = "Resposta";
                break;
            case WARNING:
                titulo = "Aviso";
                break;
        }
//        mensagem = Funcoes.textoEmUTF8(mensagem);
        Alert alert = new Alert(tipoAlerta);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);

        // Get the Stage.
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();

// Add a custom icon.
        stage.getIcons().add(Funcoes.iconeBamer());

        alert.show();
    }

    public static void colocarEstilo(Node node, String estilo) {
        for (int i = 0; i < node.getStyleClass().size(); i++) {
            String style = node.getStyleClass().get(i);
            node.getStyleClass().remove(style);
        }
        node.getStyleClass().add(estilo);
    }

    public final static long ONE_SECOND = 1000;

    public final static long ONE_MINUTE = ONE_SECOND * 60;

    public final static long ONE_HOUR = ONE_MINUTE * 60;

    public final static long ONE_DAY = ONE_HOUR * 24;

    public static String milisegundos_em_HH_MM_SS(long millis) {
        String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
        return hms;
    }
}
