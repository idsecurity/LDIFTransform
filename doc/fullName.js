/* 
 * Copyright (C) 2016 almu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 /*
 *
 * LDIFTransform JavaScript template
 * 
 *
 */
 
//This file extends the abstract Java class se.pulsen.LDIFTransform.TransformerCommon
//The TransformerCommon class implements the interface LDIFReaderEntryTranslator which this class MUST implement

/*
* Useful documentation links:
* http://www.oracle.com/technetwork/articles/java/jf14-nashorn-2126515.html
* https://wiki.openjdk.java.net/display/Nashorn/Nashorn+extensions
* https://docs.oracle.com/javase/8/docs/technotes/guides/scripting/nashorn/api.html
*/

//https://docs.ldap.com/ldap-sdk/docs/javadoc/com/unboundid/ldap/sdk/Entry.html
var Entry = Java.type("com.unboundid.ldap.sdk.Entry");
//https://docs.ldap.com/ldap-sdk/docs/javadoc/com/unboundid/ldap/sdk/Attribute.html
var Attribute = Java.type("com.unboundid.ldap.sdk.Attribute");

var SimpleDateFormat = Java.type("java.text.SimpleDateFormat");
var sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

var System = Java.type("java.lang.System");

//The calling Java code should give us access to the logger which we can use to print log messages
logger.info("Loading JavaScript code");

//Extend the TransformerCommon class
var TransformerCommon = Java.type("se.idsecurity.LDIFTransform.TransformerCommon");


/*
* Called by the LDIFTransform main application
* Creates a new instance and returns it
*/
function getTransformer(propertiesFile) {

	var tc = new TransformerCommon(propertiesFile, 
		//Modify this function to perform all necessary processing on the LDIF entry
		{ translate: function(entry, firstLineNumber) {
		//Use _super_.methodName() to call methods from the TransformerCommon class.
		var _super_ = Java.super(tc);
		
		/**
		 * Builds a new fullName attribute from the givenName and sn attribute on the entry.
		 * If the new fullName is different from the current fullName then it will be set on the entry.
		 * Otherwise we will set the entry DN to "ignore" to cause the program to skip this entry when writing the output LDIF file.
		 */
		var currentFullName = entry.getAttributeValue("fullName");
		var givenName = entry.getAttributeValue("givenName");
		var sn = entry.getAttributeValue("sn");
		var updatedFullName = givenName + " " + sn;
		entry.removeAttribute("givenName");
		entry.removeAttribute("sn");
		
		if (currentFullName === updatedFullName) {
			entry.setDN("ignore");
		} else {
			entry.setAttribute("fullName", updatedFullName);
		}

		//Uncomment to remove attributes specified by the 'delete-attribute' property
		//entry = removeAttributes(entry, _super_.getAttributesToDelete());
	
		return entry;
		}});
	return tc;
};


/*
* Removes attributes from entry
* @param {com.unboundid.ldap.sdk.Entry} entry - The entry
* @param {java.lang.String[]} attrsToRemove - Array of attribute names to remove
*/
function removeAttributes(entry, attrsToRemove) {
	for each (attrName in attrsToRemove) {
		entry.removeAttribute(attrName);		
	}
	return entry;
};