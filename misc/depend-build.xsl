<?xml version="1.0"?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="package">
	<xsl:variable name="name" select="@name"/>
	<xsl:variable name="class" select="class"/>
	<xsl:variable name="baseurl" select="baseurl"/>
	<xsl:variable name="jar" select="jar"/>
	<xsl:choose>
	    <xsl:when test="@packaged = 'true'">
		<xsl:variable name="zip" select="zip"/>
		<target name="{$name}.prepare">
		    <available property="{$name}.inclasspath" classname="{$class}">
			<classpath refid="build.classpath"/>
		    </available>
		    <available property="{$name}.downloaded" file="${{download.dir}}/{$zip}"/>
		</target>
		<target name="{$name}.download" depends="{$name}.prepare" unless="{$name}.downloaded">
		    <get src="{$baseurl}/{$zip}" dest="${{download.dir}}/{$zip}"/>
		</target>
		<target name="{$name}" depends="{$name}.prepare" unless="{$name}.inclasspath">
		    <antcall target="{$name}.download"/>
		    <unzip src="${{download.dir}}/{$zip}" dest="${{extract.dir}}/{$name}"/>
		    <copy file="${{extract.dir}}/{$name}/{$jar}" todir="${{lib.dir}}"/>
		</target>
	    </xsl:when>
	    <xsl:otherwise>
		<target name="{$name}.prepare">
		    <available property="{$name}.inclasspath" classname="{$class}">
			<classpath refid="build.classpath"/>
		    </available>
		    <available property="{$name}.downloaded" file="${{download.dir}}/{$jar}"/>
		</target>
		<target name="{$name}.download" depends="{$name}.prepare" unless="{$name}.downloaded">
		    <get src="{$baseurl}/{$jar}" dest="${{download.dir}}/{$jar}"/>
		</target>
		<target name="{$name}" depends="{$name}.prepare" unless="{$name}.inclasspath">
		    <antcall target="{$name}.download"/>
		    <copy file="${{download.dir}}/{$jar}" todir="${{lib.dir}}"/>
		</target>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <xsl:template match="/">
	<project default="all" basedir=".">
	    <property name="work.dir" value="depend"/>
	    <property name="download.dir" value="${{work.dir}}"/>
	    <property name="extract.dir" value="${{work.dir}}"/>
	    <property name="lib.dir" value="lib"/>
	    <path id="build.classpath">
		<pathelement path="${{classpath}}"/>
		<fileset dir="${{lib.dir}}">
		    <include name="**/*.jar"/>
		</fileset>
	    </path>
	    <xsl:for-each select="dependencies/package">
		<xsl:apply-templates select="."/>
	    </xsl:for-each>
	    <target name="all">
		<xsl:for-each select="dependencies/package">
		    <xsl:variable name="name" select="@name"/>
		    <antcall target="{$name}"/>
		</xsl:for-each>
	    </target>
	</project>
    </xsl:template>

</xsl:transform>
