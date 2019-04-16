package com.cfs.sqlkv.service.io;

/**
    Getting error information for SQLData/serializable data streams.
 */
interface ErrorInfo
{
	String getErrorInfo();

    Exception getNestedException();
}
