function messageFromAndroid( command, name, value ) {
}
function showAndroidToast(toast) {
	AJS.showToast(toast);
}
function putIn(selector, html) {
	setTimeout( function(){
		var affected = jQuery(selector).html(html);
	},10);
}

$(document).ready(function(){});
(function() {
	AJS.onLoad();
})();