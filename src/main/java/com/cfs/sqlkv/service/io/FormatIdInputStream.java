package com.cfs.sqlkv.service.io;

import com.cfs.sqlkv.common.context.ContextService;
import com.cfs.sqlkv.container.Monitor;
import com.cfs.sqlkv.context.Context;

import com.cfs.sqlkv.service.loader.ClassFactory;
import com.cfs.sqlkv.service.loader.ClassFactoryContext;
import com.cfs.sqlkv.util.InstanceUtil;


import java.io.*;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * A stream for reading objects with format id tags which was
 * produced by a FormatIdOutputStream.
 *
 * <P>Please see the documentation for FormatIdOutputStream for
 * information about the streams format and capabilites.
 */
public final class FormatIdInputStream extends DataInputStream
        implements ErrorObjectInput, CloneableStream {
    protected ClassFactory cf;
    private ErrorInfo errorInfo;
    private Exception myNestedException;


    /**
     * Constructor for a FormatIdInputStream
     *
     * @param in bytes come from here.
     */
    public FormatIdInputStream(InputStream in) {
        super(in);
    }

    /**
     * Read an object from this stream.
     *
     * @return The read object.
     * @throws IOException            An IO or serialization error occurred.
     * @throws ClassNotFoundException A class for an object in
     *                                the stream could not be found.
     */

    public Object readObject() throws IOException, ClassNotFoundException {
        setErrorInfo(null);
        int fmtId = FormatIdUtil.readFormatIdInteger(this);
        if (fmtId == StoredFormatIds.NULL_FORMAT_ID) {
            return null;
        }

        if (fmtId == StoredFormatIds.STRING_FORMAT_ID) {
            return readUTF();
        }

        try {

            if (fmtId == StoredFormatIds.SERIALIZABLE_FORMAT_ID) {
                ObjectInputStream ois = getObjectStream();
                try {
                    Object result = ois.readObject();
                    return result;
                } catch (IOException ioe) {
                    throw handleReadError(ioe, ois);
                } catch (ClassNotFoundException cnfe) {
                    throw handleReadError(cnfe, ois);
                } catch (LinkageError le) {
                    throw handleReadError(le, ois);
                } catch (ClassCastException cce) {
                    throw handleReadError(cce, ois);
                }
            }
            try {
                Formatable f = (Formatable)InstanceUtil.newInstanceFromIdentifier(fmtId);
                if (f instanceof Storable) {
                    boolean isNull = this.readBoolean();
                    if (isNull == true) {
                        Storable s = (Storable) f;
                        s.restoreToNull();
                        return s;
                    }
                }
                f.readExternal(this);
                return f;
            } catch (Exception se) {
                throw new ClassNotFoundException(se.toString());
            }

        } catch (ClassCastException cce) {
            // We catch this here as it is usuall a user error.
            // they have readExternal (or SQLData) that doesn't match
            // the writeExternal. and thus the object read is of
            // the incorrect type, e.g. Integer i = (Integer) in.readObject();
            StreamCorruptedException sce = new StreamCorruptedException(cce.toString());
            sce.initCause(cce);
            throw sce;
        }
    }

    /**
     * Set the InputStream for this FormatIdInputStream to the stream
     * provided.
     *
     * @param in The new input stream.
     */
    public void setInput(InputStream in) {
        this.in = in;
    }

    public InputStream getInputStream() {
        return in;
    }

    public String getErrorInfo() {
        if (errorInfo == null)
            return "";

        return errorInfo.getErrorInfo();
    }

    public Exception getNestedException() {
        if (myNestedException != null)
            return null;

        if (errorInfo == null)
            return null;

        return errorInfo.getNestedException();
    }

    private void setErrorInfo(ErrorInfo ei) {
        errorInfo = ei;
    }

    /**
     * Handle an error that happened within {@code readObject()} when reading
     * a {@code Serializable} object.
     *
     * @param <T>    the type of exception that was thrown
     * @param cause  the thrown exception
     * @param stream the stream from which the exception was thrown
     * @return the thrown exception
     */
    private <T extends Throwable> T handleReadError(
            T cause, ObjectInputStream stream) {
        // If the input stream implements the ErrorInfo interface, it contains
        // extra information about the error, and we want to make that
        // information available to error handlers on a higher level.
        if (stream instanceof ErrorInfo) {
            setErrorInfo((ErrorInfo) stream);
        }
        return cause;
    }

    ClassFactory getClassFactory() {
        if (cf == null) {

            ClassFactoryContext cfc =
                    (ClassFactoryContext) getContextOrNull
                            (ClassFactoryContext.CONTEXT_ID);

            if (cfc != null)
                cf = cfc.getClassFactory();
        }
        return cf;
    }

    /*
     ** Class private methods
     */

    private ObjectInputStream getObjectStream() throws IOException {

        return getClassFactory() == null ?
                new ObjectInputStream(this) :
                new ApplicationObjectInputStream(this, cf);
    }


    /*** CloneableStream interface ***/

    /**
     * @see CloneableStream#cloneStream()
     */
    public InputStream cloneStream() {
        InputStream new_input_stream = ((CloneableStream) in).cloneStream();

        return (new FormatIdInputStream(new_input_stream));
    }

    /**
     * Privileged lookup of a Context. Must be private so that user code
     * can't call this entry point.
     */
    private static Context getContextOrNull(final String contextID) {

        return ContextService.getContextOrNull(contextID);

    }

}
