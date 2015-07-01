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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldif.LDIFException;
import com.unboundid.ldif.LDIFModifyChangeRecord;

/**
 * Special transformation for LDIF containing AD Computer objects
 *
 * @author almu
 * @since v1.0
 */
public class TranslateComputer extends TransformerCommon {

    private final static Logger logger = LoggerFactory
            .getLogger(TranslateComputer.class);

    private final TranslateDN translateDN;
    
    public TranslateComputer(File transform) {
        super(transform);
        
        translateDN = new TranslateDN(transform);
    }

    @Override
    public Entry translate(Entry original, long firstLineNumber)
            throws LDIFException {
        logger.error("Processing dn {}, line number {}", original.getDN(),
                firstLineNumber);

        Entry entry = original;
        entry = translateDN.translate(entry, firstLineNumber);
        
      

        //Remove cn attribute from entry
        if (entry.getAttribute("cn") != null) {
            entry.removeAttribute("cn");
        }

        //Remove sAMAccountType
        if (entry.getAttribute("sAMAccountType") != null) {
            entry.removeAttribute("sAMAccountType");
        }

        //Create group modify from memberOf attribute
        if (entry.getAttribute("memberOf") != null) {
            createGroupModify(entry.getDN(), entry.getAttribute("memberOf"));
            entry.removeAttribute("memberOf");
        }

        //Reformat managedBy DN
        if (entry.getAttributeValue("managedBy") != null) {
            entry.setAttribute("managedBy", translateDN.replaceDN(entry.getAttributeValue("managedBy")));
        }

        return entry;
    }



    private void createGroupModify(String dn, Attribute attribute) {
        String[] values = attribute.getValues();

        for (String value : values) {
            value = translateDN.replaceDN(value);
            Modification mod = new Modification(ModificationType.ADD, "member", dn);
            LDIFModifyChangeRecord modLdif = new LDIFModifyChangeRecord(value, mod);
            postProcessingRecords.add(modLdif);
        }
    }

}
