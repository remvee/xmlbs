<?xml version="1.0"?>

<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>

    <xsl:template match="title"/>

    <xsl:template match="para">
	<P>
	    <xsl:value-of select="." disable-output-escaping="yes"/>
	</P>
    </xsl:template>

    <xsl:template match="release">
	<P>
	    <xsl:variable name="filebase" select="@filebase"/>
	    <A href="{$filebase}.zip"><xsl:value-of select="@filebase"/>.zip</A>
	    <BR />
	    <xsl:value-of select="." disable-output-escaping="yes"/>
	</P>
    </xsl:template>

    <xsl:template match="changelog">
	<xsl:variable name="file" select="@file"/>
	<xsl:variable name="max" select="@max"/>
	<xsl:variable name="dist" select="@dist"/>
	<P>
	    <xsl:choose>
		<xsl:when test="$dist != ''">
		    Last <xsl:value-of select="@max"/>
		    <xsl:text> </xsl:text>
		    <a href="{$dist}">changelog</a> messages.
		</xsl:when>
		<xsl:otherwise>
		    Last <xsl:value-of select="@max"/> changelog messages.
		</xsl:otherwise>
	    </xsl:choose>
	    <UL>
		<xsl:for-each select="document($file)/changelog/entry">
		    <xsl:if test="position() &lt;= $max">
			<xsl:apply-templates select="."/>
		    </xsl:if>
		</xsl:for-each>
	    </UL>
	</P>
    </xsl:template>

    <!-- changelog entries -->
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
			<LI><xsl:value-of select="."/></LI>
		    </xsl:for-each>
		</UL>
	    </P>
	</LI>
    </xsl:template>

    <xsl:template match="/">
	<xsl:variable name="location" select="/homepage/location"/>
	<HTML>
	    <HEAD>
		<TITLE><xsl:value-of select="/homepage/title"/></TITLE>
	    </HEAD>

	    <BODY bgcolor="white">
		<H1><xsl:value-of select="/homepage/title"/></H1>
		<P>
		    <xsl:value-of select="/homepage/description"/>
		</P>

		<!-- menu -->
		<UL>
		    <xsl:for-each select="/homepage/section">
			<xsl:variable name="anchor"><xsl:number/></xsl:variable>
			<LI><A name="_{$anchor}" href="#{$anchor}"><xsl:value-of select="title"/></A></LI>
		    </xsl:for-each>
		</UL>
		<HR/>

		<xsl:for-each select="/homepage/section">
		    <xsl:variable name="anchor"><xsl:number/></xsl:variable>
		    <H2><A name="{$anchor}" href="#_{$anchor}"><xsl:value-of select="title"/></A></H2>
		    <xsl:for-each select=".">
			<xsl:apply-templates select="."/>
		    </xsl:for-each>
		</xsl:for-each>
		<HR/>

		<DIV align="right">
		    $Date: 2002/10/26 22:06:07 $
		</DIV>
	    </BODY>
	</HTML>
    </xsl:template>

</xsl:transform>
