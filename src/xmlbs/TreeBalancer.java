package xmlbs;

import java.util.ArrayList;
import java.util.Iterator;

import xmlbs.tokens.CommentToken;
import xmlbs.tokens.TagToken;
import xmlbs.tokens.Token;

/**
 * @author R.W. van 't Veer
 * @version $Revision: 1.2 $
 */
public class TreeBalancer {
    public static void balance (TreeNode root, DocumentStructure ds) {
        TagToken parent = (TagToken) root.getPayload();
        for (Iterator it = (new ArrayList(root.getChildren())).iterator(); it.hasNext();) {
            TreeNode node = (TreeNode) it.next();
            Token tok = (Token) node.getPayload();
            
            if (tok instanceof CommentToken) {
                continue;
            }
            if (parent != null && !ds.canContain(parent, tok)) {
                TreeNode candidate = node.findParent(new ContainerFinder(tok, ds));
                if (candidate != null) {
                    node.move(candidate);
                } else {
                    node.remove();
                }
            }            
            if (tok instanceof TagToken) {
                if (((TagToken)tok).isCloseTag()) {
                    node.remove();
                    continue;
                }
                balance(node, ds);
            }
        }
    }
    private static class ContainerFinder implements TreeNode.Finder {
        private Token child;
        private DocumentStructure ds;
        
        public ContainerFinder (Token child, DocumentStructure ds) {
            this.child = child;
            this.ds = ds; 
        }
        
        public boolean isFound (TreeNode node) {
            Token tok = (Token) node.getPayload();
            return tok instanceof TagToken && ds.canContain((TagToken) tok, child);
        }
    }
}
