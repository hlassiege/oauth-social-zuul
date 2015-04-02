<html>
<head>
<link rel="stylesheet" href="css/wro.css"/>
</head>
<body>
<#if RequestParameters['error']??>
	<div class="alert alert-danger">
		There was a problem logging in. Please try again.
	</div>
</#if>
	<div class="container">

        <div class="signin row-fluid">
            <div class="span12">
                <form name="fb_signin" accept-charset="UTF-8" action="signin/facebook" class="form-horizontal" method="POST">
                    <input type="hidden" name="scope" value="email,user_photos">
                    <button class="btn-social btn-facebook">facebook</button>
                </form>
            </div>
        </div>

		<form role="form" action="login" method="post">
		  <div class="form-group">
		    <label for="username">Username:</label>
		    <input type="text" class="form-control" id="username" name="username"/>
		  </div>
		  <div class="form-group">
		    <label for="password">Password:</label>
		    <input type="password" class="form-control" id="password" name="password"/>
		  </div>
		  <button type="submit" class="btn btn-primary">Submit</button>
		</form>
	</div>
</body>
</html>