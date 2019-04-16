/*

   Derby - Class com.cfs.sqlkv.service.loader.GeneratedByteCode

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package com.cfs.sqlkv.service.loader;


import com.cfs.sqlkv.context.Context;


/**
	Generated classes must implement this interface.

*/
public interface GeneratedByteCode {

	/**
		Initialize the generated class from a context.
		Called by the class manager just after
		creating the instance of the new class.
	*/
	public void initFromContext(Context context)
		 ;

	/**
		Set the Generated Class. Call by the class manager just after
		calling initFromContext.
	*/
	public void setGC(GeneratedClass gc);

	/**
		Called by the class manager just after calling setGC().
	*/
	public void postConstructor();

	/**
		Get the GeneratedClass object for this object.
	*/
	public GeneratedClass getGC();

	public GeneratedMethod getMethod(String methodName)  ;


	public Object e0()   ;
	public Object e1()   ;
	public Object e2()   ;
	public Object e3()   ;
	public Object e4()   ;
	public Object e5()   ;
	public Object e6()   ;
	public Object e7()   ;
	public Object e8()   ;
	public Object e9()   ;
}
