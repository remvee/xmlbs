package xmlbs;

import java.io.*;
import java.util.*;

import org.apache.regexp.*;

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
 * @author R.W. van 't Veer
 * @version $Revision: 1.14 $
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

    public void write (PrintWriter out)
    {
	Iterator it = nodes.iterator();
	while (it.hasNext()) out.print(""+it.next());
	out.println();
	out.flush();
    }

    public static void main (String[] args)
    throws Exception
    {
	XMLBS bs = new XMLBS(new DTD(DTD.HTML), new File(args[0]));
	bs.write(new PrintWriter(System.out));
    }
}
