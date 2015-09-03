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

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldif.LDIFModifyChangeRecord;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author almu
 */
public class LDIFModifyGenerator {
    
    public static LDIFModifyChangeRecord getModify(Entry entry, ModificationType type) {
        List<Modification> mods = new ArrayList<>();
        
        for (Attribute a : entry.getAttributes()) {
            Modification mod = new Modification(type, a.getName(), a.getRawValues());
            mods.add(mod);
        }
        
        LDIFModifyChangeRecord mod = new LDIFModifyChangeRecord(entry.getDN(), mods);
        
        return mod;
    }
    
}
