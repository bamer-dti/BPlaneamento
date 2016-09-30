package sql;
//miguel.silva em 04-05-2016;

import couchbase.ArtigoAprovisionamento;
import couchbase.ArtigoOSBO;
import utils.Funcoes;
import utils.ValoresDefeito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static java.lang.System.out;

public class BamerSqlServer {
    private static final String TAG = BamerSqlServer.class.getSimpleName() + ": ";
    private static BamerSqlServer instancia;

    private LinkSQL abrirLinkSQL() throws ClassNotFoundException, SQLException {
        String sqlserver = ValoresDefeito.SQL_SERVIDOR;
        int sqlporta = ValoresDefeito.SQL_PORTA;
        String sqlbd = ValoresDefeito.SQL_DB;
        String sqluser = ValoresDefeito.SQL_USER;
        String sqlpass = ValoresDefeito.SQL_PASSWORD;

        if (sqlserver.equals("") || sqlbd.equals("") || sqluser.equals("") || sqlpass.equals("")) {
            out.println(TAG + "Dados em falta na ligação SQL");
            out.println(TAG + "Servidor: " + sqlserver);
            out.println(TAG + "Porta: " + sqlporta);
            out.println(TAG + "Base de Dados: " + sqlbd);
            out.println(TAG + "Utilizador: " + sqluser);
            out.println(TAG + "Password: " + (sqlpass.equals("") ? "não fornecida!" : "<existente>"));
            return null;
        }

        LinkSQL linkSQL = new LinkSQL();

        String connectionUrl = String.format("jdbc:sqlserver://%s:%s;databaseName=%s;user=%s;password=%s", sqlserver, sqlporta, sqlbd, sqluser, sqlpass);
        // Declare the JDBC objects.
        Connection conn;

        // Establish the connection.
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        DriverManager.setLoginTimeout(2);
        conn = DriverManager.getConnection(connectionUrl);
        if (conn == null) {
            out.println(TAG + "ERRO Conn: " + connectionUrl);
            return null;
        }

        linkSQL.setConn(conn);
        return linkSQL;
    }

