


<div class="container">
<div class="row">

   <div class="col-md-5">


    <div style="margin-bottom:15px">

    <button type="button" class="btn js-edit-project"> <i class="fa fa-pencil-square-o" aria-hidden="true"></i> Edit</button>
    <button type="button" class="btn js-delete-project btn-danger"><i class="fa fa-trash-o" aria-hidden="true"></i>Delete</button>
    <button type="button" class="btn js-add-book"> <i class="fa fa-plus" aria-hidden="true"></i> Add Book</button>

    </div>


    <h4> <b>Project Information:</b>  </h4> 


  </div>
   <div class="col-md-7">
   </div>

</div>

<div class="row">
<div class="col-lg-12">

<div style="margin-bottom:15px">

	<div>
	<b>Title : </b> <%-title%>
	</div>

	<div>
	<b>Author : </b> <%-author%>
	</div>

	<div>
	<b>Year : </b> <%-year%>
	</div>

	<div>
	<b>Language : </b> <%-language%>
	</div>


	<div>
	<b>User : </b> <%-user%>
	</div>

<hr>

</div>

</div>
</div>

<div class="row">
<div class="col-lg-12">

<h4> <b>Books:</b>  </h4> 


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

 	<% _.each(books, function(book) { %>
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

