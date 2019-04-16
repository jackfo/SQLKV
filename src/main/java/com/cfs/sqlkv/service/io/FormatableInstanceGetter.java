package com.cfs.sqlkv.service.io;


import com.cfs.sqlkv.service.loader.InstanceGetter;

/**
 * Class that loads Formattables (typically from disk)through
 * one level of indirection.
 * A concrete implementation of this class is registered as the
 * class to handle a number of format identifiers in RegisteredFormatIds.
 * When the in-memory representation of RegisteredFormatIds is set up
 * an instance of the concrete class will be created for each format
 * identifier the class is registered for, and each instances will
 * have its setFormatId() called once with the appropriate format identifier.
 * 
 * <BR>
 * When a Formattable object is read from disk and its registered class
 * is an instance of FormatableInstanceGetter the getNewInstance() method
 * will be called to create the object.
 * The implementation can use the fmtId field to determine the
 * class of the instance to be returned.
 * <BR>
 * Instances of FormatableInstanceGetter are system wide, that is there is
 * a single set of RegisteredFormatIds per system.
 * 
 * @see RegisteredFormatIds
 */
public abstract class FormatableInstanceGetter implements InstanceGetter {

    /**
     * Format identifier of the object 
     */
	protected int fmtId;

    /**
     * Set the format identifier that this instance will be loading from disk.
    */
	public final void setFormatId(int fmtId) {
		this.fmtId = fmtId;
	}
}
