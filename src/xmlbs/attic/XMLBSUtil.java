package xmlbs;

import java.io.*;

public class XMLBSUtil
{
    public static String process (DTD dtd, String in)
    throws Exception
    {
	ByteArrayInputStream _in = new ByteArrayInputStream(in.getBytes());
	ByteArrayOutputStream _out = new ByteArrayOutputStream();
	(new xmlbs.XMLBS(dtd, _in)).write(_out);
	return _out.toString();
    }
}
