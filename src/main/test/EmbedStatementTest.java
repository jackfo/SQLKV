import com.cfs.sqlkv.jdbc.EmbedConnection;
import com.cfs.sqlkv.jdbc.EmbedStatement;
import com.cfs.sqlkv.jdbc.EmbeddedDriver;
import com.cfs.sqlkv.jdbc.InternalDriver;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-05 20:41
 */
public class EmbedStatementTest {


    public static Connection connection;
    public static final String dbName = "dbname";
    private static String framework = "embedded";
    private static String protocol = "jdbc:sqlkv:";
    static {
        try {
            Class.forName(EmbeddedDriver.class.getName());
            connection = DriverManager.getConnection(protocol + dbName);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test_coon(){
        System.out.println(connection);
    }

    @Test
    public void test_query() throws SQLException {
        Properties properties = new Properties();
        InternalDriver internalDriver = new InternalDriver();
        EmbedStatement embedStatement = new EmbedStatement((EmbedConnection) connection,false);
        embedStatement.execute("select * from a;");
    }


    @Test
    public void test_insert() throws SQLException {
        Properties properties = new Properties();
        InternalDriver internalDriver = new InternalDriver();
        EmbedStatement embedStatement = new EmbedStatement((EmbedConnection) connection,false);
        embedStatement.execute("insert * from a;");
    }

    @Test
    public void test_create() throws SQLException {
        Statement s = connection.createStatement();
        s.execute("create table ac(num int,a int)");
    }

}
