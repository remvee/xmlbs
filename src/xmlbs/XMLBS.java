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
 * Patchup malformed XML, XML bodyshop.  This class tries to fix
 * the given malformed XML file by:
 * <UL>
 *   <LI>
 *     cleaning up tags; ensure attributes are quoted
 *     properly
 *   </LI>
 *   <LI>
 *     fixing XML escaping in text; detect references and escape
 *     &gt;, &lt;, &quote; and &apos;
 *   </LI>
 *   <LI>
 *     closing all unclosed tags
 *   </LI>
 *   <LI>
 *     fixing tag overlap
 *   </LI>
 * </UL>
 * Useful when, for example, converting HTML to XHTML.
 *
 *
 * @author R.W. van 't Veer
 * @version $Revision: 1.22 $
 */
public class XMLBS
{
    InputStream in;
    List nodes;

    public XMLBS (DTD dtd, InputStream in)
    throws IOException
    {
	this.in = new BufferedInputStream(in);

	List tokens = tokenize();
	// TODO: remove overclosed tags to prevented early close
	nodes = createTree(dtd, tokens);
    }
    public XMLBS (DTD dtd, File f)
    throws IOException
    {
	this(dtd, new FileInputStream(f));
    }

    private List tokenize ()
    throws IOException
    {
	List v = new Vector();
	StringBuffer b = new StringBuffer();
	int c;
	while ((c = in.read()) != -1)
	{
	    switch (c)
	    {
		case '<':
		    Tag t = null;
		    try
		    {
			t = new Tag(in);
		    }
		    catch (Exception ex) { /* ignore */ }

		    if (t != null)
		    {
			String s = b.toString();
			if (s.length() > 0)
			{
			    v.add(new Text(s));
			    b = new StringBuffer();
			}
			v.add(t);
		    }
		    else
		    {
			b.append((char) c);
		    }

		    break;
		default:
		    b.append((char) c);
	    }
	}

	String s = b.toString();
	if (s.length() > 0)
	{
	    v.add(new Text(s));
	}
	return v;
    }

    private List createTree (DTD dtd, List tokens)
    {
	List children = new Vector();
	for (int i = 0; i < tokens.size(); i++)
	{
	    Object o = tokens.get(i);
	    if (o instanceof Tag)
	    {
		Tag t = (Tag) o;
		if (t.isOpenTag() || t.isEmptyTag())
		{
		    if (! dtd.isKnownTag(t)) continue;

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
		    }
		}
	    }
	    else if (o instanceof Text)
	    {
		children.add(o);
	    }
	}
	return children;
    }

    public void write (OutputStream out)
    {
	PrintWriter pw = new PrintWriter(out);
	Iterator it = nodes.iterator();
	while (it.hasNext()) pw.print(""+it.next());
	pw.println();
	pw.flush();
	pw.close();
    }

    public static void main (String[] args)
    throws Exception
    {
	XMLBS bs = new XMLBS(new DTD(DTD.HTML), new File(args[0]));
	bs.write(System.out);
    }
}
