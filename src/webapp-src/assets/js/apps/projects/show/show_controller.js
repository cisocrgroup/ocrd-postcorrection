// ======================================
// apps/project/show/show_controller.js
// ======================================

define(["app","common/util","common/views","apps/projects/show/show_view"], function(App,Util,Views,Show){


 Controller = {

		showProject: function(id){
      		$(window).scrollTop(0);

     		require(["entities/project"], function(ProjectEntitites){

	   	      var loadingCircleView = new  Views.LoadingBackdropOpc();
              App.mainLayout.showChildView('backdropRegion',loadingCircleView);

   			  var fetchingproject = ProjectEntitites.API.getProject(id);


        	 $.when(fetchingproject).done(function(project){

			loadingCircleView.destroy();

		 	//currentProposal.set({"url_id":id}); // pass url_id to view..
			var projectShowLayout = new Show.Layout();
			var projectShowHeader;
			var projectShowInfo;
			var projectShowFooterPanel;
			// console.log(reviews);
	
			projectShowLayout.on("attach",function(){
			  

			  projectShowHeader = new Show.Header({title:"OCR Project: "+project.title});
			  projectShowInfo = new Show.Info({project:project});
              projectShowFooterPanel = new Show.FooterPanel();


			  projectShowInfo.on("show:edit_clicked",function(methods){


			   var projectsShowEditProject = new Show.ProjectForm({model: new ProjectEntitites.Project(project), asModal:true,text:"Edit OCR Project",edit_project:true});


           projectsShowEditProject.on("project:submit_clicked",function(data){
           // var postingProject = ProjectEntitites.API.createProject(data);


           //       $.when(postingProject).done(function(result){
           //        $('.loading_background').fadeOut();

           //         $('#projects-modal').modal('toggle');


           //         projectsShowEditProject.model.clear().set(projectsListEditProject.model.defaults);
           //         $('#selected_file').text("");
           //         // projectsListAddProject.render()

           //      })


          });


          App.mainLayout.showChildView('dialogRegion',projectsShowEditProject);

          });


            projectShowInfo.on("show:delete_clicked",function(methods){

			   var projectsShowDeleteProject = new Show.DeleteProjectForm({asModal:true,text:"Remove this Project?",title:"Delete OCR Project"});


           projectsShowDeleteProject.on("project:delete_clicked",function(data){
           // var postingProject = ProjectEntitites.API.createProject(data);


           //       $.when(postingProject).done(function(result){
           //        $('.loading_background').fadeOut();

           //         $('#projects-modal').modal('toggle');


           //         projectsShowDeleteProject.model.clear().set(projectsListDeleteProject.model.defaults);
           //         $('#selected_file').text("");
           //         // projectsListAddProject.render()

           //      })


          });

        

          App.mainLayout.showChildView('dialogRegion',projectsShowDeleteProject);



		  });


         projectShowInfo.on("show:add_book_clicked",function(methods){


		   var projectsShowEditProject = new Show.ProjectForm({model: new ProjectEntitites.Project(), asModal:true,text:"Add a book to the OCR Project",add_book:true});


       projectsShowEditProject.on("project:submit_clicked",function(data){
       // var postingProject = ProjectEntitites.API.createProject(data);


       //       $.when(postingProject).done(function(result){
       //        $('.loading_background').fadeOut();

       //         $('#projects-modal').modal('toggle');


       //         projectsShowEditProject.model.clear().set(projectsListEditProject.model.defaults);
       //         $('#selected_file').text("");
       //         // projectsListAddProject.render()

       //      })


      });


          App.mainLayout.showChildView('dialogRegion',projectsShowEditProject);

          });


  			// projectPanel = new Show.FooterPanel();


	          projectShowLayout.showChildView('headerRegion',projectShowHeader);
	          projectShowLayout.showChildView('infoRegion',projectShowInfo);
	          projectShowLayout.showChildView('footerRegion',projectShowFooterPanel);


    		}); // on.attach()

          App.mainLayout.showChildView('mainRegion',projectShowLayout);

          }).fail(function(response){


 			     // loadingCircleView.destroy();
				  // var errortext = Util.getErrorText(response);
      //             var errorView = new List.Error({model: currentUser,errortext:errortext})

      //             errorView.on("currentProject:loggedIn",function(){
					 //        IPS_App.projectsApp.List.Controller.listprojects();
      //             });

      //             IPS_App.mainLayout.showChildView('mainRegion',errorView);




          });  // $when fetchingproject


    	}) // require
    	
		}

	}


return Controller;

});
