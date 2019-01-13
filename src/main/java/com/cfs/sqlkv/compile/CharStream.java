package com.cfs.sqlkv.compile;

import java.io.IOException;
import java.io.Reader;

/**
 * @author zhengxiaokang
 * @Description 对字符串进行缓冲并实现能进行读取以及后退
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-27 18:38
 */
public class CharStream {

    public static final int DEFAULT_LENGTH = 4096;
    public static final boolean staticFlag = false;
    /**字符的游标*/
    public int bufpos = -1;
    /**字符数组的大小*/
    private int bufsize;
    /**可利用的自己字节数*/
    private int available;
    /**token开始位置*/
    int tokenBegin;
    /**字符所在的行*/
    private int bufline[];
    /**字符所在的列*/
    private int bufcolumn[];

    private int column = 0;
    private int line = 1;

    private boolean prevCharIsCR = false;
    private boolean prevCharIsLF = false;

    /**字符串解析的字符流*/
    private Reader inputStream;
    /**下一个字符数组*/
    private char[] nextCharBuf;
    /**当前字符数组*/
    private char[] buffer;
    /**数组最后一个可以利用字符的位置索引*/
    private int maxNextCharIndex = 0;
    /**下一个字符索引位置*/
    private int nextCharIndex = -1;

    private int inBuf = 0;


    public CharStream(Reader reader,int startLine,int startColumn){
        this(reader,startLine,startColumn,DEFAULT_LENGTH);
    }

    public CharStream(Reader reader,int startLine,int startColumn,int bufferSize){
        this.inputStream = reader;
        line = startLine;
        column = startColumn - 1;
        if (buffer == null || bufferSize != buffer.length) {
            available = bufsize = bufferSize;
            buffer = new char[bufferSize];
            nextCharBuf = new char[bufferSize];
            bufline = new int[bufferSize];
            bufcolumn = new int[bufferSize];
        }
        tokenBegin = inBuf = maxNextCharIndex = 0;
        nextCharIndex = bufpos = -1;

    }

    /**
     * @return 流中的下一个字符
     * */
    public char readChar()throws IOException{
         //inBuf>0 标识做过后退,所以要读取到后退位置的字符
        if(inBuf>0){
            --inBuf;
            int index;
            //如果bufpos已经到了数组最大长度,则需要从0开始读取
            //
            if(bufpos == bufsize - 1){
                index=bufpos=0;
            }else{
                index=++bufpos;
            }
            return (char)buffer[index];
        }

        //没有后退过则走正常的读取字符逻辑
        bufpos++;
        char c = readInternal();
        //更新当前读取到的行列位置
        updateLineColumn(c);

        //如果字符读取到游标位置达到了可利用的游标位置
        if(bufpos==available){
            if(available==bufsize){
                //如果上一个读取的字符大于一般
                if(tokenBegin > (DEFAULT_LENGTH>>1)){
                    bufpos = 0;
                    available = tokenBegin;
                }else if(tokenBegin < 0){
                    bufpos = 0;
                }else{
                    //如果上一个读取的字符在0-一般长度之间
                    expandBuff(false);
               }
            }
        }else if(available > tokenBegin){
            //如果可读取字符位置大于上一个token位置
            available = bufsize;
        }else if ((tokenBegin - available) < (DEFAULT_LENGTH>>1)){
            expandBuff(true);
        }else{
            available = tokenBegin;
        }
        return (buffer[bufpos] = c);
    }

    private final char readInternal()throws IOException{
        nextCharIndex = nextCharIndex+1;
        //表示当前缓冲数组已经读取完毕
        if(nextCharIndex>=maxNextCharIndex){
            fillBuff();
        }
        //读取下一个字符
        return nextCharBuf[nextCharIndex];
    }

    public final int getEndColumn() {
        return bufcolumn[bufpos];
    }

    public final int getEndLine() {
        return bufline[bufpos];
    }

    public final int getBeginColumn() {
        return bufcolumn[tokenBegin];
    }

    public final int getBeginLine() {
        return bufline[tokenBegin];
    }


