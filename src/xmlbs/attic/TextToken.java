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

import org.apache.regexp.*;

/**
 * Class to wrap blocks of text.  Entity refs are preserved when
 * possible and new are introduced for &lt;, &gt; and &amp;.
 */
class TextToken implements Token
{
    String txt;

    public TextToken (String txt)
    {
	this.txt = fixText(txt);
    }

    public String toString ()
    {
	return txt;
    }

    static RE entityRefRe;
    static RE charRefRe;

    static
    {
	try
	{
	    entityRefRe = new RE("^&[a-zA-Z_:][a-zA-Z0-9._:-]*;");
	    charRefRe = new RE("^&#([0-9]+;)|(x[0-9a-fA-F]+);");
	}
	catch (RESyntaxException ex)
	{
	    throw new RuntimeException(ex.toString());
	}
    }

    /**
     * Xml escape text while preserving existing entities.
     */
    public final static String fixText (String in)
    {
	StringBuffer out = new StringBuffer();
	// TODO speedup by using char array insteadof string
	for (int i = 0, l = in.length(); i < l; i++)
	{
	    char c = in.charAt(i);
	    switch (c)
	    {
		case '<':
		    out.append("&lt;");
		    break;
		case '>':
		    out.append("&gt;");
		    break;
		case '"':
		    out.append("&quot;"); // TODO is this a global entity?
		    break;
		case '\'':
		    out.append("&apos;"); // TODO is this a global entity?
		    break;
		case '&':
		    int j = in.indexOf(';', i);
		    if (j != -1)
		    {
			String s = in.substring(i);
			if (entityRefRe.match(s) || charRefRe.match(s))
			{
			    // TODO test if known entity
			    out.append(in.substring(i, j+1));
			    i = j;
			}
			else
			{
			    out.append("&amp;");
			}
		    }
		    else
		    {
			out.append("&amp;");
		    }
		    break;
		default:
		    out.append(c);
	    }
	}

	return out.toString();
    }
}
