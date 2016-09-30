package utils;


public class ValoresDefeito {
    //GLOBALS
    public static final String TITULO_APP = "Bamer Planeamento";

    //COUCHBASE
    public static final String COUCHBASE_BASE_DADOS = "bameros001";
    public static final String COUCHBASE_SERVER_URL = "http://192.168.0.3:4984";
//    public static final String COUCHBASE_SERVER_URL = "http://192.168.0.9:4984";
    public static final String NOSQL_COMPLETE_SERVER_URL = COUCHBASE_SERVER_URL + "/" + COUCHBASE_BASE_DADOS + "/";
    public static final String NOSQL_SYNC_USERID = "syncuser";
    public static final String NOSQL_SYNC_USER_PASSWORD = "SyncUser#10!";

    //SQL
    public static final String SQL_SERVIDOR = "192.168.0.3";
    public static final int SQL_PORTA = 1433;
    public static final String SQL_DB = "bamer";
    public static final String SQL_USER = "printserver";
    public static final String SQL_PASSWORD = "12345";

    //SQLITE
    public static final String BD_PREFERENCIAS = "dbprefs";

    public static final int AGENDA_NUMCOLS = 5;
    public static final String SECCAO = "10 - ALUMINIOS";
    public static final int COL_COMPRIMENTO = 160;
}
