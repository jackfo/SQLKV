package com.cfs.sqlkv.io;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-06 10:51
 */

import java.util.Enumeration;
import java.util.Properties;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectInput;

/**
 * A formatable holder for a java.util.Properties.
 * Used to avoid serializing Properties.
 */
public class FormatableProperties extends Properties implements Formatable
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
    public FormatableProperties()
    {
        this(null);
    }

    /**
     * Creates an empty property list with the specified
     * defaults.
     *
     * @param defaults the defaults
     */
    public FormatableProperties(Properties defaults)
    {
        super(defaults);
    }

    /**
     Clear the defaults from this Properties set.
     This sets the default field to null and thus
     breaks any link with the Properties set that
     was the default.
     */
    public void clearDefaults() {
        defaults = null;
    }

    //////////////////////////////////////////////
    //
    // FORMATABLE
    //
    //////////////////////////////////////////////
    /**
     * Write the properties out.  Step through
     * the enumeration and write the strings out
     * in UTF.
     *
     * @param out write bytes here
     *
     * @exception IOException thrown on error
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeInt(size());
        for (Enumeration e = keys(); e.hasMoreElements(); )
        {
            String key = (String)e.nextElement();
            out.writeUTF(key);
            out.writeUTF(getProperty(key));
        }
    }

    /**
     * Read the properties from a stream of stored objects.
     *
     * @param in read this.
     *
     * @exception IOException					thrown on error
     */
    @Override
    public void readExternal(ObjectInput in)
            throws IOException
    {
        int size = in.readInt();
        for (; size > 0; size--)
        {
            put(in.readUTF(), in.readUTF());
        }
    }

    @Override
    public int getTypeFormatId() {
        return StoredFormatIds.FORMATABLE_PROPERTIES_V01_ID;
    }

    /**
     * Get the formatID which corresponds to this class.
     *
     *	@return	the formatID of this class
     */
}
