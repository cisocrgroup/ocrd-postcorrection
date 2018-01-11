// ===============================
// apps/projects/projects.js
// ===============================

define(["marionette","app"], function(Marionette,App){

	var projectsdApp = {};

	projectsdApp.Router = Marionette.AppRouter.extend({
		appRoutes: {
		   "projects/list"    :"listProjects",
  		   "projects/show"    :"showProjects"

		}
	});

	var API = {
	
	
		showProjects: function(id){
			require(["apps/projects/show/show_controller"], function(ShowController){
       				ShowController.showprojectsd(id);
				});
		},

		listProjects: function(){
			require(["apps/projects/list/list_controller"], function(ListController){
       				ListController.listProjects();
				});
		}
	
	};


	App.on("projects:show",function(id){
		App.navigate("projects");
		API.showProjects(id);
	});


	App.on("projects:list",function(id){
		App.navigate("projects/list");
		API.listProjects(id);
	});



	var router = new projectsdApp.Router({
			controller: API
	});
	


 return projectsdApp; 	

});
