package org.commonjava.aprox.ex;

import org.apache.commons.io.FileUtils;
import org.commonjava.aprox.test.fixture.core.CoreServerFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;

/**
 * Created by jdcasey on 8/20/15.
 */
public class AbstractGistTest
{

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    protected CoreServerFixture fixture;

    protected Gist gist;

    @Before
    public void before()
            throws Exception
    {
        fixture = new CoreServerFixture( temp );
        File f = new File( fixture.getBootOptions().getAproxHome(), "etc/aprox/conf.d/scheduler.conf" );
        f.getParentFile().mkdirs();
        FileUtils.write( f, "[scheduler]\nenabled=false" );
        fixture.start();

        gist = new Gist( fixture.getUrl() );
//        gist = new Gist( "http://10.3.8.115/api" );
    }

    @After
    public void after()
    {
        if ( gist != null )
        {
            gist.close();
        }

        if ( fixture != null )
        {
            fixture.close();
        }
    }

}
