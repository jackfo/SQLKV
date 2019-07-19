package com.cfs.sqlkv.service.io;

/**
  Formatable for holding SQL data (which may be null).
  @see Formatable
 */
public interface Storable
extends Formatable
{

	/**
	  Return whether the value is null or not.
	  
	  @return true if the value is null and false otherwise.
	**/
	public boolean isNull();

	/**
	  Restore this object to its (SQL)null value.
	**/
	public void restoreToNull();
}
