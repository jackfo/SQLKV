package com.cfs.sqlkv.service.io;


import java.io.ByteArrayInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;


class DebugByteTeeOutputStream extends FilterOutputStream {
	private AccessibleByteArrayOutputStream tee = new AccessibleByteArrayOutputStream(256);

	DebugByteTeeOutputStream(OutputStream out) {
		super(out);
	}
	
	public void write(int b) throws IOException {
		out.write(b);
		tee.write(b);
	}

	public void write(byte[] b, int off, int len) throws IOException {

		out.write(b,off,len);
		tee.write(b,off,len);
	}


	void checkObject(Formatable f) {

		ByteArrayInputStream in = new ByteArrayInputStream(tee.getInternalByteArray(), 0, tee.size());

		FormatIdInputStream fin = new FormatIdInputStream(in);

		// now get an empty object given the format identification
		// read it in
		// then compare it???

		Formatable f1 = null;
		try {

			f1 = (Formatable) fin.readObject();

			if (f1.equals(f)) {
				return;
			}

			// If the two objects are not equal and it looks
			// like they don't implement their own equals()
			// (which requires a matching hashCode() then
			// just return. The object was read sucessfully.

			if ((f1.hashCode() == System.identityHashCode(f1)) &&
				(f.hashCode() == System.identityHashCode(f)))
				return;

		} catch (Throwable t) {

            // for debugging purposes print this both to derby.log and to
            // System.out.
            String err_msg = 
                "FormatableError:read error    : " + t.toString() + 
                "\nFormatableError:class written : " + f.getClass();

            err_msg += (f1 == null) ? 
                "FormatableError:read back as null" :
                ("FormatableError:class read    : " + f1.getClass());

            err_msg +=
                "FormatableError:write id      : " + 
                    FormatIdUtil.formatIdToString(f.getTypeFormatId());

            if (f1 != null) {
                err_msg += "FormatableError:read id       : " + 
                    FormatIdUtil.formatIdToString(f1.getTypeFormatId());
            }

            System.out.println(err_msg);
			t.printStackTrace(System.out);

		}

		//System.out.println("FormatableError:Class written " + f.getClass() + " format id " + f.getTypeFormatId());
		//if (f1 != null)
			//System.out.println("FormatableError:Class read    " + f1.getClass() + " format id " + f1.getTypeFormatId());
	}
}
