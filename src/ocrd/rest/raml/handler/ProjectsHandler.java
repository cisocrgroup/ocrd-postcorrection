package ocrd.rest.raml.handler;

import java.io.File;
import java.io.FileInputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.lmu.cis.pocoweb.Client;
import de.lmu.cis.pocoweb.ProjectData;

import org.raml.jaxrs.example.model.Project;
import org.raml.jaxrs.example.model.Projects;


public class ProjectsHandler {


    private static Client client;

	public ProjectsHandler() {
		
		
		   try {
			
			 client =   Client.login("http://pocoweb.cis.lmu.de/rest", "pocoweb",
			            "pocoweb123");
			   
//			      System.out.println("sid: " + client.getSid());
//			      ProjectData np = client.uploadProject(
//			          new FileInputStream(new File("testdata/hobbes-ocropus.zip")));
//			      np.author = "flo";
//			      np.title = "title";
//			      client.updateProjectData(np);
//			    
//			      ProjectData p = client.getProject(305);
//			      for (int pid : p.pageIds) {
//			        System.out.println("book " + 305 + " page id " + pid);
//			      }
//			      System.out.println("PID: " + np.projectId);
//			      client.deleteProject(np.projectId);
			    } catch (Exception e) {
			      System.out.println("error: " + e);
			    }
		
		
		
	} // ProjectsHandler()

	public Projects listProjects() {
		
		Projects result = new Projects();
		
		ArrayList projects_array = new ArrayList();
		
		  ProjectData[] ps_data;
		try {
			ps_data = client.listProjects();

			 for (ProjectData p_data : ps_data) {
			        
			        Project p = new Project();
			        p.setAuthor(p_data.author);
			        p.setTitle(p_data.title);
			        p.setProjectId(p_data.projectId);
			        p.setLanguage(p_data.language);
			        p.setYear(p_data.year);
			        p.setPages(p_data.pages);
			        
			        
			        
//			        List<Double> pageIds_d = new ArrayList<Double>(); 
//			        for (int i=0; i<p_data.pageIds.length; ++i)
//			        	pageIds_d.add((double) p_data.pageIds[i]);
//			        p.setPageIds(pageIds_d);
			        
			        p.setProfilerUrl(p_data.profilerUrl);
			        
			        projects_array.add(p);

			        
			      }
			 
			result.setProjects(projects_array);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	    
		
	}
	

}