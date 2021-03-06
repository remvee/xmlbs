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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests extends TestCase {
    public AllTests(String name) {
        super(name);
    }

    public static void main(String[] args) {
        String[] par = new String[1];
        par[0] = AllTests.class.getName();
        junit.swingui.TestRunner.main(par);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("All XMLBS tests");
        suite.addTestSuite(TokenizerTest.class);
        suite.addTestSuite(TreeNodeTest.class);
        suite.addTestSuite(TreeBuilderTest.class);
        suite.addTestSuite(TreeBalancerTest.class);
        return suite;
    }
}
