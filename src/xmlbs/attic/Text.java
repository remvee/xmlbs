package xmlbs;

import org.apache.regexp.*;

class Text
{
    String txt;

    Text (String txt)
    {
	this.txt = fixText(txt);
    }

    public String toString ()
    {
	return txt;
    }

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

}
