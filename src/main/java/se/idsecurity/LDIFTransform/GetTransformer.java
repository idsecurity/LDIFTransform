/* 
 * Copyright (C) 2017 almu
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
package se.idsecurity.LDIFTransform;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attempts to instantiate a class that extends TransformerCommon.
 * If the transformer argument passed to the program ends with .js we will try
 * to load a JavaScript file using the Nashorn engine and get an instance back.
 * Otherwise we will attempt to find a predefined transformer from the Transformers
 * enum and if that fails we attempt to load a class with that name and hope it's
 * on the class path.
 * @author almu
 */
public class GetTransformer {
	private static final Logger logger = LoggerFactory.getLogger(GetTransformer.class);

        /**
         * Instantiate a class that extends TransformerCommon using the 
         * properties file specified.
         * @param transformerClassName The class name or name of the JavaScript
         * file.
         * @param transformFile The properties file.
         * @return An object that extends TransformerCommon or if that fails
         * the program will exit.
         */
	public static TransformerCommon getTransformerClass(String transformerClassName, File transformFile) {
		TransformerCommon tc = null;
		
		if (transformerClassName.endsWith(".js")) {
			tc = getFromJavascript(transformerClassName, transformFile);
		} else {
			tc = getFromJava(transformerClassName, transformFile);
		}
		
		return tc;
		
		
	}
	
        /**
         * Attempts to load a JavaScript file that contains code that extends
         * the TransformerCommon abstract class.
         * @param filePath The path to the JavaScript file
         * @param transformFile The properties file
         * @return An object that extends TransformerCommon
         */
	private static TransformerCommon getFromJavascript(String filePath, File transformFile) {
		try {
			ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
			Logger jsLogger = LoggerFactory.getLogger("JS: " + new File(filePath).getName());
			engine.put("logger", jsLogger);
			
			Bindings bindings = new SimpleBindings();
			
			engine.getContext().setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
			
			Object eval = engine.eval(new FileReader(new File(filePath)));
			Invocable invocable = (Invocable)engine;
			
			Object transformerCommonFromJavaScript = invocable.invokeFunction("getTransformer", transformFile);
			
			TransformerCommon tc = (TransformerCommon)transformerCommonFromJavaScript;
			
			return tc;
			
			
		} catch (Exception e) {
			logger.error("Couldn't load class " + filePath, e);
			System.exit(1);
			return null;
		}
	}
	
        /**
         * Attempts to create an instance of a class that extends the TransformerCommon abstract class using reflection.
         * @param transformerClassName Name of the class.
         * @param transformFile The properties file.
         * @return An object that extends TransformerCommon
         */
	private static TransformerCommon getFromJava(String transformerClassName, File transformFile) {
		try {
			Transformers transformer = null;
			
			//Try to retrieve a Transfomers enum
			try {
				transformer = Transformers.valueOf(transformerClassName);
			} catch (IllegalArgumentException iae) {
				logger.error("Found no Transformers enum with this name: {}", transformerClassName);
			}

			//The name of the class that extends TransformerCommon
			String className = null;
			
			//If we failed to retrieve the Transformers enum we will use the class name as-is in case
			//it's available on the class path
			if (transformer == null) {
				className = transformerClassName;//Use class name as-is and hope it's on the class path
			} else {
				className = transformer.getClassName();//Use the class name from the Transformers enum
			}
					
			//Load and instantiate the class that extends TransformerCommon
			Class<?> c;
			Class<? extends TransformerCommon> transformerClass;
			TransformerCommon classToUse = null;
			c = Class.forName(className);//This will fail if the class doesn't exist on the class path
			transformerClass = c.asSubclass(TransformerCommon.class);
			Constructor<? extends TransformerCommon> con = transformerClass.getConstructor(File.class);
			
			
			classToUse = con.newInstance(transformFile);
			return classToUse;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("Couldn't load class " + transformerClassName, e);
			System.exit(1);
			return null;
		}
	}
	
}
