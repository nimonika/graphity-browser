<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of Graphity SemanticReports package.
Copyright (C) 2009-2011  Martynas Jusevičius

SemanticReports is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
-->
<!DOCTYPE uridef[
        <!ENTITY rep "http://www.semantic-web.dk/ontologies/semantic-reports/">
	<!ENTITY vis "http://code.google.com/apis/visualization/">
        <!ENTITY spin "http://spinrdf.org/sp#">
	<!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
	<!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
	<!ENTITY owl "http://www.w3.org/2002/07/owl#">
	<!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
	<!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
	<!ENTITY geo "http://www.w3.org/2003/01/geo/wgs84_pos#">
        <!ENTITY dc "http://purl.org/dc/elements/1.1/">
	<!ENTITY foaf "http://xmlns.com/foaf/0.1/">
	<!ENTITY sioc "http://rdfs.org/sioc/ns#">
        <!ENTITY skos "http://www.w3.org/2004/02/skos/core#">
        <!ENTITY dbpedia "http://dbpedia.org/resource/">
        <!ENTITY dbpedia-owl "http://dbpedia.org/ontology/">
        <!ENTITY dbpprop "http://dbpedia.org/property/">
        <!ENTITY category "http://dbpedia.org/resource/Category:">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:owl="&owl;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:xsd="&xsd;"
xmlns:sparql="&sparql;"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:id="java:util.IDGenerator"
exclude-result-prefixes="#all">

	<xsl:import href="../sparql2google-wire.xsl"/>

	<xsl:include href="../FrontEndView.xsl"/>

	<xsl:param name="visualizations-json" as="xs:string"/>
	<xsl:param name="bindings-json" as="xs:string"/>
	<xsl:param name="variables-json" as="xs:string"/>

	<xsl:param name="visualization-types-json" as="xs:string"/>
	<xsl:param name="binding-types-json" as="xs:string"/>
	<xsl:param name="data-types-json" as="xs:string"/>
	<xsl:param name="option-types-json" as="xs:string" select="'[]'"/>

	<xsl:variable name="report" select="document('arg://report')" as="document-node()"/>
	<xsl:variable name="report-uri" select="$report//sparql:binding[@name = 'report']/sparql:uri" as="xs:anyURI"/>
        <xsl:variable name="visualizations" select="document('arg://visualizations')" as="document-node()"/>
	<xsl:variable name="bindings" select="document('arg://bindings')" as="document-node()"/>
	<xsl:variable name="variables" select="document('arg://variables')" as="document-node()"/>
	<xsl:variable name="options" select="document('arg://options')" as="document-node()?"/>
	<xsl:variable name="query-uris" select="document('arg://query-uris')" as="document-node()"/>
        <!-- <xsl:variable name="binding-types" select="document('arg://binding-types')" as="document-node()"/> -->
        <xsl:variable name="comments" select="document('arg://comments')" as="document-node()"/>

        <xsl:key name="binding-type-by-vis-type" match="sparql:result" use="sparql:binding[@name = 'visType']/sparql:uri"/>
        <xsl:key name="result-by-visualization" match="sparql:result" use="sparql:binding[@name = 'visualization']/sparql:uri"/>

	<xsl:template name="title">
		<xsl:value-of select="$report//sparql:binding[@name = 'title']/sparql:literal"/>
	</xsl:template>

	<xsl:template name="head">
            <xsl:call-template name="report-scripts"/>
        </xsl:template>


	<xsl:template name="body-onload">
            <xsl:attribute name="onload">
		    <xsl:text>Report.init(</xsl:text>
                    <xsl:value-of select="$visualization-types-json"/>
		    <xsl:text>, </xsl:text>
                    <xsl:value-of select="$binding-types-json"/>
		    <xsl:text>, </xsl:text>
		    <xsl:value-of select="$data-types-json"/>
		    <xsl:text>, </xsl:text>
		    <xsl:value-of select="$option-types-json"/>
		    <xsl:text>); report = new Report(table, </xsl:text>
		    <xsl:value-of select="$visualizations-json"/>
		    <xsl:text>, </xsl:text>
		    <xsl:value-of select="$bindings-json"/>
		    <xsl:text>, </xsl:text>
		    <xsl:value-of select="$variables-json"/>
		    <xsl:text>, [</xsl:text>
		    <!-- <xsl:apply-templates select="$options//sparql:result" mode="option-json"/> -->
		    <xsl:text>], [</xsl:text>
                    <xsl:for-each select="$visualizations//sparql:result">
			<xsl:text>{ 'element' :</xsl:text>
			<xsl:text>document.getElementById('</xsl:text>
			<xsl:value-of select="generate-id()"/>
			<xsl:text>-visualization')</xsl:text>
			<xsl:text>, 'visType' : '</xsl:text>
			<xsl:value-of select="sparql:binding[@name = 'type']/sparql:uri"/>
			<xsl:text>' }</xsl:text>
			<xsl:if test="position() != last()">,</xsl:if>
		    </xsl:for-each>
		    <xsl:text>]); report.show();</xsl:text>
            </xsl:attribute>
        </xsl:template>
        
	<xsl:template name="content">
		<div id="main">
			<h2><xsl:call-template name="title"/></h2>

			<xsl:copy-of select="$report"/>
                        <!-- <xsl:copy-of select="$results"/> -->
			<!-- <xsl:copy-of select="$visualizations"/> -->
                        <!--
			<xsl:copy-of select="$query-objects"/>
			<xsl:copy-of select="$bindings"/>
                        <xsl:copy-of select="$variables"/>
                        -->

			<dl>
				<dt>Endpoint</dt>
				<dd>
					<a href="{$report//sparql:binding[@name = 'endpoint']/sparql:uri}">
                                            <xsl:choose>
                                                <xsl:when test="$report//sparql:binding[@name = 'endpointTitle']">
                                                    <xsl:value-of select="$report//sparql:binding[@name = 'endpointTitle']/sparql:literal"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="$report//sparql:binding[@name = 'endpoint']/sparql:uri"/>
                                                </xsl:otherwise>
                                            </xsl:choose>
					</a>
					(<a href="{$report//sparql:binding[@name = 'endpoint']/sparql:uri}?query={encode-for-uri($report//sparql:binding[@name = 'queryString']/sparql:literal)}">with query</a>)
				</dd>
                                <xsl:if test="$query-uris//sparql:binding[@name = 'uri']/sparql:uri">
                                    <dt>Used URIs</dt>
                                    <xsl:apply-templates select="$query-uris//sparql:result" mode="uri-list-item"/>
                                </xsl:if>
                                <dt>Created by</dt>
				<dd>
					<a href="{$report//sparql:binding[@name = 'creator']/sparql:uri}">
                                            <xsl:value-of select="$report//sparql:binding[@name = 'creatorName']/sparql:literal"/>
					</a>
				</dd>
				<dt>Created</dt>
				<dd>
                                        <xsl:value-of select="$report//sparql:binding[@name = 'dateCreated']/sparql:literal"/>
				</dd>
                                <xsl:if test="$report//sparql:binding[@name = 'dateModified']/sparql:literal">
                                    <dt>Modified</dt>
                                    <dd>
                                            <xsl:value-of select="$report//sparql:binding[@name = 'dateModified']/sparql:literal"/>
                                    </dd>
                                </xsl:if>
                                <xsl:if test="$report//sparql:binding[@name = 'description']/sparql:literal">
                                    <dt>Description</dt>
                                    <dd>
                                            <xsl:value-of select="$report//sparql:binding[@name = 'description']/sparql:literal"/>
                                    </dd>
                                </xsl:if>
                        </dl>

			<form action="{$resource//sparql:binding[@name = 'resource']/sparql:uri}" method="get" accept-charset="UTF-8">
				<p>
					<button type="submit" name="view" value="update">Edit</button>
				</p>
			</form>

			<xsl:apply-templates select="$visualizations//sparql:result" mode="vis-container"/>

			<h3 id="comments">Comments</h3>
                        <form action="{$report-uri}#comments" method="post" accept-charset="UTF-8">
                            <xsl:variable name="comment-uri" select="xs:anyURI(concat($report-uri, '#', id:generate()))" as="xs:anyURI"/>
<input type="hidden" name="rdf"/>
<input type="hidden" name="n" value="rdf"/>
<input type="hidden" name="v" value="&rdf;"/>

<input type="hidden" name="su" value="{$report-uri}"/>
<input type="hidden" name="pu" value="&sioc;container_of"/>
<input type="hidden" name="ou" value="{$comment-uri}"/>
<input type="hidden" name="su" value="{$comment-uri}"/>
<input type="hidden" name="pu" value="&rdf;type"/>
<input type="hidden" name="ou" value="&sioc;Post"/>
<input type="hidden" name="pu" value="&sioc;content"/>

                                <p>
                                        <textarea name="ol"></textarea>
                                        <br/>
					<button type="submit" name="action" value="comment">Comment</button>
				</p>
                                <xsl:if test="$comments//sparql:result">
                                    <ul>
                                        <xsl:apply-templates select="$comments//sparql:result" mode="comment"/>
                                    </ul>
                                </xsl:if>
			</form>
                </div>
	</xsl:template>

        <xsl:template match="sparql:result[sparql:binding[@name = 'visualization']]" mode="vis-container">
            <div id="{generate-id()}-visualization" class="visualization">&#160;</div>
        </xsl:template>

        <xsl:template match="sparql:result[sparql:binding[@name = 'uri']]" mode="uri-list-item">
            <xsl:variable name="uri" select="sparql:binding[@name = 'uri']/sparql:uri" as="xs:anyURI"/>
            <xsl:variable name="uri-label" as="xs:string">
                <xsl:choose>
                    <xsl:when test="starts-with($uri, '&rdf;')">
                        <xsl:value-of select="concat('rdf:', substring-after($uri, '&rdf;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&rdfs;')">
                        <xsl:value-of select="concat('rdfs:', substring-after($uri, '&rdfs;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&owl;')">
                        <xsl:value-of select="concat('owl:', substring-after($uri, '&owl;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&dc;')">
                        <xsl:value-of select="concat('dc:', substring-after($uri, '&dc;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&foaf;')">
                        <xsl:value-of select="concat('foaf:', substring-after($uri, '&foaf;'))"/>
                    </xsl:when>
		    <xsl:when test="starts-with($uri, '&skos;')">
                        <xsl:value-of select="concat('skos:', substring-after($uri, '&skos;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&sioc;')">
                        <xsl:value-of select="concat('sioc:', substring-after($uri, '&sioc;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&geo;')">
                        <xsl:value-of select="concat('geo:', substring-after($uri, '&geo;'))"/>
                    </xsl:when>
		    <xsl:when test="starts-with($uri, '&category;')">
                        <xsl:value-of select="concat('category:', substring-after($uri, '&category;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&dbpedia;')">
                        <xsl:value-of select="concat('dbpedia:', substring-after($uri, '&dbpedia;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&dbpedia-owl;')">
                        <xsl:value-of select="concat('dbpedia-owl:', substring-after($uri, '&dbpedia-owl;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&dbpprop;')">
                        <xsl:value-of select="concat('dbpprop:', substring-after($uri, '&dbpprop;'))"/>
                    </xsl:when>
		    <xsl:otherwise>
                        <xsl:value-of select="$uri"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

	    <dd>
                <a href="{$uri}">
                    <xsl:value-of select="$uri-label"/>
                </a>
            </dd>
        </xsl:template>
        
        <xsl:template match="sparql:result[sparql:binding[@name = 'comment']]" mode="comment">
            <li>
                <xsl:value-of select="sparql:binding[@name = 'dateCreated']"/>
                <br/>
                <xsl:value-of select="sparql:binding[@name = 'content']"/>
            </li>
        </xsl:template>

</xsl:stylesheet>