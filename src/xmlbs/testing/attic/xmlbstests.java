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

import java.io.*;
import java.util.*;

import junit.framework.*;

import xmlbs.*;

/**
 * Global tests for XMLBS.
 *
 * @see xmlbs.Tokenizer
 * @author R.W. van ' t Veer
 * @version $Revision: 1.2 $
 */
public class XMLBSTests extends TestCase {
    /**
     * @param name test name
     */
    public XMLBSTests (String name) {
        super(name);
    }

    /**
     * Commandline interface.
     * @param args ignored
     */
    public static void main (String[] args) {
        String[] par = new String[1];
        par[0] = XMLBSTests.class.getName();
        junit.swingui.TestRunner.main(par);
    }

    /**
     * @return suite of test available from this class
     */
    public static Test suite() {
        return new TestSuite(XMLBSTests.class);
    }

    /**
     * Test cleaning empty tags.
     * @throws IOException when reading fails
     */
    public void testCleanupEmptyTags ()
    throws IOException {
	Properties prop = new Properties();
	prop.setProperty("@ROOT", "a");
	prop.setProperty("a", "b c");
	prop.setProperty("b", "");
	prop.setProperty("c", "#TEXT");
	DocumentStructure ds = new PropertiesDocumentStructure(prop);

	String[][] data = {
	    { "<a><b><c>", "<a><b/><c/></a>" },
	    { "<a> <b> <c> ", "<a> <b/> <c/> </a>" },
	    { "<a><!-- --><b><!-- --><c>", "<a><!-- --><b/><!-- --><c/></a>" },
	    { "<a> <!-- --><b><!-- --> <c>", "<a> <!-- --><b/><!-- --> <c/></a>" },
	    { "<a> <!-- --> <b> <!-- --> <c>", "<a> <!-- --> <b/> <!-- --> <c/></a>" },
	    { "<a> !-- -- <b> !-- -- <c>", "<a><b/><c/></a>" },
	    { "<a> !-- -- <b> !-- -- <c> !-- --", "<a><b/><c> !-- --</c></a>" },
	};
	for (int i = 0; i < data.length; i++) {
	    String in = data[i][0];
	    String out = data[i][1];
	    XMLBS xmlbs = new XMLBS(new ByteArrayInputStream(in.getBytes()), ds);
	    xmlbs.process();
	    ByteArrayOutputStream bout = new ByteArrayOutputStream();
	    xmlbs.write(bout);
	    bout.flush();
	    String result = bout.toString();

	    assertTrue("from '" + in + "' got '" + result + "' expected '" + out + "'",
		    result.equals(out));
	}
    }
}
