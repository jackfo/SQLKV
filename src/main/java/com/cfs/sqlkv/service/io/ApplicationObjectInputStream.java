package com.cfs.sqlkv.service.io;

import com.cfs.sqlkv.service.loader.ClassFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
	An object input stream that implements resolve class in order
	to load the class through the ClassFactory.loadApplicationClass method.
*/
class ApplicationObjectInputStream extends ObjectInputStream implements ErrorObjectInput
{

	protected ClassFactory cf;
	protected ObjectStreamClass        initialClass;

	ApplicationObjectInputStream(InputStream in, ClassFactory cf)
		throws IOException {
		super(in);
		this.cf = cf;
	}

	protected Class resolveClass(ObjectStreamClass v)
		throws IOException, ClassNotFoundException {

		if (initialClass == null)
			initialClass = v;

		if (cf != null)
			return cf.loadApplicationClass(v);

		throw new ClassNotFoundException(v.getName());
	}

	public String getErrorInfo() {
		if (initialClass == null)
			return "";

		return initialClass.getName() + " (serialVersionUID="
			+ initialClass.getSerialVersionUID() + ")";
	}

	public Exception getNestedException() {
        return null;
	}

}
