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
 * @version $Revision: 1.3 $
 */
public class XHTMLizer {
    /**
     * Commandline interface.
     * @param args commandline arguments, 1st used as input filename
     * @throws Exception when anything goes wrong..
     */
    public static void main (String[] args)
    throws Exception {
	boolean annotate = false;
	boolean icase = false;
	int argn = 0;
	{
	    String progname = XHTMLizer.class.getName();
	    Getopt opt = new Getopt(progname, args, "ai");
	    int c;
	    while ((c = opt.getopt()) != -1) {
		switch (c) {
		    case 'a':
			annotate = true;
			break;
		    case 'i':
			icase = true;
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

	// prepare document structure
	Properties prop = new Properties();
	prop.load(ClassLoader.getSystemResourceAsStream("xmlbs/HTML.properties"));
	DocumentStructure ds = new PropertiesDocumentStructure(prop);
	ds.setIgnoreCase(icase);

	XMLBS bs = new XMLBS(new FileInputStream(args[argn]), ds);
	bs.setAnnotate(annotate);
	bs.process();
	bs.write(System.out);
    }

    public static void usage (String progname) {
	System.err.println("java "+progname+" [-ai] FILE");
    }
}
