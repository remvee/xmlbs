<?xml version="1.0"?>

<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>

    <xsl:template match="entry">
	<LI>
	    <xsl:value-of select="date"/>
	    <xsl:text> </xsl:text>
	    <xsl:value-of select="time"/>
	    by <xsl:value-of select="author"/>

	    <P>
		<PRE><xsl:for-each select="./msg"><xsl:value-of select="."/></xsl:for-each></PRE>

		<UL>
		    <xsl:for-each select="./file/name">
			<xsl:variable name="file" select="."/>
			<LI>
			    <A href="{$file}">
				<xsl:value-of select="."/>
			    </A>
			</LI>
		    </xsl:for-each>
		</UL>
	    </P>
	</LI>
    </xsl:template>

    <xsl:template match="/">
	<HTML>
	  <HEAD><TITLE>scilla changelog</TITLE></HEAD>
	  <BODY>

	    <H2>scilla changelog</H2>
	    <HR/>
	    <UL>
		<xsl:for-each select="/changelog/entry">
		    <xsl:apply-templates select="."/>
		</xsl:for-each>
	    </UL>
	    <HR/>

	  </BODY>
	</HTML>
    </xsl:template>

</xsl:transform>
