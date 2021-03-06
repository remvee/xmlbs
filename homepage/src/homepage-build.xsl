<?xml version="1.0"?>

<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="xml" indent="yes"/>

	<xsl:template match="/">
		<project default="go" basedir=".">
			<property name="cvsroot" value="/home/remco/cvsroot"/>
			<property name="cvsprefix" value="projects"/>
			<property name="xmlbs.project" value="projects/xmlbs"/>
			<property name="build.dir" value="build"/>
			<target name="go">
				<mkdir dir="${{build.dir}}"/>
				<xsl:for-each select="homepage/section/oldrelease">
					<xsl:variable name="tag" select="@tag"/>
					<xsl:variable name="filebase" select="@filebase"/>
					<mkdir dir="${{build.dir}}/{$tag}"/>
					<cvs cvsRoot="${{cvsroot}}" dest="${{build.dir}}/{$tag}"
						tag="{$tag}" package="${{xmlbs.project}}"/>
					<zip zipfile="${{build.dir}}/{$filebase}.zip"
						basedir="${{build.dir}}/{$tag}/${{cvsprefix}}" excludes="**/CVS"/>
					<delete dir="${{build.dir}}/{$tag}"/>
				</xsl:for-each>
				<xsl:for-each select="homepage/section/release">
					<xsl:variable name="tag" select="@tag"/>
					<xsl:variable name="filebase" select="@filebase"/>
					<mkdir dir="${{build.dir}}/{$tag}"/>
					<cvs cvsRoot="${{cvsroot}}" dest="${{build.dir}}/{$tag}"
						tag="{$tag}" package="${{xmlbs.project}}"/>
					<ant dir="${{build.dir}}/{$tag}/${{xmlbs.project}}" target="jar"/>
					<copy file="${{build.dir}}/{$tag}/${{xmlbs.project}}/xmlbs.jar" tofile="${{build.dir}}/{$filebase}.jar"/>
					<delete dir="${{build.dir}}/{$tag}"/>
				</xsl:for-each>
			</target>
		</project>
	</xsl:template>

</xsl:transform>
