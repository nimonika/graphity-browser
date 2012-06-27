Description
===========

Graphity Browser is a fully extensible generic Linked Data platform for building end-user Web applications.
It can be used for exploration and browsing of remote datasources, publishing and analysis of open data, as well as import and integration of private user data.

Building a data-intensive Web application on Graphity Browser is as simple as overriding generic stylesheets with own layout and defining necessary queries.
The platform supports standard RDF access methods such as Linked Data and SPARQL endpoints, and includes plugin mechanisms for importing file formats and APIs as RDF.

Installation
============

Graphity Browser is a [Maven Web application](http://maven.apache.org/guides/mini/guide-webapp.html).
Maven dependencies are discovered automatically from `pom.xml`, others (such as [SPIN API](http://topbraid.org/spin/api/)) are included as `.jar` files in the `/lib` folder (and can be "installed locally" using Maven).

Structure
=========

Java
----

* `org.graphity`: Classes shared by all Graphity applications
    * `org.graphity.adapter`: [`DatasetAdapter`](http://jena.apache.org/documentation/javadoc/fuseki/org/apache/jena/fuseki/http/DatasetAdapter.html)-related wrappers for Model caching via Graph store protocol
    * `org.graphity.browser`: Classes shared by all Graphity Browser applications
        * `org.graphity.browser.Application`: Entry point to the Browser webapp. [JAX-RS](http://docs.oracle.com/javaee/6/tutorial/doc/giepu.html) Resources, [`Provider`s](http://jackson.codehaus.org/javadoc/jax-rs/1.0/javax/ws/rs/ext/Providers.html), and configuration is initialized here
        * `org.graphity.browser.Resource`: Base class for all Browser Resources and apps built on it. Subclass of ``LinkedDataResourceImpl``.
        * `org.graphity.browser.SPARQLResource`: JAX-RS Resource for SPARQL endpoint/editor
        * `org.graphity.browser.SearchResource`: JAX-RS Resource for search/autocomplete (_unfinished_)
        * `org.graphity.browser.locator`: Pluggable classes for [GRDDL](http://www.w3.org/TR/grddl/) import of 3rd party REST APIs and XML formats. Implement Jena's [`Locator`](http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/util/Locator.html) interface. Need to be added to `DataManager` to take effect.
        * `org.graphity.browser.provider`: Browser-specific subclasses for writing [`Response`](http://jackson.codehaus.org/javadoc/jax-rs/1.0/javax/ws/rs/core/Response.html). XSLT stylesheets located in `WEB-INF/xsl` and its subfolders. They translate request parameters into XSLT parameters.
    * `org.graphity.model`: Graphity model interfaces
        * `org.graphity.model.LinkedDataResource`: Prototypical RDF Resource interface
        * `org.graphity.model.impl`: Implementations of Graphity model interfaces
            * `org.graphity.model.impl.LinkedDataResourceImpl`: Base class implementation of `LinkedDataResource`
    * `org.graphity.provider`: Generic `Provider` classes for reading request/writing `Response`
        * `org.graphity.provider.ModelProvider`: Reads `Model` from request body/writes `Model` to `Response` body
        * `org.graphity.provider.RDFPostReader`: Reads `Model` from [RDF/POST](http://www.lsrn.org/semweb/rdfpost.html) requests
        * `org.graphity.provider.ResultSetWriter`: Writes [`ResultSet`](http://jena.apache.org/documentation/javadoc/arq/com/hp/hpl/jena/query/ResultSet.html) with SPARQL results into `Response`
        * `org.graphity.provider.xslt`: Abstract base classes for XSLT transformation-based `Response` writers
    * `org.graphity.util`: Utility classes
        * `org.graphity.util.QueryBuilder`: Builds Jena [`Query`](http://jena.apache.org/documentation/javadoc/arq/com/hp/hpl/jena/query/Query.html) or [SPIN](http://spinrdf.org/spin.html) [`Query`](www.topquadrant.com/topbraid/spin/api/javadoc/org/topbraid/spin/model/class-use/Query.html) from components (e.g. `LIMIT`/`OFFSET` parameters; RDF resources specifying `OPTIONAL` or a subquery)
        * `org.graphity.util.XSLTBuilder`: Builds XSLT transformation out of components. Chaining is possible.
        * `org.graphity.util.locator`: Pluggable classes for retrieving RDF from URIs. Implement Jena's `Locator` interface.
            * `org.graphity.util.locator.LocatorGRDDL`: Generic base class for GRDDL XSLT transformation-based `Locator`s. Also see stylesheets in `WEB-INF/xsl/grddl/`.
            * `org.graphity.util.locator.LocatorLinkedData`: General-purpose class for loading RDF from Linked Data URIs using content negotiation
            * `org.graphity.util.locator.LocatorLinkedDataOAuth2`: General-purpose class for loading RDF from Linked Data URIs using content negotiation and [OAuth2](http://oauth.net/2/) authentication (_unfinished_)
            * `org.graphity.util.locator.PrefixMapper`: Subclass of `LocationMapper` for mapping resource (class, property etc.) URIs into local copies of known ontologies. Also see `resources/location-mapping.ttl`; ontologies are cached in `WEB-INF/owl`.
        * `org.graphity.util.manager`: RDF data management classes
            * `org.graphity.util.manager.DataManager`: Subclass of Jena's [`FileManager`](http://jena.sourceforge.net/how-to/filemanager.html) for loading `Model`s and `ResultSet`s from the Web. All code making requests for RDF data or SPARQL endpoints should use this class. Implements `URIResolver` and resolves URIs when `document()` function is called in XSLT.
            * `org.graphity.util.manager.SPARQLService`: Represent individual SPARQL endpoints, should only be used in case authentication or other custom features are needed. Need to be added to `DataManager` to take effect.
        * `org.graphity.util.oauth`: Classes related to JAX-RS implementation of OAuth
    * `org.graphity.vocabulary`: Graphity ontologies as classes with Jena `Resource`s

XSLT
----

* `WEB-INF/xsl`
    * `WEB-INF/xsl/grddl`: XSLT stylesheets for use with `LocatorGRDDL` and its subclasses
    * `WEB-INF/xsl/imports`: Ontology-specific stylesheets (e.g. overriding templates for certain properties), imported by the master stylesheet
        * `WEB-INF/xsl/imports/default.xsl`: Default templates for rendering RDF/XML subject/predicate/object nodes as XHTML elements. Design-independent.
    * `WEB-INF/xsl/Resource.xsl`: master stylesheet (includes design) for rendering RDF/XML with both single and multiple resources (lists) into XHTML
    * `WEB-INF/xsl/rdfxml2google-wire.xsl`: Generic conversion from RDF/XML to Google [`DataTable`](https://developers.google.com/chart/interactive/docs/reference#DataTable)
    * `WEB-INF/xsl/sparql2google-wire.xsl`: Generic conversion from SPARQL XML results to Google `DataTable`

Resources and Queries
---------------------

* `WEB-INF/owl/graphity.ttl`: Contains ontology reused by all Graphity applications
* `WEB-INF/ontology.ttl`: Graphity Browser-specific ontology, imports general Graphity ontology. Contains metadata of JAX-RS Resources as well as SPIN query Resources used by them.

Tools
=====

Validators
----------

* [RDF/XML and Turtle validator](http://www.rdfabout.com/demo/validator/)
* [SPARQL query validator](http://sparql.org/query-validator.html)
* [SPIN query converter] (http://spinservices.org/spinrdfconverter.html)