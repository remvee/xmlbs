package xmlbs.testing;

import java.io.*;
import java.util.*;
import junit.framework.*;

import xmlbs.*;

public class TokenizerTests extends TestCase
{
    public TokenizerTests (String name)
    {
        super(name);
    }

    public static void main (String[] args)
    {
        String[] par = new String[1];
        par[0] = TokenizerTests.class.getName();
        junit.swingui.TestRunner.main(par);
    }

    public static Test suite()
    {
        return new TestSuite(TokenizerTests.class);
    }

    public void testTag ()
    throws IOException
    {
	assertTrue("assert doesn't work", true);
    }

    public void testText ()
    throws IOException
    {
	{
	    String d = " ";
	    Tokenizer tokenizer = new Tokenizer(d);
	    List tokens = tokenizer.readAllTokens();

	    assertTrue("didn't read 1 token from '"+d+"' but "+tokens.size(),
		    tokens.size() == 1);
	    assertTrue("didn't read a text token from '"+d+"'",
		    tokens.get(0) instanceof TextToken);

	    TextToken tok = (TextToken) tokens.get(0);
	    String data = tok.getData();

	    assertTrue("didn't read ' ' from '"+d+"' but '"+data+"'",
		    data.equals(" "));
	}
	{
	    String d = "<";
	    Tokenizer tokenizer = new Tokenizer(d);
	    List tokens = tokenizer.readAllTokens();

	    assertTrue("didn't read 1 token from '"+d+"' but "+tokens.size(),
		    tokens.size() == 1);

	    TextToken tok = (TextToken) tokens.get(0);
	    String data = tok.getData();

	    assertTrue("didn't read '<' from '"+d+"' but '"+data+"'",
		    data.equals("<"));

	    assertTrue("didn't get '&lt;' from '"+d+"' but '"+tok+"'",
		    tok.toString().equals("&lt;"));
	}
	{
	    String d = "<blabla<>";
	    Tokenizer tokenizer = new Tokenizer(d);
	    List tokens = tokenizer.readAllTokens();

	    assertTrue("didn't read 1 token from '"+d+"' but "+tokens.size(),
		    tokens.size() == 1);

	    TextToken tok = (TextToken) tokens.get(0);
	    String data = tok.getData();

	    assertTrue("didn't read '<blabla<>' from '"+d+"' but '"+data+"'",
		    data.equals("<blabla<>"));

	    assertTrue("didn't get '&lt;blabla&lt;&gt;' from '"+d+"' but '"+tok+"'",
		    tok.toString().equals("&lt;blabla&lt;&gt;"));
	}
	{
	    String d = "<<blabla>>";
	    Tokenizer tokenizer = new Tokenizer(d);
	    List tokens = tokenizer.readAllTokens();

	    assertTrue("didn't read 3 token from '"+d+"' but "+tokens.size(),
		    tokens.size() == 3);

	    TextToken tok0 = (TextToken) tokens.get(0);
	    String data0 = tok0.getData();
	    TextToken tok1 = (TextToken) tokens.get(2);
	    String data1 = tok1.getData();

	    assertTrue("didn't read '<' from '"+d+"' but '"+data0+"'",
		    data0.equals("<"));
	    assertTrue("didn't read '>' from '"+d+"' but '"+data1+"'",
		    data1.equals(">"));
	}
	{
	    String d = ">'blabla\"<";
	    Tokenizer tokenizer = new Tokenizer(d);
	    List tokens = tokenizer.readAllTokens();

	    assertTrue("didn't read 1 token from '"+d+"' but "+tokens.size(),
		    tokens.size() == 1);

	    TextToken tok = (TextToken) tokens.get(0);
	    String data = tok.getData();

	    assertTrue("didn't read '>'blabla\"<' from '"+d+"' but '"+data+"'",
		    data.equals(d));
	    assertTrue("didn't get '&gt;&apos;blabla&quot;&lt;' from '"+d+"' but '"+tok.toString()+"'",
		    tok.toString().equals("&gt;&apos;blabla&quot;&lt;"));
	}
    }

