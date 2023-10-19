<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css">
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js"></script>
		<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js"></script>
		<script src="<?=base_url('jquery.md5.js')?>"> </script>
	</head>
	<body>
	<div class="container">
		<div class="row">
			<div class="offset-4 col-4">
				<h3 class="mt-4">Login</h3>
				<div class="alert alert-danger" role="alert" hidden>
				</div>
				<form action="" id="login">
				<div class="form-group">
					<label>Username</label>
					<input type="type" class="form-control" placeholder="Enter username" name="name" value="">
				</div>
				<div class="form-group">
					<label>Password</label>
					<input type="password" class="form-control" placeholder="Password" name="password" value="">
				</div>
				<div>
					<br>
					<button id="btnSubmit" type="button" class="btn btn-primary">Submit</button>
				</div>
				</form>
				<br>
			</div>
		</div>
	</div>
<script type="text/javascript">
$(document).ready(function() {
	
	$('#btnSubmit').click(function(){
		var myForm = document.getElementById('login');
		var dataSend = new FormData(myForm);
		dataSend.set('password', $.MD5($.MD5(dataSend.get('password'))));
        $.ajax({	
            url:'<?php echo base_url('user/logincheck') ?>',
            data: dataSend,
			processData: false,
			contentType: false,
            method: 'POST',
            dataType: 'json', 
            success: function(result){
				if(!result.status){
					alert = '<li>'+result.message+'</li>';
					console.log(alert);
					$('.alert').html(alert);
					$('.alert').attr('hidden', false);
				} else {
					window.location.replace("<?php echo base_url('admin') ?>");
				}
            }
        });
    });
	
	$('#login').submit(function (evt) {
		evt.preventDefault();
	});
});
</script>
	</body>
</html>

