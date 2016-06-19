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

import com.unboundid.ldap.sdk.Attribute;
import java.io.File;

import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldif.LDIFException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Deletes the specified attributes from the entry
 * @author almu
 * @since v1.0
 */
public class DeleteAttributes extends TransformerCommon {

	public DeleteAttributes(File transform) {
		super(transform);
		
	}

	@Override
	public Entry translate(Entry original, long firstLineNumber)
			throws LDIFException {
		
		Entry entry = original;
                
                
                for (String attrToDelete : getAttributesToDelete()) {
                    entry.removeAttribute(attrToDelete);
                }
                
                
                
                if (getAttributesToDeleteStartsWith() != null) {
                    Collection<Attribute> attributes = entry.getAttributes();
                    
                for (String attrToDeleteStartsWith : getAttributesToDeleteStartsWith()) {
                        List<Attribute> toDelete = attributes.stream().filter(attr -> attr.getBaseName().
                                startsWith(attrToDeleteStartsWith)).collect(Collectors.toList());
                        toDelete.forEach(attr -> entry.removeAttribute(attr.getBaseName()));
                }
            }
                
                
                
                return entry;

	}
	
}
