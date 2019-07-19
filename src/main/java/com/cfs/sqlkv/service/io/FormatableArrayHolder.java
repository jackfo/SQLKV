package com.cfs.sqlkv.service.io;

import com.cfs.sqlkv.common.sanity.SanityManager;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

/**
 * A formatable holder for an array of formatables.
 * Used to avoid serializing arrays.
 */
public class FormatableArrayHolder implements Formatable
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

	// the array
	private Object[] array;
	
	/**
	 * Niladic constructor for formatable
	 */
	public FormatableArrayHolder() 
	{
	}

	/**
	 * Construct a FormatableArrayHolder using the input
	 * array.
	 *
	 * @param array the array to hold
	 */
	public FormatableArrayHolder(Object[] array)
	{
		if (SanityManager.DEBUG)
		{
			SanityManager.ASSERT(array != null, 
					"array input to constructor is null, code can't handle this.");
		}

		setArray( array );
	}

	/**
	 * Set the held array to the input array.
	 *
	 * @param array the array to hold
	 */
	public void setArray(Object[] array)
	{
		if (SanityManager.DEBUG)
		{
			SanityManager.ASSERT(array != null, 
					"array input to setArray() is null, code can't handle this.");
		}

		this.array = ArrayUtil.copy( array );
	}

	/**
	 * Get the held array of formatables, and return
     * it in an array that is an instance of {@code arrayClass}.
	 *
     * @param arrayClass the type of array to return
	 *
	 * @return an array of formatables
	 */
    public <E> E[] getArray(Class<E[]> arrayClass)
	{
        return Arrays.copyOf(array, array.length, arrayClass);
	}

	//////////////////////////////////////////////
	//
	// FORMATABLE
	//
	//////////////////////////////////////////////
	/**
	 * Write this array out
	 *
	 * @param out write bytes here
	 *
 	 * @exception IOException thrown on error
	 */
	public void writeExternal(ObjectOutput out) throws IOException
	{
		if (SanityManager.DEBUG)
		{
			SanityManager.ASSERT(array != null, "Array is null, which isn't expected");
		}

		ArrayUtil.writeArrayLength(out, array);
		ArrayUtil.writeArrayItems(out, array);
	}

	/**
	 * Read this array from a stream of stored objects.
	 *
	 * @param in read this.
	 *
	 * @exception IOException					thrown on error
	 * @exception ClassNotFoundException		thrown on error
	 */
	public void readExternal(ObjectInput in)
		throws IOException, ClassNotFoundException
	{
		array = new Object[ArrayUtil.readArrayLength(in)];
		ArrayUtil.readArrayItems(in, array);
	}
	
	/**
	 * Get the formatID which corresponds to this class.
	 *
	 *	@return	the formatID of this class
	 */
	public	int	getTypeFormatId()	{ return StoredFormatIds.FORMATABLE_ARRAY_HOLDER_V01_ID; }
}
