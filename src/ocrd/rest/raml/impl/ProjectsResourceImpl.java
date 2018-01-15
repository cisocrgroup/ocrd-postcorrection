package ocrd.rest.raml.impl;

import org.raml.jaxrs.example.model.Projects;
import org.raml.jaxrs.example.resource.ProjectsResource;

import ocrd.rest.raml.handler.ProjectsHandler;

public class ProjectsResourceImpl implements ProjectsResource {

  @Override
  public GetProjectsListResponse getProjectsList() throws Exception {
    ProjectsHandler projects_handler = new ProjectsHandler();
    Projects p = projects_handler.listProjects();

    return GetProjectsListResponse.withJsonOK(p);
  }

  @Override
  public GetProjectsByProjectIDPagesByPageIDResponse
  getProjectsByProjectIDPagesByPageID(String projectID, String pageID)
      throws Exception {
    return null;
  }

  @Override
  public GetProjectsByProjectIDResponse getProjectsByProjectID(String projectID)
      throws Exception {
    return null;
  }
}
