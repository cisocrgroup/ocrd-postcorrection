
package ocrd.rest.raml.impl;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import ocrd.rest.raml.handler.OcrdHandler;

public class RunLocalJetty
{
	public static void main(String[] args) throws Exception {
		
		//Handler for multiple web apps
		HandlerCollection handlers = new HandlerCollection();

		
		WebAppContext webapp = new WebAppContext();
	
			
		webapp.setResourceBase("src/webapp/");
		webapp.setContextPath("/");

		@SuppressWarnings("unused")
		OcrdHandler idx_handler = new OcrdHandler();
    	
		final ResourceConfig config = new ResourceConfig();
        config.register(OcrdResourceImpl.class);

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
}