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

import java.io.*;
import java.util.*;

/**
 * @author R.W. van 't Veer
 * @version $Revision: 1.25 $
 */
public class XMLBS {
    private InputStream in = null;
    private DocumentStructure ds = null;
    private List tokens = null;

    private boolean debug = false;
    private boolean commentIllegalCode = false;
    private String WARNING_MARKER = "XMLBS!";

    public XMLBS (InputStream in, DocumentStructure ds)
    throws IOException {
	this.in = in;
	this.ds = ds;
    }

    public void process ()
    throws IOException {
	tokenize();

	// remove unknown tags and unknown tag attributes
	cleanupTags();
	if (debug) {
	    System.err.println("after 'cleanupTags': " + tokens);
	}

	// remove unknown entities
	// TODO

	// remove stay close tags
	removeStrayCloseTags();
	if (debug) {
	    System.err.println("after 'removeStayCloseTags': " + tokens);
	}

	// reconstruct hierarchy
	reconstHierarchy();
	if (debug) {
	    System.err.println("after 'reconstHierarchy': " + tokens);
	}

	// merge adjoined text tokens
	mergeAdjoinedText();
	if (debug) {
	    System.err.println("after 'mergeAdjoinedText': " + tokens);
	}
    }

    public void setCommentIllegalCode (boolean flag) {
	commentIllegalCode = flag;
    }

    public boolean getCommentIllegalCode () {
	return commentIllegalCode;
    }

// private stuff
    private void tokenize ()
    throws IOException {
	Tokenizer tok = new Tokenizer(in);
	tokens = tok.readAllTokens();
    }

    private void cleanupTags () {
	for (ListIterator it = tokens.listIterator(); it.hasNext();) {
	    Token tok = (Token) it.next();
	    if (tok instanceof TagToken) {
		TagToken tag = (TagToken) tok;
		if (! ds.isKnownTag(tag)) {
		    if (commentIllegalCode) {
			it.set(new CommentToken(WARNING_MARKER + ": UNKNOWN TAG: " + tag));
		    } else {
			it.remove();
		    }
		} else {
		    ds.retainKnownAttributes(tag);
		}
	    }
	}
    }

    private void removeStrayCloseTags () {
	Map m = new HashMap();
	for (ListIterator it = tokens.listIterator(); it.hasNext();) {
	    Token tok = (Token) it.next();
	    if (tok instanceof TagToken) {
		TagToken tag = (TagToken) tok;
		if (tag.isOpenTag()) {
		    // increment map entry
		    Integer i = (Integer) m.get(tag.getName());
		    if (i == null) {
			i = new Integer(0);
		    }
		    i = new Integer(i.intValue() + 1);
		    m.put(tag.getName(), i);
		} else if (tag.isCloseTag()) {
		    // decrement map entry
		    Integer i = (Integer) m.get(tag.getName());
		    if (i == null) {
			i = new Integer(0);
		    }
		    i = new Integer(i.intValue() - 1);
		    // remove tag if entry sub zeros
		    if (i.intValue() < 0) {
			if (commentIllegalCode) {
			    it.set(new CommentToken(WARNING_MARKER + ": STRAY CLOSE TAG: " + tag));
			} else {
			    it.remove();
			}
		    } else {
			m.put(tag.getName(), i);
		    }
		}
	    }
	}
    }

    private void reconstHierarchy() {
	Stack stack = new Stack();
	for (int i = 0; i < tokens.size(); i++) {
	    Token tok = (Token) tokens.get(i);
	    TagToken top = stack.empty() ? null : (TagToken) stack.peek();

	    if (tok instanceof TextToken) {
		TextToken txt = (TextToken) tok;
		if (!txt.isWhiteSpace() && !ds.canContain(top, txt)) {
System.err.println("  text in: "+top);
		    // remove stray text??
		    tokens.remove(i--);
		}
	    } else if (tok instanceof TagToken) {
System.err.println("  for: "+tok);
		TagToken tag = (TagToken) tok;
		if (tag.isOpenTag()) {
		    if (!ds.canContain(top, tag)) {
			if (stack.empty()) {
			    // illegal tag in root
System.err.println("  illegal in root: "+tok);
			    tokens.remove(i--);
			    continue;
			} else {
			    // add close tags till top will have us
			    do {
				tokens.add(i++, top.closeTag());
System.err.println("  close first: "+top.closeTag());
				stack.pop();
				if (!stack.empty()) {
				    top = (TagToken) stack.peek();
				}
			    } while (!ds.canContain(top, tag) && !stack.empty());
			}
		    }
		    // new top
		    stack.push(tag);
		} else if (tag.isCloseTag()) {
		    if (! stackContainsOpenTag(stack, tag)) {
			// remove stray close tag in root
System.err.println("  remove stray close: "+tag);
			tokens.remove(i--);
		    } else if (!tag.isSameTag(top)) {
			if (!stack.empty()) {
			    // add close tags till top same tag
			    do {
				tokens.add(i++, top.closeTag());
System.err.println("  close also: "+top.closeTag());
				stack.pop();
				if (!stack.empty()) {
				    top = (TagToken) stack.peek();
				}
			    } while (!tag.isSameTag(top) && !stack.empty());

			    // keep close tag and remove top
			    if (!stack.empty()) {
				stack.pop();
			    }
			} else {
			    // illegal tag in root
System.err.println("  illegal in root: "+tok);
			    tokens.remove(i--);
			}
		    } else {
			stack.pop();
		    }
		}
	    }
	}
    }

    private static boolean stackContainsOpenTag(Stack stack, TagToken tag) {
	for (Iterator it = stack.iterator(); it.hasNext();) {
	    TagToken t = (TagToken) it.next();
	    if (t.isSameTag(tag)) {
		return true;
	    }
	}
	return false;
    }

    private void mergeAdjoinedText () {
	Token last = null;
	for (Iterator it = tokens.iterator(); it.hasNext();) {
	    Token tok = (Token) it.next();
	    if (tok instanceof TextToken && last instanceof TextToken) {
		it.remove();
		TextToken txt = (TextToken) tok;
		TextToken ltxt = (TextToken) last;
		ltxt.setData(ltxt.getData() + " " + txt.getData());
	    }
	    last = tok;
	}
    }

    /**
     * Debug..
     */
    public static void main (String[] args)
    throws Exception {
	InputStream in = new java.io.FileInputStream(args[0]);
	DocumentStructure ds = new TestDocumentStructure();
        XMLBS bs = new XMLBS(in, ds);
	bs.debug = true;
	bs.process();

	for (Iterator it = bs.tokens.iterator(); it.hasNext();) {
	    System.out.print(it.next().toString());
	}
    }
}
