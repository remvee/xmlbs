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
 * Document structure object for testing.
 *
 * @author R.W. van 't Veer
 * @version $Revision: 1.4 $
 */
class TestDocumentStructure implements DocumentStructure {
    static List tagNames = new Vector();
    static {
	tagNames.add("table");
	tagNames.add("tr");
	tagNames.add("td");
	tagNames.add("th");
	tagNames.add("i");
	tagNames.add("b");
	tagNames.add("hr");
    }
    static Map tagAttributes = new HashMap();
    static {
	List l;

	l = new Vector();
	l.add("border");
	l.add("width");
	l.add("height");
	tagAttributes.put("table", l);

	l = new Vector();
	l.add("width");
	l.add("height");
	tagAttributes.put("tr", l);

	l = new Vector();
	l.add("width");
	l.add("height");
	tagAttributes.put("td", l);

	l = new Vector();
	l.add("width");
	l.add("height");
	tagAttributes.put("th", l);

	l = new Vector();
	tagAttributes.put("i", l);
	tagAttributes.put("b", l);
	tagAttributes.put("hr", l);
    }
    static Map tagHierarchy = new HashMap();
    static {
	List l;

	l = new Vector();
	l.add("table");
	tagHierarchy.put(null, l);

	l = new Vector();
	l.add("tr");
	tagHierarchy.put("table", l);

	l = new Vector();
	l.add("td");
	l.add("th");
	tagHierarchy.put("tr", l);

	l = new Vector();
	l.add("table");
	l.add("i");
	l.add("b");
	l.add("hr");
	l.add("#TEXT");
	tagHierarchy.put("td", l);

	l = new Vector();
	l.add("i");
	l.add("b");
	l.add("hr");
	l.add("#TEXT");
	tagHierarchy.put("th", l);

	l = new Vector();
	l.add("b");
	l.add("hr");
	l.add("#TEXT");
	tagHierarchy.put("i", l);

	l = new Vector();
	l.add("i");
	l.add("hr");
	l.add("#TEXT");
	tagHierarchy.put("b", l);

	l = new Vector();
	tagHierarchy.put("hr", l);
    }

    /** ignore case flag */
    private boolean icase = false;

    /**
     * Set ignore case flag for matching tagnames, attributes and
     * entities.
     * @param icase true where character case should be ignored
     */
    public void setIgnoreCase (boolean icase) {
	this.icase = icase;
    }

    /**
     * Get ignore case flag.
     * @return true where character case should be ignored
     */
    public boolean getIgnoreCase () {
	return icase;
    }

    /**
     * Get tag name.
     * Ignoring character case if needed.
     * @param name tag name to lookup
     * @return tag name in proper case
     */
    public String getTagName (String name) {
	if (!icase) {
	    return name;
	}

	String in = name.toLowerCase();
	for (Iterator it = tagNames.iterator(); it.hasNext();) {
	    String n = (String) it.next();
	    if (n.toLowerCase().equals(in)) {
		return n;
	    }
	}
	return name;
    }

    /**
     * Get attribute name.
     * Ignoring character case if needed.
     * @param name tag name to lookup
     * @param attr attribute name to lookup
     * @return attribute name in proper case
     */
    public String getTagAttribute (String name, String attr) {
	if (!icase) {
	    return attr;
	}
	String in = attr.toLowerCase();
	List names = (List) tagAttributes.get(name);
	for (Iterator it = names.iterator(); it.hasNext();) {
	    String n = (String) it.next();
	    if (n.toLowerCase().equals(in)) {
		return n;
	    }
	}
	return attr;
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
}
