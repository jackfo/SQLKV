/* Generated By:JavaCC: Do not edit this line. SQLParser.java */
package com.cfs.sqlkv.compile.parse;
import com.cfs.sqlkv.catalog.types.*;
import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.column.*;
import com.cfs.sqlkv.common.context.*;
import com.cfs.sqlkv.compile.CompilerContext;
import com.cfs.sqlkv.compile.TypeCompiler;
import com.cfs.sqlkv.compile.table.*;
import com.cfs.sqlkv.compile.node.*;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.compile.result.*;
import com.cfs.sqlkv.compile.name.TableName;
import com.cfs.sqlkv.common.*;
import com.cfs.sqlkv.sql.dictionary.*;
import com.cfs.sqlkv.util.*;
import java.sql.*;
import java.util.*;
public class SQLParser implements SQLParserConstants {

    private static final int NO_SET_OP = 0;
    //参数数量
    private int parameterNumber;
    //默认的参数
    private Object[] paramDefaults;
    //SQL文本
    private String statementSQLText;

    private boolean isDistinct;

    /**
      * 如果最后一个标识符或关键字是分隔标识符
      * 用来记住是否需要做序列化处理
      */
    private boolean lastTokenDelimitedIdentifier = false;
    private boolean nextToLastTokenDelimitedIdentifier = false;
    /**获取的最后一个标识符Token*/
    private Token   lastIdentifierToken;
    /**下一个标识符Token*/
    private Token   nextToLastIdentifierToken;
    private Token   thirdToLastIdentifierToken;

    private ContextManager                              cm;
        private CompilerContext                         compilerContext;

        private static final int        OPTIONAL_TABLE_CLAUSES_SIZE = 3;
    private static final int        OPTIONAL_TABLE_CLAUSES_TABLE_PROPERTIES = 0;

    private static final int    OPTIONAL_TABLE_CLAUSES_DERIVED_RCL = 1;
    private static final int    OPTIONAL_TABLE_CLAUSES_CORRELATION_NAME = 2;


        public final void setCompilerContext(CompilerContext cc) {
                this.compilerContext = cc;
                this.cm = cc.getContextManager();
        }

        //初始化Statement
    private void initStatement(String statementSQLText, Object[] paramDefaults)throws StandardException{
        parameterNumber = 0;
        this.statementSQLText = statementSQLText;
        this.paramDefaults = paramDefaults;
        //TODO:尚未实现SQL优化器
   }

        private final ContextManager getContextManager(){
                return cm;
        }

    /**
     * 根据表名获取Java数据类型
     */
        private DataTypeDescriptor getJavaClassDataTypeDescriptor(TableName typeName)throws StandardException{
        return new DataTypeDescriptor(TypeId.getUserDefinedTypeId( typeName.getSchemaName(), typeName.getTableName(), null ),true);
    }

        boolean commonDatatypeName(boolean checkFollowingToken){
            return commonDatatypeName(1, checkFollowingToken);
        }

        boolean commonDatatypeName(int start, boolean checkFollowingToken)
        {
                boolean retval = false;

                switch (getToken(start).kind)
                {
                  case CHARACTER:
                  case CHAR:
                  case VARCHAR:
                  case NVARCHAR:
                  case NCHAR:
                  case BIT:
                  case NUMERIC:
                  case DECIMAL:
                  case DEC:
                  case INTEGER:
                  case INT:
                  case SMALLINT:
              case BIGINT:
                  case FLOAT:
                  case REAL:
                  case DATE:
                  case TIME:
                  case TIMESTAMP:
                  case BOOLEAN:
                  case DOUBLE:
                  case BLOB:
                  case CLOB:
                  case NCLOB:
                  case BINARY: // LARGE OBJECT
                  case XML:
                        retval = true;
                        break;

                  case LONG:
                        if (checkFollowingToken == true)
                        {
                                switch (getToken(start+1).kind)
                                {
                                  case VARCHAR:
                                  case NVARCHAR:
                                  case BINARY:
                                  case VARBINARY:
                                  case BIT:
                                        retval = true;
                                        break;
                                }
                                break;
                        }
                        else
                        {
                                retval = true;
                                break;
                        }

                  case NATIONAL:
                        if (checkFollowingToken == true)
                        {
                                switch (getToken(start+1).kind)
                                {
                                  case CHAR:
                                  case CHARACTER:
                                        retval = true;
                                        break;
                                }
                                break;
                        }
                        else
                        {
                                retval = true;
                                break;
                        }
                }

                return retval;
        }

