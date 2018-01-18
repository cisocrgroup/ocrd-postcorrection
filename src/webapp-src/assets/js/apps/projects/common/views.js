// ==============================
// apps/projects/common/views.js
// ==============================

define(["app","marionette","backbone.syphon",
        "common/util","common/views"
        ,"tpl!apps/projects/common/templates/projectform.tpl"

        	], function(IPS_App,Marionette,Syphon,Util,CommonViews,projectTpl){


var Views = {};




 Views.ProjectsList = CommonViews.Icon_DataTable.extend({
   initialize: function(){
        this.urlroot="projects",
        this.border_color="red",
        this.datatable_options={stateSave:true},
        this.headers = [
          {name: "Title"},
          {name: "Author"},
          {name: "Language"},
          {name: "Year"},
          {name: "Pages"},


        ]

        this.columns = [
        {name:"title",id:"projectId"},
        {name:"author",id:"projectId"},
        {name:"language",id:"projectId"},
        {name:"year",id:"projectId"},
        {name:"pages",id:"projectId"},


        ]


        }

  });

Views.ProjectForm = Marionette.View.extend({
   template: projectTpl,
   className:"",
   events: {
   "click .js-submit-project": "submitClicked",
   "click .js-upload": "chooseFile",
  

   },
   initialize: function(){

  },
  onAttach : function(){

     if(this.options.asModal){

          this.$el.attr("ID","projects-modal");
          this.$el.addClass("modal fade projects-modal");
          this.$el.on('shown.bs.modal', function (e) {
           })

    }

  },

   submitClicked: function(e){
     e.preventDefault();
      var data = Backbone.Syphon.serialize(this);
       var topic = $('#topic_id').find(":selected").text();
        data['topic'] = topic;
     // var checkBox =$('#notify').is(':checked');
     // var checkBoxValue=0;
     // if(checkBox) checkBoxValue=1;
     // data['notify'] = checkBoxValue;

     //   checkBox =$('#verified_checkbox').is(':checked');
     //   checkBoxValue=0;
     // if(checkBox) checkBoxValue=1;
     // data['verified_checkbox'] = checkBoxValue;
    this.trigger("proposal:submit_clicked", data);
   },

   chooseFile: function(e){
      e.preventDefault();

      Util.openFile(e)

   },

 

    serializeData: function(){


          var data = Backbone.Marionette.View.prototype.serializeData.apply(this, arguments);
          data.asModal = Marionette.getOption(this,"asModal");
          data.text = Marionette.getOption(this,"text");
          data.topics = Marionette.getOption(this,"topics");


        return data;

    },

   });






return Views;


});
