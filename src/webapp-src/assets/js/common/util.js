// ==============
// common/util.js
// ==============

define({

// ===================
// getRolesAvailable()
// ===================
getRolesAvailable: function(userRoles)
{


 var portal_roles_available={proposal_portal:[],or_portal:[],cr_portal:[],user_portal:[]};
 

var portal_roles = {proposal_portal:["Cat-1","PI-AO","CO-I","Reviewer"]
         ,or_portal:["Cat-1","Charter","Background","Internal","Cat-2","Cat-2-Distributor","Cat-1-Distributor"]
         ,cr_portal:["Internal"]
         ,user_portal:["applicationSupport"]

        }



         for (var key in portal_roles){
          portal = portal_roles[key];

          for (var i = 0; i < portal.length; i++) {

             for (userRoleKey in userRoles.roles){
              var userRole = userRoles.roles[userRoleKey]['role'];

              if(userRole == portal[i]){
                portal_roles_available[key].unshift(userRole);
              }
             }

          }

         }
  return portal_roles_available;

}, // getUrlInfo()

getRoleArray: function(userRoles){

            var result = [];

            for(key in userRoles.roles){

                var obj = userRoles.roles[key];
                result.push(obj.role);

            }
    return result;

},

 getSortedCards: function(obj) {
  var result=[];
    var keys = []; for(var key in obj) keys.push(key);
    keys.sort(function(a,b){return obj[a].seq-obj[b].seq});
    for(key in keys){
      result.push(obj[keys[key]]);
    }
    return result;
},   
// ==============
// getErrorText()
// ==============
getErrorText: function(response) {
  return "Error "+response.status+": "+response.responseJSON.errorMessage;  
},

getSymbolByTopic: function(topic){

var result; 

         if (topic == "WOOD") {     result="assets/images/icons/woodicon.svg" } 
         if (topic == "AGRI") {     result="assets/images/icons/agriicon.svg" } 
         if (topic == "ECO") {      result="assets/images/icons/ecoicon.svg"   } 
         if (topic == "GEO") {      result="assets/images/icons/landicon.svg"   } 
         if (topic == "H2O") {      result="assets/images/icons/watericon.svg"   } 
         if (topic == "GLACIER") {  result="assets/images/icons/glaciericon.svg"    } 
         if (topic == "SOIL") {     result="assets/images/icons/soilicon.svg"    } 
         if (topic == "HYDRO") {    result="assets/images/icons/hydroicon.svg"    } 
         if (topic == "PHYSIC") {   result="assets/images/icons/physic_icon.svg"    } 


return result;

},

getMarkerIconByTopic: function(topic){


 var LeafIcon = L.Icon.extend({

    options: {
    iconSize:     [38, 50.6],
    iconAnchor:   [19, 50.6],
    popupAnchor:  [0, -50.6]
       }
  });

var result; 

if (topic == "WOOD") {     result= new LeafIcon({iconUrl: 'assets/images/markers/woodmarker.svg'}); } 
if (topic == "AGRI") {     result= new LeafIcon({iconUrl: 'assets/images/markers/agrimarker.svg'}); } 
if (topic == "ECO") {      result= new LeafIcon({iconUrl: 'assets/images/markers/ecomarker.svg'});  } 
if (topic == "GEO") {      result= new LeafIcon({iconUrl: 'assets/images/markers/landmarker.svg'}); } 
if (topic == "H2O") {      result= new LeafIcon({iconUrl: 'assets/images/markers/watermarker.svg'}); } 
if (topic == "GLACIER") {  result= new LeafIcon({iconUrl: 'assets/images/markers/glaciermarker.svg'});   } 
if (topic == "SOIL") {     result= new LeafIcon({iconUrl: 'assets/images/markers/soilmarker.svg'});   } 
if (topic == "HYDRO") {    result= new LeafIcon({iconUrl: 'assets/images/markers/hydromarker.svg'});    } 
if (topic == "PHYSIC") {   result= new LeafIcon({iconUrl: 'assets/images/markers/physicmarker.svg'});  } 


return result;

},

getTopics: function(){

 return    ["AGRI",
            "ECO",
            "GEO",
            "GLACIER",
            "H2O",
            "HYDRO",
            "PHYSIC",
            "SOIL",
            "WOOD"];
            
}


});
