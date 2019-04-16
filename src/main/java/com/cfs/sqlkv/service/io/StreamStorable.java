package com.cfs.sqlkv.service.io;



import java.io.InputStream;

/**
 * Streaming interface for a data value. The format of
 * the stream is data type dependent and represents the
 * on-disk format of the value. That is it is different
 * to the value an application will see through JDBC
 * with methods like getBinaryStream and getAsciiStream.
 * 
 * <BR>
 * If the value is NULL (DataValueDescriptor.isNull returns
 * true then these methods should not be used to get the value.

  @see Formatable
 */
public interface StreamStorable
{
	/**
	  Return the on-disk stream state of the object.
	  
	**/
	public InputStream returnStream();

	/**
	  sets the on-disk stream state for the object.
	**/
	public void setStream(InputStream newStream);

	/**
     * Set the value by reading the stream and
     * converting it to an object form.
     * 
		@exception StandardException on error
	**/
	public void loadStream()  ;
}
