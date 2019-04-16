import com.cfs.sqlkv.compile.CharStream;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-28 11:56
 */
public class CharStreamTest {

    @Test
    public void test_001() throws IOException {
        String sql = "select * from a" +
                " \n a";
        printResult(sql);
    }

    public void printResult(String sql) throws IOException {
        StringReader stringReader = new StringReader(sql);
        CharStream charStream = new CharStream(stringReader,1,1,4096);
        System.out.println(charStream.beginToken());
        System.out.println(charStream.beginToken());

        System.out.println(charStream.getBeginColumn());
        System.out.println(charStream.getEndColumn());
        System.out.println(charStream.getEndLine());
        System.out.println(charStream.getImage());
    }

    @Test
    public void test_blankString() throws IOException {
        CharStream charStream = new CharStream(new StringReader(" "),1,1);
        char temp = charStream.readChar();
        System.out.println(temp);
    }


    public void test_backup(String sql) throws IOException {
        StringReader stringReader = new StringReader(sql);
        CharStream charStream = new CharStream(stringReader,1,1);
        System.out.println(charStream.readChar());
        charStream.backup(1);
        System.out.println(charStream.readChar());

        System.out.println(charStream.readChar());
        System.out.println(charStream.readChar());
        charStream.backup(2);
        System.out.println(charStream.readChar());
        System.out.println(charStream.readChar());
    }

    @Test
    public void test_back_001() throws IOException {
        String sql = "select * from test";
        test_backup(sql);
    }
}
