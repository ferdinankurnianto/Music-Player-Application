<!DOCTYPE html>
<html>
    <head> 
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Simple Database Lagu Pujian</title>
	<script src="https://code.jquery.com/jquery-3.6.3.min.js" integrity="sha256-pvPw+upLPUjgMXY0G+8O0xUf+/Im1MZjXxxgOcBQBXU=" crossorigin="anonymous"></script>
    <link href="//cdn.datatables.net/1.13.1/css/jquery.dataTables.min.css" rel="stylesheet">
	<script src="//cdn.datatables.net/1.13.1/js/jquery.dataTables.min.js"></script>
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css">
		<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js"></script>
    </head> 
<body>
    <div class="container-fluid">
        <h1 style="font-size:20pt">Database Lagu Pujian</h1>
 
        <h3>Lagu Pujian</h3>
        <br />
        <nav class="row navbar navbar-expand navbar-light pr-5 mr-0" id="nav">
			<div class="my-3">
				<button id="btnAdd" class="btn btn-primary" data-toggle="modal" data-target="#mdEdit"><i class="fa fa-plus-circle" aria-hidden="true"></i> Add</button>
			</div>
			<div class="ml-auto" style="text-align:center;">
				<a href="<?php echo base_url('logout') ?>" class="btn btn-primary">Log Out</a>
			</div>
		</nav>

        <table id="table" class="display" cellspacing="0" width="100%">
            <thead>
                <tr>
                    <th>Id</th>
                    <th>No</th>
                    <th>Title</th>
                    <th>Tipe</th>
                    <th>Penulis</th>
                    <th>Singer</th>
                    <th>Category</th>
                    <th>Year</th>
                    <th>Lyric</th>
                    <th>Image</th>
                    <th>File</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
            </tbody>
 
            <tfoot>
                <tr>
                    <th>Id</th>
                    <th>No</th>
                    <th>Title</th>
                    <th>Tipe</th>
                    <th>Penulis</th>
                    <th>Singer</th>
                    <th>Category</th>
                    <th>Year</th>
                    <th>Lyric</th>
                    <th>Image</th>
                    <th>File</th>
                    <th>Actions</th>
                </tr>
            </tfoot>
        </table>
		
		<div id="mdEdit" class="modal fade" tabindex="-1" role="dialog">
		  <div class="modal-dialog modal-lg" role="document">
			<div class="modal-content">
			  
			  <div class="modal-header">
				<h5 class="modal-title">Lagu Pujian</h5>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
				  <span aria-hidden="true">&times;</span>
				</button>
			  </div>
			  
			  <div class="modal-body">
				<form id="formEdit" method="POST" action="" enctype="multipart/form-data">
			
				  <input type="hidden" id="id" name="id" value="">

				  <div class="form-group">
					<label>Title</label>
					<input type="type" class="form-control" id="title" name="title" value="">
				  </div>
				  <div class="form-group">
					<label>Tipe Lagu</label>
					<select id="type" name="type" class="form-control">
						<option value="" disabled hidden>== Please select ==</option>
					</select>
				  </div>
				  <div class="form-group">
					<label>Penulis</label>
					<input type="type" class="form-control" id="artist" name="artist" value="">
				  </div>
				  <div class="form-group">
					<label>Singer</label>
					<input type="type" class="form-control" id="singer" name="singer" value="">
				  </div>
				  <div class="form-group">
					<label>Category</label>
					<input type="type" class="form-control" id="category" name="category" value="">
				  </div>
				  <div class="form-group">
					<label>Year</label>
					<input type="type" class="form-control" id="year" name="year" value="">
				  </div>
				  <div class="form-group">
					<label>Lyric</label>
					<textarea class="form-control" rows="3" cols="60" id="lyric" name="lyric"></textarea>
				  </div>
				  <div class="form-group">
					<label>Image</label>
					<input type="file" class="form-control" id="image" name="image">
				  </div>
				  <div class="form-group">
					<label>File</label>
					<input type="file" class="form-control" id="file" name="file">
				  </div>
				</form>
			  </div>
			  
			  <div class="modal-footer">
				<button id="btnClose" type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
				<button id="btnSubmit" type="button" class="btn btn-primary">Submit</button>
			  </div>
			</div>
		  </div>
		</div>

		<div id="mdDelete" class="modal fade" tabindex="-1" role="dialog">
			<div class="modal-dialog modal-lg" role="document">
				<div class="modal-content">
				  <div class="modal-header">
					<h5 class="modal-title2">Delete</h5>
					<button type="button" class="close" data-dismiss="modal" aria-label="Close">
					  <span aria-hidden="true">&times;</span>
					</button>
				  </div>
				  <div class="modal-body2">
					  <form id="formDelete" method="POST" action="" enctype="multipart/form-data">
				
					  <input type="hidden" id="idDel" name="id" value="">
					  </form>
					  <a class="m-1">Are you sure you want to delete this row?</a>
				  </div>
				  <div class="modal-footer">
					<button id="btnCls" type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
					<button id="btnYes" type="button" class="btn btn-primary">Yes</button>
				  </div>
				</div>
			</div>
		</div>
		
    </div>

