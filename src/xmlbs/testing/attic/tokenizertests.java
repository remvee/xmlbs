/*
 * xmlbs
 *
 * Copyright (C) 2002  R.W. van 't Veer
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 */

package xmlbs.testing;

import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.*;

import xmlbs.*;

/**
 * Unit tests for tokenizer.
 *
 * @see xmlbs.Tokenizer
 * @author R.W. van ' t Veer
 * @version $Revision: 1.9 $
 */
public class TokenizerTests extends TestCase {
    /**
     * @param name test name
     */
    public TokenizerTests (String name) {
        super(name);
    }

    /**
     * Commandline interface.
     * @param args ignored
     */
    public static void main (String[] args) {
        String[] par = new String[1];
        par[0] = TokenizerTests.class.getName();
        junit.swingui.TestRunner.main(par);
    }

    /**
     * @return suite of test available from this class
     */
    public static Test suite() {
        return new TestSuite(TokenizerTests.class);
    }

    /**
     * Test reading of element tags.
     * @throws IOException when reading fails
     */
    public void testTag ()
    throws IOException {
	DocumentStructure ds = new DummyDocumentStructure();

        // test open tags
        {
            final String d[] =
                {
                    "<foo>", "<foo >", "<foo foo=bar>", "<foo foo=bar >",
                    "<foo foo=bar bar=foo>", "<foo foo=bar bar=foo >"
                };
            for (int i = 0; i < d.length; i++) {
                Tokenizer tokenizer = new Tokenizer(d[i], ds);
                List tokens = tokenizer.readAllTokens();

                assertTrue(
			"didn't read 1 token from '" + d + "' but " + tokens.size(),
			tokens.size() == 1);

                TagToken tok = (TagToken) tokens.get(0);
                String data = tok.getName();

                assertTrue(
			"didn't read a 'foo' tag from '" + d + "' but '" + data + "'",
			data.equals("foo"));
                assertTrue(
			"didn't read a 'foo' open tag from '" + d[i] + "'",
			tok.isOpenTag());
            }
        }
        // test empty tags
        {
            final String d[] =
                {
                    "<foo/>", "<foo />", "<foo foo=bar/>", "<foo foo=bar />",
                    "<foo foo=bar bar=foo/>", "<foo foo=bar bar=foo />"
                };
            for (int i = 0; i < d.length; i++) {
                Tokenizer tokenizer = new Tokenizer(d[i], ds);
                List tokens = tokenizer.readAllTokens();

                assertTrue(
			"didn't read 1 token from '" + d + "' but " + tokens.size(),
			tokens.size() == 1);

                TagToken tok = (TagToken) tokens.get(0);
                String data = tok.getName();

                assertTrue(
			"didn't read a 'foo' tag from '" + d[i] + "' but '" + data + "'",
			data.equals("foo"));
                assertTrue(
			"didn't read a 'foo' empty tag from '" + d[i] + "'",
			tok.isEmptyTag());
            }
        }
        // test close tags
        {
            final String d[] =
                {
                    "</foo>", "</ foo>", "</ foo >"
                };
            for (int i = 0; i < d.length; i++) {
                Tokenizer tokenizer = new Tokenizer(d[i], ds);
                List tokens = tokenizer.readAllTokens();

                assertTrue(
			"didn't read 1 token from '" + d + "' but " + tokens.size(),
			tokens.size() == 1);

                TagToken tok = (TagToken) tokens.get(0);
                String data = tok.getName();

                assertTrue(
			"didn't read a 'foo' tag from '" + d[i] + "' but '" + data + "'",
			data.equals("foo"));
                assertTrue(
			"didn't read a 'foo' close tag from '" + d[i] + "'",
			tok.isCloseTag());
            }
        }
        // test attribute handling
        {
            final String d[] =
                {
                    "<foo foo=bar bar=foo>", "<foo foo='bar' bar='foo'>",
                    "<foo foo=\"bar\" bar=\"foo\">", "<foo foo='bar' bar=\"foo\">",
                    "<foo\nfoo=bar\r\nbar=foo\r\b>", "<foo\tfoo='bar'\tbar='foo'\t>",
                };
            for (int i = 0; i < d.length; i++) {
                Tokenizer tokenizer = new Tokenizer(d[i], ds);
                List tokens = tokenizer.readAllTokens();

                assertTrue(
			"didn't read 1 token from '" + d + "' but " + tokens.size(),
			tokens.size() == 1);

                TagToken tok = (TagToken) tokens.get(0);
                String data = tok.getName();
                Map attrs = tok.getAttributes();

                assertTrue(
			"didn't read a 'foo' tag from '" + d[i] + "' but '" + data + "'",
			data.equals("foo"));
                assertTrue(
			"didn't read a 'foo' open tag from '" + d[i] + "'",
			tok.isOpenTag());
                assertTrue(
			"didn't read a 2 attributes from '" + d[i] + "'",
			attrs.keySet().size() == 2);
                assertTrue(
			"didn't read a 'foo' attribute from '" + d[i] + "'",
			attrs.get("foo").equals("bar"));
                assertTrue(
			"didn't read a 'bar' attribute from '" + d[i] + "'",
			attrs.get("bar").equals("foo"));
            }
        }
        // more attribute tests
        {
            final String d[][] =
                {
                    { "<foo foo=bar=foo>", "<foo foo=\"bar=foo\">" },
                    { "<foo foo='\"bar=foo\"'>", "<foo foo=\"&#034;bar=foo&#034;\">" },
                    { "<foo foo=\"'bar=foo'\">", "<foo foo=\"&#039;bar=foo&#039;\">" },
                };
            for (int i = 0; i < d.length; i++) {
		String in = d[i][0];
		String out = d[i][1];

                Tokenizer tokenizer = new Tokenizer(in, ds);
                List tokens = tokenizer.readAllTokens();

                assertTrue(
			"didn't read 1 token from '" + d + "' but " + tokens.size(),
			tokens.size() == 1);

                TagToken tok = (TagToken) tokens.get(0);
                String data = tok.toString();

                assertTrue(
			"input '" + in + "' didn't give '" + out + "' but '" + data + "'",
			data.equals(d[i][1]));
            }
        }
    }

