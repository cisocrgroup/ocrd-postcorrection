// ======================================
// apps/ocrd/show/show_controller.js
// ======================================

define(["app","common/util","common/views","apps/ocrd/show/show_view"], function(App,Util,Views,Show){


 Controller = {

		showOcrd: function(id){
      		$(window).scrollTop(0);

	   		require(["entities/util"], function(UtilEntitites){

	   	      var loadingCircleView = new  Views.LoadingBackdropOpc();
              App.mainLayout.showChildView('backdropRegion',loadingCircleView);

        	
			loadingCircleView.destroy();

		 	//currentProposal.set({"url_id":id}); // pass url_id to view..
			var ocrdShowLayout = new Show.Layout();
			var ocrdShowHeader;
			var ocrdShowInfo;

			// console.log(reviews);
	
			ocrdShowLayout.on("attach",function(){
			  

			  ocrdShowHeader = new Show.Header();
			  ocrdShowInfo = new Show.Info();

			  ocrdShowInfo.on("show:formSubmitted",function(data){

			  	var postingHalloWorld = UtilEntitites.API.postHalloWorld(data); 

  				$.when(postingHalloWorld).done(function(test){


  					var ocrdShowExampleResponse = new Show.Resp({model:test})
	         		ocrdShowLayout.showChildView('respRegion',ocrdShowExampleResponse);

	    		}).fail(function(response){ 

	 			  //     loadingCircleView.destroy();
					  // var errortext = Util.getErrorText(response);    
	      //             var errorView = new Show.Error({errortext:errortext})

	      //             App.mainLayout.showChildView('mainRegion',errorView);

	          }); //  $.when




			  });


  			// ocrdPanel = new Show.FooterPanel();


	          ocrdShowLayout.showChildView('headerRegion',ocrdShowHeader);
	          ocrdShowLayout.showChildView('infoRegion',ocrdShowInfo);



    		}); // on.attach()

          App.mainLayout.showChildView('mainRegion',ocrdShowLayout);



    	}) // require
    	
		}

	}


return Controller;

});
