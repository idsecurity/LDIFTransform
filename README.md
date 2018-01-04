# LDIFTransform

> A tool based on the UnboundID LDAP SDK library that can be used to transform LDIF files.

# Requirements

- Java 8u151 or newer
- UnboundID LDAP SDK
- SLF4J

# Instructions

For help run:
    java -jar LDIFTransform-1.5.jar`

which outputs:


    java -jar LDIFTransform-1.5.jar <path to transform file> <path to input LDIF> <path to output LDIF> <name of transformer class/path to JavaScript file> <add|delete|modify-replace|modify-add|modify-delete|none> [NoSort]
    Transformer class names:
    TranslateDN
    DeleteAttributes
    TranslateADUser
    TranslateComputer


Explanation of the different options:

|Argument | Explanation
|--------------------------|-----------------| 
| \<path to transform file> | Required, specifies a properties file with different key/values that are used by the transformer. |
| \<path to input LDIF> | Required, the LDIF file containing content records that will be transformed |
| \<path to output LDIF> | Required, the LDIF file that will be created and populated with modified data |
| <name of transformer class/path to JavaScript file> | Required, does the actual transformation of each LDIF record. All transformers extend the TransformerCommon class. Can be a predefined class included with the program, a class you write yourself, compile and include on the classpath when running the program or a JavaScript file that extends the TransformerCommon class using Nashorns Java.extend() function. For more details see the Transformers section of the documentation |
| <add\|delete\|modify-replace\|modify-add\|modify-delete\|none> | Required, the changetype of LDIF record to produce. `add` produces `changetype: add`, `delete` produces `changetype: delete`, `modify-add` produces `changetype: modify` where each attribute value is an `add`, `modify-replace` produces `changetype: modify` where each attribute value is a `replace`, `modify-delete` produces `changetype: modify` where each attribute value is a `delete`, `none` returns the Entry as it was returned by the transformer. |
| [NoSort] | Optional, if absent then the output will be sorted hierarchically. Use NoSort if the file is already sorted or sorting doesn't matter. Sorting can cause OutOfMemory errors if processing large files. In that increase the heap size when running the application. |

# Transformers

There are several transformers that come with the application that can be used to change the LDIF file in some specific ways.
You may also write your own either in Java or JavaScript. The JavaScript way is easier since you don't have to have the JDK to compile the code.

This is a list of the built-in transformers, you can also see the list when running the application without any arguments.

| Transformer name | Explanation  |
|-----------------|-------------|
| `DeleteAttributes` | Deletes attributes from the entry using the property `delete-attribute`. |
| `TranslateDN` | Translates the DN using regex using the properties `dnPartToReplace` and `dnPartToReplaceWith`. Also calls `DeleteAttributes` |
| `TranslateADUser` | Calls `TranslateDN` first. Sets a static password on the entry using the property `static-password`. Translates the DN for the `memberOf` values. Creates separate LDIF modifies for each `memberOf` value that modify the group specified in the `memberOf` value. Deletes the `cn` attribute if present. 
| `TranslateComputer` | Calls `TranslateDN` first. Removes the `samAccountType` and `cn` attributes if present. Translates the DN for the `memberOf` and `managedBy` values. Creates separate LDIF modifies for each `memberOf` value that modify the group specified in the `memberOf` value. |

# Writing transformers in JavaScript

One can write own transformers in JavaScript.
For examples see the `.js` files in the `doc` directory, use the `transformer-template.js` file as a base for creating new transformers.
Modify the translate function, this is where the magic happens:


    function getTransformer(propertiesFile) {

	var tc = new TransformerCommon(propertiesFile, 
		//Modify this function to perform all necessary processing on the LDIF entry
		{ translate: function(entry, firstLineNumber) {
		//Use _super_.methodName() to call methods from the TransformerCommon class.
		var _super_ = Java.super(tc);

		//Uncomment to remove attributes specified by the 'delete-attribute' property
		//entry = removeAttributes(entry, _super_.getAttributesToDelete());
	
		return entry;
		}});
	return tc;
    };


This requires knowledge of the UnboundID LDAP SDK Entry and Attribute classes. Links to the Javadoc can be found in the `.js` files in the `doc` directory.


# Properties file (transform file)

The transform file which is the first argument expected by the application is just a simple Java properties file which you can access during runtime.
You can add any key/value pairs you want. There are some predefined that will be preloaded and for which there exists convenience methods you can call to get the values.

| Key | Explanation | Method name |
|------|------------|---------------|
| `delete-attribute` | Comma separated list of attributes to delete from the entry if present, e.g. `cn,lastLogon,sAMAccountType` | getAttributesToDelete() |
| `dnPartToReplace` | Which part of the DN to replace, e.g. `dc=acme,dc=se` | getDnPartToReplace() |
| `dnPartToReplaceWith` | The string that will replace the part specified in `dnPartToReplace`, e.g. `dc=mydomain,dc=de` | getDnPartToReplaceWith() |
| `prefixToDN` | A string that should be prefixed to the DN, how and where depends on the implementation | getPrefixToDN() |
| `reformatDN-attribute` | Attributes that contain DN's that should be reformatted in the same way as the DN of the entry, e.g. `memberOf` and such. | getAttributesToReformat() |
| `static-password` | A password that should be set on all entries, the name of the attribute depends on the implementation | getStaticPassword() |
| `delete-attribute-starts-with` | Comma separated list of attribute name substrings that should be deleted from the entry if present and if the attribute name starts which one such substring, e.g. `msExch` will delete all attributes that start with `msExch` | getAttributesToDeleteStartsWith() |

You can access the Properties object from the transformer code by calling the `getProperties()` method and that way access your own properties.

# Concepts

The basic concept is: input -> transform -> output

The transform part something that is different from case to case and you can basically do almost anything you want.

Let's say you have a file containing entries from directory server A (DS1) and you need to import those entries into directory server B which runs another vendors software (DS2).
There are of course differences in where the entries will be placed so we need to modify the DN of all entries.
We might need to remove some attributes that shouldn't be imported into DS2.
We might need to add some attributes that don't exists in DS1 or we need to reformat the existing attributes.

All this is solvable using the usual tools such as `sed` or `awk` or `perl` or just about any other scripting language.
You can also of course solve this using an Identity Management solution.

For one time imports, an IDM solution is often to much. Using other tools that don't understand LDIF is workable but you have to take care to think about line folding, base 64 encoding/decoding that might occur if you have non-ASCII characters in your values or in the DN and other LDIF specifics when you parse the file.

So the basic idea is to use something that understands LDIF and is specifically written from the start for that purpose.
Then we can concentrate on the task at hand and we don't have worry if we will produce valid LDIF or not since we are using a well know and tested library for that.

# Example usage

- Convert content records to add change records and transform the DN and sort the entries

`java -jar LDIFTransform-1.5.jar doc/dn.properties ./input.ldif ./output.ldif TranslateDN changetype add`

- Convert content records to delete change records without transforming the DN and don't sort

`java -jar LDIFTransform-1.5.jar doc/empty.properties ./input.ldif ./delete.ldif ./doc/transformer-template.js delete NoSort`

- Convert content records to content records and transform the DN and don't sort

`java -jar LDIFTransform-1.5.jar doc/dn.properties ./input.ldif ./output.ldif TranslateDN changetype none NoSort`

- How to run with additional JAR files on the class path and with a custom JavaScript transform file

`java -cp .\Ldiftransform-1.5.jar;lib\* se.idsecurity.LDIFTransform.LDIFTransform ..\..\doc\initialimport.properties "users.ldif" "users-initial.ldif" ..\..\doc\initialimport.js none NoSort`

# Skipping entries
Since v1.3 it is possible to skip entries from being copied from the input LDIF to the output LDIF by setting the entry DN in a transformer to `ignore`.
For an example see the `doc\fullName.js` transformer.

# Limitations

Handles only LDIF files containing content records or add records. Modify records and other changetypes are not supported.

# Changelog

v1.5 (2018-01-01)

+ The Nashorn ECMAScript engine is now loaded with "--language=es6" so that you
 can use ES6 features supported by Nashorn such as const and let.

+ Prints timining statistics on how long each operation took.


v1.4 (2017-05-26)

* Updated help information

* Add support for reformatting DN attributes used by the NetIQ eDirectory ACL attribute

* Updated libraries to latest versions

v1.3 (2016-06-17)

* Add new property to the properties file: delete-attribute-starts-with
 Comma separated list of attribute name substring. For example delete-attribute-starts-with=msExch will cause a delete of all attributes whose name start with msExch

v1.2 (2016-05-11)

* Add version header to the output LDIF file.
* Add better logging of input parameters.
* Add support for skipping entries, i.e. not writing some entries that are in the input LDIF to the output LDIF by setting the entry DN to `ignore`.

v1.1 (2015-09-03)

* Added support for producing LDIF files with `changetype: modify`

v1.0 (2015-07-02)

* Initial release

# License

[GPL v3.0](http://www.gnu.org/licenses/gpl-3.0.txt)
