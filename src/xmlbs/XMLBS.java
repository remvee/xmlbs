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
 * @version $Revision: 1.13 $
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

    public List createTree (DTD dtd, List tokens)
    {
	List children = new Vector();
	for (int i = 0; i < tokens.size(); i++)
	{
	    Object o = tokens.get(i);
	    if (o instanceof Tag)
	    {
		Tag t = (Tag) o;
		if (t.isOpenTag())
		{
		    Node n = new Node(dtd, t, tokens, i);
		    children.add(n);

		    int j = n.getEndPosition();
		    if (j != -1) i = j-1;
		}
	    }
	    else if (o instanceof Text)
	    {
		children.add(o);
	    }
	}
	return children;
    }


    public static void main (String[] args)
    throws Exception
    {
	XMLBS bs = new XMLBS(new File(args[0]));
	List tokens = bs.tokenize();
System.err.println("tokens: "+tokens);

	List nodes = bs.createTree(new DTD(DTD.HTML), tokens);
System.err.println("nodes: "+nodes);
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
String zz = "Node"+openTag+startPos;

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
		    if (! t.equals(closeTag)) closedBy = t;
		    endPos = i;
		    break;
		}
		else if (t.isOpenTag())
		{
System.out.println(zz+">");
		    Set types = (Set) dtd.decendantSet(openTag.getName());
System.out.println(zz+"<"+types);
		    if (types != null && ! types.contains(t.getName()))
		    {
			endPos = i-1;
			break;
		    }
		    else if (types == null)
		    {
			endPos = i;
			break;
		    }

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
	    else if (o instanceof Text)
	    {
		children.add(o);
	    }
	}
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