<script type="text/javascript">
 
var table;
 
$(document).ready(function() {
	populateType();

	var writepath = 'localhost/gkimusicplayer/writable'
    table = $('#table').DataTable({
		
        ajax: `<?php echo base_url('lagupujian/getall') ?>`,
        columnDefs: [
            {
                targets: 0,
                visible: false,
                searchable: false,
            },
            {
                targets: -1, render: function(data, type, row, meta){
					var id = row[0];
					return '<button id ="edit" data-id="'+id+'" class="btn btn-primary" data-toggle="modal" data-target="#mdEdit">Edit</button> '+
					'<button id ="delete" data-id="'+id+'" class="btn btn-primary" data-toggle="modal" data-target="#mdDelete">Delete</button>'
				}
            },
			{
                targets: -2, render: function(data, type, row, meta){
					var file = '/'+row[10];
					return '<audio controls> <source src="'+`<?php echo base_url('song') ?>`+file+'"type="audio/mpeg">'+
						'Your browser does not support the audio element. </audio>'
				}
            },
			{
                targets: -3, render: function(data, type, row, meta){
					var img = '/'+row[9];
					return '<img src="'+`<?php echo base_url('img') ?>`+img+'" width="100" height="100">'
				}
            },
        ],
    });
	
	$(document).on('click', '#edit', function(){
		$('.modal-title').html('Edit Lagu Pujian');
		var id = $(this).attr("data-id");
		$.ajax({
			data: {id},
            url:'<?php echo base_url('lagupujian/get') ?>',
            method: 'GET',
            dataType: 'json', 
            success: function(result){
					$('#id').attr("value", result.id);
					$('#title').attr("value", result.title);
					document.querySelector('option[value = "'+result.type+'"]').setAttribute("selected", "selected");
					$('#artist').attr("value", result.artist);
					$('#singer').attr("value", result.singer);
					$('#category').attr("value", result.category);
					$('#year').attr("value", result.year);
					$('#lyric').html(result.lyric);
					$('#image').attr("value", result.image_url);
					$('#file').attr("value", result.file_url);
			}
		});
	});
	
	$(document).on('click', '#delete', function(){
		var id = $(this).attr("data-id");
		$("#idDel").attr("value", id);
	});
	
	$('#btnSubmit').click(function(){
		var myForm = document.getElementById('formEdit');
		var dataSend = new FormData(myForm);
		var id = $('#id').val();
		if(id){
			$.ajax({
				url:'<?php echo base_url('lagupujian/edit') ?>',
				data:dataSend,
				processData: false,
				contentType: false,
				method:'POST',
				dataType:'JSON',
				success:function(result){
					var alert = '<div class="alert alert-danger" role="alert">';
					if(result.status){
						$('.modal-body').html('<div class="alert alert-primary" role="alert">'
						+result.message+'</div>');
						$('#btnSubmit').hide();
						$('#btnClose').attr('onclick','window.location.reload()');
					} else {
						$.each(result.message, function(index, value){
							alert += '<li>'+value+'</li>';
						});
						$('.modal-body').prepend(alert+'</div>');
					}
				}
			})
		} else {
			$.ajax({
				url:'<?php echo base_url('lagupujian/add') ?>',
				data:dataSend,
				processData: false,
				contentType: false,
				method:'POST',
				dataType:'JSON',
				success:function(result){
					var alert = '<div class="alert alert-danger" role="alert">';
					if(result.status){
						$('.modal-body').html('<div class="alert alert-primary" role="alert">'
						+result.message+'</div>');
						$('#btnSubmit').hide();
						$('#btnClose').attr('onclick','window.location.reload()');
					} else {
						$.each(result.message, function(index, value){
							alert += '<li>'+value+'</li>';
						});
						$('.modal-body').prepend(alert+'</div>');
					}
				}
			})
		}
	});
	
	$('#btnYes').click(function(){
		var id = $("#idDel").val();
		
        $.ajax({	
            data: {id},
            url:'<?php echo base_url('lagupujian/delete') ?>',
            method: 'POST',
            dataType: 'json', 
            success: function(result){
				var alert = '<div class="alert alert-danger" role="alert">';
				if(result.status){
					$('.modal-body2').html('<div class="alert alert-primary" role="alert">'
					+result.message+'</div>');
					$('#btnYes').hide();
					$('#btnCls').attr('onclick','window.location.reload()');
				} else {
					$.each(result.message, function(index, value){
						alert += '<li>'+value+'</li>';
					});
					$('.modal-body').prepend(alert+'</div>');
				}
            }
        });
    });
	
	$('#btnAdd').click(function(){
		$('#formEdit :input').attr('value', '');
		$('#lyric').html('');
		$('.modal-title').html('Add Lagu Pujian');
	});

	function populateType(){

	$.ajax({	
		url: '<?php echo base_url('tipelagu') ?>',
		method: 'GET',
		dataType: 'json', 
		success: function(data){
			var select="";
			$.each(data, function (key, value) {
				select += '<option value="'+value.id+'">'+value.name+'</option>';
			});
			console.log(select);
			$('#type').append(select);
		}
	});
	}
});
</script>
 
</body>
</html>