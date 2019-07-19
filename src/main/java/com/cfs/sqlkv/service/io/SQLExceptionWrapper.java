package com.cfs.sqlkv.service.io;

import com.cfs.sqlkv.common.sanity.SanityManager;

import java.io.IOException;
import java.sql.SQLException;

/**
    Wrapper class for SQLExceptions
 */
class SQLExceptionWrapper extends SQLException
{
    private Exception myException;

    SQLExceptionWrapper(Exception e)
    {
        myException = e;
    }

    void handleMe()
        throws IOException, ClassNotFoundException
    {
        if (myException instanceof IOException)
        {
            throw ((IOException) myException);
        }
        else if (myException instanceof ClassNotFoundException)
        {
            throw ((ClassNotFoundException) myException);
        }

        if (SanityManager.DEBUG)
        {
            SanityManager.NOTREACHED();
        }
    }

    void handleMeToo()
        throws IOException
    {
        if (myException instanceof IOException)
        {
            throw ((IOException) myException);
        }

        if (SanityManager.DEBUG)
        {
            SanityManager.NOTREACHED();
        }
    }


}
