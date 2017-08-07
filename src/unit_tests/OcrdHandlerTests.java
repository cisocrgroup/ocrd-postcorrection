package unit_tests;


import javax.ws.rs.client.WebTarget;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ocrd.rest.raml.handler.OcrdHandler;

import ocrd.rest.raml.impl.OcrdResourceImpl;

import junit.framework.Assert;

public class OcrdHandlerTests {
	
	
	public static HttpServer server;
	private WebTarget target;

	@Before
	public void setUp() throws Exception {
	
		
		//Handler for multiple web apps
		HandlerCollection handlers = new HandlerCollection();

		
		WebAppContext webapp = new WebAppContext();
	
			
		webapp.setResourceBase("src/webapp/");
		webapp.setContextPath("/");

		@SuppressWarnings("unused")
		OcrdHandler idx_handler = new OcrdHandler();
    	
		final ResourceConfig config = new ResourceConfig();
        config.register(OcrdResourceImpl.class);
        config.register(MultiPartFeature.class);

		Server server = new Server(8181);
		
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/api");
        ServletHolder servletHolder = new ServletHolder(new ServletContainer(config));
        contextHandler.addServlet(servletHolder, "/");
		handlers.addHandler(contextHandler);

		//Disable file locking
		webapp.getInitParams().put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
		handlers.addHandler(webapp);
		
		// Adding the handlers to the server
		server.setHandler(handlers);

		// Starting the Server
		server.start();
		server.join();
	}

	// test methods here..
	
	@Test
//	public void testGetXY){
//		
//	}
	
	
	@After
	public void tearDown() throws Exception {
		server.stop();
	}

}