/*
 * xmlbs
 *
 * Copyright (C) 2002  R.W. van 't Veer
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 */

package xmlbs;

import java.io.*;
import java.util.*;

/**
 * Document structure configurable using property files.
 *
 * @author R.W. van 't Veer
 * @version $Revision: 1.1 $
 */
public class PropertiesDocumentStructure implements DocumentStructure {
    private Set tagNames = new HashSet();
    private Map tagAttributes = new HashMap();
    private Map tagHierarchy = new HashMap();

    public PropertiesDocumentStructure (Properties prop) {
	// collect tag names
	{
	    tagNames.addAll(prop.keySet());
	    for (Iterator it = tagNames.iterator(); it.hasNext();) {
		String key = (String) it.next();
		if (key.startsWith("_")) {
		    it.remove();
		}
	    }
	}

	// create "master" map by resolving all includes
	Map master = new HashMap();
	for (Iterator it = tagNames.iterator(); it.hasNext();) {
	    String key = (String) it.next();
	    master.put(key, include(prop, key));
	}

	// create hierarchy map from master
	for (Iterator it = tagNames.iterator(); it.hasNext();) {
	    String key = (String) it.next();
	    List l = new Vector((List) master.get(key));
	    // remove attribute info
	    for (Iterator it0 = l.iterator(); it0.hasNext();) {
		String key0 = (String) it0.next();
		if (key0.startsWith("$")) {
		    it0.remove();
		}
	    }
	    tagHierarchy.put(key, l);
	}

	// create attribute map from master
	for (Iterator it = tagNames.iterator(); it.hasNext();) {
	    String key = (String) it.next();
	    List in = new Vector((List) master.get(key));
	    List l = new Vector();
	    // copy attribute only info to list
	    for (Iterator it0 = in.iterator(); it0.hasNext();) {
		String key0 = (String) it0.next();
		if (key0.startsWith("$")) {
		    l.add(key0.substring(1));
		}
	    }
	    tagAttributes.put(key, l);
	}
    }
    
    private List include(Properties prop, String key) {
	List l = new Vector();
	StringTokenizer st = new StringTokenizer(prop.getProperty(key));
	while (st.hasMoreTokens()) {
	    String v = st.nextToken();
	    if (v.startsWith("_")) {
		l.addAll(include(prop, v));
	    } else {
		l.add(v);
	    }
	}
	return l;
    }

    public boolean isKnownTag (TagToken tag) {
	return tagNames.contains(tag.getName());
    }

    public void retainKnownAttributes (TagToken tag) {
	List names = (List) tagAttributes.get(tag.getName());
	Iterator it = tag.getAttributes().entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry en = (Map.Entry) it.next();
	    if (!names.contains(en.getKey())) {
		it.remove();
	    }
	}
    }

    public boolean canContain (TagToken parent, Token child) {
	String parentName = parent == null ? null : parent.getName();

	List hier = (List) tagHierarchy.get(parentName);
	if (child instanceof TextToken) {
	    return hier.contains("#TEXT");
	} else if (child instanceof TagToken) {
	    TagToken childtag = (TagToken) child;
	    return hier.contains(childtag.getName());
	}
	return false;
    }

    public boolean isEndpoint (TagToken tag) {
	return ((List)tagHierarchy.get(tag.getName())).size() == 0;
    }

    public String toString () {
	return
	    "tagNames="+tagNames+"\n"+
	    "tagAttributes="+tagAttributes+"\n"+
	    "tagHierarchy="+tagHierarchy;
    }
}
