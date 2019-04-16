import com.cfs.sqlkv.compile.CompilerContext;
import com.cfs.sqlkv.compile.parse.ParseException;
import com.cfs.sqlkv.compile.parse.ParserImpl;
import com.cfs.sqlkv.compile.parse.SQLParser;

import com.cfs.sqlkv.jdbc.InternalDriver;
import org.junit.Test;

import java.sql.*;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-02 20:57
 */
public class SQLParseTest {

    public static Connection connection;
    public static final String dbName = "xr";
    private static String protocol = "jdbc:sqlkv:";
    static {
        try {
            Class.forName(InternalDriver.class.getName());
            connection = DriverManager.getConnection(protocol + dbName+";create=true");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test_createStudent() throws SQLException {
        Statement s = connection.createStatement();
        s.execute("create table student(sno int,age int)");
    }

    @Test
    public void test_insertStudent() throws SQLException {
        Statement s = connection.createStatement();
        for(int i=1;i<12;i++){
            s.execute(String.format("insert into student values (%d,%d)",i,i));
        }
    }

    @Test
    public void test_selectStudent() throws SQLException{
        Statement s = connection.createStatement();
        ResultSet resultSet = s.executeQuery("select * from student");
        while (resultSet.next()){
            System.out.print(resultSet.getInt("sno")+" ");
            System.out.print(resultSet.getInt("age"));
            System.out.println();
        }
    }

    @Test
    public void test_updateStudent() throws SQLException {
        Statement s = connection.createStatement();
        s.execute("update student set sno = 2 where sno =3");
    }

    @Test
    public void test_deleteStudent() throws SQLException {
        Statement s = connection.createStatement();
        s.execute("delete from student where sno =2");
    }
}