    /**
     * Test reading of text blocks.
     * @throws IOException when reading fails
     */
    public void testText ()
    throws IOException {
	DocumentStructure ds = new DummyDocumentStructure();

        {
            String d = " ";
            Tokenizer tokenizer = new Tokenizer(d, ds);
            List tokens = tokenizer.readAllTokens();

            assertTrue(
		    "didn't read 1 token from '" + d + "' but " + tokens.size(),
		    tokens.size() == 1);
            assertTrue(
		    "didn't read a text token from '" + d + "'",
		    tokens.get(0) instanceof TextToken);

            TextToken tok = (TextToken) tokens.get(0);
            String data = tok.getData();

            assertTrue(
		    "didn't read ' ' from '" + d + "' but '" + data + "'",
		    data.equals(" "));
        }
        {
            String d = "<";
            Tokenizer tokenizer = new Tokenizer(d, ds);
            List tokens = tokenizer.readAllTokens();

            assertTrue(
		    "didn't read 1 token from '" + d + "' but " + tokens.size(),
		    tokens.size() == 1);

            TextToken tok = (TextToken) tokens.get(0);
            String data = tok.getData();

            assertTrue(
		    "didn't read '<' from '" + d + "' but '" + data + "'",
		    data.equals("<"));

            assertTrue(
		    "didn't get '&lt;' from '" + d + "' but '" + tok + "'",
		    tok.toString().equals("&lt;"));
        }
        {
            String d = "<foobar<>";
            Tokenizer tokenizer = new Tokenizer(d, ds);
            List tokens = tokenizer.readAllTokens();

            assertTrue(
		    "didn't read 1 token from '" + d + "' but " + tokens.size(),
		    tokens.size() == 1);

            TextToken tok = (TextToken) tokens.get(0);
            String data = tok.getData();

            assertTrue(
		    "didn't read '<foobar<>' from '" + d + "' but '" + data + "'",
		    data.equals("<foobar<>"));

            assertTrue(
		    "didn't get '&lt;foobar&lt;&gt;' from '" + d + "' but '" + tok + "'",
		    tok.toString().equals("&lt;foobar&lt;&gt;"));
        }
        {
            String d = "<<foobar>>";
            Tokenizer tokenizer = new Tokenizer(d, ds);
            List tokens = tokenizer.readAllTokens();

            assertTrue(
		    "didn't read 3 token from '" + d + "' but " + tokens.size(),
		    tokens.size() == 3);

            TextToken tok0 = (TextToken) tokens.get(0);
            String data0 = tok0.getData();
            TextToken tok1 = (TextToken) tokens.get(2);
            String data1 = tok1.getData();

            assertTrue(
		    "didn't read '<' from '" + d + "' but '" + data0 + "'",
		    data0.equals("<"));
            assertTrue(
		    "didn't read '>' from '" + d + "' but '" + data1 + "'",
		    data1.equals(">"));
        }
        {
            String d = ">'foobar\"<";
            Tokenizer tokenizer = new Tokenizer(d, ds);
            List tokens = tokenizer.readAllTokens();

            assertTrue(
		    "didn't read 1 token from '" + d + "' but " + tokens.size(),
		    tokens.size() == 1);

            TextToken tok = (TextToken) tokens.get(0);
            String data = tok.getData();
	    String out = "&gt;&#039;foobar&#034;&lt;";

            assertTrue(
		    "didn't read '>'foobar\"<' from '" + d + "' but '" + data + "'",
		    data.equals(d));
            assertTrue(
		    "didn't get '" + out + "' from '" + d + "' but '" + tok + "'",
                       tok.toString().equals(out));
        }
	// normal entity stuff
	{
	    Properties prop = new Properties();
	    prop.setProperty("tag", "$attr");
	    prop.setProperty("&", "foo bar");
	    DocumentStructure ds0 = new PropertiesDocumentStructure(prop);
	    String d[][] = {
		{ "&foo;&bar;&fam;", "&foo;&bar;&amp;fam;" },
		{ "&foo ;&bar;&fam;", "&amp;foo ;&bar;&amp;fam;" },
		{ "<tag attr='&foo ;&bar;&fam;'>", "<tag attr=\"&amp;foo ;&bar;&amp;fam;\">" },
	    };
	    for (int i = 0; i < d.length; i++) {
		String in = d[i][0];
		String out = d[i][1];

		Tokenizer tokenizer = new Tokenizer(in, ds0);
		List tokens = tokenizer.readAllTokens();

		assertTrue(
			"didn't read 1 token from '" + in + "' but " + tokens.size(),
			tokens.size() == 1);

		Token tok = (Token) tokens.get(0);
		assertTrue(
			"didn't get '" + out + "' from '" + in + "' but '" + tok + "'",
			tok.toString().equals(out));
	    }
	}
	// ignore case entity stuff
	{
	    Properties prop = new Properties();
	    prop.setProperty("tag", "$attr");
	    prop.setProperty("&", "foo bar baR");
	    DocumentStructure ds0 = new PropertiesDocumentStructure(prop);
	    ds0.setIgnoreCase(true);
	    String d[][] = {
		{ "&foo;&bar;&fam;", "&foo;&bar;&amp;fam;" },
		{ "&foo ;&baR;&FAM;", "&amp;foo ;&baR;&amp;FAM;" },
		{ "&FOO;&bar;&FAM;", "&foo;&bar;&amp;FAM;" },
		{ "<tag attr=&FOO;&bar;&FAM;>", "<tag attr=\"&foo;&bar;&amp;FAM;\">" },
	    };
	    for (int i = 0; i < d.length; i++) {
		String in = d[i][0];
		String out = d[i][1];

		Tokenizer tokenizer = new Tokenizer(in, ds0);
		List tokens = tokenizer.readAllTokens();

		assertTrue(
			"didn't read 1 token from '" + in + "' but " + tokens.size(),
			tokens.size() == 1);

		Token tok = (Token) tokens.get(0);
		assertTrue(
			"didn't get '" + out + "' from '" + in + "' but '" + tok + "'",
			tok.toString().equals(out));
	    }
	}
    }

