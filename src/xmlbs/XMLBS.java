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
 * @version $Revision: 1.1 $
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
			    out.append(s.substring(0, j));
			    i = j+1;
			}
			else
			{
			    out.append('&');
			}
		    }
		    else
		    {
			out.append('&');
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

class Tag
{
    String raw;
    String tagName;
    Map attrs = new HashMap();
    int type;

    final static int OPEN = 0;
    final static int CLOSE = 1;
    final static int EMPTY = 2;
    final static int SPECIAL = 3;
    final static int DECLARATION = 4;

    final static int BUFFER_SIZE = 64*1024;

    static RE closeRe;
    static RE emptyRe;
    static RE specialRe;
    static RE declRe;

    static RE nameRe;

    static RE attRe;
    static RE valRe1;
    static RE valRe2;
    static RE valRe3;

    static
    {
	try
	{
	    closeRe = new RE("^\\s*/");
	    emptyRe = new RE("/\\s*$");
	    specialRe = new RE("^\\s*\\?");
	    declRe = new RE("^\\s*\\!");

	    nameRe = new RE("([a-zA-Z0-9:]+)\\s*");

	    attRe = new RE("\\s([a-zA-Z_:][a-zA-Z0-9]*)\\s*=");
	    valRe1 = new RE("^\\s*=\\s*'([^']*)'");
	    valRe2 = new RE("^\\s*=\\s*\"([^\"]*)\"");
	    valRe3 = new RE("^\\s*=\\s*([^\\s]*)");
	}
	catch (RESyntaxException ex)
	{
	    throw new RuntimeException(ex.toString());
	}
    }


    /**
     * The &lt; character was already read.
     */
    Tag (InputStream in)
    throws IOException, Exception
    {
	// try to read full tag
	StringBuffer b = new StringBuffer();
	int c;
	in.mark(BUFFER_SIZE);
	while ((c = in.read()) != -1)
	{
	    if (c == '<') // illegal in attribute value TODO allow it?
	    {
		in.reset();
		throw new Exception();
	    }
	    // TODO allow > to be in attribute value
	    if (c == '>') break;
	    b.append((char) c);
	}
	// TODO handle if c == -1
	raw = b.toString();

	// determine tag type
	{
	    if (closeRe.match(raw)) type = CLOSE;
	    else if (emptyRe.match(raw)) type = EMPTY;
	    else if (specialRe.match(raw)) type = SPECIAL; // TODO handle it!!
	    else if (declRe.match(raw)) type = DECLARATION; // TODO handle it!!
	    else type = OPEN;
	}

	// determine tag name
	{
	    nameRe.match(raw);
	    tagName = nameRe.getParen(1);
	}

	// collect attributes
	{

	    int pos = 0;
	    while (attRe.match(raw, pos))
	    {
		String attr = attRe.getParen(1);
		pos = attRe.getParenEnd(1);
		String valStr = raw.substring(pos);

		String val = null;
		RE valRe = null;
		if (valRe1.match(valStr)) valRe = valRe1;
		else if (valRe2.match(valStr)) valRe = valRe2;
		else if (valRe3.match(valStr)) valRe = valRe3;
		else break;
		val = valRe.getParen(1); // TODO handle entity and char refs
		pos += valRe.getParenEnd(1);

		attrs.put(attr, val);
	    }
	}
    }

    public String toString ()
    {
	StringBuffer sb = new StringBuffer();

	sb.append('<');

	if (type == CLOSE)
	{
	    sb.append('/');
	    sb.append(tagName);
	    sb.append('>');

	    return sb.toString();
	}
	else if (type == SPECIAL || type == DECLARATION) // TODO
	{
	    sb.append(raw);
	    sb.append('>');

	    return sb.toString();
	}

	sb.append(tagName);

	Iterator it = attrs.keySet().iterator();
	while (it.hasNext())
	{
	    String attr = (String) it.next();
	    String val = (String) attrs.get(attr);

	    sb.append(' ');
	    sb.append(attr);
	    sb.append('=');
	    sb.append('"');
	    sb.append(XMLBS.fixText(val));
	    sb.append('"');
	}

	if (type == EMPTY) sb.append('/');
	sb.append('>');

	return sb.toString();
    }
}

class Text
{
    String txt;

    Text (String txt)
    {
	this.txt = XMLBS.fixText(txt);
    }

    public String toString ()
    {
	return txt;
    }
}
