/**
 * Copyright (C) 2011 Red Hat, Inc. (jdcasey@commonjava.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.aprox.ex;

import org.commonjava.aprox.client.core.Aprox;
import org.commonjava.aprox.client.core.AproxClientException;
import org.commonjava.aprox.depgraph.client.DepgraphAproxClientModule;
import org.commonjava.aprox.depgraph.model.ArtifactRepoContent;
import org.commonjava.aprox.depgraph.model.ProjectRepoContent;
import org.commonjava.aprox.depgraph.model.RepoContentResult;
import org.commonjava.cartographer.graph.discover.patch.DepgraphPatcherConstants;
import org.commonjava.cartographer.request.MetadataCollationRequest;
import org.commonjava.cartographer.request.ProjectGraphRequest;
import org.commonjava.cartographer.request.RepositoryContentRequest;
import org.commonjava.cartographer.result.*;
import org.commonjava.maven.atlas.graph.model.EProjectCycle;
import org.commonjava.maven.atlas.graph.rel.ProjectRelationship;
import org.commonjava.maven.atlas.graph.traverse.model.BuildOrder;
import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.ref.SimpleProjectVersionRef;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 * Created by jdcasey on 8/19/15.
 */
public class Gist
        implements Closeable
{

    private final DepgraphAproxClientModule mod;

    private final Aprox aprox;

    private String url;

    public Gist( String url )
            throws AproxClientException
    {
        this.url = url;
        mod = new DepgraphAproxClientModule();
        aprox = new Aprox( url, mod ).connect();
    }

    public ZipInputStream zipRepoContent()
            throws AproxClientException, IOException
    {
        ProjectVersionRef pvr = new SimpleProjectVersionRef( "org.commonjava.aprox.launch",
                                                             "aprox-launcher-savant", "0.24.0" );

        RepositoryContentRequest req = mod.newRepositoryContentRequest()
                                          .withWorkspaceId( "content-" + pvr.toString() )
                                          .withSource( "group:public" )
                                          .withPatcherIds( DepgraphPatcherConstants.ALL_PATCHERS )
                                          .withResolve( true )
                                          .withGraphs( mod.newGraphComposition()
                                                          .withGraph( mod.newGraphDescription()
                                                                         .withRoots( pvr )
                                                                         .withPreset( "runtime" )
                                                                         .build() )
                                                          .build() )
                                          .withMetas( RepositoryContentRequest.NO_METAS_SET )
                                          .build();

        return new ZipInputStream( mod.repositoryZip( req ) );
    }

    public RepoContentResult graphRepoContent()
            throws AproxClientException
    {
        ProjectVersionRef pvr = new SimpleProjectVersionRef( "org.commonjava.aprox.launch",
                                                             "aprox-launcher-savant", "0.24.0" );

        RepositoryContentRequest req = mod.newRepositoryContentRequest()
                                     .withWorkspaceId( "content-" + pvr.toString() )
                                     .withSource( "group:public" )
                                     .withPatcherIds( DepgraphPatcherConstants.ALL_PATCHERS )
                                     .withResolve( true )
                                     .withGraphs( mod.newGraphComposition()
                                                     .withGraph( mod.newGraphDescription()
                                                                    .withRoots( pvr )
                                                                    .withPreset( "runtime" )
                                                                    .build() )
                                                     .build() )
                                     .withMetas( RepositoryContentRequest.NO_METAS_SET )
                                     .build();

        RepoContentResult result = mod.repositoryContent( req );

        if ( result != null )
        {
            for ( ProjectVersionRef ref : result )
            {
                ProjectRepoContent projectContent = result.getProject( ref );
                for ( ArtifactRepoContent arc : projectContent )
                {
                    System.out.println( arc.getArtifact().toString() );
                }
            }
        }

        return result;
    }

    public GraphExport exportGraph()
            throws AproxClientException
    {
        ProjectVersionRef pvr = new SimpleProjectVersionRef( "xom",
                                                             "xom", "1.2.5" );

        ProjectGraphRequest req = mod.newProjectGraphRequest()
                                     .withWorkspaceId( "export-" + pvr.toString() )
                                     .withSource( "group:public" )
                                     .withPatcherIds( DepgraphPatcherConstants.ALL_PATCHERS )
                                     .withResolve( true )
                                     .withGraph( mod.newGraphDescription()
//                                                    .withRoots( new SimpleProjectVersionRef( "org.commonjava.util",
//                                                                                       "partyline",
//                                                                                       "1.4" ) )
                                                         .withRoots( pvr )
                                                    .withPreset( "requires" )
                                                    .build() )
                                     .build();

        GraphExport export = mod.graph( req );

        if ( export != null )
        {
            for ( ProjectRelationship<?, ?> rel : export.getRelationships() )
            {
                ProjectVersionRef declaring = rel.getDeclaring();
                ProjectVersionRef targeting = rel.getTargetArtifact();

                System.out.printf( "Relationship (type: %s) from: %s to: %s", rel.getType(), declaring, targeting );
            }

            req.setResolve( false ); // get ready to resubmit to retrieve errors...don't re-resolve.
            ProjectErrors errors = export.getErrors();

            System.out.println( "Got project errors (during graph discovery):\n" );
            int i=0;
            if ( errors != null )
            {
                List<ProjectError> projects = errors.getProjects();
                if ( projects != null )
                {
                    for ( ProjectError error : projects )
                    {
                        System.out.printf( "\n%d. %s:\n\n%s\n\n", i++, error.getProject(), error.getError() );
                    }
                }
            }

            if ( i < 1)
            {
                System.out.println( "\n  None\n" );
            }
        }

        return export;
    }

    public void collateByScmUrlAndGroupId()
            throws AproxClientException
    {
        MetadataCollationRequest req = mod.newMetadataCollationRequest()
                                          .withWorkspaceId( "collation" )
                                          .withKeys( Arrays.asList( "scm-url", "groupId" ) )
                                          .withResolve( true )
                                          .withSource( "group:public" )
                                          .withPatcherIds( DepgraphPatcherConstants.ALL_PATCHERS )
                                          .withGraph( mod.newGraphDescription()
                                                         .withRoots(
                                                                 new SimpleProjectVersionRef( "org.commonjava.util", "partyline",
                                                                                        "1.4" ) )
                                                         .withPreset( "build-requires" )
                                                         .build() )
                                          .build();

        MetadataCollationResult result = mod.collateMetadata( req );
        for ( MetadataCollationEntry entry : result )
        {
            System.out.println( "\n---------------------------------\nMetadata:\n---------------------------------" );

            entry.getMetadata().forEach( ( k, v ) -> {
                System.out.printf( "%s = %s\n", k, v );
            } );

            System.out.println( "\n---------------------------------\nProjects:\n---------------------------------" );

            entry.getProjects().forEach( ( p ) -> {
                System.out.println( p );
            } );

            System.out.println( "\n---------------------------------" );
        }
    }

    public void buildOrder()
            throws AproxClientException
    {
        ProjectGraphRequest req = mod.newProjectGraphRequest()
                                     .withWorkspaceId( "build-order" )
                                     .withResolve( true )
                                     .withSource( "group:public" )
                                     .withPatcherIds( DepgraphPatcherConstants.ALL_PATCHERS )
                                     .withGraph( mod.newGraphDescription()
                                                    .withRoots( new SimpleProjectVersionRef( "org.commonjava.util",
                                                                                       "partyline",
                                                                                       "1.4" ) )
                                                    .withPreset( "build-requires" )
                                                    .build() )
                                     .build();

        BuildOrder result = mod.buildOrder( req );

        System.out.println( "Build Order:" );
        for ( ProjectRef ref : result.getOrder() )
        {
            System.out.println( ref );
        }

        System.out.println( "\n\nDependency Cycles:" );
        int i = 0;
        if ( result.getCycles() != null )
        {
            for ( EProjectCycle cycle : result.getCycles() )
            {
                System.out.println( Integer.toString( i ) + ". " + cycle.getAllParticipatingProjects() );
            }
        }
    }

    public void close()
    {
        aprox.close();
    }
}
