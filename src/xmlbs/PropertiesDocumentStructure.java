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
 * Document structure configurable using a property file.  A
 * property key is a tag name, when it starts with a <tt>_</tt>
 * character a includable set or <tt>&#64;ROOT</tt> when it
 * denotes the document root.  Property values give a list of
 * tags which can be parents of the given key, when a value
 * starts with <tt>$</tt> it denotes a attribute name and a value
 * starting with <tt>_</tt> references an other property.
 * <p>
 * The following example has a <tt>table</tt> element as possible
 * root tag and a structure similar to tables in html:
 * <pre>
 * &#64;ROOT: table
 * table: tr $width $height
 * tr: td th
 * td: _cell
 * th: _cell
 * _cell: #TEXT table $colspan $rowspan 
 * </pre>
 *
 * @author R.W. van 't Veer
 * @version $Revision: 1.4 $
 */
public class PropertiesDocumentStructure implements DocumentStructure {
    /** set to keep tag names */
    private Set tagNames = new HashSet();
    /** map to keep tag attributes */
    private Map tagAttributes = new HashMap();
    /** map to keep lists of possible tag parents */
    private Map tagHierarchy = new HashMap();

    /**
     * @param prop properties map describing possible parent tags
     * and attributes
     */
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

    /**
     * @param prop properties to read from
     * @param key to read
     * @return fully dereferenced list
     */
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

    /**
     * @param tag a tag token
     * @return true if tag is known
     */
    public boolean isKnownTag (TagToken tag) {
	return tagNames.contains(tag.getName());
    }

    /**
     * @param tag retain known attributes in this tag
     */
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

    /**
     * @param parent top tag
     * @param child possible child token
     * @return true if parent can contain child
     */
    public boolean canContain (TagToken parent, Token child) {
	String parentName = parent == null ? "@ROOT" : parent.getName();

	List hier = (List) tagHierarchy.get(parentName);
	if (child instanceof TextToken) {
	    return hier.contains("#TEXT");
	} else if (child instanceof TagToken) {
	    TagToken childtag = (TagToken) child;
	    return hier.contains(childtag.getName());
	}
	return false;
    }

    /**
     * @return debug info
     */
    public String toString () {
	return "names=" + tagNames + "\n"
		+ "attributes=" + tagAttributes + "\n"
		+ "hierarchy=" + tagHierarchy;
    }
}
