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
 * @version $Revision: 1.4 $
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

	// open/close statistics
	System.out.println("open/close statistics");
	Set unclosedSet = new HashSet();
	{
	    Set tags = new HashSet();
	    tags.addAll(openMap.keySet());
	    tags.addAll(closeMap.keySet());
	    Vector v = new Vector(tags);
	    Collections.sort(v);
	    Iterator it = v.iterator();
	    while (it.hasNext())
	    {
		String tn = (String) it.next();
		int open, close;
		Integer o;

		o = (Integer) openMap.get(tn);
		if (o == null) open = 0;
		else open = o.intValue();

		o = (Integer) closeMap.get(tn);
		if (o == null) close = 0;
		else close = o.intValue();

		if (open > close)
		{
		    System.out.println("  "+tn+": not closed: "+(open-close));
		    unclosedSet.add(tn);
		}
		else if (open < close)
		{
		    System.out.println("  "+tn+": over closed: "+(close-open));
		}
	    }
	}

	// close unclosed tags
	{
	    for (int i = 0; i < tokens.size(); i++)
	    {
		Object o = tokens.get(i);
		if (o instanceof Tag)
		{
		    Tag t = (Tag) o;
		    if (t.isOpenTag() && unclosedSet.contains(t.getName()))
		    {
			System.out.println("should close: "+t);
			List l = tokens.subList(i+1, tokens.size());
			int j = l.indexOf(t);
			if (j != -1)
			{
			    tokens.add(j+i+1, new Tag(t.getName(), null, Tag.CLOSE));
			}
			else
			{
			    tokens.add(new Tag(t.getName(), null, Tag.CLOSE));
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
	    while (it.hasNext()) System.out.print(""+it.next());
	}
    }
}
