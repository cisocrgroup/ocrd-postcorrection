// ================
// entities/util.js
// ================

define(["app"], function(App){

  var Entities={};

  var Test = Backbone.Model.extend({});
 

Entities.API = {


  postHalloWorld: function(data){
    console.log(data);
    var defer = jQuery.Deferred();
    $.ajax({
         headers: { 
        'Accept': 'application/json',
        'Content-Type': 'application/json' 
        },
        url: "/api/halloworld",
        type: "POST",
        data:JSON.stringify(data),
        success: function(data) {

            var result = new Test();
            result.set(data);

             defer.resolve(result);

            },
            error: function(data){
              defer.resolve(undefined);
            }
    });

    return defer.promise();
    
}


};



return Entities;

});
