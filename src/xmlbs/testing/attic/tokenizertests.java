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
/*
    public void testTag ()
    {
	assertTrue("assert doesn't work", true);
    }

    public void testText ()
    {
	assertTrue("assert doesn't work", true);
    }

    public void testComment ()
    {
	assertTrue("assert doesn't work", true);
    }
*/
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
	}{
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
