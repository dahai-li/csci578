var spinner = null;
function startSpinner() {
    var opts = {
	    lines: 12, // The number of lines to draw
	    length: 10, // The length of each line
	    width: 4, // The line thickness
	    radius: 10, // The radius of the inner circle
	    color: '#000', // #rgb or #rrggbb
	    speed: 1, // Rounds per second
	    trail: 60, // Afterglow percentage
	    shadow: false// Whether to render a shadow
    };
 
var target = document.getElementById('spinner');
	if (spinner == null) {
		spinner = new Spinner(opts).spin(target);
	}
}
 
function stopSpinner() {
	if (spinner != null) {
		spinner.stop();
	    spinner = null;
	}
}