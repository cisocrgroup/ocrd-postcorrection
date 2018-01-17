// ================================
// apps/projects/show/show_view.js
// ================================

define(["marionette","app","backbone.syphon","common/views",
        "tpl!apps/projects/show/templates/layout.tpl",
        "tpl!apps/projects/show/templates/info.tpl",
        "tpl!apps/projects/show/templates/resp.tpl"


  ], function(Marionette,App,BackboneSyphon,Views,layoutTpl,infoTpl,respTpl){


    var Show = {};

  Show.Layout = Marionette.View.extend({
    template:layoutTpl,
    regions:{
       headerRegion: "#hl-region"
      ,infoRegion: "#info-region"
      ,respRegion: "#resp-region"
      ,footerRegion: "#footer-region"
    }

  });


  Show.Header = Views.Header.extend({
    initialize: function(){
        this.title = "projects Prototype Tests"
      }
  });



  Show.Info = Marionette.View.extend({
      template: infoTpl,
      events:{
      'click .js-train' : 'training_clicked'
      },
     

      training_clicked:function(e){
        e.preventDefault();
        methods = [];

          $('.method_select').children().each(function(){

              if($(this).is(':checked')){
                methods.push("{type:"+$(this).val()+"}")

              }
        
        })

      var that = this;
         $('.loading_background').fadeIn(function(){
          that.trigger("show:training_clicked",methods);
          })
        

      },
     onAttach: function(){
       var table = $('#book_table').DataTable();

      },
     serializeData: function(){
       return {
       project: Marionette.getOption(this,"project")
       }
      }          

  });

  Show.Resp = Marionette.View.extend({
      template: respTpl,


      formSubmitted:function(e){
        e.preventDefault();
        var data = Backbone.Syphon.serialize(this);

        this.trigger("show:formSubmitted",data);

      },
     onDomRefresh: function(){
      
      results = Marionette.getOption(this,"results")
      console.log(results)
         if(results.length==0){
          $('.loading_background').fadeOut(function(){
              });
          }
          else {
            $("#tabs").tab();
            $('.loading_background').hide();
          }

      },  
      serializeData: function(){
       return {
       results: Marionette.getOption(this,"results")
       }
      }
        

  });


Show.FooterPanel = Views.FooterPanel.extend({
    });

 
	Show.Missingprojects = Views.Error.extend({errortext:"Error 404: projects not found"});


return Show;

});

