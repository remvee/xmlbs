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

import junit.framework.TestCase;
import xmlbs.TreeNode;

/**
 * Tests for the TreeNode class.
 * @author R.W. van 't Veer
 * @version $Revision: 1.1 $
 */
public class TreeNodeTest extends TestCase {

    /**
     * Constructor for TreeNodeTest.
     * @param arg0
     */
    public TreeNodeTest(String arg0) {
        super(arg0);
    }

    public void testGetParent() {
        TreeNode root = new TreeNode();
        TreeNode node01 = root.addChild("foo");
        TreeNode node02 = root.addChild("bar");
        TreeNode node11 = node01.addChild("bla");
        TreeNode node12 = node02.addChild("die");

        assertTrue(node01.getParent() == root);
        assertTrue(node02.getParent() == root);
        assertTrue(node11.getParent() == node01);
        assertTrue(node12.getParent() == node02);

    }

    public void testFindParent() {
        TreeNode root = new TreeNode();
        TreeNode node1 = root.addChild("foo");
        TreeNode node2 = node1.addChild("bar");
        TreeNode node3 = node1.addChild("bla").addChild("die");
        
        TreeNode.Finder barFinder = new TreeNode.Finder () {
            public boolean isFound (TreeNode n) {
                return n.getPayload() != null && n.getPayload().equals("bar");
            }
        };
        assertTrue(null == root.findParent(barFinder));
        assertTrue(null == node1.findParent(barFinder));
        assertTrue(null != node2.findParent(barFinder));
        assertTrue(null == node3.findParent(barFinder));
        
        TreeNode.Finder fooFinder = new TreeNode.Finder () {
            public boolean isFound (TreeNode n) {
                return n.getPayload() != null && n.getPayload().equals("foo");
            }
        };
        assertTrue(null == root.findParent(fooFinder));
        assertTrue(null != node1.findParent(fooFinder));
        assertTrue(null != node2.findParent(fooFinder));
        assertTrue(null != node3.findParent(fooFinder));
    }

    public void testGetChildren() {
        TreeNode root = new TreeNode();
        for (int i = 0; i < 10; i++) {
            root.addChild(null);
        }
        assertTrue(root.getChildren().size() == 10);
        for (int i = 0; i < 10; i++) {
            root.addChild(null);
        }
        assertTrue(root.getChildren().size() == 20);
    }

    public void testGetDepth() {
        TreeNode root = new TreeNode();
        root.addChild(null).addChild(null).addChild(null);
        assertTrue(root.getDepth() == 4);
        root.addChild(null).addChild(null).addChild(null);
        assertTrue(root.getDepth() == 4);
        root.addChild(null).addChild(null).addChild(null).addChild(null);
        assertTrue(root.getDepth() == 5);
        assertTrue(root.getChildren().size() == 3);
    }
}