  final public StatementNode statement(String statementSQLText, Object[] paramDefaults) throws ParseException, StandardException {
        StatementNode   statementNode;
    initStatement(statementSQLText, paramDefaults);
    statementNode = StatementPart(null);
    jj_consume_token(0);
        //statementNode.setBeginOffset(0);
        //statementNode.setEndOffset(statementSQLText.length() - 1);
                {if (true) return statementNode;}
    throw new Error("Missing return statement in function");
  }

  final public StatementNode searchCondition(String sql) throws ParseException {
                                              {if (true) return null;}
    throw new Error("Missing return statement in function");
  }

/**
 * 第一个实现查询语句preparableSQLDataStatement
 */
  final public StatementNode StatementPart(Token[] tokenHolder) throws ParseException, StandardException {
        StatementNode   statementNode;
        if (tokenHolder != null){
                tokenHolder[0] = getToken(1);
        }
    switch (jj_nt.kind) {
    case CREATE:
      statementNode = createStatements();
      break;
    case SELECT:
      //实现查询语句preparableSQLDataStatement
           statementNode = preparableSQLDataStatement();
      break;
    default:
      jj_la1[0] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
        {if (true) return statementNode;}
    throw new Error("Missing return statement in function");
  }

  final public StatementNode preparableSQLDataStatement() throws ParseException, StandardException {
  StatementNode dmlStatement=null;
  ResultColumnList      selectList;
    preparableSelectStatement(false);
        {if (true) return dmlStatement;}
    throw new Error("Missing return statement in function");
  }

  final public CursorNode preparableSelectStatement(boolean checkParams) throws ParseException, StandardException {
   ResultSetNode queryExpression;
   ArrayList<String> updateColumns = new ArrayList<String>();
   CursorNode retval;
    queryExpression = queryExpression(null, NO_SET_OP);
       retval = new CursorNode(getContextManager());
       {if (true) return retval;}
    throw new Error("Missing return statement in function");
  }

  final public ResultSetNode queryExpression(ResultSetNode leftSide, int operatorType) throws ParseException, StandardException {
  ResultSetNode term;
    term = nonJoinQueryTerm(leftSide, operatorType);
                {if (true) return term;}
    throw new Error("Missing return statement in function");
  }

  final public ResultSetNode nonJoinQueryTerm(ResultSetNode leftSide, int operatorType) throws ParseException, StandardException {
   ResultSetNode term;
    term = nonJoinQueryPrimary();
    {if (true) return term;}
    throw new Error("Missing return statement in function");
  }

  final public ResultSetNode nonJoinQueryPrimary() throws ParseException, StandardException {
  ResultSetNode primary;
    primary = simpleTable();
                {if (true) return primary;}
    throw new Error("Missing return statement in function");
  }

  final public ResultSetNode simpleTable() throws ParseException, StandardException {
        ResultSetNode   resultSetNode;
    resultSetNode = querySpecification();
                {if (true) return resultSetNode;}
    throw new Error("Missing return statement in function");
  }

/**
 *详细查询
 */
  final public ResultSetNode querySpecification() throws ParseException, StandardException {
        ResultColumnList        selectList;
        SelectNode                      selectNode;
        boolean isDistinct = false;
    jj_consume_token(SELECT);
    selectList = selectList();
    selectNode = tableExpression(selectList);
                {if (true) return selectNode;}
    throw new Error("Missing return statement in function");
  }

  final public SelectNode tableExpression(ResultColumnList selectList) throws ParseException, StandardException {
  SelectNode selectNode;
  FromList      fromList;
    fromList = fromClause();
     selectNode = new SelectNode(selectList,fromList,getContextManager());
      {if (true) return selectNode;}
    throw new Error("Missing return statement in function");
  }

  final public boolean setQuantifier() throws ParseException {
    jj_consume_token(DISTINCT);
            {if (true) return false;}
    throw new Error("Missing return statement in function");
  }

  final public ResultColumnList selectList() throws ParseException, StandardException {
     ResultColumn       allResultColumn;
     ResultColumnList resultColumns = new ResultColumnList(getContextManager());
    switch (jj_nt.kind) {
    case ASTERISK:
      jj_consume_token(ASTERISK);
                allResultColumn = new AllResultColumn(null, getContextManager());
                resultColumns.addResultColumn(allResultColumn);
                {if (true) return resultColumns;}
      break;
    default:
      jj_la1[1] = jj_gen;
      selectColumnList(resultColumns);
          {if (true) return resultColumns;}
    }
    throw new Error("Missing return statement in function");
  }

/**
 *首先在selectSublist中消费当前Token
 *之后如果匹配到,则消费<COMMA>之后继续消费一个列
 *这样最后将所有的列明都进行了消费
 */
  final public void selectColumnList(ResultColumnList resultColumns) throws ParseException, StandardException {
    selectSublist(resultColumns);
    label_1:
    while (true) {
      switch (jj_nt.kind) {
      case COMMA:
        ;
        break;
      default:
        jj_la1[2] = jj_gen;
        break label_1;
      }
      jj_consume_token(COMMA);
      selectSublist(resultColumns);
    }
  }

  final public void selectSublist(ResultColumnList resultColumns) throws ParseException, StandardException {
    /**定义结果列*/
        ResultColumn    resultColumn;
        ResultColumn    allResultColumn;
        TableName       tableName;
    if (getToken(2).kind == PERIOD &&(getToken(3).kind == ASTERISK ||(getToken(4).kind == PERIOD && getToken(5).kind == ASTERISK))) {
      tableName = qualifiedName(Limits.MAX_IDENTIFIER_LENGTH);
      jj_consume_token(PERIOD);
      jj_consume_token(ASTERISK);
        allResultColumn = new AllResultColumn(tableName, getContextManager());
                resultColumns.addResultColumn(allResultColumn);
    } else {
      resultColumn = derivedColumn(resultColumns);
                resultColumns.addResultColumn(resultColumn);
    }
  }

  final public ResultColumn derivedColumn(ResultColumnList resultColumns) throws ParseException, StandardException {
        ValueNode       columnExpression;
        String          columnName = null;
       columnExpression = valueExpression();
       {if (true) return new ResultColumn(columnName,columnExpression,getContextManager());}
    throw new Error("Missing return statement in function");
  }

/**
 * 获取表名
 */
  final public TableName qualifiedName(int id_length_limit) throws ParseException, StandardException {
        String  schemaName = null;
        String  qualifiedId;
        String  firstName = null;
        String  secondName = null;
    Token firstNameToken = null;
    //去获取标识符的第一个名字
       firstName = identifier(Limits.MAX_IDENTIFIER_LENGTH, false);
    if (getToken(1).kind == PERIOD &&getToken(2).kind != ASTERISK) {
      jj_consume_token(PERIOD);
      secondName = identifier(Limits.MAX_IDENTIFIER_LENGTH, false);
    } else {
      ;
    }
       if (secondName == null){
                //如果不是.则获取相应firstName为列名
                        qualifiedId = firstName;
                firstNameToken = lastIdentifierToken;
        }else{
                        schemaName = firstName;
                        qualifiedId = secondName;
                firstNameToken = nextToLastIdentifierToken;
        }
        {if (true) return new TableName(schemaName,qualifiedId,firstNameToken.beginOffset,lastIdentifierToken.endOffset,getContextManager());}
    throw new Error("Missing return statement in function");
  }

  final public String identifier(int id_length_limit, boolean checkLength) throws ParseException, StandardException {
        String  id;
    id = internalIdentifier(id_length_limit, checkLength);
        {if (true) return id;}
    throw new Error("Missing return statement in function");
  }

/**
 * 遇到标识符Token之后，将当前Token记录为最后一个Token
 * 在这里是记录倒数三个Token
 */
  final public String internalIdentifier(int id_length_limit, boolean checkLength) throws ParseException, StandardException {
        String  str;
        Token   tok;
    tok = jj_consume_token(IDENTIFIER);
            //将标识符转化为大写
        str = StringUtil.SQLToUpperCase(tok.image);
                nextToLastTokenDelimitedIdentifier = lastTokenDelimitedIdentifier;
        lastTokenDelimitedIdentifier = false;
        thirdToLastIdentifierToken = nextToLastIdentifierToken;
                nextToLastIdentifierToken = lastIdentifierToken;
                lastIdentifierToken = tok;
                {if (true) return str;}
    throw new Error("Missing return statement in function");
  }

  final public FromList fromClause() throws ParseException, StandardException {
    FromList fromList = new FromList(getContextManager());

        int     tokKind;
    Token   beginToken;
    Token   endToken;
    jj_consume_token(FROM);
                beginToken = getToken(1);
    dummyTableReferenceRule(fromList);
                                           endToken = getToken(0);
        fromList.setBeginOffset( beginToken.beginOffset);
        fromList.setEndOffset( endToken.endOffset);
                {if (true) return fromList;}
    throw new Error("Missing return statement in function");
  }

  final public void dummyTableReferenceRule(FromList fromList) throws ParseException, StandardException {
        FromTable tableReference;
    if (getToken(1).kind == TABLE &&
                                    getToken(2).kind == LEFT_PAREN &&
                                    (
                                            getToken(3).kind == SELECT ||
                                            getToken(3).kind == VALUES
                                    )) {
      jj_consume_token(TABLE);
      tableReference = tableReferenceTypes(false);
                fromList.addFromTable(tableReference);
    } else {
      switch (jj_nt.kind) {
      case IDENTIFIER:
        tableReference = tableReferenceTypes(false);
                fromList.addFromTable(tableReference);
        break;
      default:
        jj_la1[3] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
  }

  final public ValueNode valueExpression() throws ParseException {
   int a;
    {if (true) return null;}
    throw new Error("Missing return statement in function");
  }

  final public FromTable tableReferenceTypes(boolean nestedInParens) throws ParseException, StandardException {
        FromTable tableReference;
    tableReference = tableReference(nestedInParens);
                {if (true) return tableReference ;}
    throw new Error("Missing return statement in function");
  }

  final public FromTable tableReference(boolean nestedInParens) throws ParseException, StandardException {
    FromTable fromTable;
    TableOperatorNode joinTable = null;
    fromTable = tableFactor();
        {if (true) return joinTable == null ? fromTable : joinTable;}
    throw new Error("Missing return statement in function");
  }

  final public FromTable tableFactor() throws ParseException, StandardException {
        JavaToSQLValueNode      javaToSQLNode = null;
        TableName                       tableName;
        String                          correlationName = null;
        ResultColumnList        derivedRCL = null;
        FromTable                       fromTable;
        FromTable                       tableReference;
        Object[]                        optionalTableClauses = new Object[OPTIONAL_TABLE_CLAUSES_SIZE];
        Properties                      tableProperties = null;
        SubqueryNode            derivedTable;
    /* identifier() used to be correlationName() */
            tableName = qualifiedName(Limits.MAX_IDENTIFIER_LENGTH);
            fromTable = new FromBaseTable(
                tableName,
                (String) optionalTableClauses[
                    OPTIONAL_TABLE_CLAUSES_CORRELATION_NAME],
                (ResultColumnList) optionalTableClauses[
                    OPTIONAL_TABLE_CLAUSES_DERIVED_RCL],
                (Properties) optionalTableClauses[
                    OPTIONAL_TABLE_CLAUSES_TABLE_PROPERTIES],
                getContextManager());
                {if (true) return fromTable;}
    throw new Error("Missing return statement in function");
  }

  final public JavaToSQLValueNode vtiTableConstruct() throws ParseException, StandardException {
   MethodCallNode invocationNode = null;
   TableName vtiTableName;
    jj_consume_token(TABLE);
    jj_consume_token(LEFT_PAREN);
    vtiTableName = qualifiedName(Limits.MAX_IDENTIFIER_LENGTH);
    jj_consume_token(RIGHT_PAREN);
    {if (true) return new JavaToSQLValueNode(invocationNode, getContextManager());}
    throw new Error("Missing return statement in function");
  }

  final public StatementNode createStatements() throws ParseException, StandardException {
        StatementNode statementNode;
        Token beginToken;
        int tokKind;
    beginToken = jj_consume_token(CREATE);
    statementNode = tableDefinition();
         {if (true) return statementNode;}
    throw new Error("Missing return statement in function");
  }

  final public StatementNode tableDefinition() throws ParseException, StandardException {
        char                            lockGranularity = TableDescriptor.DEFAULT_LOCK_GRANULARITY;
        Properties                      properties = null;
        TableName                       tableName;
        TableElementList        tableElementList;
        ResultColumnList        resultColumns = null;
        ResultSetNode           queryExpression;
        boolean                         withData = true;
    jj_consume_token(TABLE);
    tableName = qualifiedName(Limits.MAX_IDENTIFIER_LENGTH);
    if (getToken(1).kind == LEFT_PAREN
                  && getToken(3).kind != COMMA
                  && getToken(3).kind != RIGHT_PAREN) {

    } else {
      jj_consume_token(-1);
      throw new ParseException();
    }
    tableElementList = tableElementList();
                   {if (true) return new CreateTableNode(
                                                                                tableName,
                                                                                tableElementList,
                                                                                properties,
                                           lockGranularity,
                                                                                getContextManager());}
    throw new Error("Missing return statement in function");
  }

  final public TableElementList tableElementList() throws ParseException, StandardException {
    TableElementList tableElementList = new TableElementList(getContextManager());
    jj_consume_token(LEFT_PAREN);
    tableElement(tableElementList);
    label_2:
    while (true) {
      switch (jj_nt.kind) {
      case COMMA:
        ;
        break;
      default:
        jj_la1[4] = jj_gen;
        break label_2;
      }
      jj_consume_token(COMMA);
      tableElement(tableElementList);
    }
    jj_consume_token(RIGHT_PAREN);
                {if (true) return tableElementList;}
    throw new Error("Missing return statement in function");
  }

  final public void tableElement(TableElementList tableElementList) throws ParseException, StandardException {
        TableElementNode        tableElement;
    tableElement = columnDefinition(tableElementList);
                tableElementList.addTableElement(tableElement);
  }

  final public TableElementNode columnDefinition(TableElementList tableElementList) throws ParseException, StandardException {
        DataTypeDescriptor[]    typeDescriptor = new DataTypeDescriptor[1];
        ValueNode                       defaultNode = null;
        String                          columnName;
        long[]                          autoIncrementInfo = new long[5];
    columnName = identifier(Limits.MAX_IDENTIFIER_LENGTH, true);
    if (jj_2_1(1)) {
      typeDescriptor[0] = dataTypeDDL();
    } else {
      ;
    }
        {if (true) return new ColumnDefinitionNode(
                                                                columnName,
                                                                defaultNode,
                                                                typeDescriptor[0],
                                                                autoIncrementInfo,
                                                                getContextManager());}
    throw new Error("Missing return statement in function");
  }

  final public DataTypeDescriptor dataTypeDDL() throws ParseException, StandardException {
        DataTypeDescriptor      typeDescriptor;
    if (commonDatatypeName(false)) {
      typeDescriptor = dataTypeCommon();
                {if (true) return typeDescriptor;}
    } else if (getToken(1).kind != GENERATED) {
      typeDescriptor = javaType(new TableName[1]);
                {if (true) return typeDescriptor;}
    } else {
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public DataTypeDescriptor dataTypeCommon() throws ParseException, StandardException {
        DataTypeDescriptor      typeDescriptor;
        boolean checkCS = false;
    typeDescriptor = numericType();
                {if (true) return typeDescriptor;}
    throw new Error("Missing return statement in function");
  }

  final public DataTypeDescriptor javaType(TableName[] udtName) throws ParseException, StandardException {
        TableName       typeName;
    typeName = qualifiedName(Limits.MAX_IDENTIFIER_LENGTH);
        udtName[0] = typeName;
                {if (true) return getJavaClassDataTypeDescriptor(typeName);}
    throw new Error("Missing return statement in function");
  }

  final public DataTypeDescriptor numericType() throws ParseException, StandardException {
        DataTypeDescriptor      typeDescriptor;
    typeDescriptor = exactNumericType();
                {if (true) return typeDescriptor;}
    throw new Error("Missing return statement in function");
  }

  final public DataTypeDescriptor exactNumericType() throws ParseException, StandardException {
        int precision = TypeCompiler.DEFAULT_DECIMAL_PRECISION;
        int scale = TypeCompiler.DEFAULT_DECIMAL_SCALE;
        int type = Types.DECIMAL;
        String typeStr = "DECIMAL";
        int maxWidth;
        DataTypeDescriptor dtd =  null;
    dtd = exactIntegerType();
                        {if (true) return dtd;}
    throw new Error("Missing return statement in function");
  }

  final public DataTypeDescriptor exactIntegerType() throws ParseException, StandardException {
    switch (jj_nt.kind) {
    case INTEGER:
      jj_consume_token(INTEGER);
      break;
    case INT:
      jj_consume_token(INT);
      break;
    default:
      jj_la1[5] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
                {if (true) return DataTypeDescriptor.getBuiltInDataTypeDescriptor(Types.INTEGER);}
    throw new Error("Missing return statement in function");
  }

  final public ValueNode defaultAndConstraints(DataTypeDescriptor[] typeDescriptor,
                                          TableElementList tableElementList,
                                          String columnName,
                                          long[] autoIncrementInfo) throws ParseException, StandardException {
        ValueNode               defaultNode = null;
                {if (true) return defaultNode;}
    throw new Error("Missing return statement in function");
  }

  final private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  final private boolean jj_3_1() {
    if (jj_3R_3()) return true;
    return false;
  }

  final private boolean jj_3R_7() {
    if (jj_3R_9()) return true;
    return false;
  }

  final private boolean jj_3R_12() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_scan_token(108)) {
    jj_scanpos = xsp;
    if (jj_scan_token(107)) return true;
    }
    return false;
  }

  final private boolean jj_3R_10() {
    if (jj_3R_12()) return true;
    return false;
  }

  final private boolean jj_3R_5() {
    if (jj_3R_7()) return true;
    return false;
  }

  final private boolean jj_3R_11() {
    if (jj_3R_13()) return true;
    return false;
  }

  final private boolean jj_3R_3() {
    Token xsp;
    xsp = jj_scanpos;
    lookingAhead = true;
    jj_semLA = commonDatatypeName(false);
    lookingAhead = false;
    if (!jj_semLA || jj_3R_4()) {
    jj_scanpos = xsp;
    lookingAhead = true;
    jj_semLA = getToken(1).kind != GENERATED;
    lookingAhead = false;
    if (!jj_semLA || jj_3R_5()) return true;
    }
    return false;
  }

  final private boolean jj_3R_4() {
    if (jj_3R_6()) return true;
    return false;
  }

  final private boolean jj_3R_8() {
    if (jj_3R_10()) return true;
    return false;
  }

  final private boolean jj_3R_13() {
    if (jj_scan_token(IDENTIFIER)) return true;
    return false;
  }

  final private boolean jj_3R_6() {
    if (jj_3R_8()) return true;
    return false;
  }

  final private boolean jj_3R_9() {
    if (jj_3R_11()) return true;
    return false;
  }

  public SQLParserTokenManager token_source;
  public Token token, jj_nt;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  public boolean lookingAhead = false;
  private boolean jj_semLA;
  private int jj_gen;
  final private int[] jj_la1 = new int[6];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static private int[] jj_la1_2;
  static private int[] jj_la1_3;
  static private int[] jj_la1_4;
  static private int[] jj_la1_5;
  static private int[] jj_la1_6;
  static private int[] jj_la1_7;
  static private int[] jj_la1_8;
  static private int[] jj_la1_9;
  static private int[] jj_la1_10;
  static private int[] jj_la1_11;
  static private int[] jj_la1_12;
  static private int[] jj_la1_13;
  static {
      jj_la1_0();
      jj_la1_1();
      jj_la1_2();
      jj_la1_3();
      jj_la1_4();
      jj_la1_5();
      jj_la1_6();
      jj_la1_7();
      jj_la1_8();
      jj_la1_9();
      jj_la1_10();
      jj_la1_11();
      jj_la1_12();
      jj_la1_13();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0x0,0x0,0x0,0x0,0x0,0x0,};
   }
   private static void jj_la1_1() {
      jj_la1_1 = new int[] {0x4000,0x0,0x0,0x0,0x0,0x0,};
   }
   private static void jj_la1_2() {
      jj_la1_2 = new int[] {0x0,0x0,0x0,0x0,0x0,0x0,};
   }
   private static void jj_la1_3() {
      jj_la1_3 = new int[] {0x0,0x0,0x0,0x0,0x0,0x1800,};
   }
   private static void jj_la1_4() {
      jj_la1_4 = new int[] {0x0,0x0,0x0,0x0,0x0,0x0,};
   }
   private static void jj_la1_5() {
      jj_la1_5 = new int[] {0x20,0x0,0x0,0x0,0x0,0x0,};
   }
   private static void jj_la1_6() {
      jj_la1_6 = new int[] {0x0,0x0,0x0,0x0,0x0,0x0,};
   }
   private static void jj_la1_7() {
      jj_la1_7 = new int[] {0x0,0x0,0x0,0x0,0x0,0x0,};
   }
   private static void jj_la1_8() {
      jj_la1_8 = new int[] {0x0,0x0,0x0,0x0,0x0,0x0,};
   }
   private static void jj_la1_9() {
      jj_la1_9 = new int[] {0x0,0x0,0x0,0x0,0x0,0x0,};
   }
   private static void jj_la1_10() {
      jj_la1_10 = new int[] {0x0,0x0,0x0,0x0,0x0,0x0,};
   }
   private static void jj_la1_11() {
      jj_la1_11 = new int[] {0x0,0x0,0x0,0x0,0x0,0x0,};
   }
   private static void jj_la1_12() {
      jj_la1_12 = new int[] {0x0,0x800,0x4000,0x0,0x4000,0x0,};
   }
   private static void jj_la1_13() {
      jj_la1_13 = new int[] {0x0,0x0,0x0,0x10,0x0,0x0,};
   }
  final private JJCalls[] jj_2_rtns = new JJCalls[1];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  public SQLParser(CharStream stream) {
    token_source = new SQLParserTokenManager(stream);
    token = new Token();
    token.next = jj_nt = token_source.getNextToken();
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public void ReInit(CharStream stream) {
    token_source.ReInit(stream);
    token = new Token();
    token.next = jj_nt = token_source.getNextToken();
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public SQLParser(SQLParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    token.next = jj_nt = token_source.getNextToken();
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public void ReInit(SQLParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    token.next = jj_nt = token_source.getNextToken();
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken = token;
    if ((token = jj_nt).next != null) jj_nt = jj_nt.next;
    else jj_nt = jj_nt.next = token_source.getNextToken();
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    jj_nt = token;
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static private final class LookaheadSuccess extends java.lang.Error { }
  final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  final private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }

  final public Token getNextToken() {
    if ((token = jj_nt).next != null) jj_nt = jj_nt.next;
    else jj_nt = jj_nt.next = token_source.getNextToken();
    jj_gen++;
    return token;
  }

  final public Token getToken(int index) {
    Token t = lookingAhead ? jj_scanpos : token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private java.util.Vector<int[]> jj_expentries = new java.util.Vector<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      boolean exists = false;
      for (java.util.Enumeration e = jj_expentries.elements(); e.hasMoreElements();) {
        int[] oldentry = (int[])(e.nextElement());
        if (oldentry.length == jj_expentry.length) {
          exists = true;
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              exists = false;
              break;
            }
          }
          if (exists) break;
        }
      }
      if (!exists) jj_expentries.addElement(jj_expentry);
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[445];
    for (int i = 0; i < 445; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 6; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
          if ((jj_la1_2[i] & (1<<j)) != 0) {
            la1tokens[64+j] = true;
          }
          if ((jj_la1_3[i] & (1<<j)) != 0) {
            la1tokens[96+j] = true;
          }
          if ((jj_la1_4[i] & (1<<j)) != 0) {
            la1tokens[128+j] = true;
          }
          if ((jj_la1_5[i] & (1<<j)) != 0) {
            la1tokens[160+j] = true;
          }
          if ((jj_la1_6[i] & (1<<j)) != 0) {
            la1tokens[192+j] = true;
          }
          if ((jj_la1_7[i] & (1<<j)) != 0) {
            la1tokens[224+j] = true;
          }
          if ((jj_la1_8[i] & (1<<j)) != 0) {
            la1tokens[256+j] = true;
          }
          if ((jj_la1_9[i] & (1<<j)) != 0) {
            la1tokens[288+j] = true;
          }
          if ((jj_la1_10[i] & (1<<j)) != 0) {
            la1tokens[320+j] = true;
          }
          if ((jj_la1_11[i] & (1<<j)) != 0) {
            la1tokens[352+j] = true;
          }
          if ((jj_la1_12[i] & (1<<j)) != 0) {
            la1tokens[384+j] = true;
          }
          if ((jj_la1_13[i] & (1<<j)) != 0) {
            la1tokens[416+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 445; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  final public void enable_tracing() {
  }

  final public void disable_tracing() {
  }

  final private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 1; i++) {
    try {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
          }
        }
        p = p.next;
      } while (p != null);
      } catch(LookaheadSuccess ls) { }
    }
    jj_rescan = false;
  }

  final private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}
