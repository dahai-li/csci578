$('document').ready(function() {
	$('#myform').submit(function() {
		startSpinner()
		$.ajax({
			url: "test.php",
			type: "GET",
			cache: false,
			success: function(response, status) {
				takeAction()
				stopSpinner()
			},
			error: function(jqXHR, textStatus, errorThrown) {
				stopSpinner()
			}
		});
		return false;
	});
});
