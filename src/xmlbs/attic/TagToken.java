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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Tag token.  Represents an XML element.
 *
 * @see <A href="http://www.w3.org/TR/REC-xml#sec-logical-struct">XML: Logical Structures</A>
 * @author R.W. van 't Veer
 * @version $Revision: 1.8 $
 */
public class TagToken implements Token {
    /** tag name */
    private String tagName;
    /** map of tag attributes */
    private Map attrs = new HashMap();
    /** document structure this tag lives in */
    private DocumentStructure ds = null;

    /** type of tag */
    private int type;
    /** open tag type constant */
    public static final int OPEN = 0;
    /** close tag type constant */
    public static final int CLOSE = 1;
    /** empty tag type constant */
    public static final int EMPTY = 2;

    /** regular expression to match close tags */
    private static final Pattern closePat;
    /** regular expression to match empty tags */
    private static final Pattern emptyPat;
    /** regular expression to match tag name */
    private static final Pattern namePat;
    /** regular expression to match attribute name */
    private static final Pattern attPat;
    /** regular expression to match single-quoted attribute value */
    private static final Pattern valPat1;
    /** regular expression to match double-quoted attribute value */
    private static final Pattern valPat2;
    /** regular expression to match unquoted attribute value */
    private static final Pattern valPat3;

    static {
        try {
            closePat = Pattern.compile("^\\s*/");
            emptyPat = Pattern.compile("/\\s*$");
            namePat = Pattern.compile("([a-zA-Z_:][a-zA-Z0-9._:-]*)\\s*");
            attPat = Pattern.compile("\\s([a-zA-Z_:][a-zA-Z0-9]*)\\s*=");
            valPat1 = Pattern.compile("^\\s*=\\s*'([^']*)'");
            valPat2 = Pattern.compile("^\\s*=\\s*\"([^\"]*)\"");
            valPat3 = Pattern.compile("^\\s*=\\s*([^\\s]*)");
        } catch (PatternSyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @param raw tag text without &lt; and &gt;
     */
    public TagToken (String raw, DocumentStructure ds) {
	this.ds = ds;

        // determine tag type
        {
            if (closePat.matcher(raw).find()) {
                type = CLOSE;
            } else if (emptyPat.matcher(raw).find()) {
                type = EMPTY;
            } else {
                type = OPEN;
	    }
        }

        // determine tag name
        {
            Matcher ma = namePat.matcher(raw);
            ma.find(); // TODO assert a match is found
            tagName = ma.group(1);

	    if (ds.getIgnoreCase()) {
		String t = ds.getTagName(tagName);
		tagName = t == null ? tagName : t;
	    }
        }

        // collect attributes
        {
            int pos = 0;
            for (Matcher ma = attPat.matcher(raw); ma.find(pos);) {
                String attr = ma.group(1);
                pos = ma.end(1);
                String valStr = raw.substring(pos);

                String val = null;
                Matcher valMa = null;
                Matcher valMa1 = valPat1.matcher(valStr);
                Matcher valMa2 = valPat2.matcher(valStr);
                Matcher valMa3 = valPat3.matcher(valStr);
                if (valMa1.find()) {
                    valMa = valMa1;
                } else if (valMa2.find()) {
                    valMa = valMa2;
                } else if (valMa3.find()) {
                    valMa = valMa3;
                } else {
                    break;
		}
                val = valMa.group(1);
                pos += valMa.end(1);

		if (ds.getIgnoreCase()) {
		    String t = ds.getTagAttribute(tagName, attr);
		    attr = t == null ? attr : t;
		}
                attrs.put(attr, val);
            }
        }
    }

    /**
     * @param tagName tag name
     * @param attrs map of attributes
     * @param type type of tag
     * @see #OPEN
     * @see #CLOSE
     * @see #EMPTY
     */
    public TagToken (String tagName, Map attrs, int type) {
        this.tagName = tagName;
        this.attrs = attrs;
        this.type = type;
    }

    /**
     * @return empty version of this tag
     */
    public TagToken emptyTag () {
        TagToken tok = new TagToken(tagName, attrs, EMPTY);
        return tok;
    }

    /**
     * @return closing version of this tag
     */
    public TagToken closeTag () {
        TagToken tok = new TagToken(tagName, attrs, CLOSE);
        return tok;
    }

    /**
     * @return tag name
     */
    public String getName () {
        return tagName;
    }

    /**
     * @return map of tag attributes
     */
    public Map getAttributes () {
        return attrs;
    }

    /**
     * @return true if this is a open tag
     * @see #OPEN
     */
    public boolean isOpenTag () {
        return type == OPEN;
    }

    /**
     * @return true if this is a close tag
     * @see #CLOSE
     */
    public boolean isCloseTag () {
        return type == CLOSE;
    }

    /**
     * @return true if this is a empty tag
     * @see #EMPTY
     */
    public boolean isEmptyTag () {
        return type == EMPTY;
    }

    /**
     * @param tag token to compare to
     * @return true if name of given tag matches
     */
    public boolean isSameTag (TagToken tag) {
        return tagName.equals(tag.getName());
    }

    /**
     * @return proper string representation of this tag
     */
    public String toString () {
        StringBuffer sb = new StringBuffer();

        sb.append('<');

        if (type == CLOSE) {
            sb.append('/');
            sb.append(tagName);
            sb.append('>');

            return sb.toString();
        }

        // else OPEN or EMPTY
        sb.append(tagName);

        if (attrs != null) {
            List l = new ArrayList(attrs.keySet());
            Collections.sort(l);
            Iterator it = l.iterator();
            while (it.hasNext()) {
                String attr = (String) it.next();
                String val = (String) attrs.get(attr);

                sb.append(' ');
                sb.append(attr);
                sb.append('=');
                sb.append('"');
                sb.append(TextToken.fixText(val, ds));
                sb.append('"');
            }
        }

        if (type == EMPTY) {
            sb.append('/');
	}
        sb.append('>');

        return sb.toString();
    }
}
