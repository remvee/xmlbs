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
 * @version $Revision: 1.27 $
 */
public class XMLBS {
    private InputStream in = null;
    private DocumentStructure ds = null;
    private List tokens = null;

    private boolean annotate = false;
    private static final String WARNING_MARKER = "XMLBS!";

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

	// reconstruct hierarchy
	hierarchy();

	// merge adjoined text tokens
	mergeAdjoinedText();

	// remove unknown entities
	// TODO
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
		    if (annotate) {
			it.set(comment("unknow tag", tag));
		    } else {
			it.remove();
		    }
		} else {
		    ds.retainKnownAttributes(tag);
		}
	    }
	}
    }

    private void hierarchy () {
	CrumbTrail trail = new CrumbTrail(ds);
	for (int i = 0; i < tokens.size(); i++) {
	    Token tok = (Token) tokens.get(i);
	    TagToken top = trail.getTop();

	    if (tok instanceof TextToken) {
		TextToken txt = (TextToken) tok;
		if (!txt.isWhiteSpace() && !ds.canContain(top, txt)) {
		    // remove stray text??
		    if (annotate) {
			tokens.set(i, comment("stray text", txt));
		    } else {
			tokens.remove(i--);
		    }
		}
	    } else if (tok instanceof TagToken) {
		TagToken tag = (TagToken) tok;
		if (tag.isOpenTag()) {
		    if (!ds.canContain(top, tag)) {
			if (!trail.hasContainerFor(tag)) {
			    // misplaced tag
			    if (annotate) {
				tokens.set(i, comment("misplaced tag", tag));
			    } else {
				tokens.remove(i--);
			    }
			} else {
			    // add close tags till top will have us
			    do {
				if (annotate) {
				    tokens.add(i++, comment("close first", top));
				}
				tokens.add(i++, top.closeTag());
				trail.pop();
				top = trail.getTop();
			    } while (!ds.canContain(top, tag) && trail.getDepth() > 0);
			    // new top
			    trail.push(tag);
			}
		    } else {
			// new top
			trail.push(tag);
		    }
		} else if (tag.isCloseTag()) {
		    if (!trail.hasOpenFor(tag)) {
			// remove stray close tag in root
			if (annotate) {
			    tokens.set(i, comment("remove close", tag));
			} else {
			    tokens.remove(i--);
			}
		    } else if (!tag.isSameTag(top)) {
			if (trail.getDepth() > 0) {
			    // add close tags till top same tag
			    do {
				if (annotate) {
				    tokens.add(i++, comment("close also", top));
				}
				tokens.add(i++, top.closeTag());
				trail.pop();
				top = trail.getTop();
			    } while (!tag.isSameTag(top) && trail.getDepth() > 0);

			    // keep close tag and remove top
			    trail.pop();
			} else {
			    // stray close
			    if (annotate) {
				tokens.set(i, comment("stray close", tag));
			    } else {
				tokens.remove(i--);
			    }
			}
		    } else {
			// keep close tag and remove top
			trail.pop();
		    }
		}
	    }
	}
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

    private CommentToken comment (String msg, Token loc) {
	return new CommentToken(WARNING_MARKER + "(" + msg + ")" + loc);
    }

    class CrumbTrail {
	private List trail = new Vector();
	private DocumentStructure ds = null;

	public CrumbTrail (DocumentStructure ds) {
	    this.ds = ds;
	}

	public TagToken getTop () {
	    return (TagToken) (trail.size() == 0 ? null : trail.get(0));
	}

	public void push (TagToken tok) {
	    trail.add(0, tok);
	}

	public TagToken pop () {
	    return (TagToken) (trail.size() == 0 ? null : trail.remove(0));
	}

	public int getDepth () {
	    return trail.size();
	}

	public boolean hasOpenFor (TagToken tag) {
	    for (Iterator it = trail.iterator(); it.hasNext();) {
		TagToken t = (TagToken) it.next();
		if (t.isSameTag(tag)) {
		    return true;
		}
	    }
	    return false;
	}

	public boolean hasContainerFor (TagToken tag) {
	    for (Iterator it = trail.iterator(); it.hasNext();) {
		TagToken t = (TagToken) it.next();
		if (ds.canContain(t, tag)) {
		    return true;
		}
	    }
	    return false;
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
	bs.annotate = true;
	bs.process();

	for (Iterator it = bs.tokens.iterator(); it.hasNext();) {
	    System.out.print(it.next().toString());
	}
    }
}
