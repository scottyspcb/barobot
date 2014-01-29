var pp = Processing.getInstanceById('maindrawing1');
function sendToDraw( input ){
	if(pp){
		pp.readInput( input );
	}else{
		pp = Processing.getInstanceById('maindrawing1');
	}
}
function oscyloskop_interval( input ){
	if(pp){
		pp.new_interval();
	}else{
		pp = Processing.getInstanceById('maindrawing1');
	}
}

var runOnProc = function( funcName, args){
	console.log(funcName + ""+args)
	args = args || [];
	if(pp){
		pp[funcName].apply(pp, args);
	}else{
		pp = Processing.getInstanceById('maindrawing1');
	}		
};
var column = function(val){
	runOnProc( "column", [ val ] );
};
var reverseY = function(val){
	runOnProc( "reverseY", [ val ] );
};
var dots = function(val){
	runOnProc( "dots", [ val ] );
};
var lines = function(val){
	runOnProc( "lines", [ val ] );
};
var toggleLocalMin = function( val ){
	runOnProc( "toggleLocalMin", [ val ] );
};
var sethighspeed = function(val){
	runOnProc( "sethighspeed", [ val ] );
};
var changex = function( val ){
	runOnProc( "changex", [ val ] );
};
var changefps = function( val ){
	runOnProc( "changefps", [ val ] );
};

var tt			= null;
var random_on	= false;
var show_random = function( speed ){
	if( random_on || speed == '0' ||speed == 0 ){
		clearTimeout(tt);
		tt = null;
		random_on = false;
	}else{
		random_on = true;
		tt = setTimeout( simulate, speed );
	}
};

var add = 0;
var simulate = function(){
	var ttt1 = Math.round((Math.random()) * 500 +add )-250
	//var ttt2 = Math.round(Math.random() * 200)-150;
	
	add++;
//	var ttt3 = Math.round(Math.random() * 300)-250;
//	var ttt4 = Math.round(Math.random() * 400)-250;
	//var inp	= ttt1 + "," + ttt2+ "," + ttt3+ "," + ttt4
//	var inp	= ttt1 + "," + ttt2;
	var inp	= "" + ttt1;
	sendToDraw( inp );
	if(random_on){
		tt = setTimeout( simulate, 10 );
	}
};