    /**
     * Test reading of comments.
     * @throws IOException when reading fails
     */
    public void testComment ()
    throws IOException {
	DocumentStructure ds = new DummyDocumentStructure();

        {
            String d = "<!---->";
            Tokenizer tokenizer = new Tokenizer(d, ds);
            List tokens = tokenizer.readAllTokens();

            assertTrue(
		    "didn't read 1 token from '" + d + "' but " + tokens.size(),
		    tokens.size() == 1);
            assertTrue(
		    "didn't read a comment token from '" + d + "'",
		    tokens.get(0) instanceof CommentToken);

            CommentToken tok = (CommentToken) tokens.get(0);
            String data = tok.getData();

            assertTrue(
		    "didn't read an empty comment from '" + d + "' but '" + data + "'",
		    data.length() == 0);
        }
        {
            String d = " <!----> ";
            Tokenizer tokenizer = new Tokenizer(d, ds);
            List tokens = tokenizer.readAllTokens();

            assertTrue(
		    "didn't read 3 token from '" + d + "' but " + tokens.size(),
		    tokens.size() == 3);
        }
        {
            String d = "<!-- -->";
            Tokenizer tokenizer = new Tokenizer(d, ds);
            List tokens = tokenizer.readAllTokens();

            assertTrue(
		    "didn't read 1 token from '" + d + "' but " + tokens.size(),
		    tokens.size() == 1);

            CommentToken tok = (CommentToken) tokens.get(0);
            String data = tok.getData();

            assertTrue(
		    "didn't read ' ' from '" + d + "' but '" + data + "'",
		    data.equals(" "));
        }
        {
            String d = "<!-<!--<!---->-->";
            Tokenizer tokenizer = new Tokenizer(d, ds);
            List tokens = tokenizer.readAllTokens();

            assertTrue(
		    "didn't read 3 token from '" + d + "' but " + tokens.size(),
		    tokens.size() == 3);

            TextToken tok0 = (TextToken) tokens.get(0);
            CommentToken tok1 = (CommentToken) tokens.get(1);
            String data = tok1.getData();
            TextToken tok2 = (TextToken) tokens.get(2);

            assertTrue(
		    "didn't read '<!--' from '" + d + "' but '" + data + "'",
		    data.equals("<!--"));
        }
        {
            String d = "<!--->-->";
            Tokenizer tokenizer = new Tokenizer(d, ds);
            List tokens = tokenizer.readAllTokens();

            assertTrue(
		    "didn't read 1 token from '" + d + "' but " + tokens.size(),
		    tokens.size() == 1);

            CommentToken tok = (CommentToken) tokens.get(0);
            String data = tok.getData();

            assertTrue(
		    "didn't read '->' from '" + d + "' but '" + data + "'",
		    data.equals("->"));
        }
    }

