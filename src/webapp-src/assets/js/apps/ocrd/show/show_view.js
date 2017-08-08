// ================================
// apps/ocrd/show/show_view.js
// ================================

define(["marionette","app","common/views",
        "tpl!apps/ocrd/show/templates/layout.tpl",
        "tpl!apps/ocrd/show/templates/info.tpl"

  ], function(Marionette,App,Views,layoutTpl,infoTpl){


    var Show = {};

  Show.Layout = Marionette.View.extend({
    template:layoutTpl,
    regions:{
       headerRegion: "#hl-region"
      ,infoRegion: "#info-region"
      ,buttonPanelRegion: "#button-panel-region"
      ,reviewsRegion: "#reviews-region"
      ,panelRegion: "#panel-region"
    }

  });


  Show.Header = Views.Header.extend({
    initialize: function(){
        this.title = "OCRD Prototype Tests"
      }
  });



  Show.Info = Marionette.View.extend({
      template: infoTpl,
     
     onAttach: function(){
      
      }          

  });


 
	Show.Missingocrd = Views.Error.extend({errortext:"Error 404: ocrd not found"});


return Show;

});

