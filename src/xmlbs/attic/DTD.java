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

import java.util.*;
import java.io.*;

/**
 * Simple document definition object.
 */
public class DTD
{
    Map elementMap = new HashMap();
    Set emptySet = new HashSet();

    final static int HTML = 0;

    public DTD (int type)
    throws IOException
    {
	if (type == HTML)
	{
	    ClassLoader cl = getClass().getClassLoader();
	    InputStream in = cl.getResourceAsStream("xmlbs/HTML.properties");
	    Properties propMap = new Properties();
	    propMap.load(in);
	    in.close();

	    fromProperties(propMap);
	}
    }

    public DTD (Properties prop)
    {
	fromProperties(prop);
    }

    Map setMap = new HashMap();
    private void fromProperties (Properties propMap)
    {
	// collect sets
	{
	    Iterator it = propMap.keySet().iterator();
	    while (it.hasNext())
	    {
		String tag = (String) it.next();
		String val = (String) propMap.get(tag);

		if (tag.startsWith("_"))
		{
		    Set l = new HashSet();
		    StringTokenizer st = new StringTokenizer(val);
		    while (st.hasMoreTokens()) l.add(st.nextToken());
		    setMap.put(tag, l);
		}
	    }
	}

	// collect decendant lists
	{
	    Iterator it = propMap.keySet().iterator();
	    while (it.hasNext())
	    {
		String tag = (String) it.next();
		String val = (String) propMap.get(tag);

		if (! tag.startsWith("_"))
		{
		    Set l = new HashSet();
		    StringTokenizer st = new StringTokenizer(val);
		    while (st.hasMoreTokens())
		    {
			String t = st.nextToken();
			if (t.startsWith("_"))
			{
			    l.addAll(resolveSet((Set) setMap.get(t)));
			}
			else if (t.equals("!EMPTY"))
			{
			    emptySet.add(tag);
			}
			else
			{
			    l.add(t);
			}
		    }
		    elementMap.put(tag, l);
		}
	    }
	}
    }
    private Set resolveSet (Set in)
    {
	Set out = new HashSet();
	Iterator it = in.iterator();
	while (it.hasNext())
	{
	    String t = (String) it.next();
	    if (t.startsWith("_"))
	    {
		out.addAll(resolveSet((Set) setMap.get(t)));
	    }
	    else
	    {
		out.add(t);
	    }
	}
	return out;
    }

    /**
     * Determine of given parent may include given child.
     * @param parent tag
     * @param child tag
     * @return true if child may be a child of parent
     */
    public boolean canInclude (TagToken parent, TagToken child)
    {
	Set set = (Set) elementMap.get(parent.getName().toLowerCase());
	return set.contains(child.getName().toLowerCase());
    }

    /**
     * Determine tag is known.
     * @param tag
     * @return true if tag is included in definition
     */
    public boolean isKnownTag (TagToken tag)
    {
	return elementMap.containsKey(tag.getName().toLowerCase());
    }

    /**
     * Determine tag doesn't allow nested text or tags
     * @param tag
     * @return true if tag may not have offspring
     */
    public boolean isEmptyTag (TagToken tag)
    {
	return emptySet.contains(tag.getName().toLowerCase());
    }
}
