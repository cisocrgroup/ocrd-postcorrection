 <div class="container" style="padding-bottom:50px;">
 <div class="row">
 <div class="col col-lg-12" >


 <table class="table table-bordered  dt-responsive no-footer dtr-inline collapsed custom-table red-border myhover" 
 id="table" cellspacing="0" width="100%"  style="margin-top: 20px !important; margin-bottom: 30px !important;" >


     <thead>
      <tr>
      <% _.each(headers, function(header) { %>  
        <th class="table_head <%-border_color%>-border-bottom <%-header_bg%>-light-bg"><%-header.name%></th>
       <% }); %>         
      </tr>
     </thead>
     <tbody>


     <%
     var count = 0;
     _.each(items, function(item) {
     
      %>

      <% if(columns[0]['id']=="index") { %>

       <tr class='clickable-row' data-href="#" data-idx ="<%-count%>">
       
       <% } else { %>

       <tr class='clickable-row' data-href="#<%-urlroot%>/<%-item[columns[0]['id']]%>">

       <% } %>

        <% for(var i=0;i<columns.length;i++){ %>  

          <% column=columns[i]; if(column.name=="topicacronym") { %>

          <td class="prop_icon-td">
          <div class="prop_icon">
            
        <% if (item['topicacronym'] == "WOOD") { %> <img src="assets/images/icons/woodicon.svg" > <% } %>
        <% if (item['topicacronym'] == "AGRI") { %> <img src="assets/images/icons/agriicon.svg" > <% } %>
        <% if (item['topicacronym'] == "ECO") { %> <img src="assets/images/icons/ecoicon.svg" > <% } %>
        <% if (item['topicacronym'] == "GEO") { %> <img src="assets/images/icons/landicon.svg" > <% } %>
        <% if (item['topicacronym'] == "H2O") { %> <img src="assets/images/icons/watericon.svg" > <% } %>
        <% if (item['topicacronym'] == "GLACIER") { %> <img src="assets/images/icons/glaciericon.svg" > <% } %>
        <% if (item['topicacronym'] == "SOIL") { %> <img  src="assets/images/icons/soilicon.svg" > <% } %>
        <% if (item['topicacronym'] == "HYDRO") { %> <img  src="assets/images/icons/hydroicon.svg" > <% } %>
        <% if (item['topicacronym'] == "PHYSIC") { %> <img src="assets/images/icons/physic_icon.svg" > <% } %>

        <div> <span class="prop_icon_title"> <%- item[column.name]%> </span> </div>

          </div> 
          </td>

          <% } else {%>

          <td> <%- item[column.name] %> </td>

          <% } %>


      

        <% } %>  
       
     
      </tr>

    <% count++;  }); %>

     </tbody>
    </table>


    </div>
    </div>
    </div>