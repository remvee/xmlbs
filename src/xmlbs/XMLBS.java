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
 * @version $Revision: 1.9 $
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

    /**
     * Close unclosed tags in the given tokenlist.
     * TODO allow nested tags
     * @param tokens list of tokens
     */
    void fixUnclosedTags (List tokens)
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
		    Tag closeTag = tag.closeTag();
		    List l = tokens.subList(i+1, tokens.size());

		    // locate close tag
		    int close = findCloseTag(openTag, l);
		    if (close == -1)
		    {
			close = l.indexOf(closeTag);
			// locate next open
			int open = l.indexOf(openTag);

			if (close == -1) // tag not closed
			{
			    if (open != -1)
			    {
				// TODO allow nesting for certain tags
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
    }

    static int fct_ = 0;
    private final static int findCloseTag (Tag tag, List tokens)
    {
	int fct = fct_++;
	String thiz = "findCloseTag"+fct+"("+tag+","+tokens+")";
System.out.println(thiz);
	int depth = 0, i = 0;
	Tag openTag = tag;
	Tag closeTag = tag.closeTag();
	for (; i < tokens.size(); i++)
	{
	    Object o = tokens.get(i);
	    if (o instanceof Tag)
	    {
		Tag t = (Tag) o;
		if (t.equals(closeTag))
		{
System.out.println(thiz+t+"@"+i+".depth="+depth);
		    if (depth == 0) break;
		    depth--;
		}
		else if (t.equals(openTag))
		{
System.out.println(thiz+t+"@"+i+".depth="+depth);
		    depth++;
		}
		else if (t.isOpenTag())
		{
System.out.println(thiz+t+"@"+i);
		    List list = tokens.subList(i+1, tokens.size());
		    int j = findCloseTag(t, list);
		    if (j != -1) i += j + 1;
		}
	    }
	}

	int r = i < tokens.size() ? i : -1;
System.out.println(thiz+"="+r);
	return r;
    }

    /**
     * Try to parse document and fix overlaps.  Tags in the list
     * will be replaced by {@link Block} objects.
     * @param tokens list of tokens
     */
    void fixOverlap (List tokens)
    {
        Vector result = new Vector();
        ListIterator it = tokens.listIterator();
        while (it.hasNext())
        {
            Object o = it.next();
            if (o instanceof Tag)
            {
                Tag t = (Tag) o;
                if (t.isOpenTag() || t.isEmptyTag())
                {
                    result.add(new Block(t, it));
                }
                else
                {
		    // discard close/other tag
		}
	    }
	    else
	    {
		result.add(o);
	    }
	}

	// replace tokens by result
	tokens.clear();
	tokens.addAll(result);
    }

    public static void main (String[] args)
    throws Exception
    {
	XMLBS bs = new XMLBS(new File(args[0]));
	List tokens = bs.tokenize();
System.err.println("after tokenze: "+tokens);

	// fix tags not closed
	bs.fixUnclosedTags(tokens);
System.err.println("after fixUnclosedTags: "+tokens);

	// fix overlap
	bs.fixOverlap(tokens);
System.err.println("after fixOverlap: "+tokens);

	// dump result
	{
	    Iterator it = tokens.iterator();
	    while (it.hasNext()) System.out.println(""+it.next());
	}
    }

    void openCloseStats (List list)
    {
	// count open and close tags
	Map openMap = new HashMap();
	Map closeMap = new HashMap();
	for (int i = 0; i < list.size(); i++)
	{
	    Object o = list.get(i);
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
	System.err.println("open/close statistics");
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
		    System.err.println("  "+tn+": not closed: "+(open-close));
		    unclosedSet.add(tn);
		}
		else if (open < close)
		{
		    System.err.println("  "+tn+": over closed: "+(close-open));
		}
	    }
	}
    }
}

class Block
{
    List result = new Vector();
    Set overlap = new HashSet();

    Block (Tag openTag, ListIterator it)
    {
	result.add(openTag);
	if (openTag.isEmptyTag()) return;

	Tag closeTag = openTag.closeTag();
	while (it.hasNext())
	{
	    Object o = it.next();
	    if (o instanceof Tag)
	    {
		Tag t = (Tag) o;
		if (t.isOpenTag()) // TODO handle empty tags
		{
		    int pos = it.nextIndex() - 1;
		    Block child = new Block(t, it);
		    if (child.getOverlap().contains(closeTag))
		    {
			result.add(closeTag);
			// rewind iterator to beginning of child block
			while (pos < it.nextIndex()) it.previous();
			return;
		    }
		    result.add(child);
		}
		else if (t.equals(closeTag))
		{
		    result.add(t);
		    return;
		}
		else if (t.isCloseTag())
		{
		    overlap.add(t);
		}
		else
		{
		    result.add(t);
		}
	    }
	    else
	    {
		result.add(o);
	    }
	}

	// missing close tag??
	result.add(closeTag);
    }

    public Set getOverlap ()
    {
	Set overlap = new HashSet();
	overlap.addAll(this.overlap);
	Iterator it = result.iterator();
	while (it.hasNext())
	{
	    Object o = it.next();
	    if (o instanceof Block)
	    {
		overlap.addAll(((Block)o).getOverlap());
	    }
	}
	return overlap;
    }

    public String toString ()
    {
	StringBuffer sb = new StringBuffer();
	Iterator it = result.iterator();
	while (it.hasNext())
	{
	    Object o = it.next();
	    sb.append(o.toString());
	}
	return sb.toString();
    }
}
