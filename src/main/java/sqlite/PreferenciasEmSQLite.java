package sqlite;

import utils.ValoresDefeito;

import java.sql.*;

import static java.lang.System.out;

///**
// * Created by miguel.silva on 09-05-2016.
// */
@SuppressWarnings("unused")
public class PreferenciasEmSQLite {
    private static final String TAG = PreferenciasEmSQLite.class.getSimpleName() + ": ";

    private static final String TABELA_PREFS = "tab_prefs";
    private static final String CAMPO_ID = "_id";
    private static final String CAMPO_NOME = "tnome";
    private static final String CAMPO_STRING = "tstr";
    private static final String CAMPO_BOOLEAN = "tbool";
    private static final String CAMPO_INTEGER = "tint";
    private static final String CAMPO_TIPO = "ttipo";

    private static final int TIPO_STRING = 1;
    private static final int TIPO_BOOLEAN = 2;
    private static final int TIPO_INT = 3;

    private static PreferenciasEmSQLite sqlDefaults;
    private static Connection connection;
    private static Statement statement;


    public static PreferenciasEmSQLite getInstancia() {
        if (sqlDefaults == null) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            sqlDefaults = new PreferenciasEmSQLite();
            try {
                sqlDefaults.connect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return sqlDefaults;
    }

    public void put(String pref, String valor) {
        //Existe?
        try {
            if (existePref(pref, TIPO_STRING) > 0) {
                updatePref(pref, valor, TIPO_STRING);
            } else {
                insertPref(pref, valor, TIPO_STRING);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void putBoolean(String pref, Boolean valor) {
        try {
            if (existePref(pref, TIPO_BOOLEAN) > 0) {
                updatePref(pref, valor, TIPO_BOOLEAN);
            } else {
                insertPref(pref, valor, TIPO_BOOLEAN);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void putInt(String pref, int valor) {
        try {
            if (existePref(pref, TIPO_INT) > 0) {
                updatePref(pref, valor, TIPO_INT);
            } else {
                insertPref(pref, valor, TIPO_INT);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String get(String pref, String valordefeito) {
        String valorRetorno = "*erro*";
        try {
            if (existePref(pref, TIPO_STRING) > 0) {
                valorRetorno = (String) selectPref(pref, TIPO_STRING);
            } else {
                insertPref(pref, valordefeito, TIPO_STRING);
                valorRetorno = valordefeito;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return valorRetorno;
    }

    public Boolean getBoolean(String pref, Boolean valordefeito) {
        Boolean valorRetorno = null;
        try {
            if (existePref(pref, TIPO_BOOLEAN) > 0) {
                int inteiro = (int) selectPref(pref, TIPO_BOOLEAN);
                valorRetorno = inteiro != 0;
            } else {
                insertPref(pref, valordefeito, TIPO_BOOLEAN);
                valorRetorno = valordefeito;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return valorRetorno;
    }

    public int getInt(String pref, int valordefeito) {
        int valorRetorno = 0;
        try {
            if (existePref(pref, TIPO_INT) > 0) {
                valorRetorno = (int) selectPref(pref, TIPO_INT);
            } else {
                insertPref(pref, valordefeito, TIPO_INT);
                valorRetorno = valordefeito;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return valorRetorno;
    }

    private Object selectPref(String pref, int tipo) throws SQLException {
        String textoSQL = null;
        Object valorRetorno = null;
        switch (tipo) {
            case TIPO_STRING:
                textoSQL = String.format(
                        "select " + CAMPO_STRING + " as VALOR from "
                                + TABELA_PREFS + " where "
                                + CAMPO_NOME + " = '%s' and " + CAMPO_TIPO + " = %d"
                        , pref, tipo);
                break;

            case TIPO_BOOLEAN:
                textoSQL = String.format(
                        "select " + CAMPO_BOOLEAN + " as VALOR from "
                                + TABELA_PREFS + " where "
                                + CAMPO_NOME + " = '%s' and " + CAMPO_TIPO + " = %d"
                        , pref, tipo);
                break;

            case TIPO_INT:
                textoSQL = String.format(
                        "select " + CAMPO_INTEGER + " as VALOR from "
                                + TABELA_PREFS + " where "
                                + CAMPO_NOME + " = '%s' and " + CAMPO_TIPO + " = %d"
                        , pref, tipo);
                break;
        }

        ResultSet resultSet = statement.executeQuery(textoSQL);
        while (resultSet.next()) {
            switch (tipo) {
                case TIPO_STRING:
                    valorRetorno = resultSet.getString("VALOR");
                    break;

                case TIPO_BOOLEAN:
                    valorRetorno = resultSet.getInt("VALOR");
                    break;

                case TIPO_INT:
                    valorRetorno = resultSet.getInt("VALOR");
                    break;
            }

        }

        return valorRetorno;
    }

    private void insertPref(String pref, Object valor, int tipo) throws SQLException {
        String textoSQL = null;
        switch (tipo) {
            case TIPO_STRING:
                textoSQL = String.format(
                        "insert into " + TABELA_PREFS + " ("
                                + CAMPO_NOME + ", "
                                + CAMPO_STRING + ", "
                                + CAMPO_TIPO + ")"
                                + " VALUES ("
                                + "'%s' , '%s', %s)"
                        , pref, valor, tipo);
                break;

            case TIPO_BOOLEAN:
                textoSQL = String.format(
                        "insert into " + TABELA_PREFS + " ("
                                + CAMPO_NOME + ", "
                                + CAMPO_BOOLEAN + ", "
                                + CAMPO_TIPO + ")"
                                + " VALUES ("
                                + "'%s' , %d, %s)"
                        , pref, (Boolean) valor ? 1 : 0, tipo);
                break;

            case TIPO_INT:
                //noinspection MalformedFormatString
                textoSQL = String.format(
                        "insert into " + TABELA_PREFS + " ("
                                + CAMPO_NOME + ", "
                                + CAMPO_INTEGER + ", "
                                + CAMPO_TIPO + ")"
                                + " VALUES ("
                                + "'%s' , %d, %s)"
                        , pref, valor, tipo);
                break;
        }

        statement.executeUpdate(textoSQL);

        out.println(TAG + "Pref SQL: " + textoSQL);
    }

    private void updatePref(String pref, Object valor, int tipo) throws SQLException {
        String textoSQL = null;
        switch (tipo) {
            case TIPO_STRING:
                textoSQL = String.format(
                        "update " + TABELA_PREFS + " set " + CAMPO_STRING + " = '%s' "
                                + " WHERE " + CAMPO_NOME + " = '%s' "
                        , valor, pref
                );
                break;
            case TIPO_BOOLEAN:
                textoSQL = String.format(
                        "update " + TABELA_PREFS + " set " + CAMPO_BOOLEAN + " = %d "
                                + " WHERE " + CAMPO_NOME + " = '%s' "
                        , (Boolean) valor ? 1 : 0, pref
                );
                break;

            case TIPO_INT:
                //noinspection MalformedFormatString
                textoSQL = String.format(
                        "update " + TABELA_PREFS + " set " + CAMPO_INTEGER + " = %d "
                                + " WHERE " + CAMPO_NOME + " = '%s' "
                        , valor, pref
                );
                break;
        }

        statement.executeUpdate(textoSQL);

        out.println(TAG + "Pref SQL: " + textoSQL);
    }

    private int existePref(String pref, int tipo) throws SQLException {
        String textoSQL = String.format(
                "select count(" + CAMPO_ID + ") as " + CAMPO_ID + " from " + TABELA_PREFS + " where " + CAMPO_NOME + " = '%s' and " + CAMPO_TIPO + " = %s"
                , pref, tipo
        );
        int linhas = 0;
        try {
            ResultSet resultSet = statement.executeQuery(textoSQL);
            while (resultSet.next()) {
                linhas = resultSet.getInt(CAMPO_ID);
            }
        } catch (SQLException e) {
            out.println("Erro ao executar SQLITE: " + textoSQL);
            e.printStackTrace();
        }
        return linhas;
    }

    private void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + ValoresDefeito.BD_PREFERENCIAS + ".db");
        statement = connection.createStatement();

//        String comandoSql = "DROP TABLE " + TABELA_PREFS;
//        statement.executeUpdate(comandoSql);

        String comandoSql = "CREATE TABLE IF NOT EXISTS " + TABELA_PREFS + "("
                + CAMPO_ID + " integer primary key autoincrement, "
                + CAMPO_NOME + " integer not null, "
                + CAMPO_STRING + " text, "
                + CAMPO_BOOLEAN + " integer, "
                + CAMPO_INTEGER + " integer, "
                + CAMPO_TIPO + " integer"
                + ")";
        statement.executeUpdate(comandoSql);
    }

    public void disconnect() {
        if (statement != null)
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        connection = null;
        statement = null;
    }
}
