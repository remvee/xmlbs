package xmlbs;

import java.io.*;
import java.util.*;

import org.apache.regexp.*;

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
    public Tag (InputStream in)
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

    public Tag (String tagName, Map attrs, int type)
    {
	this.tagName = tagName;
	this.attrs = attrs;
	this.type = type;
    }

    public String getName () { return tagName; }
    public boolean isOpenTag () { return type == OPEN; }
    public boolean isCloseTag () { return type == CLOSE; }
    public boolean isEmptyTag () { return type == EMPTY; }

    public Tag closeTag () { return new Tag(getName(), null, Tag.CLOSE); }
    public Tag emptyTag () { return new Tag(getName(), null, Tag.EMPTY); }

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

	if (attrs != null)
	{
	    Iterator it = attrs.keySet().iterator();
	    while (it.hasNext())
	    {
		String attr = (String) it.next();
		String val = (String) attrs.get(attr);

		sb.append(' ');
		sb.append(attr);
		sb.append('=');
		sb.append('"');
		sb.append(Text.fixText(val));
		sb.append('"');
	    }
	}

	if (type == EMPTY) sb.append('/');
	sb.append('>');

	return sb.toString();
    }

    public boolean equals (Object that)
    {
	if (that instanceof Tag)
	{
	    Tag t = (Tag) that;
	    if (t.type == type && t.getName().equals(tagName))
	    {
		return true;
	    }
	}
	return false;
    }

    public int hashCode ()
    {
	if (tagName != null) return tagName.hashCode();
	return 0;
    }
}
