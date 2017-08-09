// ================================
// apps/ocrd/show/show_view.js
// ================================

define(["marionette","app","backbone.syphon","common/views",
        "tpl!apps/ocrd/show/templates/layout.tpl",
        "tpl!apps/ocrd/show/templates/info.tpl",
        "tpl!apps/ocrd/show/templates/resp.tpl"


  ], function(Marionette,App,BackboneSyphon,Views,layoutTpl,infoTpl,respTpl){


    var Show = {};

  Show.Layout = Marionette.View.extend({
    template:layoutTpl,
    regions:{
       headerRegion: "#hl-region"
      ,infoRegion: "#info-region"
      ,respRegion: "#resp-region"
      ,footerlRegion: "#footer-region"
    }

  });


  Show.Header = Views.Header.extend({
    initialize: function(){
        this.title = "OCRD Prototype Tests"
      }
  });



  Show.Info = Marionette.View.extend({
      template: infoTpl,
      events: {
       "click .js-submit": "formSubmitted"
      },

      formSubmitted:function(e){
        e.preventDefault();
        var data = Backbone.Syphon.serialize(this);

        this.trigger("show:formSubmitted",data);

      },
     onAttach: function(){
      
      },          

  });

  Show.Resp = Marionette.View.extend({
      template: respTpl,
      events: {
       "click .js-submit": "formSubmitted"
      },

      formSubmitted:function(e){
        e.preventDefault();
        var data = Backbone.Syphon.serialize(this);

        this.trigger("show:formSubmitted",data);

      },
     onAttach: function(){
      
      },          

  });


 
	Show.Missingocrd = Views.Error.extend({errortext:"Error 404: ocrd not found"});


return Show;

});

