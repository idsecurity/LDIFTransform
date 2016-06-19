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
package se.idsecurity.LDIFTransform;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unboundid.ldif.LDIFReaderEntryTranslator;
import com.unboundid.ldif.LDIFRecord;

/**
 * Contains convenience methods, all transformers are required to extend this
 * class
 *
 * @author almu
 * @since v1.0
 */
public abstract class TransformerCommon implements LDIFReaderEntryTranslator {

    private final static Logger logger = LoggerFactory.getLogger(TransformerCommon.class);
    private final File transform;
    private Properties properties = new Properties();

    /**
     * Well known properties
     */
    private final String[] knownProperties = {"delete-attribute", "dnPartToReplace", "dnPartToReplaceWith", "prefixToDN", "reformatDN-attribute", "static-password", "delete-attribute-starts-with"};

    /**
     * Preloaded with property values from property file
     */
    private String[] attributesToDelete;
    private String dnPartToReplace;
    private String dnPartToReplaceWith;
    private String prefixToDN;
    private String[] attributesToPrefix;
    private String[] attributesToReformat;
    private String staticPassword;
    private String[] attributesToDeleteStartsWith;

    /**
     * Contains the changes to groups that will be performed based on the
     * memberOf attribute
     */
    protected List<LDIFRecord> postProcessingRecords = new ArrayList<>();

    /**
     * Preloads a number of variables from the properties file
     *
     * @param transform
     */
    public TransformerCommon(File transform) {
        this.transform = transform;

        try {
            FileInputStream propStream = new FileInputStream(transform);
            properties.load(propStream);

            //Parse the properties file for known properties, unknown properties have to be fetched from each class extending TransformerCommon
            for (String propertyName : knownProperties) {
                String propertyValue = properties.getProperty(propertyName, "");
                switch (propertyName) {
                    case "delete-attribute"://delete-attribute is comma separated
                        this.attributesToDelete = propertyValue.split(",");
                        break;
                    case "dnPartToReplace":
                        this.dnPartToReplace = propertyValue;
                        break;
                    case "dnPartToReplaceWith":
                        this.dnPartToReplaceWith = propertyValue;
                        break;
                    case "prefixToDN":
                        this.prefixToDN = propertyValue;
                        break;
                    case "reformatDN-attribute":
                        this.attributesToReformat = propertyValue.split(",");
                        break;
                    case "attributesToPrefix":
                        this.attributesToPrefix = propertyValue.split(",");
                        break;
                    case "static-password":
                        this.staticPassword = propertyValue;
                        break;
                    case "delete-attribute-starts-with":
                        this.attributesToDeleteStartsWith = propertyValue.split(",");
                        break;
                }

            }

        } catch (IOException e) {
            logger.error("Exception reading transform file", e);
            throw new IllegalStateException("Could not create instance", e);
        }

    }

    protected Properties getProperties() {
        return properties;
    }

    public List<LDIFRecord> getPostProcessingRecords() {
        return postProcessingRecords;
    }

    /**
     * @return the attributesToDelete
     */
    protected String[] getAttributesToDelete() {
        return attributesToDelete;
    }

    /**
     * @return the dnPartToReplace
     */
    protected String getDnPartToReplace() {
        return dnPartToReplace;
    }

    /**
     * @return the dnPartToReplaceWith
     */
    protected String getDnPartToReplaceWith() {
        return dnPartToReplaceWith;
    }

    /**
     * @return the prefixToDN
     */
    protected String getPrefixToDN() {
        return prefixToDN;
    }

    /**
     * @return the attributesToReformat
     */
    protected String[] getAttributesToReformat() {
        return attributesToReformat;
    }

    /**
     * @return the attributesToPrefix
     */
    protected String[] getAttributesToPrefix() {
        return attributesToPrefix;
    }

    /**
     * @return the staticPassword
     */
    protected String getStaticPassword() {
        return staticPassword;
    }
    
    /**
     * @return the attributesToDeleteStartsWith
     */
    protected String[] getAttributesToDeleteStartsWith() {
        return attributesToDeleteStartsWith;
    }

}