     /**
      * 通过amount来记录备份的字符数
      * 如果已经读取了某些字符,Lexer会调用这个方法,但是不会匹配一个token
      * 返回下一个token
      * */
     public void backup(int amount){
         inBuf +=amount;
         /**
          *  如果当前游标小于需要备份的字符数,证明备份到上一个字符数组了
          *  因此bufpos变成了一个负数加上数组长度就是本应该所在的位置
          */
         bufpos=bufpos-amount;
         if (bufpos<0){
             bufpos = bufpos+bufsize;
         }
     }

    /**
     * 读取流中的下一个字符
     * */
    public char beginToken() throws IOException {
        tokenBegin = -1;
        char c = readChar();
        tokenBegin = bufpos;
        return c;
    }

    public void clear() {
        nextCharBuf = null;
        buffer = null;
        bufline = null;
        bufcolumn = null;
    }

    /**
     *
     * */
    public String getImage(){
        if (bufpos >= tokenBegin){
            return new String(buffer, tokenBegin, bufpos - tokenBegin + 1);
        } else{
            return new String(buffer, tokenBegin, bufsize - tokenBegin) + new String(buffer, 0, bufpos + 1);
        }
    }

    /**
     * 将字符流中数据装填的缓冲数组
     * */
    private final void fillBuff()throws IOException{
        //表示缓冲数组有效空间已经使用完毕
        if(maxNextCharIndex == DEFAULT_LENGTH){
           //将位置重新记录为0,数组做一个重新利用
           maxNextCharIndex = nextCharIndex =0;
        }
        //表示当前这次流阅读的字符数
        int readCount;
        try{
            //这样设计的目的是存在上一行换行数组为填充完
            readCount = inputStream.read(nextCharBuf,maxNextCharIndex,DEFAULT_LENGTH-maxNextCharIndex);
            if(readCount==-1){
                //表示为获取成功
                inputStream.close();
                throw new IOException();
            }else{
                //数组可利用位置增大
                maxNextCharIndex = maxNextCharIndex+readCount;
            }
        }catch (IOException e){

        }
    }

    private final void expandBuff(boolean wrapAround){
       char[] newbuffer = new char[bufsize+DEFAULT_LENGTH>>1];
       int newbufline[] = new int[bufsize + DEFAULT_LENGTH>>1];
       int newbufcolumn[] = new int[bufsize + DEFAULT_LENGTH>>1];

       try{
           if(wrapAround){
               //将原有的buffer里tokenBegin开始位置到后面所有的迁移的newbuffer开始位置
               System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
               //再将原有buffer中bufpos之前的都移动到新数组bufsize - tokenBegin之后的位置
               System.arraycopy(buffer, 0, newbuffer, bufsize - tokenBegin, bufpos);
               buffer = newbuffer;

               System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);
               System.arraycopy(bufline, 0, newbufline, bufsize - tokenBegin, bufpos);
               bufline = newbufline;

               System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);
               System.arraycopy(bufcolumn, 0, newbufcolumn, bufsize - tokenBegin, bufpos);
               bufcolumn = newbufcolumn;
               //游标的位置是bufpos+所有迁移的长度
               bufpos += (bufsize - tokenBegin);
           }else{
               System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
               buffer = newbuffer;

               System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);
               bufline = newbufline;

               System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);
               bufcolumn = newbufcolumn;

               bufpos -= tokenBegin;
           }
       }catch (Throwable t){
           available = (bufsize += 2048);
           tokenBegin = 0;
       }
    }


    /**
     * 更新字符的行列
     * */
    private final void updateLineColumn(char c){
        column++;
        if(prevCharIsLF){
            //如果前一个字符是\r
            prevCharIsLF = false;
            column=1;
            line = line+1;
        }else if(prevCharIsCR){
            //如果前一个字符是\r
            prevCharIsCR = false;
            if(c == '\n'){
                prevCharIsLF = true;
            }else{
                column=1;
                line = line+1;
            }
        }

        switch (c){
            case '\r' :
                prevCharIsCR = true;
                break;
            case '\n' :
                prevCharIsLF = true;
                break;
            case '\t' :
                column--;
                column += (8 - (column & 07));
                break;
            default :
                break;
        }
    }
}
