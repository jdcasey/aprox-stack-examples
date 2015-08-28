package org.commonjava.cartographer.ex;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.commonjava.cartographer.CartoDataException;
import org.commonjava.maven.atlas.graph.rel.ProjectRelationship;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.management.relation.Relation;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

public class RelationshipReaderTest
{
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private RelationshipReader reader;

    @Before
    public void before()
            throws Exception
    {
        reader = new RelationshipReader( temp.newFolder() );
    }

    @Test
    public void readRelationshipsFromPomNoParent()
            throws URISyntaxException, CartoDataException, TransferException, GalleyMavenException
    {
        URL resource = Thread.currentThread().getContextClassLoader().getResource( "foo-1.0.pom" );
        assertThat( resource, notNullValue() );

        File pom = new File( resource.getPath() );

        Set<ProjectRelationship<?, ?>> relationships = reader.readRelationships( pom );

        // can be null...
        if ( relationships != null )
        {
            relationships.forEach( System.out::println );
        }
    }
}
