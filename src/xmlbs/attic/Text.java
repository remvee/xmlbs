package xmlbs;

class Text
{
    String txt;

    Text (String txt)
    {
	this.txt = XMLBS.fixText(txt);
    }

    public String toString ()
    {
	return txt;
    }
}