    /**
     * Test reading of CDATA sections.
     * @throws IOException when reading fails
     */
    public void testCDATA ()
    throws IOException {
	DocumentStructure ds = new DummyDocumentStructure();

        {
            String d = "<![CDATA[]]>";
            Tokenizer tokenizer = new Tokenizer(d, ds);
            List tokens = tokenizer.readAllTokens();

            assertTrue(
		    "didn't read 1 token from '" + d + "' but " + tokens.size(),
		    tokens.size() == 1);
            assertTrue(
		    "didn't read a cdata token from '" + d + "'",
		    tokens.get(0) instanceof CDATAToken);
        }
        {
            String d = " <![CDATA[]]> ";
            Tokenizer tokenizer = new Tokenizer(d, ds);
            List tokens = tokenizer.readAllTokens();

            assertTrue(
		    "didn't read 3 token from '" + d + "' but " + tokens.size(),
		    tokens.size() == 3);
        }
        {
            String d = "<![CDATA[ ]]>";
            Tokenizer tokenizer = new Tokenizer(d, ds);
            List tokens = tokenizer.readAllTokens();

            assertTrue(
		    "didn't read 1 token from '" + d + "' but " + tokens.size(),
		    tokens.size() == 1);

            CDATAToken tok = (CDATAToken) tokens.get(0);
            String data = tok.getData();

            assertTrue(
		    "didn't read ' ' from '" + d + "' but '" + data + "'",
		    data.equals(" "));
        }
        {
            String d = "<![CDATA<![CDATA[<![CDATA[]]>]]>";
            Tokenizer tokenizer = new Tokenizer(d, ds);
            List tokens = tokenizer.readAllTokens();

            assertTrue(
		    "didn't read 3 token from '" + d + "' but " + tokens.size(),
		    tokens.size() == 3);

            TextToken tok0 = (TextToken) tokens.get(0);
            CDATAToken tok1 = (CDATAToken) tokens.get(1);
            String data = tok1.getData();
            TextToken tok2 = (TextToken) tokens.get(2);

            assertTrue(
		    "didn't read '<![CDATA[' from '" + d + "' but '" + data + "'",
		    data.equals("<![CDATA["));
        }
        {
            String d = "<![CDATA[]>]]>";
            Tokenizer tokenizer = new Tokenizer(d, ds);
            List tokens = tokenizer.readAllTokens();

            assertTrue(
		    "didn't read 1 token from '" + d + "' but " + tokens.size(),
		    tokens.size() == 1);

            CDATAToken tok = (CDATAToken) tokens.get(0);
            String data = tok.getData();

            assertTrue(
		    "didn't read ']>' from '" + d + "' but '" + data + "'",
		    data.equals("]>"));
        }
    }
}
