
import java.net.URI;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ocrd.rest.raml.handler.ProjectsHandler;
import ocrd.rest.raml.impl.OcrdResourceImpl;

@Ignore
class OcrdHandlerTest {

	private HttpServer server;

	@Before
	public void setUp() throws Exception {

		new ProjectsHandler();

		// Add REST api classes
		final ResourceConfig config = new ResourceConfig();

		config.register(OcrdResourceImpl.class);

		config.register(MultiPartFeature.class);

		// Define static webcontent
		StaticHttpHandler staticHttpHandler = new StaticHttpHandler("src/webapp/");

		// Create server using ResourceConfig & set root of api
		server = GrizzlyHttpServerFactory.createHttpServer(new URI("http://0.0.0.0:8181/api"), config);

		// Add static webcontent
		server.getServerConfiguration().addHttpHandler(staticHttpHandler, "/");

		// Disable file locking of static webcontent
		server.getListener("grizzly").getFileCache().setEnabled(false);
	}

	// test methods here..

	@Test
	// public void testGetXY){
	//
	// }

	@After
	public void tearDown() throws Exception {
		server.stop();
	}
}
