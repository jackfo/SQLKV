package com.cfs.sqlkv.compile.sql;

import com.cfs.sqlkv.compile.result.ResultSet;
import com.cfs.sqlkv.context.LanguageConnectionContext;

import com.cfs.sqlkv.sql.activation.Activation;



public interface PreparedStatement {

    Activation getActivation(LanguageConnectionContext lcc, boolean scrollable)  ;

    ResultSet execute(Activation activation, boolean forMetaData, long timeoutMillis)  ;
}
