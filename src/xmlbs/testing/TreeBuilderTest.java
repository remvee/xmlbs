package xmlbs.testing;

import java.util.Arrays;

import junit.framework.TestCase;
import xmlbs.DocumentStructure;
import xmlbs.TreeBuilder;
import xmlbs.TreeNode;
import xmlbs.tokens.CommentToken;
import xmlbs.tokens.TagToken;
import xmlbs.tokens.TextToken;
import xmlbs.tokens.Token;

/**
 * Tests for the TreeBuilder class.
 * @author R.W. van 't Veer
 * @version $Revision: 1.1 $
 */
public class TreeBuilderTest extends TestCase {
    private static final DocumentStructure dummyDs = new DummyDocumentStructure();

    /**
     * Constructor for TreeBuilderTest.
     * @param arg0
     */
    public TreeBuilderTest(String arg0) {
        super(arg0);
    }
    
    public void testDepth () {
        Object[][] tests = {
            {
                new Token[] {
                    new TagToken("a", dummyDs),
                        new TagToken("b/", dummyDs),
                        new TagToken("c", dummyDs),
                            new TextToken("foo", dummyDs),
                        new TagToken("/c", dummyDs),
                    new TagToken("/a", dummyDs),
                }, new Integer(4)
            },
            {
                new Token[] {
                    new TagToken("a", dummyDs),
                        new TagToken("b", dummyDs),
                            new TagToken("c", dummyDs),
                            new TagToken("/c", dummyDs),
                    new TagToken("/a", dummyDs),
                }, new Integer(4)
            },
            {
                new Token[] {
                    new TagToken("a", dummyDs),
                        new CommentToken("foo"),
                        new TagToken("b/", dummyDs),
                        new TagToken("/b", dummyDs),
                        new TagToken("c", dummyDs),
                            new TextToken("foo", dummyDs),
                            new TextToken("bar", dummyDs),
                        new TagToken("/c", dummyDs),
                    new TagToken("/a", dummyDs),
                    new TagToken("c/", dummyDs),
                }, new Integer(4)
            },
        };
        for (int i = 0; i < tests.length; i++) {
            Token[] tokens = (Token[]) tests[i][0];
            int depth = ((Integer)tests[i][1]).intValue();
            
            TreeNode root = TreeBuilder.construct(Arrays.asList(tokens));
            int result = root.getDepth();
            assertTrue("got " + result + " expected " + depth + " for test #" + i, result == depth);
        }
    }

}
