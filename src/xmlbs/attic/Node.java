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

class Node
{
    DTD dtd;
    Tag openTag;
    List tokens;
    int startPos;

    int endPos = -1; // exclusive
    List children = new Vector();
    Tag closedBy = null;

    public Node (DTD dtd, Tag openTag, List tokens, int startPos)
    {
	this.dtd = dtd;
	this.openTag = openTag;
	this.tokens = tokens;
	this.startPos = startPos;

	Tag closeTag = openTag.closeTag();
	int i = startPos + 1;
	for (; i < tokens.size(); i++)
	{
	    Object o = tokens.get(i);
	    if (o instanceof Tag)
	    {
		Tag t = (Tag) o;
		if (t.isCloseTag())
		{
		    if (! dtd.isKnownTag(t))
		    {
			continue;
		    }

		    if (! t.equals(closeTag))
		    {
			closedBy = t;
		    }

		    endPos = i;
		    break;
		}
		else if (t.isOpenTag() || t.isEmptyTag())
		{
		    if (! dtd.isKnownTag(t))
		    {
			continue;
		    }

		    if (! dtd.canInclude(openTag, t))
		    {
			endPos = i-1;
			break;
		    }

		    if (dtd.isEmptyTag(t) || t.isEmptyTag())
		    {
			children.add(t.emptyTag());
		    }
		    else
		    {
			Node n = new Node(dtd, t, tokens, i);
			children.add(n);
			int j = n.getEndPosition();
			if (j != -1) i = j;
			if (n.closedByTag(closeTag))
			{
			    endPos = i;
			    break;
			}
		    }
		}
	    }
	    else if (o instanceof Text)
	    {
		children.add(o);
	    }
	}
	
	if (endPos == -1) endPos = i;
    }

    public boolean closedByTag (Tag t)
    {
	if (closedBy != null && closedBy.equals(t))
	{
	    closedBy = null;
	    return true;
	}
	Iterator it = children.iterator();
	while (it.hasNext())
	{
	    Object o = it.next();
	    if (o instanceof Node)
	    {
		Node n = (Node) o;
		if (n.closedByTag(t)) return true;
	    }
	}
	return false;
    }

    public int getEndPosition () { return endPos; }

    public String toString ()
    {
	StringBuffer sb = new StringBuffer();
	if (false)
	{
	    sb.append(""+openTag+"@"+startPos);
	    if (endPos != -1)
	    {
		sb.append("(");
		Iterator it = children.iterator();
		while (it.hasNext())
		{
		    sb.append(""+it.next());
		    if (it.hasNext()) sb.append(", ");
		}
		sb.append(")");
	    }
	    else
	    {
		sb.append("..");
	    }
	}
	else
	{
	    sb.append(openTag);
	    Iterator it = children.iterator();
	    while (it.hasNext()) sb.append(it.next().toString());
	    sb.append(openTag.closeTag());
	}
	return sb.toString();
    }
}
