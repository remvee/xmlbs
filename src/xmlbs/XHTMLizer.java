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

import gnu.getopt.Getopt;

/**
 * Commandline tool to help translate html4 to xhtml.
 *
 * @author R.W. van 't Veer
 * @version $Revision: 1.2 $
 */
public class XHTMLizer {

    private XMLBS bs = null;

    public XHTMLizer (InputStream in)
    throws IOException {
	Properties prop = new Properties();
	prop.load(getClass().getClassLoader().getResourceAsStream("xmlbs/HTML.properties"));
	DocumentStructure ds = new PropertiesDocumentStructure(prop);

	bs = new XMLBS(in, ds);
    }

    public void setAnnotate (boolean flag) {
	bs.setAnnotate(flag);
    }
    public void process ()
    throws IOException {
	bs.process();
    }
    public void write (OutputStream out)
    throws IOException {
	bs.write(out);
    }

    /**
     * Commandline interface.
     * @param args commandline arguments, 1st used as input filename
     * @throws Exception when anything goes wrong..
     */
    public static void main (String[] args)
    throws Exception {
	boolean annotate = false;
	int argn = 0;
	{
	    String progname = XHTMLizer.class.getName();
	    Getopt opt = new Getopt(progname, args, "a");
	    int c;
	    while ((c = opt.getopt()) != -1) {
		switch (c) {
		    case 'a':
			annotate = true;
			break;
		    default:
		    case '?':
			usage(progname);
			System.exit(-1);
			break; // getopt already printed an error
		}
	    }
	    argn = opt.getOptind();

	    if (args.length-1 != argn) {
		usage(progname);
		System.exit(-1);
	    }
	}

	InputStream in = new FileInputStream(args[argn]);
	XHTMLizer xhtmlizer = new XHTMLizer(in);
	xhtmlizer.setAnnotate(annotate);
	xhtmlizer.process();
	xhtmlizer.write(System.out);
    }

    public static void usage (String progname) {
	System.err.println("java "+progname+" [-a] FILE");
    }
}
