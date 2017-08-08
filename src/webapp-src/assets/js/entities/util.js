// ================
// entities/util.js
// ================

define(["app"], function(IPS_App){

  var Entities={};

Entities.API = {

  getHelpText: function(route,role){
    var defer = jQuery.Deferred();
        $.ajax({
        url: "api/v1/help/"+route+"/"+role,
        type: "GET",
        success: function(data) {
           defer.resolve(data);

            },
            error: function(data){
              defer.resolve(undefined);
            }
    });


    return defer.promise();
    
},
  getCardHubData: function(data){
    var defer = jQuery.Deferred();
        $.ajax({
        headers: { 
        'Accept': 'application/json',
        'Content-Type': 'application/json' 
         },
        url: "api/v1/help/cardHubData/",
        type: "POST",
        dataType:"json",
        data:JSON.stringify(data),
        success: function(data) {
           defer.resolve(data['cardHubParent']);

            },
            error: function(data){
              defer.resolve(undefined);
            }
    });


    return defer.promise();
    
},

  getInterfaceData: function(data){
    var defer = jQuery.Deferred();
        $.ajax({
        headers: { 
        'Accept': 'application/json',
        'Content-Type': 'application/json' 
         },
        url: "api/v1/help/interface/",
        type: "POST",
        dataType:"json",
        data:JSON.stringify(data),
        success: function(data) {
           defer.resolve(data);

            },
            error: function(data){
              defer.resolve(undefined);
            }
    });


    return defer.promise();
    
},

  getInstitutions: function(userKey,userRole){
    var defer = jQuery.Deferred();
    $.ajax({
        url: "/api/v1/proposals/"+userKey+"/"+userRole+"/institutions",
        type: "GET",
        success: function(data) {
// #ifdef DEVELOPMENT_VERSION
           // console.log(data);
// #endif DEVELOPMENT_VERSION

             defer.resolve(data);

            },
            error: function(data){
              defer.resolve(undefined);
            }
    });

    return defer.promise();
    
},

  getTopics: function(userKey,userRole){
    var defer = jQuery.Deferred();
    $.ajax({
        url: "/api/v1/proposals/"+userKey+"/"+userRole+"/topics",
        type: "GET",
        success: function(data) {
// #ifdef DEVELOPMENT_VERSION
           // console.log(data);
// #endif DEVELOPMENT_VERSION

             defer.resolve(data);

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
