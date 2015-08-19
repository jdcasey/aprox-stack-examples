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
import org.commonjava.cartographer.graph.discover.patch.DepgraphPatcherConstants;
import org.commonjava.cartographer.request.MetadataCollationRequest;
import org.commonjava.cartographer.request.ProjectGraphRequest;
import org.commonjava.cartographer.graph.discover.patch.DepgraphPatcher;
import org.commonjava.cartographer.result.GraphExport;
import org.commonjava.cartographer.result.MetadataCollationEntry;
import org.commonjava.cartographer.result.MetadataCollationResult;
import org.commonjava.maven.atlas.graph.model.EProjectCycle;
import org.commonjava.maven.atlas.graph.rel.ProjectRelationship;
import org.commonjava.maven.atlas.graph.traverse.model.BuildOrder;
import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by jdcasey on 8/19/15.
 */
public class Gist
{

    public void exportGraph()
            throws AproxClientException
    {
        DepgraphAproxClientModule mod = new DepgraphAproxClientModule();
        Aprox aprox = new Aprox( "http://localhost:8080/api", mod ).connect();

        ProjectGraphRequest req = mod.newProjectGraphRequest()
                                     .withSource( "group:public" )
                                     .withPatcherIds( DepgraphPatcherConstants.ALL_PATCHERS )
                                     .withResolve( true )
                                     .withGraph( mod.newGraphDescription()
                                                    .withRoots( new ProjectVersionRef( "org.commonjava.aprox.embed",
                                                                                       "aprox-embedder-savant",
                                                                                       "0.23.2" ) )
                                                    .withPreset( "build-requires" )
                                                    .build() )
                                     .build();

        GraphExport export = mod.graph( req );

        for ( ProjectRelationship<?> rel : export.getRelationships() )
        {
            ProjectVersionRef declaring = rel.getDeclaring();
            ProjectVersionRef targeting = rel.getTargetArtifact();

            System.out.printf( "Relationship (type: %s) from: %s to: %s", rel.getType(), declaring, targeting );
        }
    }

    public void collateByScmUrlAndGroupId()
            throws AproxClientException
    {
        DepgraphAproxClientModule mod = new DepgraphAproxClientModule();
        Aprox aprox = new Aprox( "http://localhost:8080/api", mod ).connect();

        MetadataCollationRequest req = mod.newMetadataCollationRequest()
                                          .withKeys( Arrays.asList( "scm-url", "groupId" ) )
                                          .withResolve( true )
                                          .withSource( "group:public" )
                                          .withPatcherIds( DepgraphPatcherConstants.ALL_PATCHERS )
                                          .withGraph( mod.newGraphDescription()
                                                         .withRoots( Arrays.asList( ProjectVersionRef.parse(
                                                                 "org.commonjava.aprox.embed:aprox-embedder-savant:0.23.2" ) ) )
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
        DepgraphAproxClientModule mod = new DepgraphAproxClientModule();
        Aprox aprox = new Aprox( "http://localhost:8080/api", mod ).connect();

        ProjectGraphRequest req = mod.newProjectGraphRequest()
                                     .withResolve( true )
                                     .withSource( "group:public" )
                                     .withPatcherIds( DepgraphPatcherConstants.ALL_PATCHERS )
                                     .withGraph( mod.newGraphDescription()
                                                    .withRoots( ProjectVersionRef.parse(
                                                            "org.commonjava.aprox.embed:aprox-embedder-savant:0.23.2" ) )
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
        for ( EProjectCycle cycle : result.getCycles() )
        {
            System.out.println( Integer.toString( i ) + ". " + cycle.getAllParticipatingProjects() );
        }
    }
}
