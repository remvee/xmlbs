<?xml version="1.0"?>

<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
	<project default="go" basedir=".">
	    <property name="cvsroot" value="/home/remco/cvsroot"/>
	    <property name="cvsprefix" value="projects"/>
	    <property name="scilla.project" value="projects/scilla"/>
	    <property name="build.dir" value="build"/>
	    <target name="go">
		<mkdir dir="${{build.dir}}"/>
		<xsl:for-each select="homepage/section/release">
		    <xsl:variable name="tag" select="@tag"/>
		    <xsl:variable name="filebase" select="@filebase"/>
		    <mkdir dir="${{build.dir}}/{$tag}"/>
		    <cvs cvsRoot="${{cvsroot}}" dest="${{build.dir}}/{$tag}"
			    tag="{$tag}" package="${{scilla.project}}"/>
		    <zip zipfile="${{build.dir}}/{$filebase}.zip"
			    basedir="${{build.dir}}/{$tag}/${{cvsprefix}}" excludes="**/CVS"/>
		    <delete dir="${{build.dir}}/{$tag}"/>
		</xsl:for-each>
	    </target>
	</project>
    </xsl:template>

</xsl:transform>
