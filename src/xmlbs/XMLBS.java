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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import xmlbs.tokens.CommentToken;
import xmlbs.tokens.TagToken;
import xmlbs.tokens.TextToken;
import xmlbs.tokens.Token;

/**
 * XML body shop tries to correct broken XML files. XMLBS is able to fix to
 * following problems:
 * <UL>
 * <LI>unquoted tag attributes</LI>
 * <LI>stray illegal characters like &lt; and &gt;</LI>
 * <LI>close unclosed tags trying to obey structure rules</LI>
 * <LI>fix tag overlap like <TT>&lt;i&gt; foo &lt;b&gt; bar &lt;/i&gt; boo
 * &lt;/b&gt;</TT></LI>
 * </UL>
 * 
 * @author R.W. van 't Veer
 * @version $Revision: 1.44 $
 */
public class XMLBS {
    /** input */
    private InputStream in = null;

    /** input */
    private String inStr = null;

    /** document structure */
    private DocumentStructure ds = null;

    /** annotate flag */
    private boolean annotate = false;

    /** Charset encoding of InputStream */
    private String encoding = null;

    /** token list */
    private List tokens = null;

    /** marker used for annotation */
    private static final String WARNING_MARKER = "XMLBS!";

    /** processed flag, set to true when processing finished */
    private boolean processed = false;

    /**
     * Construct a body shop instances for stream with structure descriptor.
     * 
     * @param in
     *            input stream
     * @param ds
     *            document structure descriptor
     * @throws IOException
     *             when reading from stream failed
     */
    public XMLBS(InputStream in, DocumentStructure ds) throws IOException {
        this(in, ds, null);
    }

    /**
     * Construct a body shop instances for stream with structure descriptor.
     * 
     * @param in
     *            input stream
     * @param ds
     *            document structure descriptor
     * @param encoding
     *            Charset encoding
     * @throws IOException
     *             when reading from stream failed
     */
    public XMLBS(InputStream in, DocumentStructure ds, String encoding)
            throws IOException {
        this.in = in;
        this.ds = ds;
        this.encoding = encoding;
    }

    /**
     * Construct a body shop instances for stream with structure descriptor.
     * 
     * @param in
     *            input stream
     * @param ds
     *            document structure descriptor
     * @throws IOException
     *             when reading from stream failed
     */
    public XMLBS(String in, DocumentStructure ds) throws IOException {
        this.inStr = in;
        this.ds = ds;
    }

    /**
     * Read and restructure data.
     * 
     * @throws IOException
     *             when reading from stream failed
     */
    public void process() throws IOException {
        // read tokens from stream
        tokenize();

        // remove unknown tags and unknown tag attributes
        cleanupTags();

        // reconstruct hierarchy
        hierarchy();

        // merge adjoined text tokens
        mergeAdjoinedText();

        processed = true;
    }

    /**
     * Write result data to stream.
     * 
     * @param out
     *            output stream
     * @throws IOException
     *             when writing to stream fails
     * @throws IllegalStateException
     *             when data not yet <a href="#process()">processed </a>.
     */
    public void write(OutputStream out) throws IOException,
            IllegalStateException {
        if (!processed) {
            throw new IllegalStateException();
        }

        for (Iterator it = tokens.iterator(); it.hasNext();) {
            Token tok = (Token) it.next();
            if (encoding != null) {
                out.write(tok.toString().getBytes(encoding));
            } else {
                out.write(tok.toString().getBytes());
            }
        }
        out.flush();
    }

    /**
     * @return true when annotation is configurated for this processor
     */
    public boolean getAnnotate() {
        return annotate;
    }

    /**
     * @param flag
     *            turn annotation on (<TT>true</TT>) or off
     */
    public void setAnnotate(boolean flag) {
        annotate = flag;
    }

    /**
     * @return String
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the Charset encoding.
     * 
     * @param encoding
     *            The encoding to set
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    // private stuff
    /**
     * Tokenize input stream.
     * 
     * @throws IOException
     *             when reading from stream failed
     */
    private void tokenize() throws IOException {
        Tokenizer tok = null;

        if (in != null) {
            Reader reader = null;
            if (encoding != null) {
                reader = new BufferedReader(new InputStreamReader(in, encoding));
            } else {
                reader = new BufferedReader(new InputStreamReader(in));
            }
            tok = new Tokenizer(reader, ds);
        } else {
            tok = new Tokenizer(inStr, ds);
        }
        tokens = tok.readAllTokens();
    }

    /**
     * Remove unknown tags and unknown tag attributes.
     */
    private void cleanupTags() {
        for (ListIterator it = tokens.listIterator(); it.hasNext();) {
            Token tok = (Token) it.next();
            if (tok instanceof TagToken) {
                TagToken tag = (TagToken) tok;
                if (!ds.isKnownTag(tag)) {
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

    /**
     * Verify and restructure tag hierarchy.
     */
    private void hierarchy() {
        TreeNode root = TreeBuilder.build(tokens);
        TreeBalancer.balance(root, ds);
        tokens = TreeBuilder.flatten(root, new ArrayList());
    }

    /**
     * Merge adjoined text blocks.
     */
    private void mergeAdjoinedText() {
        Token last = null;
        for (Iterator it = tokens.iterator(); it.hasNext();) {
            Token tok = (Token) it.next();
            if (tok instanceof TextToken && last instanceof TextToken) {
                it.remove();
                TextToken txt = (TextToken) tok;
                TextToken ltxt = (TextToken) last;
                ltxt.setData(ltxt.getData() + " " + txt.getData());
            } else {
                last = tok;
            }
        }
    }

    /**
     * Create comment token for annotation.
     * 
     * @param msg
     *            message
     * @param tok
     *            token to include
     * @return comment token for annotation
     */
    private static CommentToken comment(String msg, Token tok) {
        return new CommentToken(WARNING_MARKER + "(" + msg + ")" + tok);
    }
}