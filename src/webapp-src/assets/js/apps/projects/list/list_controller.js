// ======================================
// apps/projects/list/list_controller.js
// ======================================

define(["app","common/util","common/views","apps/projects/list/list_view"], function(IPS_App,Util,Views,List){


  var Controller = {

 	listProjects: function(){

     		require(["entities/project"], function(ProjectEntitites){

          var loadingCircleView = new  Views.LoadingBackdrop();
          IPS_App.mainLayout.showChildView('backdropRegion',loadingCircleView);


     var fetchingprojects = ProjectEntitites.API.getProjects();

		 var projectsListLayout = new List.Layout();

    	 $.when(fetchingprojects).done(function(projects){
		   loadingCircleView.destroy();


    		projectsListLayout.on("attach",function(){


      var projectsListHeader = new List.Header();
			var projectsListView = new List.projectsList({collection: projects.books});
      var porposalsListFooterPanel = new List.FooterPanel();

          projectsListLayout.showChildView('headerRegion',projectsListHeader);
          projectsListLayout.showChildView('contentRegion',projectsListView);
          projectsListLayout.showChildView('panelRegion',porposalsListFooterPanel);

          $(window).scrollTop(0);


 		}); // onAttach()


       IPS_App.mainLayout.showChildView('mainRegion',projectsListLayout);

		}).fail(function(response){


 			     // loadingCircleView.destroy();
				  var errortext = Util.getErrorText(response);
                  var errorView = new List.Error({model: currentUser,errortext:errortext})

                  errorView.on("currentProject:loggedIn",function(){
					        IPS_App.projectsApp.List.Controller.listprojects();
                  });

                  IPS_App.mainLayout.showChildView('mainRegion',errorView);




          }); //  $.when(fetchingAuth).done // $when fetchingprojects

		}); // require
	}
 }

return Controller;

});
