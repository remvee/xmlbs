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
 * @version $Revision: 1.2 $
 */
public class XMLBS
{
    InputStream in;

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
			v.add(new Text(b.toString()));
			v.add(t);
			b = new StringBuffer();
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

    public final static String fixText (String in)
    {
	StringBuffer out = new StringBuffer();
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
		    out.append("&quot;");
		    break;
		case '\'':
		    out.append("&apos;");
		    break;
		case '&':
		    int j = in.indexOf(';', i);
		    if (j != -1)
		    {
			String s = in.substring(i);
			if (entityRefRe.match(s) || charRefRe.match(s))
			{
			    out.append(in.substring(i, j+1));
			    i = j+1;
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

    public static void main (String[] args)
    throws Exception
    {
	XMLBS mxml = new XMLBS(new File(args[0]));
	System.out.println(mxml.tokenize());
	// fix unclosed tags
	/*
	    in:  <A> bla <A> die <A> bla
	    out: <A> bla </A><A> die </A><A> bla </A>

	    in:  <A> bla <B> die <A> bla
	    out: <A> bla </A><B> die </B><A> bla </A>

	    in:  <A> bla <B> die <A> bla </B>
	    out: <A> bla </A><B> die <A> bla </A></B>
	*/

	// fix overlap
	/*
	    in:  <A> bla <B> die </A> bla </B>
	    out: <A> bla <B> die </B></A><B> bla </B>
	*/
    }
}
