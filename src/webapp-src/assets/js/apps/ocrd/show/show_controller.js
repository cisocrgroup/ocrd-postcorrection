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

        	
			$.when().done(function(){
			loadingCircleView.destroy();

		 	//currentProposal.set({"url_id":id}); // pass url_id to view..
			var ocrdhowLayout = new Show.Layout();
			var ocrdhowHeader;
			var ocrdhowInfo;

			// console.log(reviews);
	
			ocrdhowLayout.on("attach",function(){
			  

			  ocrdhowHeader = new Show.Header();
			  ocrdhowInfo = new Show.Info();

  			// ocrdPanel = new Show.FooterPanel();



	          ocrdhowLayout.showChildView('headerRegion',ocrdhowHeader);
	          ocrdhowLayout.showChildView('infoRegion',ocrdhowInfo);



    		}); // on.attach()

          App.mainLayout.showChildView('mainRegion',ocrdhowLayout);


    		}).fail(function(response){ 

 			  //     loadingCircleView.destroy();
				  // var errortext = Util.getErrorText(response);    
      //             var errorView = new Show.Error({errortext:errortext})

      //             errorView.on("currentproposal:loggedIn",function(){
					 //    IPS_App.ocrdApp.Show.Controller.showProposal(id);
      //             });

      //             IPS_App.mainLayout.showChildView('mainRegion',errorView);

          }); //  $.when

    	}) // require
    	
		}

	}


return Controller;

});
