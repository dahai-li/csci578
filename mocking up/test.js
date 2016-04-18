$('document').ready(function() {
	$('#myform').submit(function() {
		$.ajax({
			url: "test.php",
			type: "GET",
			cache: false,
			success: function(response, status) {
				takeAction()
			}
		});
		return false;
	});
});
