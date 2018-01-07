
package ocrd.rest.raml.impl;

import java.net.URI;
import java.util.Scanner;



import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import ocrd.rest.raml.handler.OcrdHandler;


public class RunLocalGrizzly
{
    @SuppressWarnings("resource")
	public static void main(final String[] args) throws Exception
    //Runs the API & webapp within a local grizzly2 server under port 8181
    {
		OcrdHandler ocrd_handler = new OcrdHandler();

		//Add REST api classes
        final ResourceConfig config = new ResourceConfig();

        config.register(OcrdResourceImpl.class);

        config.register(MultiPartFeature.class);
        
        //Define static webcontent
        StaticHttpHandler staticHttpHandler = new StaticHttpHandler("src/webapp/");
        
        //Create server using ResourceConfig & set root of api
        HttpServer server =  GrizzlyHttpServerFactory.createHttpServer(new URI("http://0.0.0.0:8181/api"),config);
        
        //Add static webcontent
        server.getServerConfiguration().addHttpHandler(staticHttpHandler, "/");
        
        //Disable file locking of static webcontent
        server.getListener("grizzly").getFileCache().setEnabled(false);

        System.out.println("Strike ENTER to stop...");
        new Scanner(System.in).nextLine();

        server.stop();

        System.out.println("Bye!");
        System.exit(0);
    }
}
