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

package xmlbs;

import java.util.Iterator;
import java.util.List;

import xmlbs.tokens.TagToken;
import xmlbs.tokens.Token;

/**
 * Build a hierarchy from a list of tokens.  The tokens will be wrapped by tree
 * nodes.  The tree advances on open tags, matching close tags will be dropped
 * and unmatched close tags retained.
 *  
 * @author R.W. van 't Veer
 * @version $Revision: 1.2 $
 */
public class TreeBuilder {
    /**
     * Construct a tree for a list of tokens
     * @param tokens list to read from
     * @return the root node for the result tree
     */
    public static TreeNode build (List tokens) {
        TreeNode root = new TreeNode();
        TreeNode current = root;
        for (Iterator it = tokens.iterator(); it.hasNext();) {
            Token tok = (Token) it.next();
            
            if (tok instanceof TagToken) {
                TagToken tag = (TagToken) tok;
                if (tag.isOpenTag()) {
                    current = current.addChild(tok);
                } else if (tag.isCloseTag()) {
                    OpenFinder finder = new OpenFinder(tag);
                    TreeNode node = finder.isFound(current) ? current : current.findParent(finder);
                    if (node != null) {
                        current = node.getParent();
                    } else {
                        current.addChild(tok);
                    }
                } else {
                    current.addChild(tok);
                }
            } else {
                current.addChild(tok);
            }
        }
        
        return root;
    }

    /**
     * The open finder finds an open tag for a given close tag.
     */
    private static class OpenFinder implements TreeNode.Finder {
        private String tagName;
        public OpenFinder (TagToken tag) {
            tagName = tag.getName();
        }
        public boolean isFound (TreeNode node) {
            Object p = node.getPayload();
            if (p instanceof TagToken) {
                 TagToken t = (TagToken) p;
                 return t.isOpenTag() && t.getName().equals(tagName);
            }
            return false;
        }
    }
    
    /**
     * Flatten a tree to a list.
     * @param root root node of tree
     * @param result list to write result to
     */
    public static List flatten (TreeNode root, List result) {
        for (Iterator it = root.getChildren().iterator(); it.hasNext();) {
            flatten_((TreeNode) it.next(), result);
        }
        return result;
    }
    private static void flatten_ (TreeNode root, List result) {
        Object p = root.getPayload();
        if (p instanceof TagToken) {
            TagToken tag = (TagToken) p;
            if (tag.isOpenTag()) {
                List children = root.getChildren();
                if (children.size() == 0) {
                    result.add(tag.emptyTag());
                } else {
                    result.add(tag);
                    for (Iterator it = root.getChildren().iterator(); it.hasNext();) {
                        TreeNode node = (TreeNode) it.next();
                        flatten_(node, result);
                    }
                    result.add(tag.closeTag());
                }
            } else if (!tag.isCloseTag()) {
                result.add(tag);
            }
        } else if (p != null) {
            result.add(p);
        }
    }
}
