package com.cfs.sqlkv.service.loader;


import com.cfs.sqlkv.io.storage.StorageFile;

public interface JarReader {

	/**
	 * Get the StorageFile for an installed jar file.
	 */
	StorageFile getJarFile(String schemaName, String sqlName)  ;
}

