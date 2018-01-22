package ocrd.rest.raml.impl;

import de.lmu.cis.pocoweb.Client;
import de.lmu.cis.pocoweb.ProjectBook;
import java.util.Base64;
import ocrd.rest.raml.handler.ProjectsHandler;
import org.apache.commons.io.IOUtils;
import org.raml.jaxrs.example.model.Project;
import org.raml.jaxrs.example.model.Projects;
import org.raml.jaxrs.example.model.UploadProjectData;
import org.raml.jaxrs.example.resource.ProjectsResource;

public class ProjectsResourceImpl implements ProjectsResource {

  @Override
  public GetProjectsListResponse getProjectsList() throws Exception {
    try (Client client = newClient();) {
      return GetProjectsListResponse.withJsonOK(client.listProjects());
    }
  }

  @Override
  public GetProjectsByProjectIDPagesByPageIDResponse
  getProjectsByProjectIDPagesByPageID(String projectID, String pageID)
      throws Exception {
    throw new Exception("Not implemented");
  }

  @Override
  public GetProjectsByProjectIDResponse getProjectsByProjectID(String projectID)
      throws Exception {
    try (Client client = newClient();) {
      return GetProjectsByProjectIDResponse.withJsonOK(
          client.getProject(Integer.parseInt(projectID)));
    }
  }
  @Override
  public PostProjectsByProjectIDAddBookResponse
  postProjectsByProjectIDAddBook(String projectID, UploadProjectData data)
      throws Exception {
    throw new Exception("Not implemented");
  }
  @Override
  public PostProjectsCreateResponse postProjectsCreate(UploadProjectData data)
      throws Exception {
    try (Client client = newClient();) {
      if (data.getProject().getBooks().isEmpty()) {
        throw new Exception("cannot upload project with no book data");
      }
      // ProjectBook book = new ProjectBook()
      //                        .withOcrUser(data.getProject().getUser())
      //                    //.withOcrEngine(data.getOcrEngine());
      //                    book.withAuthor(data.getProject().getAuthor())
      //                        .withTitle(data.getProject().getTitle())
      //                        .withYear(data.getProject().getYear())
      //                        .withLanguage(data.getProject().getLanguage());
      Project project = client.uploadProject(
          data.getProject(), Base64.getDecoder().wrap(IOUtils.toInputStream(
                                 data.getContent(), "UTF-8")));
      // Project project = book.newProjectFromThis();
      return PostProjectsCreateResponse.withJsonCreated(project);
    }
  }

  @Override
  public PutProjectsByProjectIDUpdateResponse
  putProjectsByProjectIDUpdate(String projectID, Project entity)
      throws Exception {
    System.out.println(projectID);
    System.out.println(entity.getAuthor());
    return null;
  }

  
  @Override
  public DeleteProjectsByProjectIDDeleteResponse deleteProjectsByProjectIDDelete(String projectID) throws Exception {
  	System.out.println(projectID);
  	return null;
  }
  
  private static Client newClient() throws Exception {
    return Client.login("http://pocoweb.cis.lmu.de/rest", "pocoweb",
                        "pocoweb123");
  }




}
