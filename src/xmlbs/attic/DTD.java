package xmlbs;

import java.util.*;
import java.io.*;

class DTD
{
    Map elementMap = new HashMap();
    Set emptySet = new HashSet();

    final static int HTML = 0;

    public DTD (int type)
    throws IOException
    {
	if (type == HTML)
	{
	    ClassLoader cl = getClass().getClassLoader();
	    InputStream in = cl.getResourceAsStream("xmlbs/HTML.properties");
	    Properties propMap = new Properties();
	    propMap.load(in);
	    in.close();

	    fromProperties(propMap);
	}
    }

    Map setMap = new HashMap();
    private void fromProperties (Properties propMap)
    {
	// collect sets
	{
	    Iterator it = propMap.keySet().iterator();
	    while (it.hasNext())
	    {
		String tag = (String) it.next();
		String val = (String) propMap.get(tag);

		if (tag.startsWith("_"))
		{
		    Set l = new HashSet();
		    StringTokenizer st = new StringTokenizer(val);
		    while (st.hasMoreTokens()) l.add(st.nextToken());
		    setMap.put(tag, l);
		}
	    }
	}

	// collect decendant lists
	{
	    Iterator it = propMap.keySet().iterator();
	    while (it.hasNext())
	    {
		String tag = (String) it.next();
		String val = (String) propMap.get(tag);

		if (! tag.startsWith("_"))
		{
		    Set l = new HashSet();
		    StringTokenizer st = new StringTokenizer(val);
		    while (st.hasMoreTokens())
		    {
			String t = st.nextToken();
			if (t.startsWith("_"))
			{
			    l.addAll(resolveSet((Set) setMap.get(t)));
			}
			else if (t.equals("!EMPTY"))
			{
			    emptySet.add(tag);
			}
			else
			{
			    l.add(t);
			}
		    }
		    elementMap.put(tag, l);
		}
	    }
	}
    }
    private Set resolveSet (Set in)
    {
	Set out = new HashSet();
	Iterator it = in.iterator();
	while (it.hasNext())
	{
	    String t = (String) it.next();
	    if (t.startsWith("_"))
	    {
		out.addAll(resolveSet((Set) setMap.get(t)));
	    }
	    else
	    {
		out.add(t);
	    }
	}
	return out;
    }

    public Set decendantSet (String name)
    {
	return (Set) elementMap.get(name.toLowerCase());
    }

    public boolean isKnownTag (String name)
    {
	return elementMap.containsKey(name);
    }

    public boolean isEmptyTag (String name)
    {
	return emptySet.contains(name);
    }
}
