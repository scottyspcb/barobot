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
	args = args || [];
	if(pp){
		pp[funcName].apply(pp, args);
	}else{
		pp = Processing.getInstanceById('maindrawing1');
	}		
};

var column = function( btn ){
	$(btn).toggleClass("active");
	runOnProc( "column" );
};var reverseY = function( btn ){
	$(btn).toggleClass("active");
	runOnProc( "reverseY" );
};
var dots = function( btn ){
	$(btn).toggleClass("active");
	runOnProc( "dots" );
};
var lines = function( btn ){
	$(btn).toggleClass("active");
	runOnProc( "lines" );
};

var toggleLocalMin = function(btn){
	$(btn).toggleClass("active");
	runOnProc( "toggleLocalMin" );
};
var sethighspeed = function(btn){
	$(btn).toggleClass("active");
	runOnProc( "sethighspeed" );
};
var changex = function( btn, val ){
	runOnProc( "changex", [ val ] );
};
var changefps = function( btn, val ){
	runOnProc( "changefps", [ val ] );
};
var clear = function(btn){
	runOnProc( "clear" );
};


var dimm = function( name ){
	if(pp){
		var ret = pp.toggleDimm( name );
		if( ret ){
			$(".dimm_" + name ).removeClass("disabled");
		}else{
			$(".dimm_" + name ).addClass("disabled");
		}
	}else{
		pp = Processing.getInstanceById('maindrawing1');
	}
};

var addDimm = function( name, color ){
	var btn	= '<a href="javascript:void(0)" id="dimm_'+ name +'" onclick="dimm('+name+')" class="btn">'+ name +'</a>';
	$(".colors").append( btn );

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
	setTimeout( simulate, 10 );
};
