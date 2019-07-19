package com.cfs.sqlkv.service.bytecode;

/**
 */
class BCMethodCaller extends BCLocalField {

	final short opcode;

	BCMethodCaller(short opcode, Type type, int cpi) {
		super(type, cpi);
		this.opcode = opcode;
	}
}

