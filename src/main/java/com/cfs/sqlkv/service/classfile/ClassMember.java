package com.cfs.sqlkv.service.classfile;

import java.io.IOException;


public class ClassMember {
	protected ClassHolder cpt;
	protected int access_flags;
	protected int name_index;
	protected int descriptor_index;
	protected Attributes attribute_info; // can be null

	ClassMember(ClassHolder cpt, int modifier, int name, int descriptor) {
		this.cpt = cpt;
		name_index = name;
		descriptor_index = descriptor;
		access_flags = modifier;
	}

	/*
	** Public methods from ClassMember
	*/

    public int getModifier() {
			return access_flags;
	}

    public String getDescriptor() {
		return cpt.nameIndexToString(descriptor_index);
	}
	
	public String getName() {
		return cpt.nameIndexToString(name_index);
	}

	public void addAttribute(String attributeName, ClassFormatOutput info) {

		if (attribute_info == null)
			attribute_info = new Attributes(1);

		attribute_info.addEntry(new AttributeEntry(cpt.addUtf8(attributeName), info));
	}


	/*
	**	 ----
	*/

	void put(ClassFormatOutput out) throws IOException {
		out.putU2(access_flags);
		out.putU2(name_index);
		out.putU2(descriptor_index);

		if (attribute_info != null) {
			out.putU2(attribute_info.size());
			attribute_info.put(out);
		} else {
			out.putU2(0);
		}
	}

	int classFileSize() {
		int size = 2 + 2 + 2 + 2;
		if (attribute_info != null)
			size += attribute_info.classFileSize();
		return size;
	}
}
