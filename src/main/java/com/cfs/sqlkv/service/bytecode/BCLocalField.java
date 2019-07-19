package com.cfs.sqlkv.service.bytecode;

import com.cfs.sqlkv.service.compiler.LocalField;

class BCLocalField implements LocalField {

	final int	   cpi; // of the Field Reference
	final Type     type;

	BCLocalField(Type type, int cpi) {
		this.cpi = cpi;
		this.type = type;
	}
}
