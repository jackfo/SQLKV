package com.cfs.sqlkv.service.io;

import java.io.ObjectInput;

/**
	Limit and ObjectInput capabilities.

	Combin
 */
public interface ErrorObjectInput extends ObjectInput, ErrorInfo
{
	public String getErrorInfo();

    public Exception getNestedException();

}

