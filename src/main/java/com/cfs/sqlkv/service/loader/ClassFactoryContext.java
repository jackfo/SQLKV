package com.cfs.sqlkv.service.loader;


import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.context.ContextImpl;


/**
 * Context that provides the correct ClassFactory for the
 * current service. Allows stateless code to obtain the
 * correct class loading scheme.
*/
public abstract class ClassFactoryContext extends ContextImpl {

	public static final String CONTEXT_ID = "ClassFactoryContext";

	private final ClassFactory cf;

	protected ClassFactoryContext(ContextManager cm, ClassFactory cf) {

		super(cm, CONTEXT_ID);

		this.cf = cf;
	}

	public final ClassFactory getClassFactory() {
		return cf;
	}






	/**
		Get the mechanism to rad jar files. The ClassFactory
		may keep the JarReader reference from the first class load.
	*/
	public abstract JarReader getJarReader();

    /**
     * Handle any errors. Only work here is to pop myself
     * on a session or greater severity error.
     */
	public final void cleanupOnError(Throwable error) {

    }
}
