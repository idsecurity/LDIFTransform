/* 
 * Copyright (C) 2015-2018 almu
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
 
//This file extends the abstract Java class se.idsecurity.LDIFTransform.TransformerCommon
//The TransformerCommon class implements the interface LDIFReaderEntryTranslator which this class MUST implement

/*
* Useful documentation links:
* http://www.oracle.com/technetwork/articles/java/jf14-nashorn-2126515.html
* https://wiki.openjdk.java.net/display/Nashorn/Nashorn+extensions
* https://docs.oracle.com/javase/8/docs/technotes/guides/scripting/nashorn/api.html
*/

//https://docs.ldap.com/ldap-sdk/docs/javadoc/com/unboundid/ldap/sdk/Entry.html
const Entry = Java.type("com.unboundid.ldap.sdk.Entry");
//https://docs.ldap.com/ldap-sdk/docs/javadoc/com/unboundid/ldap/sdk/Attribute.html
const Attribute = Java.type("com.unboundid.ldap.sdk.Attribute");

const SimpleDateFormat = Java.type("java.text.SimpleDateFormat");
const sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

const System = Java.type("java.lang.System");

//The calling Java code should give us access to the logger which we can use to print log messages
logger.info("Loading JavaScript code");

//Extend the TransformerCommon class
const TransformerCommon = Java.type("se.idsecurity.LDIFTransform.TransformerCommon");

//DeleteAttributes transformer
const DeleteAttributes = Java.type("se.idsecurity.LDIFTransform.DeleteAttributes");

//https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
const DateTimeFormatter = Java.type("java.time.format.DateTimeFormatter");

//https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html
const LocalDate = Java.type("java.time.LocalDate");

//https://docs.oracle.com/javase/8/docs/api/index.html?java/time/LocalDate.html
const LocalDateTime = Java.type("java.time.LocalDateTime");

//https://docs.oracle.com/javase/8/docs/api/java/time/ZoneId.html
const ZoneId = Java.type("java.time.ZoneId");

//https://docs.oracle.com/javase/7/docs/api/java/util/HashSet.html
const HashSet = Java.type("java.util.HashSet");

const Files = Java.type("java.nio.file.Files");

const StandardCharsets = Java.type("java.nio.charset.StandardCharsets");

const Paths = Java.type("java.nio.file.Paths");

const Collectors = Java.type("java.util.stream.Collectors");

const Charset = Java.type("java.nio.charset.Charset");



/*
* Called by the LDIFTransform main application
* Creates a new instance and returns it
*/
function getTransformer(propertiesFile) {
        const deleteAttributes = new DeleteAttributes(propertiesFile);
	var tc = new TransformerCommon(propertiesFile, 
		//Modify this function to perform all necessary processing on the LDIF entry
		{ translate: function(entry, firstLineNumber) {
		//Use _super_.methodName() to call methods from the TransformerCommon class.
		var _super_ = Java.super(tc);

                /**
                * Delete attributes from entry based on the propertiesFile
                * Uncomment to remove attributes specified by the 'delete-attribute' property
                */        
                deleteAttributes.translate(entry, firstLineNumber);
		
	
		return entry;
		}});
	return tc;
};


