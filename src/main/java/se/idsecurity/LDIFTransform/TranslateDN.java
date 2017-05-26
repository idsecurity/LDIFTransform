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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldif.LDIFException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Changes the DN using regex based on the properties dnPartToReplace and
 * dnPartToReplaceWith
 *
 * @author almu
 * @since v1.0
 */
public class TranslateDN extends TransformerCommon {

    private final static Logger logger = LoggerFactory
            .getLogger(TranslateDN.class);

    //Pattern dnPattern;

    Pattern prefixToDNPattern;

    Pattern attrToPrefixPattern = Pattern.compile("(.+)");
    
    Pattern eDirAclAttributesPattern = Pattern.compile(".+#.+#(.+)#.+");
    
    Set<String> eDirAclAttributes = new HashSet<>();
    
    Map<String, String> replaceMap = new HashMap<>();

    DeleteAttributes deleteAttributes;

    public TranslateDN(File transform) {
        super(transform);

        eDirAclAttributes.add("ACL");
        eDirAclAttributes.add("nspmPasswordACL");
        
        String dnPartToReplace = getDnPartToReplace();
        String dnPartToReplaceWith = getDnPartToReplaceWith();
        
       
            String[] dnPartToReplaceSplit = dnPartToReplace.split("\\|");
            String[] dnPartToReplaceWithSplit = dnPartToReplaceWith.split("\\|");
            
            if (dnPartToReplaceSplit.length != dnPartToReplaceWithSplit.length) {
                logger.error("dnPartToReplace and dnPartToReplaceWith must contain the same number of components!"
                + "\ndnPartToReplace contains " + dnPartToReplaceSplit.length
                + "\ndnPartToReplaceWith contains " + dnPartToReplaceWithSplit.length);
                
                throw new IllegalArgumentException("Incorrect number of components in dnPartToReplace/dnPartToReplaceWith property!");
            }
            
            for (int i = 0; i<dnPartToReplaceSplit.length; i++) {
                replaceMap.put(dnPartToReplaceSplit[i], dnPartToReplaceWithSplit[i]);
            }
            
       
           // dnPattern = Pattern.compile(getDnPartToReplace());
        
        
        

        /**
         * Match beginning of the dn string, CN=XYZ,OU=... and get the part
         * between XYZ part in a group for later use
         */
        prefixToDNPattern = Pattern.compile("^CN=(.+?,)");

        deleteAttributes = new DeleteAttributes(transform);

    }

    @Override
    public Entry translate(Entry original, long firstLineNumber)
            throws LDIFException {

        Entry entry = original;
        logger.info("Processing dn {}, line number {}", entry.getDN(),
                firstLineNumber);
		// Replace the DC=ACME,DC=SE part in the dn with the string in
        // dnPartToReplaceWith, e.g. OU=MyTest,DC=ACMEX,DC=SE
        String dn = entry.getDN();


        String replacedDN = replaceDN(dn);
        logger.info("Original DN: " + dn + "\nReplaced DN: " + replacedDN);
        
        entry.setDN(replacedDN);

        /**
         * Delete any attributes that we don't want
         */
        entry = deleteAttributes.translate(entry, firstLineNumber);
        
        //Translate any attributes from the "reformatDN-attribute" property
        String[] attributesToReformat = getAttributesToReformat();
        for (String attrName : attributesToReformat) {
            if (entry.hasAttribute(attrName)) {
                String[] attributeValues = entry.getAttributeValues(attrName);

                if (eDirAclAttributes.contains(attrName)) {
                    for (String attrValue : attributeValues) {
                        entry.removeAttributeValue(attrName, attrValue);
                        attrValue = replaceDNACL(attrValue);
                        entry.addAttribute(attrName, attrValue);
                    }
                } else {
                    for (String attrValue : attributeValues) {
                        entry.removeAttributeValue(attrName, attrValue);
                        attrValue = replaceDN(attrValue);
                        entry.addAttribute(attrName, attrValue);
                    }
                }

            }
        }
        
        return entry;
    }
    
    	// Replace the DC=ACME,DC=SE part in the dn with the string in
    // dnPartToReplaceWith, e.g. OU=BetaTest,DC=ACME,DC=SE
    public String replaceDN(String originalDN) {

        String before = originalDN;

        for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
            originalDN = originalDN.replaceFirst(entry.getKey(), entry.getValue());
        }
        logger.info("Before: {}, after: {}", before, originalDN);

        return originalDN;
    }

    /**
     * For use with NetIQ eDirectory ACL attribute which contains a DN
     * @param originalACL
     * @return The reformatted DN
     * @since v1.4
     */
    public String replaceDNACL(String originalACL) {
        Objects.requireNonNull(originalACL);
        Matcher dnMatcher = eDirAclAttributesPattern.matcher(originalACL);
        dnMatcher.matches();
        if (dnMatcher.group(1).contains("=")) {
            String dnToReplace = dnMatcher.group(1);
            String replacedDN = replaceDN(dnToReplace);
            originalACL = originalACL.replace(dnToReplace, replacedDN);
        }
        
        return originalACL;
    }
}
