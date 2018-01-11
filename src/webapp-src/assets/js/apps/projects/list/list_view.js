// ================================
// apps/projects/list/list_view.js
// ================================

define(["marionette","app","common/views","apps/projects/common/views"], function(Marionette,IPS_App,CommonViews,Views){

  var List ={};

  List.Layout = CommonViews.Layout.extend({    
  });

  
    List.Header = CommonViews.Header.extend({
    initialize: function(){
        this.title = "OCR Projects"
        this.icon ="fa fa-th-list"
        this.color ="red"
      }
  });


  List.projectsList = Views.projectsList.extend({
   
  });

  List.FooterPanel = CommonViews.FooterPanel.extend({
    });



return List;

});
