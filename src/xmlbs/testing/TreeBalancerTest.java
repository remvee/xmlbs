package xmlbs.testing;

import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;
import xmlbs.DocumentStructure;
import xmlbs.PropertiesDocumentStructure;
import xmlbs.Tokenizer;
import xmlbs.TreeBalancer;
import xmlbs.TreeBuilder;
import xmlbs.TreeNode;

/**
 * Tests for the TreeBalancer class.
 * @author R.W. van 't Veer
 * @version $Revision: 1.2 $
 */
public class TreeBalancerTest extends TestCase {
    private static final DocumentStructure ds;
    static {
        Properties p = new Properties();
        p.put("a", "a b c");
        p.put("b", "a b");
        ds = new PropertiesDocumentStructure(p);
    }
  
    /**
     * Constructor for TreeBalancerTest.
     * @param arg0
     */
    public TreeBalancerTest(String arg0) {
        super(arg0);
    }

    public void testBalance() throws IOException {
        String test = "<a><b><c></c></b></a>";
        TreeNode root = TreeBuilder.build(new Tokenizer(test, ds).readAllTokens());
        TreeBalancer.balance(root, ds);
        assertTrue("unexpected result", root.getDepth() == 3);
        assertTrue("MAKE TESTS NOW! " + root, false);
    }
}
