package xmlbs;

import java.io.*;
import java.util.*;

import org.apache.regexp.*;

/**
 * Patchup malformed XML, XML bodyshop.  This class tries to fix
 * the given malformed XML file by:
 * <UL>
 *   <LI>
 *     leaning up tags; ensure attributes are quoted
 *     properly
 *   </LI>
 *   <LI>
 *     fixing XML escaping in text; detect references and escape
 *     &gt;, &lt;, &quote; and &apos;
 *   </LI>
 *   <LI>
 *     closing all unclosed tags; TODO
 *   </LI>
 *   <LI>
 *     fixing tag overlap; TODO
 *   </LI>
 * </UL>
 * Useful when, for example, converting HTML to XHTML.
 *
 * @author R.W. van 't Veer
 * @version $Revision: 1.5 $
 */
public class XMLBS
{
    InputStream in;

    public XMLBS (InputStream in)
    {
	this.in = new BufferedInputStream(in);
    }
    public XMLBS (File f)
    throws IOException
    {
	this(new FileInputStream(f));
    }

    public List tokenize ()
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
	return v;
    }

    public static void main (String[] args)
    throws Exception
    {
	// fix unclosed tags
	/*
	    in:  <A> bla <A> die <A> bla
	    out: <A> bla </A><A> die </A><A> bla </A>

	    in:  <A> bla <B> die <A> bla
	    out: <A> bla <B> die </A><A> bla </A></B>
	*/

	// fix overlap
	/*
	    in:  <A> bla <B> die </A> bla </B>
	    out: <A> bla <B> die </B></A><B> bla </B>
	*/

	XMLBS bs = new XMLBS(new File(args[0]));
	List tokens = bs.tokenize();

	// count open and close tags
	Map openMap = new HashMap();
	Map closeMap = new HashMap();
	for (int i = 0; i < tokens.size(); i++)
	{
	    Object o = tokens.get(i);
	    if (o instanceof Tag)
	    {
		Tag t = (Tag) o;
		if (t.isOpenTag())
		{
		    String tn = t.getName();
		    Integer n = (Integer) openMap.get(tn);
		    openMap.put(tn, n == null
			    ? new Integer(1)
			    : new Integer(n.intValue()+1)
			);
		}
		else if (t.isCloseTag())
		{
		    String tn = t.getName();
		    Integer n = (Integer) closeMap.get(tn);
		    closeMap.put(tn, n == null
			    ? new Integer(1)
			    : new Integer(n.intValue()+1)
			);
		}
	    }
	}

	// fix unclosed tags
	{
	    for (int i = 0; i < tokens.size(); i++)
	    {
		Object o = tokens.get(i);
		if (o instanceof Tag)
		{
		    Tag tag = (Tag) o;
		    if (tag.isOpenTag())
		    {
			Tag openTag = tag;
			Tag closeTag = new Tag(tag.getName(), null, Tag.CLOSE);
			List l = tokens.subList(i+1, tokens.size());
			// locate close tag
			int close = l.indexOf(closeTag);
			// locate next open
			int open = l.indexOf(openTag);

			if (close == -1) // tag not closed
			{
			    if (open != -1)
			    {
				tokens.add(open+i+1, closeTag);
			    }
			    else
			    {
				tokens.add(closeTag);
			    }
			}
			else if (open != -1 && open < close) // nesting detected
			{
			    // TODO allow nesting for certain tags
			    tokens.add(open+i+1, closeTag);
			}
		    }
		}
	    }
	}

	// remove redundant close tags

	// fix overlap

	// dump result
	{
	    Iterator it = tokens.iterator();
	    int indent = 0;
	    while (it.hasNext())
	    {
		Object o = it.next();

		if (o instanceof Tag && ((Tag)o).isCloseTag()) indent -= 4;

		for (int i = 0; i < indent; i++) System.out.print(" ");
		System.out.println((""+o).trim());

		if (o instanceof Tag && ((Tag)o).isOpenTag()) indent += 4;
	    }
	}
    }
}
