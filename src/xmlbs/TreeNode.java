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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A tree node.
 * 
 * @author R.W. van 't Veer
 * @version $Revision: 1.3 $
 */
public class TreeNode {
    /** parent node */
    private TreeNode parent = null;
    /** list of children */
    private List children = new ArrayList();
    /** node payload */
    private Object payload = null;
    
    /**
     * Construct an empty root node.
     */
    public TreeNode () {
    }
    
    /**
     * Construct a child node.
     * @param parent the parent of this node
     * @param payload the data held by this node
     */
    private TreeNode (TreeNode parent, Object payload) {
        this.parent = parent;
        this.payload = payload;
    }
    
    /**
     * @return the parent of this node or <tt>null</tt> when it's the root node
     */
    public TreeNode getParent () {
        return parent;
    }
    
    /**
     * Find a parent node.
     * @param finder the class which determines if the node is found
     * @return a node approved by the finder or <tt>null</tt>
     */
    public TreeNode findParent (Finder finder) {
        for (TreeNode node = getParent(); node != null; node = node.getParent()) {
            if (finder.isFound(node)) {
                return node;
            }
        }
        return null;
    }

    /**
     * @return a list of the children of this node
     */    
    public List getChildren () {
        return children;
    }
    
    /**
     * Add a child to this node
     * @param payload the data to be held by the new node
     * @return new treenode for given payload
     */
    public TreeNode addChild (Object payload) {
        TreeNode node = new TreeNode(this, payload);
        children.add(node);
        return node;
    }
    
    /**
     * @return the data held by this node
     */
    public Object getPayload () {
        return payload;
    }
    
    /**
     * Determine the maximum depth by traversing all children
     * @return maximum depth measured
     */
    public int getDepth () {
        int max = 0;
        for (Iterator it = getChildren().iterator(); it.hasNext();) {
            TreeNode n = (TreeNode) it.next();
            max = Math.max(max, n.getDepth());
        }
        return max + 1;
    }
    
    /**
     * The interface finders should implement.
     * @see #findParent(Finder)
     * @see #findChild(Finder)
     */
    public static interface Finder {
        boolean isFound (TreeNode node);
    }
    
    /**
     * Remove this node from tree.
     */
    public void remove () {
        getParent().getChildren().remove(this);
    }
    
    /**
     * Remove node from parent and add as last child to adopter.
     * @param adopter new parent
     */
    public void move (TreeNode adopter) {
        getParent().getChildren().remove(this);
        adopter.getChildren().add(this);
    }
    

// debug stuff
    /**
     * @return textual representation for debugging.
     */
    public String toString () {
        return toString(new StringBuffer(), 0).toString();
    }
    /**
     * @param out buffer to write to
     * @param depth current depth for indentation
     * @return same as <tt>out</tt>
     */
    private StringBuffer toString (StringBuffer out, int depth) {
        for (int i = 0; i < depth; i++) {
            out.append(' ');
        }
        out.append(">>" + getPayload() + "<<");
        out.append('\n');
        for (Iterator it = getChildren().iterator(); it.hasNext();) {
            TreeNode n = (TreeNode) it.next();
            n.toString(out, depth + 1);
        }
        return out; 
    }
}
