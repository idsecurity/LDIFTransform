/* 
 * Copyright (C) 2015 almu
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

    Pattern dnPattern;

    Pattern prefixToDNPattern;

    Pattern attrToPrefixPattern = Pattern.compile("(.+)");

    DeleteAttributes deleteAttributes;

    public TranslateDN(File transform) {
        super(transform);

        dnPattern = Pattern.compile(getDnPartToReplace());

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
        logger.error("Processing dn {}, line number {}", entry.getDN(),
                firstLineNumber);
		// Replace the DC=ACME,DC=SE part in the dn with the string in
        // dnPartToReplaceWith, e.g. OU=MyTest,DC=ACMEX,DC=SE
        String dn = entry.getDN();
        Matcher dnMatcher = dnPattern.matcher(dn);
        if (dnMatcher.find()) {
            dn = dnMatcher.replaceFirst(getDnPartToReplaceWith());
            entry.setDN(dn);
        } else {
            logger.error("Couldn't find DN {} in string {} on line number {}",
                    getDnPartToReplace(), dn, firstLineNumber);
        }

        /**
         * Delete any attributes that we don't want
         */
        entry = deleteAttributes.translate(entry, firstLineNumber);
        
        return entry;
    }
    
    	// Replace the DC=ACME,DC=SE part in the dn with the string in
    // dnPartToReplaceWith, e.g. OU=BetaTest,DC=ACME,DC=SE
    public String replaceDN(String originalDN) {
        Matcher dnMatcher = dnPattern.matcher(originalDN);
        if (dnMatcher.find()) {
            originalDN = dnMatcher.replaceFirst(getDnPartToReplaceWith());

        }
        return originalDN;
    }

}
