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
     console.log(items);
     _.each(items, function(item) {
     
      %>

      <% if(columns[0]['id']=="index") { %>

       <tr class='clickable-row' data-href="#" data-idx ="<%-count%>">
       
       <% } else { %>

       <tr class='clickable-row' data-href="#<%-urlroot%>/<%-item[columns[0]['id']]%>">

       <% } %>

        <% for(var i=0;i<columns.length;i++){ %>  

          <% column=columns[i]; %>
    
          <td> <%- item[column.name] %> </td>      

        <% } %>  
       
     
      </tr>

    <% count++;  }); %>

     </tbody>
    </table>


    </div>
    </div>
    </div>