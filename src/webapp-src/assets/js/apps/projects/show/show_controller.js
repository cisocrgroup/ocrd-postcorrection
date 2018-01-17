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

			  projectShowExampleResponse = new Show.Resp({results:{}})


			  projectShowInfo.on("show:training_clicked",function(methods){


			  	var postingTrainingData = UtilEntitites.API.startTraining({methods:methods}); 

  				$.when(postingTrainingData).done(function(response){
  					
  					var resultarray = JSON.parse(response.result);

  					for (key in resultarray) {
						console.log(resultarray[key].evalstring) 

  						resultarray[key].evalstring = resultarray[key].evalstring[0].replace(/(?:\r\n|\r|\n)/g, '<br />');

  					}


  					projectShowExampleResponse.options.results = resultarray;
  					projectShowExampleResponse.render();

	    		}).fail(function(response){ 

	 			  //     loadingCircleView.destroy();
					  // var errortext = Util.getErrorText(response);    
	      //             var errorView = new Show.Error({errortext:errortext})

	      //             App.mainLayout.showChildView('mainRegion',errorView);

	          }); //  $.when




			  });


  			// projectPanel = new Show.FooterPanel();


	          projectShowLayout.showChildView('headerRegion',projectShowHeader);
	          projectShowLayout.showChildView('infoRegion',projectShowInfo);
	          projectShowLayout.showChildView('respRegion',projectShowExampleResponse);
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