    public void testComment ()
    throws IOException
    {
	{
	    String d = "<!---->";
	    Tokenizer tokenizer = new Tokenizer(d);
	    List tokens = tokenizer.readAllTokens();

	    assertTrue("didn't read 1 token from '"+d+"' but "+tokens.size(),
		    tokens.size() == 1);
	    assertTrue("didn't read a comment token from '"+d+"'",
		    tokens.get(0) instanceof CommentToken);

	    CommentToken tok = (CommentToken) tokens.get(0);
	    String data = tok.getData();

	    assertTrue("didn't read an empty comment from '"+d+"' but '"+data+"'",
		    data.length() == 0);
	}
	{
	    String d = " <!----> ";
	    Tokenizer tokenizer = new Tokenizer(d);
	    List tokens = tokenizer.readAllTokens();

	    assertTrue("didn't read 3 token from '"+d+"' but "+tokens.size(),
		    tokens.size() == 3);
	}
	{
	    String d = "<!-- -->";
	    Tokenizer tokenizer = new Tokenizer(d);
	    List tokens = tokenizer.readAllTokens();

	    assertTrue("didn't read 1 token from '"+d+"' but "+tokens.size(),
		    tokens.size() == 1);

	    CommentToken tok = (CommentToken) tokens.get(0);
	    String data = tok.getData();

	    assertTrue("didn't read ' ' from '"+d+"' but '"+data+"'",
		    data.equals(" "));
	}
	{
	    String d = "<!-<!--<!---->-->";
	    Tokenizer tokenizer = new Tokenizer(d);
	    List tokens = tokenizer.readAllTokens();

	    assertTrue("didn't read 3 token from '"+d+"' but "+tokens.size(),
		    tokens.size() == 3);

	    TextToken tok0 = (TextToken) tokens.get(0);
	    CommentToken tok1 = (CommentToken) tokens.get(1);
	    String data = tok1.getData();
	    TextToken tok2 = (TextToken) tokens.get(2);

	    assertTrue("didn't read '<!--' from '"+d+"' but '"+data+"'",
		    data.equals("<!--"));
	}
	{
	    String d = "<!--->-->";
	    Tokenizer tokenizer = new Tokenizer(d);
	    List tokens = tokenizer.readAllTokens();

	    assertTrue("didn't read 1 token from '"+d+"' but "+tokens.size(),
		    tokens.size() == 1);

	    CommentToken tok = (CommentToken) tokens.get(0);
	    String data = tok.getData();

	    assertTrue("didn't read '->' from '"+d+"' but '"+data+"'",
		    data.equals("->"));
	}
    }

    public void testCDATA ()
    throws IOException
    {
	{
	    String d = "<![CDATA[]]>";
	    Tokenizer tokenizer = new Tokenizer(d);
	    List tokens = tokenizer.readAllTokens();

	    assertTrue("didn't read 1 token from '"+d+"' but "+tokens.size(),
		    tokens.size() == 1);
	    assertTrue("didn't read a cdata token from '"+d+"'",
		    tokens.get(0) instanceof CDATAToken);
	}
	{
	    String d = " <![CDATA[]]> ";
	    Tokenizer tokenizer = new Tokenizer(d);
	    List tokens = tokenizer.readAllTokens();

	    assertTrue("didn't read 3 token from '"+d+"' but "+tokens.size(),
		    tokens.size() == 3);
	}
	{
	    String d = "<![CDATA[ ]]>";
	    Tokenizer tokenizer = new Tokenizer(d);
	    List tokens = tokenizer.readAllTokens();

	    assertTrue("didn't read 1 token from '"+d+"' but "+tokens.size(),
		    tokens.size() == 1);

	    CDATAToken tok = (CDATAToken) tokens.get(0);
	    String data = tok.getData();

	    assertTrue("didn't read ' ' from '"+d+"' but '"+data+"'",
		    data.equals(" "));
	}
	{
	    String d = "<![CDATA<![CDATA[<![CDATA[]]>]]>";
	    Tokenizer tokenizer = new Tokenizer(d);
	    List tokens = tokenizer.readAllTokens();

	    assertTrue("didn't read 3 token from '"+d+"' but "+tokens.size(),
		    tokens.size() == 3);

	    TextToken tok0 = (TextToken) tokens.get(0);
	    CDATAToken tok1 = (CDATAToken) tokens.get(1);
	    String data = tok1.getData();
	    TextToken tok2 = (TextToken) tokens.get(2);

	    assertTrue("didn't read '<![CDATA[' from '"+d+"' but '"+data+"'",
		    data.equals("<![CDATA["));
	}
	{
	    String d = "<![CDATA[]>]]>";
	    Tokenizer tokenizer = new Tokenizer(d);
	    List tokens = tokenizer.readAllTokens();

	    assertTrue("didn't read 1 token from '"+d+"' but "+tokens.size(),
		    tokens.size() == 1);

	    CDATAToken tok = (CDATAToken) tokens.get(0);
	    String data = tok.getData();

	    assertTrue("didn't read ']>' from '"+d+"' but '"+data+"'",
		    data.equals("]>"));
	}
    }
}
