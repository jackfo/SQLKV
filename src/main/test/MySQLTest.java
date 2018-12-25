import java.sql.*;

/**
 * @Description
 * @auther zhengxiaokang
 * @Email zhengxiaokang@qq.com
 * @create 2018-12-11 20:51
 */
public class MySQLTest {

    private static final String URL="jdbc:mysql://47.106.121.254:3306/tcc?useUnicode=true&amp;characterEncoding=utf-8";
    private static final String USER="root";
    private static final String PASSWORD="zheng0812";
    private static final String driver = "com.mysql.jdbc.Driver";

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName(driver); //classLoader,加载对应驱动
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        String sql = "SELECT * FROM test";
        Statement statement =  conn.createStatement();
        ResultSet resultSet =  statement.executeQuery(sql);
        while (resultSet.next()){
            System.out.println("-------------------------");
            String name = resultSet.getString("name");
            System.out.println(name);
        }
        statement.close();
        conn.close();
    }

}
