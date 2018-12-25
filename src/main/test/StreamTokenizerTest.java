import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;

/**
 * @Description
 * @auther zhengxiaokang
 * @Email zhengxiaokang@qq.com
 * @create 2018-12-12 11:04
 */
public class StreamTokenizerTest {

    public static void main(String[] args) throws IOException {
        StreamTokenizer st = new StreamTokenizer(new StringReader(",hek,hek 测试 ss a"));
        int token;
        while((token=st.nextToken() )!= StreamTokenizer.TT_EOF)
        {
            switch (token){
                case StreamTokenizer.TT_WORD:
                    System.out.println(st.sval);
                    break;
                case StreamTokenizer.TT_NUMBER:
                    System.out.println((char)st.nval);
                    break;
            }


        }
    }
}
