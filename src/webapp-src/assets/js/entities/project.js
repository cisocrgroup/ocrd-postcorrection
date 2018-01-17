// ================
// entities/projects.js
// ================

define(["app"], function(IPS_App){

  var Entities={};

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




};



return Entities;

});
