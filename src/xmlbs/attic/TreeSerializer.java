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

/**
 * Make a list of tokens from a tree.
 * @author R.W. van 't Veer
 * @version $Revision: 1.1 $
 */
public class TreeSerializer { 
    /**
     * Flatten a tree to a list.
     */
    public static void flatten (TreeNode root, List result) {
        Object p = root.getPayload();
        if (p instanceof TagToken) {
            TagToken tag = (TagToken) p;
            if (tag.isOpenTag()) {
                result.add(tag);
                for (Iterator it = root.getChildren().iterator(); it.hasNext();) {
                    TreeNode node = (TreeNode) it.next();
                    flatten(node, result);
                }
                result.add(tag.closeTag());
            } else if (!tag.isCloseTag()) {
                result.add(tag);
            }
        } else if (p != null) {
            result.add(p);
        }
    }
}
