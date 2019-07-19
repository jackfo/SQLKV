package com.cfs.sqlkv.service.io;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A formatable holder for an long.
 */
public class FormatableLongHolder implements Formatable
{

	// the int
	private long theLong;
	
	/**
	 * Niladic constructor for formatable
	 */
	public FormatableLongHolder() 
	{
	}

	/**
	 * Construct a FormatableLongHolder using the input integer.
	 *
	 * @param theLong the long to hold
	 */
	public FormatableLongHolder(long theLong)
	{
		this.theLong = theLong;
	}

	/**
	 * Set the held long to the input int.
	 *
	 * @param theLong the int to hold
	 */
	public void setLong(int theLong)
	{
		this.theLong = theLong;
	}

	/**
	 * Get the held int.
	 *
	 * @return	The held int.
	 */
	public long getLong()
	{
		return theLong;
	}

	/**
	 * Create and return an array of FormatableLongHolders
	 * given an array of ints.
	 *
	 * @param theLongs	The array of longs
	 *
	 * @return	An array of FormatableLongHolders
	 */
	public static FormatableLongHolder[] getFormatableLongHolders(long[] theLongs)
	{
		if (theLongs == null)
		{
			return null;
		}

		FormatableLongHolder[] flhArray = new FormatableLongHolder[theLongs.length];

		for (int index = 0; index < theLongs.length; index++)
		{
			flhArray[index] = new FormatableLongHolder(theLongs[index]);
		}
		return flhArray;
	}

	//////////////////////////////////////////////
	//
	// FORMATABLE
	//
	//////////////////////////////////////////////
	/**
	 * Write this formatable out
	 *
	 * @param out write bytes here
	 *
 	 * @exception IOException thrown on error
	 */
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeLong(theLong);
	}

	/**
	 * Read this formatable from a stream of stored objects.
	 *
	 * @param in read this.
	 *
	 * @exception IOException					thrown on error
	 */
	public void readExternal(ObjectInput in)
		throws IOException
	{
		theLong = in.readLong();
	}
	
	/**
	 * Get the formatID which corresponds to this class.
	 *
	 *	@return	the formatID of this class
	 */
	public	int	getTypeFormatId()	{ return StoredFormatIds.FORMATABLE_LONG_HOLDER_V01_ID; }
}
