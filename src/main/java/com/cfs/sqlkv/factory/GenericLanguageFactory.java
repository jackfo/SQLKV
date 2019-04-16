package com.cfs.sqlkv.factory;

import com.cfs.sqlkv.compile.sql.GenericParameterValueSet;
import com.cfs.sqlkv.compile.sql.ParameterValueSet;
import com.cfs.sqlkv.service.loader.ClassInspector;

public class GenericLanguageFactory {

    private GenericParameterValueSet emptySet=new GenericParameterValueSet(null, 0, false);;

    public ParameterValueSet newParameterValueSet(ClassInspector ci, int numParms, boolean hasReturnParam){
        if (numParms == 0){
            return emptySet;
        }
        return new GenericParameterValueSet(ci, numParms, hasReturnParam);
    }
}
