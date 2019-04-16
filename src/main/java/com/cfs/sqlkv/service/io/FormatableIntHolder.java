package com.cfs.sqlkv.service.io;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A formatable holder for an int.
 */
public class FormatableIntHolder implements Formatable
{
	/********************************************************
	**
	**	This class implements Formatable. That means that it
	**	can write itself to and from a formatted stream. If
	**	you add more fields to this class, make sure that you
	**	also write/read them with the writeExternal()/readExternal()
	**	methods.
	**
	**	If, inbetween releases, you add more fields to this class,
	**	then you should bump the version number emitted by the getTypeFormatId()
	**	method.
	**
	********************************************************/

	// the int
	private int theInt;
	
	/**
	 * Niladic constructor for formatable
	 */
	public FormatableIntHolder() 
	{
	}

	/**
	 * Construct a FormatableIntHolder using the input int.
	 *
	 * @param theInt the int to hold
	 */
	public FormatableIntHolder(int theInt)
	{
		this.theInt = theInt;
	}

	/**
	 * Set the held int to the input int.
	 *
	 * @param theInt the int to hold
	 */
	public void setInt(int theInt)
	{
		this.theInt = theInt;
	}

	/**
	 * Get the held int.
	 *
	 * @return	The held int.
	 */
	public int getInt()
	{
		return theInt;
	}

	/**
	 * Create and return an array of FormatableIntHolders
	 * given an array of ints.
	 *
	 * @param theInts	The array of ints
	 *
	 * @return	An array of FormatableIntHolders
	 */
	public static FormatableIntHolder[] getFormatableIntHolders(int[] theInts)
	{
		if (theInts == null)
		{
			return null;
		}

		FormatableIntHolder[] fihArray = new FormatableIntHolder[theInts.length];

		for (int index = 0; index < theInts.length; index++)
		{
			fihArray[index] = new FormatableIntHolder(theInts[index]);
		}
		return fihArray;
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
		out.writeInt(theInt);
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
		theInt = in.readInt();
	}
	
	/**
	 * Get the formatID which corresponds to this class.
	 *
	 *	@return	the formatID of this class
	 */
	public	int	getTypeFormatId()	{ return StoredFormatIds.FORMATABLE_INT_HOLDER_V01_ID; }
}
