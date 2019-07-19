package com.cfs.sqlkv.service.io;
/**
  SQLKV interface for identifying the format id for the
  stored form of an object. Objects of different classes may
  have the same format id if:

  <UL>
  <LI> The objects read and write the same stored forms.
  <LI> The object's getTypeId() method returns the same
  identifier.
  <LI> The objects support all the interfaces the type
  implies.
  </UL>
  */
public interface TypedFormat
{
	/**
	  Get a universally unique identifier for the type of
	  this object. 

	  @return The identifier. (A UUID stuffed in an array
	  of 16 bytes).
	 */	
	int getTypeFormatId();
}
