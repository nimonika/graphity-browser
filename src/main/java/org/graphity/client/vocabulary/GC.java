/*
 * Copyright (C) 2013 Martynas Jusevičius <martynas@graphity.org>
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
package org.graphity.client.vocabulary;

import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class GC
{
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static OntModel m_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://client.graphity.org/ontology#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI()
    {
	return NS;
    }
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
        
    public static final OntClass CreateMode = m_model.createClass( NS + "CreateMode" );
    
    public static final OntClass EditMode = m_model.createClass( NS + "EditMode" );

    public static final OntClass ListMode = m_model.createClass( NS + "ListMode" );
    
    public static final OntClass TableMode = m_model.createClass( NS + "TableMode" );

    public static final OntClass ThumbnailMode = m_model.createClass( NS + "ThumbnailMode" );
    
    public static final OntClass MapMode = m_model.createClass( NS + "MapMode" );

    public static final OntClass PropertyMode = m_model.createClass( NS + "PropertyMode" );
    
    public static final ObjectProperty stylesheet = m_model.createObjectProperty( NS + "stylesheet" );

    //public static final DatatypeProperty ontologyLocation = m_model.createDatatypeProperty( NS + "ontologyLocation" );
    
    public static final ObjectProperty defaultMode = m_model.createObjectProperty( NS + "defaultMode" );

    public static final ObjectProperty mode = m_model.createObjectProperty( NS + "mode" );

}
