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

import org.apache.regexp.*;

public class Tokenizer
{
    Token holdBack = null;
    InputStream in = null;

    final static int BUFFER_SIZE = 4096;

    public Tokenizer (InputStream in)
    {
	this.in = new BufferedInputStream(in);
    }

    public Token readToken ()
    throws IOException
    {
	if (holdBack != null)
	{
	    Token tok = holdBack;
	    holdBack = null;
	    return tok;
	}

	StringBuffer sb = new StringBuffer();
	for (int c; (c = in.read()) != -1; )
	{
	    if (c == '<')
	    {
		in.mark(BUFFER_SIZE);
		c = in.read();
		Token tok = null;

		// tag?
		if (c == '/' || Character.isLetter((char) c))
		{
		    tok = readTagToken((char) c);
		}
		// special?
		else if (c == '?')
		{
		    tok = readSpecialToken();
		}
		// declaration?
		else if (c == '!')
		{
		    tok = readDeclToken();
		}

		// succeeded in reading a token?
		if (tok != null)
		{
		    String t = sb.toString();
		    if (t.length() > 0)
		    {
			// hold back token and return text token first
			holdBack = tok;
			return new TextToken(t);
		    }
		    return tok;
		}

		// stray < character
		sb.append('<');
		in.reset();
	    }
	    else
	    {
		sb.append((char) c);
	    }
	}

	String t = sb.toString();
	return (t.length() > 0) ? new TextToken(t) : null;
    }

    private Token readTagToken (char firstChar)
    throws IOException
    {
	StringBuffer sb = new StringBuffer();
	sb.append(firstChar);
	for (int c; (c = in.read()) != -1; )
	{
	    if (c == '<')
	    {
		// oeps this can't be a tag..
		return null;
	    }
	    if (c == '>')
	    {
		// tag body read
		break;
	    }
	    sb.append((char) c);
	}

	String raw = sb.toString();
	return raw.length() > 0 ? new TagToken(raw) : null;
    }

    private Token readSpecialToken ()
    throws IOException
    {
	return null;
    }

    private Token readDeclToken ()
    throws IOException
    {
	int c = in.read();

	if (c == '-') // comment?
	{
	    c = in.read();
	    if (c != '-') return null;

	    // try to find end of comment marker "-->"
	    StringBuffer sb = new StringBuffer();
	    while ((c = in.read()) != -1)
	    {
		sb.append((char) c);

		if (c == '>' && sb.toString().endsWith("-->"))
		{
		    break;
		}
	    }

	    // unterminated comment != comment
	    if (c == -1) return null;

	    String t = sb.toString();
	    return new CommentToken(t.substring(0, t.length()-3));
	}
	else if (c == '[') // cdata section?
	{
	}

	return null;
    }
}