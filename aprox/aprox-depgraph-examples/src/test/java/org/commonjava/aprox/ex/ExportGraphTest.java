package org.commonjava.aprox.ex;

import org.commonjava.cartographer.result.GraphExport;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by jdcasey on 8/20/15.
 */
public class ExportGraphTest
        extends AbstractGistTest
{

    @Test
    @Ignore
    public void exportGraph()
            throws Exception
    {
        GraphExport export = gist.exportGraph();

        Assert.assertNotNull( export );
        Assert.assertThat( export.getRelationships(), CoreMatchers.notNullValue() );
        Assert.assertThat( export.getRelationships().isEmpty(), CoreMatchers.equalTo( false ) );
    }

}
