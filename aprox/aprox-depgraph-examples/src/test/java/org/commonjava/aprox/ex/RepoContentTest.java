package org.commonjava.aprox.ex;

import org.commonjava.aprox.depgraph.model.RepoContentResult;
import org.commonjava.cartographer.result.GraphExport;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by jdcasey on 8/20/15.
 */
public class RepoContentTest
        extends AbstractGistTest
{

    @Test
    public void exportGraph()
            throws Exception
    {
        RepoContentResult content = gist.graphRepoContent();

        Assert.assertNotNull( content );
        Assert.assertThat( content.getProjects(), CoreMatchers.notNullValue() );
        Assert.assertThat( content.getProjects().isEmpty(), CoreMatchers.equalTo( false ) );
    }

}
