/*
 * Copyright (C) 2012 Martynas Jusevičius <martynas@graphity.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.graphity;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.util.FileUtils;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.graphity.provider.ModelProvider;
import org.graphity.util.QueryBuilder;
import org.graphity.vocabulary.Graphity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
abstract public class RDFResourceImpl extends ResourceImpl implements RDFResource
{
    public static final Map<javax.ws.rs.core.MediaType, Double> QUALIFIED_TYPES;    
    static
    {
	Map<javax.ws.rs.core.MediaType, Double> typeMap = new HashMap<javax.ws.rs.core.MediaType, Double>();
	
	typeMap.put(MediaType.APPLICATION_RDF_XML_TYPE, null);

	typeMap.put(MediaType.TEXT_TURTLE_TYPE, 0.9);
	
	typeMap.put(MediaType.TEXT_PLAIN_TYPE, 0.7);
	
	typeMap.put(MediaType.APPLICATION_XML_TYPE, 0.5);
	
	QUALIFIED_TYPES = Collections.unmodifiableMap(typeMap);
    }    
    private static final Logger log = LoggerFactory.getLogger(RDFResourceImpl.class);
    
    private com.hp.hpl.jena.rdf.model.Resource resource = null;
    private ClientConfig config = new DefaultClientConfig();
    private Client client = null;

    public RDFResourceImpl()
    {
	config.getClasses().add(ModelProvider.class);
	client = Client.create(config); // add OAuth filter
    }

    @GET
    @Produces("text/html; charset=UTF-8")
    public Response getResponse()
    {
	return Response.ok(this).
	    //type(MediaType.TEXT_HTML).
	    build();

    }

    // 2 options here: load RDF/XML directly from getURI(), or via DESCRIBE from SPARQL endpoint
    // http://openjena.org/wiki/ARQ/Manipulating_SPARQL_using_ARQ
    @Override
    @GET
    @Produces("text/plain; charset=UTF-8")
    public Model getModel()
    {
	Model model = null; // ModelFactory.createDefaultModel();

	log.debug("getURI(): {}", getURI());
	log.debug("getServiceURI(): {}", getServiceURI());

	// query the available SPARQL endpoint (either local or remote)
	if (getServiceURI() == null && isRemote())
	{
		// load remote Linked Data
		try
		{
		    log.trace("Loading Model from URI: {} with Accept header: {}", getURI(), getAcceptHeader());
		    model = client.resource(getURI()).
			    header("Accept", getAcceptHeader()).
			    get(Model.class);
		    log.debug("Number of Model stmts read: {}", model.size());
		}
		catch (Exception ex)
		{
		    log.trace("Could not load Model from URI: {}", getURI());
		}
	}
	else
	{
	    try
	    {
		if (getQuery().isConstructType()) model = getQueryExecution().execConstruct();
		if (getQuery().isDescribeType()) model = getQueryExecution().execDescribe();
		//model = getOntModel(); // we're on a local host! load local sitemap
		log.debug("Number of Model stmts read: {}", model.size());
	    }
	    catch (Exception ex)
	    {
		log.trace("Could not execute Query: {}", getQuery());
	    }
	}

	// RDF/XML description must include some statements about this URI, otherwise it's 404 Not Found
	//if (!model.containsResource(model.createResource(getURI())))
	//    throw new WebApplicationException(Response.Status.NOT_FOUND);
	if (model == null)
	    throw new WebApplicationException(Response.Status.NOT_FOUND);
	
	return model;
    }
    
    protected boolean isRemote()
    {
	// resolve somehow better?
	return !getURI().startsWith(getUriInfo().getBaseUri().toString());
    }
    
    public OntModel getOntModel()
    {
	OntModel ontModel = ModelFactory.createOntologyModel(); // .createDefaultModel().

	log.debug("@base: {}", getUriInfo().getBaseUri().toString());
	ontModel.read(getServletContext().getResourceAsStream("/WEB-INF/graphity.ttl"), null, FileUtils.langTurtle);
	ontModel.read(getServletContext().getResourceAsStream("/WEB-INF/sitemap.ttl"), getUriInfo().getBaseUri().toString(), FileUtils.langTurtle);
	
	return ontModel;
    }
    
    public QueryExecution getQueryExecution()
    {
	if (getServiceURI() != null)
	{
	    QueryEngineHTTP request = QueryExecutionFactory.createServiceRequest(getServiceURI(), getQuery());
	    request.setBasicAuthentication("M6aF7uEY9RBQLEVyxjUG", "X".toCharArray());
	    log.trace("Request to SPARQL endpoint: {} with query: {}", getServiceURI(), getQuery());	    
	    return request;
	}
	else
	{
	    log.trace("Querying Model: {} with query: {}", getOntModel(), getQuery());
	    return QueryExecutionFactory.create(getQuery(), getOntModel());
	    //return QueryExecutionFactory.create(getDefaultQuery(), getOntModel());
	}
    }
    
    public Query getQuery()
    {
	Query query = null;
	if (!isRemote()) query = getExplicitQuery();
	if (getServiceURI() == null) query = getDefaultQuery();
	if (query == null) query = getDefaultQuery();
	if (query == null) query = QueryFactory.create("DESCRIBE <" + getURI() + ">");
	return query;
    }

    public Query getExplicitQuery()
    {
	if (getIndividual() == null)  return null; // in case of most remote URIs
	Resource queryRes = getIndividual().getPropertyResourceValue(Graphity.query);
	//getOntModel().getIndividual(Graphity.ConstructItem.getURI());
	//QueryExecutionFactory.
	
	log.trace("Explicit query resource {} for URI {}", queryRes, getURI());
	if (queryRes == null) return null;
	
	return QueryBuilder.fromResource(queryRes).
	    bind("uri", getURI()).
	    build();
    }

    public Query getDefaultQuery()
    {
log.trace("Default query {} for URI {}", "DESCRIBE <" + getURI() + ">", getURI());
return QueryFactory.create("DESCRIBE <" + getURI() + ">");

	/*
	Resource queryRes = getBaseIndividual().getPropertyResourceValue(Graphity.defaultQuery);
	log.trace("Default query resource {} for URI {}", queryRes, getURI());
	if (queryRes == null) return null;
		
	return QueryBuilder.fromResource(queryRes).
	    bind("uri", getURI()).
	    build();
	 */
    }
	
    protected Resource getResource()
    {
	if (resource == null)
	    resource = getModel().createResource(getURI());
	
	return resource;
    }
    
    protected Individual getIndividual()
    {
	log.debug("getUriInfo().getAbsolutePath().toString(): {}", getUriInfo().getAbsolutePath().toString());
	return getOntModel().getIndividual(getUriInfo().getAbsolutePath().toString());
	//return getOntModel().getIndividual(getUriInfo().getBaseUri().toString());
    }

    protected Individual getBaseIndividual()
    {
	log.debug("getUriInfo().getBaseUri().toString(): {}", getUriInfo().getAbsolutePath().toString());
	return getOntModel().getIndividual(getUriInfo().getBaseUri().toString());
    }

    @Override
    public String getURI()
    {
	if (getUriInfo().getQueryParameters().getFirst("uri") != null)
	    return getUriInfo().getQueryParameters().getFirst("uri");
	
	return super.getURI();
    }
    
    @Override
    public final String getServiceURI()
    {
	// browsing remote resource, SPARQL endpoint is either supplied or null (or discovered from voiD?)
	if (getUriInfo().getQueryParameters().getFirst("uri") != null)
	{
	    String serviceUri = getUriInfo().getQueryParameters().getFirst("service-uri");
	    if (serviceUri == null || serviceUri.isEmpty()) return null;
	    return serviceUri;
	}
	log.trace("getBaseIndividual(): {}", getBaseIndividual());
	// browsing local resource, SPARQL endpoint is retrieved from the sitemap
	if (getBaseIndividual() == null) return null;
	
	return getBaseIndividual().
		getPropertyResourceValue(getOntModel().
		    getProperty("http://rdfs.org/ns/void#inDataset")).
		getPropertyResourceValue(getOntModel().
		    getProperty("http://rdfs.org/ns/void#sparqlEndpoint")).
		getURI();
    }

    public String getAcceptHeader()
    {
	String header = null;

	//for (Map.Entry<String, Double> type : getQualifiedTypes().entrySet())
	Iterator <Entry<javax.ws.rs.core.MediaType, Double>> it = QUALIFIED_TYPES.entrySet().iterator();
	while (it.hasNext())
	{
	    Entry<javax.ws.rs.core.MediaType, Double> type = it.next();
	    if (header == null) header = "";
	    
	    header += type.getKey();
	    if (type.getValue() != null) header += ";q=" + type.getValue();
	    
	    if (it.hasNext()) header += ",";
	}
	
	return header;
    }

    @Override
    public Date getLastModified()
    {
	//ResIterator it = getModel().listResourcesWithProperty(DCTerms.modified);
	return null;
    }
    
    @Override
    public AnonId getId()
    {
	return getResource().getId();
    }

    @Override
    public Resource inModel(Model m)
    {
	return getResource().inModel(m);
    }

    @Override
    public boolean hasURI(String uri)
    {
	return getResource().hasURI(uri);
    }

    @Override
    public String getNameSpace()
    {
	return getResource().getNameSpace();
    }

    @Override
    public String getLocalName()
    {
	return getResource().getLocalName();
    }

    @Override
    public Statement getRequiredProperty(Property p)
    {
	return getResource().getRequiredProperty(p);
    }

    @Override
    public Statement getProperty(Property p)
    {
	return getResource().getProperty(p);
    }

    @Override
    public StmtIterator listProperties(Property p)
    {
	return getResource().listProperties(p);
    }

    @Override
    public StmtIterator listProperties()
    {
	return getResource().listProperties();
    }

    @Override
    public Resource addLiteral(Property p, boolean o)
    {
	return getResource().addLiteral(p, o);
    }

    @Override
    public Resource addLiteral(Property p, long o)
    {
	return getResource().addLiteral(p, o);
    }

    @Override
    public Resource addLiteral(Property p, char o)
    {
	return getResource().addLiteral(p, o);
    }

    @Override
    public Resource addLiteral(Property value, double d)
    {
	return getResource().addLiteral(value, d);
    }

    @Override
    public Resource addLiteral(Property value, float d)
    {
	return getResource().addLiteral(value, d);
    }

    @Override
    public Resource addLiteral(Property p, Object o)
    {
	return getResource().addLiteral(p, o);
    }

    @Override
    public Resource addLiteral(Property p, Literal o)
    {
	return getResource().addLiteral(p, o);
    }

    @Override
    public Resource addProperty(Property p, String o)
    {
	return getResource().addProperty(p, o);
    }

    @Override
    public Resource addProperty(Property p, String o, String l)
    {
	return getResource().addProperty(p, o, l);
    }

    @Override
    public Resource addProperty(Property p, String lexicalForm, RDFDatatype datatype)
    {
	return getResource().addProperty(p, lexicalForm, datatype);
    }

    @Override
    public Resource addProperty(Property p, RDFNode o)
    {
	return getResource().addProperty(p, o);
    }

    @Override
    public boolean hasProperty(Property p)
    {
	return getResource().hasProperty(p);
    }

    @Override
    public boolean hasLiteral(Property p, boolean o)
    {
	return getResource().hasLiteral(p, o);
    }

    @Override
    public boolean hasLiteral(Property p, long o)
    {
	return getResource().hasLiteral(p, o);
    }

    @Override
    public boolean hasLiteral(Property p, char o)
    {
	return getResource().hasLiteral(p, o);
    }

    @Override
    public boolean hasLiteral(Property p, double o)
    {
	return getResource().hasLiteral(p, o);
    }

    @Override
    public boolean hasLiteral(Property p, float o)
    {
	return getResource().hasLiteral(p, o);
    }

    @Override
    public boolean hasLiteral(Property p, Object o)
    {
	return getResource().hasLiteral(p, o);
    }

    @Override
    public boolean hasProperty(Property p, String o)
    {
	return getResource().hasProperty(p, o);
    }

    @Override
    public boolean hasProperty(Property p, String o, String l)
    {
	return getResource().hasProperty(p, o, l);
    }

    @Override
    public boolean hasProperty(Property p, RDFNode o)
    {
	return getResource().hasProperty(p, o);
    }

    @Override
    public Resource removeProperties()
    {
	return getResource().removeProperties();
    }

    @Override
    public Resource removeAll(Property p)
    {
	return getResource().removeAll(p);
    }

    @Override
    public Resource begin()
    {
	return getResource().begin();
    }

    @Override
    public Resource abort()
    {
	return getResource().abort();
    }

    @Override
    public Resource commit()
    {
	return getResource().commit();
    }

    @Override
    public Resource getPropertyResourceValue(Property p)
    {
	return getResource().getPropertyResourceValue(p);
    }

    @Override
    public boolean isAnon()
    {
	return getResource().isAnon();
    }

    @Override
    public boolean isLiteral()
    {
	return getResource().isLiteral();
    }

    @Override
    public boolean isURIResource()
    {
	return getResource().isURIResource();
    }

    @Override
    public boolean isResource()
    {
	return getResource().isResource();
    }

    @Override
    public <T extends RDFNode> T as(Class<T> view)
    {
	return getResource().as(view);
    }

    @Override
    public <T extends RDFNode> boolean canAs(Class<T> view)
    {
	return getResource().canAs(view);
    }

    @Override
    public Object visitWith(RDFVisitor rv)
    {
	return getResource().visitWith(rv);
    }

    @Override
    public Resource asResource()
    {
	return getResource().asResource();
    }

    @Override
    public Literal asLiteral()
    {
	return getResource().asLiteral();
    }

    @Override
    public Node asNode()
    {
	return getResource().asNode();
    }
}
