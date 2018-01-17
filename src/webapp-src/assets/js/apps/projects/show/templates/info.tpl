


<div class="container">
<div class="row">

   <div class="col-md-5">
   	<% console.log(project)%>
    <h4> <b>Project Information:</b>  </h4> 

  </div>
   <div class="col-md-7">
   </div>

</div>

<div class="row">
<div class="col-lg-12">

<div style="margin-bottom:15px">

	<div>
	<b>Title : </b> <%-project.title%>
	</div>

	<div>
	<b>Author : </b> <%-project.author%>
	</div>

	<div>
	<b>Year : </b> <%-project.year%>
	</div>

	<div>
	<b>Language : </b> <%-project.language%>
	</div>


	<div>
	<b>User : </b> <%-project.user%>
	</div>

<hr>

</div>

</div>
</div>

<div class="row">
<div class="col-lg-12">

<h4> <b>Books:</b>  </h4> 


<button type="submit" class="btn back_btn js-add-book" style="margin-bottom:15px"> <i class="fa fa-plus" aria-hidden="true"></i> Add Book</button>

</div>
</div>

<table class="table table-bordered table-hover" 
 id="book_table" cellspacing="0" width="100%"  style="margin-top: 20px !important; margin-bottom: 30px !important;" >

 <thead>
      <tr>
        <th>Title</th>
        <th>Author</th>
        <th>Language</th>
        <th>Ocr Engine</th>

      </tr>
 </thead>
 <tbody>

 	<% _.each(project.books, function(book) { %>
 	<tr>  
        <th><%-book.book.title%></th>
        <th><%-book.book.author%></th>
        <th><%-book.book.language%></th>
        <th><%-book.book.ocrEngine%></th>

    </tr>
    <% }); %>   

 </tbody>

</table>

</div>

