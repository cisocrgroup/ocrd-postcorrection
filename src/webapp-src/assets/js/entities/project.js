// ================
// entities/projects.js
// ================

define(["app"], function(IPS_App){

  var Entities={};

Entities.Project = Backbone.Model.extend({
     urlRoot: "api/v1/proposals",

     defaults:{
  author:null,
  books:null,
  language:null,
  profilerUrl:null,
  projectId:null,
  title:"",
  user:"",
  year:""
     }
  });


Entities.API = {


  getProjects: function(){
    var defer = jQuery.Deferred();
        $.ajax({
        
        url: "/api/projects/list",
        type: "GET",
        dataType:"json",
        success: function(data) {
          defer.resolve(data);

            },
            error: function(data){
              defer.resolve(undefined);
            }
    });


    return defer.promise();
    
},

getProject: function(id){
  var defer = jQuery.Deferred();
      $.ajax({
      
      url: "/api/projects/"+id,
      type: "GET",
      dataType:"json",
      success: function(data) {
        console.log(data)
        defer.resolve(data);

          },
          error: function(data){
            defer.resolve(undefined);
          }
  });


  return defer.promise();
  
},

createProject: function(data){
    var defer = jQuery.Deferred();
       $.ajax({
        headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
         },
        url: "api/projects/create",
        type: "POST",
        // type: "GET",
        data:JSON.stringify(data),
        dataType: "json",
        success: function(data) {
// #ifdef DEVELOPMENT_VERSION
          console.log(data);
// #endif DEVELOPMENT_VERSION
              defer.resolve(data);
            },
            error: function(data){
              defer.resolve(undefined);
            }
    });

    return defer.promise();
  },




};



return Entities;

});
