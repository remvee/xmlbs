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
 * @version $Revision: 1.3 $
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
