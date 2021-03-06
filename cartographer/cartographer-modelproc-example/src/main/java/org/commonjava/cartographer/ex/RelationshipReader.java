package org.commonjava.cartographer.ex;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.commonjava.cartographer.CartoDataException;
import org.commonjava.cartographer.CartographerCore;
import org.commonjava.cartographer.CartographerCoreBuilder;
import org.commonjava.cartographer.graph.MavenModelProcessor;
import org.commonjava.cartographer.graph.discover.DiscoveryConfig;
import org.commonjava.cartographer.graph.discover.DiscoveryResult;
import org.commonjava.maven.atlas.graph.rel.ProjectRelationship;
import org.commonjava.maven.atlas.graph.spi.neo4j.FileNeo4jConnectionFactory;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.model.view.DependencyView;
import org.commonjava.maven.galley.maven.model.view.MavenPomView;
import org.commonjava.maven.galley.maven.parse.MavenPomReader;
import org.commonjava.maven.galley.maven.parse.PomPeek;
import org.commonjava.maven.galley.maven.util.ArtifactPathUtils;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.model.Transfer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Uses galley + cartographer to read relationships from a given POM file. If necessary, uses maven central to resolve
 * parent POMs and BOMs in order to arrive at a fully inherited Maven project model. Dependencies defined in the parent
 * will be included, if present.
 */
public class RelationshipReader
{

    private final CartographerCore carto;

    public RelationshipReader( File cacheDir )
            throws IOException, CartoDataException
    {
        File unused = File.createTempFile( "unused.", ".db" );
        unused.delete();

        carto = new CartographerCoreBuilder( cacheDir,
                                             new FileNeo4jConnectionFactory( unused, true ) ).withDefaultTransports()
                                                                                             .build();
    }

    public Set<ProjectRelationship<?, ?>> readRelationships( File pomRepoDir, ProjectVersionRef gav )
            throws TransferException, GalleyMavenException, URISyntaxException, CartoDataException
    {
        MavenPomReader pomReader = carto.getGalley().getPomReader();
        MavenModelProcessor processor = new MavenModelProcessor();

        Location location = new SimpleLocation( "file:" + pomRepoDir.getAbsolutePath() );

        List<? extends Location> repoLocations =
                Arrays.asList( location, new SimpleLocation( "central", "http://repo.maven.apache.org/maven2/" ) );

        MavenPomView pomView = pomReader.read( gav, repoLocations );

        // some debug output from the pom view itself
        System.out.println( "Docs in view:\n  " + StringUtils.join( pomView.getDocRefStack(), "\n  " ) );

        System.out.println( "Found dependency artifacts:" );
        List<DependencyView> deps = pomView.getAllDirectDependencies();
        deps.forEach( ( dep ) -> {
            try
            {
                System.out.println( dep.asArtifactRef() );
            }
            catch ( GalleyMavenException e )
            {
                e.printStackTrace();
            }
        } );
        // back to the main event...

        URI src = new URI( location.getUri() );

        DiscoveryConfig disConf = new DiscoveryConfig( src );
        disConf.setIncludeBuildSection( false );
        disConf.setIncludeManagedDependencies( false );
        disConf.setIncludeManagedPlugins( false );

        DiscoveryResult result = processor.readRelationships( pomView, src, disConf );

        return result.getAcceptedRelationships();
    }

    public void setupRepositoryDirectoryFromClasspath( File dir, String[] poms )
            throws TransferException, IOException
    {
        for ( String pom: poms )
        {
            URL resource = Thread.currentThread().getContextClassLoader().getResource( pom );
            assert( resource != null );

            File pomFile = new File( resource.getPath() );
            PomPeek peek = new PomPeek( pomFile );

            String path = ArtifactPathUtils.formatArtifactPath( peek.getKey().asPomArtifact(),
                                                             carto.getGalley().getTypeMapper() );

            File f = new File( dir, path );
            f.getParentFile().mkdirs();
            FileUtils.copyFile( pomFile, f );
        }
    }
}
