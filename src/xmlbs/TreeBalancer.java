/*
 * xmlbs
 *
 * Copyright (C) 2004  R.W. van 't Veer
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

package xmlbs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import xmlbs.tokens.CommentToken;
import xmlbs.tokens.TagToken;
import xmlbs.tokens.Token;

/**
 * Balance tree by moving treenodes to friendly locations.
 * @author R.W. van 't Veer
 * @version $Revision: 1.3 $
 */
public class TreeBalancer {
    /**
     * Balance tree by moving treenodes to friendly locations.
     * @param root the treenode to work from
     * @param ds the document structure which determines if a location is friendly
     */
    public static void balance (TreeNode root, DocumentStructure ds) {
        TagToken parent = (TagToken) root.getPayload();
        
        // copy children list to avoid concurrent modifications while iterating this list
        List l = new ArrayList(root.getChildren());
        for (Iterator it = l.iterator(); it.hasNext();) {
            TreeNode node = (TreeNode) it.next();
            Token tok = (Token) node.getPayload();
            
            // comments can go anywhere
            if (tok instanceof CommentToken) {
                continue;
            }

            // if parent tag can not contain this token
            if (parent != null && !ds.canContain(parent, tok)) {
                // find a place up in the tree for this token
                TreeNode candidate = node.findParent(new ContainerFinder(tok, ds));
                if (candidate != null) {
                    // a place was found, move it there
                    node.move(candidate);
                } else {
                    // otherwise drop it
                    node.remove();
                }
            }

            if (tok instanceof TagToken) {
                // drop close tags
                if (((TagToken)tok).isCloseTag()) {
                    node.remove();
                    continue;
                }
                
                // recurse into open or empty tags
                balance(node, ds);
            }
        }
    }
    
    /**
     * A visitor class to determine if a treenode can hold a given token.
     */
    private static class ContainerFinder implements TreeNode.Finder {
        /**
         * Child token.
         */
        private Token child;
        /**
         * Document structure.
         */
        private DocumentStructure ds;
        
        /**
         * @param child token to find parent for
         * @param ds document structure description
         */
        public ContainerFinder (Token child, DocumentStructure ds) {
            this.child = child;
            this.ds = ds; 
        }
        
        /**
         * @param node treenode to test for
         * @return <tt>true</tt> when given treenode can hold child token
         */
        public boolean isFound (TreeNode node) {
            Token tok = (Token) node.getPayload();
            return tok instanceof TagToken && ds.canContain((TagToken) tok, child);
        }
    }
}
