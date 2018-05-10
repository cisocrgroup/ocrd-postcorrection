package de.lmu.cis.ocrd.train.test;

import de.lmu.cis.ocrd.train.Environment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
public class EnvironmentTest {

    private final static String base = "src/test/resources";
    private final static String name = "test-environment";
    private Environment environment;

    @Before
    public void init() throws IOException {
        this.environment = new Environment(base, name);
        assertThat(Files.exists(this.environment.getPath()), is(true));
    }

    @Test
    public void testName() {
        assertThat(environment.getName(), is(name));
    }

    @After
    public void deInit() throws IOException {
        this.environment.remove();
        assertThat(Files.exists(this.environment.getPath()), is(false));
    }
}