    private void fecharLinkSQL(LinkSQL objSql) {
        if (objSql == null) {
            return;
        }
        try {
            objSql.getConn().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isSQLServerOnline() {
        boolean isOnoline = true;
        LinkSQL objSql = null;
        try {
            objSql = this.abrirLinkSQL();
        } catch (ClassNotFoundException | SQLException e) {
            isOnoline = false;
            out.println(TAG + "ERRO em abrirLinkSQL: " + e.getMessage());
        }

        if (objSql == null) {
            out.println(TAG + "ERRO: objSql é nulo!");
            isOnoline = false;
        } else {
            if (objSql.getConn() == null) {
                isOnoline = false;
                out.println(TAG + "ERRO: objSql.conn é nulo!");
            }
            fecharLinkSQL(objSql);
        }
        out.println(TAG + "O SQL está online? " + isOnoline);
        return isOnoline;
    }

    public static BamerSqlServer getInstancia() {
        if (instancia == null) {
            instancia = new BamerSqlServer();
        }
        return instancia;
    }

    private LinkSQL getSQLConn() throws SQLException, ClassNotFoundException {
        if (!isSQLServerOnline()) {
            throw new SQLException("O sqlserver " + ValoresDefeito.SQL_SERVIDOR + " na porta " + ValoresDefeito.SQL_PORTA + " não está Online!\nQualquer alteração não será gravada definitivamente");
        }

        LinkSQL linkSQL = abrirLinkSQL();
        if (linkSQL == null) {
            throw new SQLException("Nao foi possivel abrir a ligação SQL\nQualquer alteração não será gravada definitivamente");
        }
        return linkSQL;
    }

    public int actualizarDataCorte(ArtigoOSBO artigo) throws Exception {
        int rows;
        LinkSQL linkSQL;
        linkSQL = getSQLConn();

        String bostamp = artigo.getBostamp();

        String dataCorteStr = Funcoes.dToCZeroHour(artigo.getDtcortef());
        String dataCorteSQL = Funcoes.cToSQL(dataCorteStr);

        String seccao = artigo.getSeccao();
        int dias = seccao.equals("12 - CARPINTARIA") ? 2 : 1;
        LocalDateTime dataCorteLDT = Funcoes.cToT(dataCorteStr);
        LocalDateTime dataTransfLDT = dataCorteLDT.plusDays(dias);
        String dataTransfSQL = Funcoes.cToSQL(Funcoes.dToCZeroHour(dataTransfLDT));

        String query = "update bo2 set u_dtcortef = ?, u_dttransf = ? where bo2stamp = ? and (u_dtcortef != ? or u_dttransf != ?)";
        PreparedStatement preparedStatement = linkSQL.getConn().prepareStatement(query);
        preparedStatement.setString(1, dataCorteSQL);
        preparedStatement.setString(2, dataTransfSQL);
        preparedStatement.setString(3, bostamp);
        preparedStatement.setString(4, dataCorteSQL);
        preparedStatement.setString(5, dataTransfSQL);

        rows = preparedStatement.executeUpdate();
        out.println("DATA: Actualizados " + rows + " registos em actualizarDataCorte(" + bostamp + ") para corte = " + dataCorteSQL + ", transf = " + dataTransfSQL);
        fecharLinkSQL(linkSQL);
        preparedStatement.close();

        actualizarOrdem(artigo);

        return rows;
    }

    public int actualizarOrdem(ArtigoOSBO artigo) throws SQLException, ClassNotFoundException {
        int rows;
        LinkSQL objSql;
        objSql = getSQLConn();

        String bostamp = artigo.getBostamp();

        String dataCorteStr = Funcoes.dToCZeroHour(artigo.getDtcortef());
        String dataCorteSQL = Funcoes.cToSQL(dataCorteStr);

        String seccao = artigo.getSeccao();
        int dias = seccao.equals("12 - CARPINTARIA") ? 2 : 1;
        LocalDateTime dataCorteLDT = Funcoes.cToT(dataCorteStr);
        LocalDateTime dataTransfLDT = dataCorteLDT.plusDays(dias);
        String dataTransfSQL = Funcoes.cToSQL(Funcoes.dToCZeroHour(dataTransfLDT));

        String query = "update bo set tecnico = ? where bo.bostamp = ? and bo.tecnico != ?";
        PreparedStatement preparedStatement = objSql.getConn().prepareStatement(query);
        preparedStatement.setInt(1, artigo.getOrdem());
        preparedStatement.setString(2, bostamp);
        preparedStatement.setInt(3, artigo.getOrdem());

        rows = preparedStatement.executeUpdate();
        out.println("SQLSERVER: Actualizados " + rows + " registos em actualizarDataCorte(" + bostamp + ") para corte = " + dataCorteSQL + ", transf = " + dataTransfSQL);
        fecharLinkSQL(objSql);
        preparedStatement.close();

        return rows;
    }

    public int actualizar_De_Aprovisionamento_para_Corte(ArtigoAprovisionamento artigo) throws SQLException, ClassNotFoundException {
        int rows;
        LinkSQL linkSQL;
        linkSQL = getSQLConn();

        String bostamp = artigo.getBostamp();

        String dataCorteStr = Funcoes.dToCZeroHour(artigo.getDtcortef());
        String dataCorteSQL = Funcoes.cToSQL(dataCorteStr);

        String seccao = artigo.getSeccao();
        int dias = seccao.equals("12 - CARPINTARIA") ? 2 : 1;
        LocalDateTime dataCorteLDT = Funcoes.cToT(dataCorteStr);
        LocalDateTime dataTransfLDT = dataCorteLDT.plusDays(dias);
        String dataTransfSQL = Funcoes.cToSQL(Funcoes.dToCZeroHour(dataTransfLDT));

        String query = "update bo2 set u_dtcortef = ?, u_dttransf = ? where bo2stamp = ? and (u_dtcortef != ? or u_dttransf != ?)";
        PreparedStatement preparedStatement = linkSQL.getConn().prepareStatement(query);
        preparedStatement.setString(1, dataCorteSQL);
        preparedStatement.setString(2, dataTransfSQL);
        preparedStatement.setString(3, bostamp);
        preparedStatement.setString(4, dataCorteSQL);
        preparedStatement.setString(5, dataTransfSQL);

        rows = preparedStatement.executeUpdate();
        out.println("SQLSERVER: Actualizados " + rows + " registos em actualizar_De_Aprovisionamento_para_Corte(" + bostamp + ") para corte = " + dataCorteSQL + ", transf = " + dataTransfSQL);
        fecharLinkSQL(linkSQL);
        preparedStatement.close();

        actualizarEstado(bostamp, "01 - CORTE");

        return rows;
    }

    private int actualizarEstado(String bostamp, String estado) throws SQLException, ClassNotFoundException {
        int rows;
        LinkSQL linkSQL;
        linkSQL = getSQLConn();

        String query = "update bo set tabela1 = ? where bostamp = ?";
        PreparedStatement preparedStatement = linkSQL.getConn().prepareStatement(query);
        preparedStatement.setString(1, estado);
        preparedStatement.setString(2, bostamp);

        rows = preparedStatement.executeUpdate();
        out.println("SQLSERVER: Actualizados " + rows + " registos em actualizarEstado(" + bostamp + ") para estado = " + estado);
        fecharLinkSQL(linkSQL);
        preparedStatement.close();

        return rows;
    }

    public int actualizarCor(String bostamp, int cor) throws SQLException, ClassNotFoundException {
        int rows;
        LinkSQL linkSQL;
        linkSQL = getSQLConn();

        String query = "update bo set pno = ? where bostamp = ?";
        PreparedStatement preparedStatement = linkSQL.getConn().prepareStatement(query);
        preparedStatement.setInt(1, cor);
        preparedStatement.setString(2, bostamp);

        rows = preparedStatement.executeUpdate();
        out.println("SQLSERVER: Actualizados " + rows + " registos em actualizarCor(" + bostamp + ", " + cor + ")");
        fecharLinkSQL(linkSQL);
        preparedStatement.close();

        return rows;
    }

    public int editarEactualizarDatas(String bostamp, String dtcortef, String dttransf, String dtembala, String dtexpedi, String estado) throws SQLException, ClassNotFoundException {
        int rows;
        LinkSQL linkSQL;
        linkSQL = getSQLConn();

        String query = "update bo2 set u_dtcortef = ?, u_dttransf = ?, u_dtembf = ? where bo2stamp = ? and (u_dtcortef != ?  or u_dttransf != ? or u_dtembf != ?)";
        PreparedStatement preparedStatement = linkSQL.getConn().prepareStatement(query);
        preparedStatement.setString(1, dtcortef);
        preparedStatement.setString(2, dttransf);
        preparedStatement.setString(3, dtembala);
        preparedStatement.setString(4, bostamp);
        preparedStatement.setString(5, dtcortef);
        preparedStatement.setString(6, dttransf);
        preparedStatement.setString(7, dtembala);

        rows = preparedStatement.executeUpdate();
        out.println("SQLSERVER: Actualizados " + rows + " registos em editarEactualizarDatas('"
                + bostamp + "', '"
                + dtcortef + "', '"
                + dttransf + "', '"
                + dtembala + "', '"
                + dtexpedi + "', '"
                + estado
                + "')");
        fecharLinkSQL(linkSQL);
        preparedStatement.close();

        updateEstado(bostamp, estado);

        updateDataExpedi(bostamp, dtexpedi);

        return rows;
    }

    private int updateDataExpedi(String bostamp, String dtexpedi) throws SQLException, ClassNotFoundException {
        if (dtexpedi.equals("19000101"))
            out.println("SQLSERVER: updateDataExpedi tem data vazia");
        int rows;
        LinkSQL linkSQL;
        linkSQL = getSQLConn();

        String query = "update bo set datafinal = ? " +
                "from bo inner join bo2 on bo.bostamp = bo2.bo2stamp and bo.ndos = 19 " +
                "inner join bo2 bo2os on bo2.u_ctboitem = bo2os.u_ctboitem and bo2os.bo2stamp = ? " +
                "where bo.datafinal != ?";
        PreparedStatement preparedStatement = linkSQL.getConn().prepareStatement(query);
        preparedStatement.setString(1, dtexpedi);
        preparedStatement.setString(2, bostamp);
        preparedStatement.setString(3, dtexpedi);

        rows = preparedStatement.executeUpdate();
        out.println("SQLSERVER: Actualizados " + rows + " registos em updateDataExpedi('"
                + bostamp + "', '"
                + dtexpedi
                + "')");
        fecharLinkSQL(linkSQL);
        preparedStatement.close();

        return rows;
    }

    private int updateEstado(String bostamp, String estado) throws SQLException, ClassNotFoundException {
        int rows;
        LinkSQL linkSQL;
        linkSQL = getSQLConn();

        String query = "update bo set tabela1 = ? where bostamp = ? and (tabela1 != ?)";
        PreparedStatement preparedStatement = linkSQL.getConn().prepareStatement(query);
        preparedStatement.setString(1, estado);
        preparedStatement.setString(2, bostamp);
        preparedStatement.setString(3, estado);

        rows = preparedStatement.executeUpdate();
        out.println("SQLSERVER: Actualizados " + rows + " registos em updateEstado('"
                + bostamp + "', '"
                + estado
                + "')");
        fecharLinkSQL(linkSQL);
        preparedStatement.close();

        return rows;
    }

    private class LinkSQL {
        private Connection conn;

        LinkSQL() {

        }

        Connection getConn() {
            return conn;
        }

        void setConn(Connection conn) {
            this.conn = conn;
        }
    }


}
