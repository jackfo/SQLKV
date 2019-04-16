package com.cfs.sqlkv.service.io;


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Enumeration;
import java.util.Hashtable;


/**
 * A formatable holder for a java.util.Hashtable.
 * Used to avoid serializing Properties.
 */
public class FormatableHashtable extends Hashtable<Object,Object> implements Formatable
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

	/**
	 * Niladic constructor for formatable
	 */
	public FormatableHashtable() 
	{
	}


	/**
	 * Our special put method that wont barf
	 * on a null value.
	 * @see Hashtable
	 */
	public Object put(Object key, Object value)
	{
		if (value == null)
		{
			return remove(key);
		}

		return super.put(key, value);
	}

	public void putInt(Object key, int value) {

		super.put(key, new FormatableIntHolder(value));
	}

	public int getInt(Object key) {

		return ((FormatableIntHolder) get(key)).getInt();
	}
	public void putLong(Object key, long value) {

		super.put(key, new FormatableLongHolder(value));
	}

	public long getLong(Object key) {

		return ((FormatableLongHolder) get(key)).getLong();
	}
	public void putBoolean(Object key, boolean value) {

		putInt(key,value ? 1 : 0);
	}

	public boolean getBoolean(Object key) {

		return getInt(key) == 0 ? false : true;

	}

	//////////////////////////////////////////////
	//
	// FORMATABLE
	//
	//////////////////////////////////////////////
	/**
	 * Write the hash table out.  Step through
	 * the enumeration and write the strings out
	 * in UTF.
	 *
	 * @param out write bytes here
	 *
 	 * @exception IOException thrown on error
	 */
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(size());
		for (Enumeration e = keys(); e.hasMoreElements(); )
		{
			Object key = e.nextElement();
			out.writeObject(key);
			out.writeObject(get(key));
		}
	}					

	/**
	 * Read the hash table from a stream of stored objects.
	 *
	 * @param in read this.
	 *
	 * @exception IOException					thrown on error
	 * @exception ClassNotFoundException		thrown on error
	 */
	public void readExternal(ObjectInput in)
		throws IOException, ClassNotFoundException
	{
		int size = in.readInt();
		for (; size > 0; size--)
		{
			super.put(in.readObject(), in.readObject());
		}
	}
	
	/**
	 * Get the formatID which corresponds to this class.
	 *
	 *	@return	the formatID of this class
	 */
	public	int	getTypeFormatId()	{ return StoredFormatIds.FORMATABLE_HASHTABLE_V01_ID; }
}
