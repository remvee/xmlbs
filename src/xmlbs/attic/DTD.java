package xmlbs;

import java.util.*;
import java.io.*;

class DTD
{
    Map elementMap = new HashMap();

    final static int HTML = 0;

    public DTD (int type)
    throws Exception
    {
	if (type == HTML)
	{
	    ClassLoader cl = getClass().getClassLoader();
	    InputStream in = cl.getResourceAsStream("xmlbs/HTML.properties");
	    Properties propMap = new Properties();
	    propMap.load(in);

	    Iterator it = propMap.keySet().iterator();
	    while (it.hasNext())
	    {
		String tag = (String) it.next();
		String val = (String) propMap.get(tag);

		Set l = new HashSet();
		StringTokenizer st = new StringTokenizer(val);
		while (st.hasMoreTokens()) l.add(st.nextToken());
		elementMap.put(tag, l);
	    }
	    in.close();
	}
    }
    public Set decendantSet (String name)
    {
	return (Set) elementMap.get(name);
    }
}
