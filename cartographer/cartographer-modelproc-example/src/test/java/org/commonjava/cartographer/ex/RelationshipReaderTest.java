package org.commonjava.cartographer.ex;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.commonjava.cartographer.CartoDataException;
import org.commonjava.maven.atlas.graph.rel.ProjectRelationship;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.ref.SimpleProjectVersionRef;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.parse.PomPeek;
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

    private static final ProjectVersionRef CHILD_POM = new SimpleProjectVersionRef( "org.commonjava.cartographer.ex", "foo", "1.0" );
    private static final String[] POMS = {
            "foo-1.0.pom",
            "foo-parent-5.pom"
    };

    private RelationshipReader reader;

    private File pomRepoDir;

    @Before
    public void before()
            throws Exception
    {
        pomRepoDir = temp.newFolder( "pom-origin" );
        reader = new RelationshipReader( temp.newFolder() );
        reader.setupRepositoryDirectoryFromClasspath( pomRepoDir, POMS );
    }

    @Test
    public void readRelationshipsFromPomNoParent()
            throws URISyntaxException, CartoDataException, TransferException, GalleyMavenException
    {

        Set<ProjectRelationship<?, ?>> relationships = reader.readRelationships( pomRepoDir, CHILD_POM );

        // can be null...
        if ( relationships != null )
        {
            relationships.forEach( System.out::println );
        }
    }
}
