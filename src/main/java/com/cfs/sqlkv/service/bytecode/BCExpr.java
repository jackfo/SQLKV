package com.cfs.sqlkv.service.bytecode;

/**
 *
 * To be able to identify the expressions as belonging to this
 * implementation, and to be able to generate code off of
 * it if so.
 *
 */
interface BCExpr {

	// maybe these should go into Declarations, instead?
	// note there is no vm_boolean; boolean is an int
	// except in arrays, where it is a byte.
	short vm_void = -1; // not used in array mappings.
	short vm_byte = 0;
	short vm_short = 1;
	short vm_int = 2;
	short vm_long = 3;
	short vm_float = 4;
	short vm_double = 5;
	short vm_char = 6;
	short vm_reference = 7;

}
