package com.cfs.sqlkv.compile.sql;
import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.catalog.SchemaDescriptor;
import com.cfs.sqlkv.compile.CompilerContext;
import com.cfs.sqlkv.compile.node.StatementNode;
import com.cfs.sqlkv.compile.parse.ParseException;
import com.cfs.sqlkv.compile.parse.ParserImpl;
import com.cfs.sqlkv.context.LanguageConnectionContext;
import com.cfs.sqlkv.context.StatementContext;

import com.cfs.sqlkv.service.loader.GeneratedClass;
import com.cfs.sqlkv.service.loader.GeneratedClass;
import com.cfs.sqlkv.util.ByteArray;

import java.sql.Timestamp;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-27 15:30
 */
public class GenericStatement implements Statement{

    private final SchemaDescriptor compilationSchema;
    private final String statementText;
    private final boolean isForReadOnly;

    private GenericPreparedStatement preparedStmt;


    public GenericStatement(SchemaDescriptor schemaDescriptor,String statementText, boolean isForReadOnly){
        this.compilationSchema = schemaDescriptor;
        this.statementText = statementText;
        this.isForReadOnly = isForReadOnly;
    }

    @Override
    public PreparedStatement prepare(LanguageConnectionContext lcc) throws ParseException {
        return prepare(lcc,false);
    }

    @Override
    public PreparedStatement prepare(LanguageConnectionContext lcc, boolean forMetaData) throws ParseException {
        return prepMinion(lcc, true, (Object[]) null, (SchemaDescriptor) null, forMetaData);
    }

    @Override
    public PreparedStatement prepareStorable(LanguageConnectionContext lcc, PreparedStatement ps, Object[] paramDefaults, SchemaDescriptor spsSchema, boolean internalSQL)   {
        return null;
    }

    @Override
    public String getSource() {
        return statementText;
    }

    /**
     * 记录相关的时间
     * */
    private PreparedStatement prepMinion(LanguageConnectionContext lcc, boolean cacheMe, Object[] paramDefaults, SchemaDescriptor spsSchema, boolean internalSQL) throws ParseException{
        long				beginTime = 0;
        long				parseTime = 0;
        long				bindTime = 0;
        long				optimizeTime = 0;
        long				generateTime = 0;
        Timestamp beginTimestamp = null;
        Timestamp			endTimestamp = null;
        beginTime = System.currentTimeMillis();
        StatementContext statementContext = null;


        if (beginTime != 0) {
            beginTimestamp = new Timestamp(beginTime);
        }


        if (preparedStmt == null){
            preparedStmt = new GenericPreparedStatement(this);
        }
        synchronized (preparedStmt){

        }

        if (!preparedStmt.isStorable() || lcc.getStatementDepth() == 0) {
            statementContext = lcc.pushStatementContext(true, isForReadOnly, getSource(), null, false, 0L);
        }

        /**
         * 获取编译上下文,根据编译上下获取解析实例
         * 将SQL语句进行对应的解析
         * */
        CompilerContext cc = lcc.pushCompilerContext(compilationSchema);

        ParserImpl p = cc.getParser();
        //将SQL语句解析出对应的StatementNode
        StatementNode qt = (StatementNode) p.parseStatement(statementText, paramDefaults);
        //获取数据词典,根据语言连接上下文
        DataDictionary dataDictionary = lcc.getDataDictionary();


        int ddMode;
        if(dataDictionary==null){
            ddMode =0;
        }else {
            ddMode=dataDictionary.startReading(lcc);
        }

        qt.bindStatement();
        /**
         * 对Statement做一些优化
         * */
        qt.optimizeStatement();

        try{

        }finally {
            if (dataDictionary != null){
                dataDictionary.doneReading(ddMode, lcc);
            }
        }

        //
        GeneratedClass ac =qt.generate(preparedStmt.getByteCodeSaver());
        preparedStmt.setConstantAction(qt.makeConstantAction() );
        preparedStmt.setSavedObjects( cc.getSavedObjects() );
        preparedStmt.setActivationClass(ac);
        preparedStmt.completeCompile(qt);

        return preparedStmt;
    }


}
