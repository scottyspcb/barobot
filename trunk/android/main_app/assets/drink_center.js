
var loading = false;
var drinkList = null;
var slots = null;
var loader;
loader = loader || (function () {
	var stop = false;
    return {
        showPleaseWait: function() {
			stop = false;
			setTimeout( function(){
				if(!stop){
					$(".fakeloader").fadeIn(1).fakeLoader({
							// Time in milliseconds for fakeLoader disappear
						timeToHide:10000,
						// 'spinner1', 'spinner2', 'spinner3', 'spinner4', 'spinner5', 'spinner6', 'spinner7'
						spinner:"spinner7",//Options:
						// Background color. Hex, RGB or RGBA colors
						bgColor:"#222222",	 
					});
				}
			}, 500 );
			//var pleaseWaitDiv = $('#pleaseWaitDialog');
            //pleaseWaitDiv.modal();
			//pleaseWaitDiv.modal('show');
        },
        hidePleaseWait: function () {
			stop = true;
			$(".fakeloader").fadeOut(1);
			//var pleaseWaitDiv = $('#pleaseWaitDialog');
            //pleaseWaitDiv.modal('hide');
        },
    };
})();

function loadDrinks(){
	loader.showPleaseWait();
	$.getJSON( "/api/get_recipes", function( data ){
		if( data.result && data.result.length > 0 ){
			drinkList = data.result;
			loader.hidePleaseWait();
			showList();
		}else{
			loader.hidePleaseWait();
			alert("Drink list is empty. Add new ingrediengs or set up new drinks using creator.");
		}
	});
}

function showList(){
	var window_drink_list	= $("#window_drink_list");
	var drink_list			= $("#drink_list");
	drink_list.empty();
	for(var i =0;i<drinkList.length;i++){
		var size = getSize(drinkList[i]);
		var tpl = '<div class="drink_item" id="drink_'+drinkList[i].id+'" drink_id="'+drinkList[i].id+'" onclick="selectDrink( this )">'+drinkList[i].name+'<span class="label label-default  pull-right">'+size+'ml</span></div>';
		drink_list.append(tpl);
	}
	drink_list.trigger("appened");
	$("#window_main_question").hide(500);
	window_drink_list.show(500);
}

function drink_lists(){
	loadDrinks();
}
function selectDrink( btn ){
	if(loading){
		return;
	}
	var drink_id			= $(btn).attr( "drink_id" );
	var drink_ingredients	= $("#drink_ingredients");
	var drink				= getDrinkById(drink_id);
	var drink_details		= $("#drink_details");
	if(drink){
		if(drink_details.is(":hidden")){
			drink_details.show(500);
		}
		$("#drink_name").text(drink.name);
		var ings = drink.ingredients;
		drink_ingredients.empty();
		for( k=0; k<ings.length;k++){
			var ing = ings[k];
			var tpl = '<div class="ing_item" id="ing_'+ing.id+'" liquid_id="'+ing.liquid_id+'">'+ing.name+'<span class="badge pull-right">'+ing.quantity+'ml</span></div>';
			drink_ingredients.append(tpl);
		}
		$("#doDrinkButton").attr("drink_id", drink_id );
		drink_ingredients.trigger("appened");
		
		var taste_sweet = drink.taste.sweet;
		var taste_sour = drink.taste.sour;	
		var taste_bitter = drink.taste.bitter;
		var taste_strenght = drink.taste.strenght;
		taste_strenght = (taste_strenght / 40) * 100;		// 40% alc. = 100% width
		$('#taste_sweet').css('width', taste_sweet+'%').attr('aria-valuenow', taste_sweet);
		$('#taste_sour').css('width', taste_sour+'%').attr('aria-valuenow', taste_sour);  	  
		$('#taste_bitter').css('width', taste_bitter+'%').attr('aria-valuenow', taste_bitter); 
		$('#taste_strenght').css('width', taste_strenght+'%').attr('aria-valuenow', taste_strenght);  
	}else{
		drink_details.hide();
	}
}

function doDrink( btn ){
	var r = confirm("Do you want me to pour this drink?");
	if (r == true) {
		var drink_id		= $(btn).attr( "drink_id" );
		if(drink_id){
			loading = true;
			$.getJSON( "api/do_drink?recipe_id="+ drink_id, function( data ){
				loading = false;
			});
			goBack();
		}
	}
}

var custom_drink_ings	 = [];

function goBack(){
	clearCustom();
	var window_drink_creator	= $("#window_drink_creator");
	var window_drink_list		= $("#window_drink_list");
	$("#drink_details").hide(500);
	$("#window_main_question").show(500);
	if(window_drink_list.is(":visible")){
		window_drink_list.hide(500);
	}
	if(window_drink_creator.is(":visible")){
		window_drink_creator.hide(500);
	}
}
function goCustomBack(){
	clearCustom();
	var window_drink_creator	= $("#window_drink_creator");
	var window_drink_list		= $("#window_drink_list");
	
	$("#window_main_question").show(500);
	if(window_drink_list.is(":visible")){
		window_drink_list.hide(500);
	}
	if(window_drink_creator.is(":visible")){
		window_drink_creator.hide(500);
	}
}

function getDrinkById( drink_id ){
	for(var i =0;i<drinkList.length;i++){
		if(drinkList[i].id == drink_id ){
			return drinkList[i];
		}
	}
	return null;
}
function getSize( drink ){
	var qty = 0;
	for(var j =0;j<drink.ingredients.length;j++){
		qty+=drink.ingredients[j].quantity;
	}
	return qty;
}

function drink_creator(){
	// get ingrediends

	loader.showPleaseWait();
	$.getJSON( "/api/get_slots", function( data ){
		if( data.result && data.result.length > 0 ){
			slots = data.result;
			loader.hidePleaseWait();
			showIngsList();
		}else{
			loader.hidePleaseWait();
			alert("Ingrediengs list is empty.");
		}
	});
}
function showIngsList(){
	var window_drink_creator	= $("#window_drink_creator");
	var ing_list				= $("#ing_list");
	ing_list.empty();
	for(var i =0;i<slots.length;i++){
		if( slots[i].liquid_id > 0 ){
			var tpl = '<div class="custom_ing_item" id="ing_'+slots[i].id+'" position_id="'+slots[i].position+'" onclick="selectIng( this )">'+slots[i].name+'<a href="javascript:" class="add_ing btn btn-success">Add '+slots[i].dispenser_type+'ml</a></div>';
			ing_list.append(tpl);
		}
	}
	ing_list.trigger("appened");
	clearCustom();
	$("#window_main_question").hide(500);
	window_drink_creator.show(500);
	//alert("Not available using the remote control");
}

function selectIng( btn ){
	var custom_drink_ingredients	= $("#custom_drink_ingredients");	
	var position					= $(btn).attr("position_id");
	var slot						= slots[position-1];
	if(slot){
		addCustom(slot);	
	}
}

function clearCustom(){
	custom_drink_ings	 = [];	
	var custom_drink_ingredients	= $("#custom_drink_ingredients");
	custom_drink_ingredients.children().fadeOut(200, function() {
		custom_drink_ingredients.empty();
	});
	$("#doCustomDrinkButton").toggle( false );
}
	
function addCustom( ing ){
	var custom_drink_ingredients	= $("#custom_drink_ingredients");
	custom_drink_ings.push(ing);

	var id			= 'ing_'+ing.id;
	var quantity	= ing.dispenser_type;
	var tpl			= $('<div class="ing_item" style="display:none" id="'+ id +'" liquid_id="'+ing.liquid_id+'">'+ing.name+'<span class="badge pull-right">'+quantity+'ml</span></div>');
	
	tpl.appendTo( custom_drink_ingredients ).show(200);
	$("#doCustomDrinkButton").toggle( true );

	/*
	custom_drink_ingredients.empty();
	//console.log(custom_drink_ings);
	for( k=0; k<custom_drink_ings.length;k++){
		var ing			= custom_drink_ings[k];
		var id			= 'ing_'+ing.id;
		var quantity	= ing.dispenser_type;
		var tpl		= '<div class="ing_item" id="'+ id +'" liquid_id="'+ing.liquid_id+'">'+ing.name+'<span class="badge pull-right">'+quantity+'ml</span></div>';
		custom_drink_ingredients.append(tpl);
	}
	custom_drink_ingredients.trigger("appened");
	$("#doCustomDrinkButton").toggle( custom_drink_ings.length > 0 );
	*/
}	


function doCustomDrink(){
	var list	= {};
	for( k=0; k<custom_drink_ings.length;k++){
		var ing			= custom_drink_ings[k];
		if( !list[ ing.liquid_id ] ){
			list[ ing.liquid_id ] = 0;
		}
		list[ ing.liquid_id ]	+=  ing.dispenser_type;
	}
	var liquids_list = [];
	var size_list = [];
	for( var ind in list){
		liquids_list.push(ind);
		size_list.push(list[ind]);
	}
	var url = "api/do_custom_drink?liquids="+ liquids_list.join(",") + "&volume="+ size_list.join(",");
	var r = confirm("Do you want me to pour this drink?");
	if (r == true) {
		loading = true;
		$.getJSON( url, function( data ){
			loading = false;
		});
		goBack();
	}
}	

/*
"id": 1,
"name": "Vodka",
"position": 1,
"currentVolume": 1000,
"dispenser_type": 50,
"counter": 0,
"product_id": 1,
"taste":{
    "sweet": 0,
    "sour": 0,
    "bitter": 0,
    "strenght": 40
}
*/

// When document has finished loading
$(document).ready(function() {
	var sizes		= [
		{
			selector	: ".header_option",
			size		: 350
		},{
			selector	: ".drink_item",
			size		: 180
		},{
			selector	: ".ing_item",
			size		: 180
		},{
			selector	: ".do_bottons",
			size		: 180
		},{
			selector	: ".custom_ing_item",
			size		: 180
		},{
			selector	: ".add_ing",
			size		: 40
		},{
			selector	: "#drink_name",
			size		: 300
		}
	];

    var resizeText = function () {
        var preferredSize = 1024 * 768;
        var currentSize = $(window).width() * $(window).height();
        var scalePercentage = Math.sqrt(currentSize) / Math.sqrt(preferredSize);
		for(var i=0;i<sizes.length;i++){
			 $(sizes[i].selector).css("font-size", (sizes[i].size * scalePercentage) + '%');
		}
		$("#drink_list_td").height( $(window).height() );
		$("#drink_details").height( $(window).height() );

		$("#ing_list_td").height( $(window).height() );
		$("#custom_drink_details").height( $(window).height() );	
    };
    $(window).bind('resize', function() {
        resizeText();
    }).trigger('resize');

	$("body").on("appened", "div", function(event){
		 resizeText();
	});	
});

function goClear(){	
	clearCustom();
}
// old window, todo - delete


function run_command( command, btn ){
	$(btn).addClass("btn-danger");
	$.get("/command/" + command,{
	  }, function(dane){
	    $(btn).removeClass("btn-danger");
	  }
	);
}
function send_message( btn ){
	$(btn).addClass("btn-danger");
	var blocking	= $("#blocking:checked").val();
	var bypass		= $("#bypass:checked").val();
	var msg			= $("#message_text").val();
	$.get("/message",{
		  message		: msg,
		  blocking		: blocking || false,
		  bypass		: bypass || false,
	  }, function(dane){
		$(btn).removeClass("btn-danger");
	  }
	);
}

function pre(){
	if(arguments.length == 0){
		return console.log( arguments[0] );
	}
	if(arguments.length == 1){
		return console.log( arguments[0],arguments[1]  );
	}
	if(arguments.length == 2){
		return console.log( arguments[0],arguments[1],arguments[2] );
	}
}




if(typeof console === "undefined"){ console = {}; }

var tab_def_list = {
	tab_fav:{
		color		: "#777777",
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){
		},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){
			closeTab("fav_item");
		},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	tab_choose:{
		color		: "#666666",
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){
		},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){
			closeTab("tab_fav");
		},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	tab_history:{
		color		: "#555555",
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){
		},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){
			closeTab("history_item");
		},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	tab_lucky:{
		color		: "#444444",
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){},
		on_hide		: function( num, tab_btn, tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	tab_create:{
		color		: "#333333",
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){},
		on_hide		: function( num, tab_btn, tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	tab_settings:{
		color		: "#222222",
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){
		},
		on_hide		: function( num, tab_btn, tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	tab_off:{
		color		: "#111111",
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){
		},
		on_hide		: function( num, tab_btn, tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	fav_item:{
		color		: "#552244",
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){
			var h = theight - $(".item_buttons:first").height();
			console.log(theight, $(".item_buttons:first") );
			$("#fav_details_parent").height( h - 10 );
		},
		on_hide		: function( num, tab_btn, tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	history_item:{
		color		: "#004400",
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){

			var imgn = Math.round(Math.random() * 20) + 1;		// 1-21
			var prog1 = Math.round(Math.random() * 100);
			var prog2 = Math.round(Math.random() * 100);
			var prog3 = Math.round(Math.random() * 100);

			loadsth();

			tab = $("#drink_details");
			tab.find(".drink_name").text( "Drink numer " + num);
			tab.find(".prog1").find(".progress-bar").css("width", prog1+"%");
			tab.find(".prog2").find(".progress-bar").css("width", prog2+"%");
			tab.find(".prog3").find(".progress-bar").css("width", prog3+"%");

			$("#main_image").attr("src", "/images/" + imgn + ".jpg");
		},
		on_hide		: function( num, tab_btn, tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	type_item:{
		color		: "#001100",
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){
		},
		on_hide		: function( num, tab_btn, tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	lucky_item:{
		color		: "#223311",
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){},
		on_hide		: function( num, tab_btn, tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	setup_bottle	:{
		color		: "#113333",
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){},
		on_hide		: function( num, tab_btn, tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	setup_light:{
		color		: "#331111",
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){},
		on_hide		: function( num, tab_btn, tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	setup_sound:{
		color		: "#113311",
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){},
		on_hide		: function( num, tab_btn, tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	demo_mode:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){},
		on_hide		: function( num, tab_btn, tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	setup_advanced:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){},
		on_hide		: function( num, tab_btn, tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	setup_calibrate:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){},
		on_hide		: function( num, tab_btn, tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	goto_shop:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){
			var data = $("#buy_robot").html();
			fullscreen( tab_btn.text(), data);
		},
		on_hide		: function( num, tab_btn, tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	goto_contrib:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){
			var data = $("#contribs").html();
			fullscreen( tab_btn.text(), data);
		},
		on_hide		: function( num, tab_btn, tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	goto_www:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){
		},
		on_hide		: function( num, tab_btn, tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	goto_about:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){
			var data = $("#authors").html();
			fullscreen( tab_btn.text(), data);
		},
		on_hide		: function( num, tab_btn, tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	off_stop:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){},
		on_hide		: function( num, tab_btn, tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	off_app:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){},
		on_hide		: function( num, tab_btn, tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	off_robot:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){},
		on_hide		: function( num, tab_btn, tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	},
	off_all:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, twidth, theight ){},
		on_show		: function( num, tab_btn, tab, twidth, theight ){},
		on_hide		: function( num, tab_btn, tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
	}
};

function closeTab( old_name, old_btn ){
	var item		= tab_def_list[ old_name ];
		if(item){
		if(!old_btn){
			old_btn		= $();
		}
		var old_tab_btn	= old_btn.find("a:first");
		var num			= old_tab_btn.attr("num");
		var old_tab		= $("#"+old_name);	
		old_btn.removeClass("active");
		old_tab.removeClass("active");
		item.on_hide( num, old_tab_btn, old_tab );
	}
}

var scrollElementTo	= function( parent, inner_btn ){
	var max		= parent.height();
	var pos2 	= inner_btn.position().top + inner_btn.height()/2;
	//pos2 = Math.round(pos2);
	//pre(pos2+ "/" +Math.round(max) );
	if( pos2 < 0 || pos2 > max){
		var pos = parent.scrollTop() + inner_btn.position().top - parent.height()/2 + inner_btn.height()/2;
		pos = Math.round(pos);
		pre("scroll " + pos );
		parent.stop();
		//parent.css("overflow","hidden");
		parent.animate({scrollTop: pos}, 300, function(){
			//parent.css("overflow","auto");
		});
	}
};
		
		
$(document).ready(function() {
	window.scrollTo(0, 1);
	
	var mt		= $("#main_tabs");
    var pb		= $('#animated');
	var swidth	= $(window).innerWidth();
	var sheight	= $(window).height();
	mt.css( "min-height", sheight );
	$("#main_tabs_parent").css( "height", sheight );
	//var w = swidth - $("#main_tabs").outerWidth() - 20;
	pre("screen: " + $(document).width() + "/" + $(document).height() );
	//$(".tab-content").css( "width", w );

    $(".list_level2").scroll(function () {
        var currentScroll = $(this).scrollTop();
        pre("list_level2 " + currentScroll);
    });
    //działa:
    $(".list_level2_parent").scroll(function () {
//        var currentScroll = $(this).scrollTop();
//        pre("list_level2_parent " + currentScroll);
    });
    $(".tab-pane").parent().scroll(function () {
        var currentScroll = $(this).parent().scrollTop();
        pre("tab-pane " + currentScroll);
    });

	for( var tab_name in tab_def_list){
		if(tab_def_list[tab_name].on_load){
			tab_def_list[ tab_name ].on_load( swidth, sheight );
			tab_def_list[ tab_name ].on_load = null;			
		}
		/*
		if(tab_def_list[ tab_name ].color){
			var new_tab		= $("#"+tab_name);
			new_tab.css("background-color", tab_def_list[ tab_name ].color );
		}*/
	}
	//colorTabs($("#main_tabs"));
	var setActive = function( new_btn ){
		setTimeout( function(){
				var nav_root	= new_btn.closest( ".nav-tabs" );
				var old_btn		= nav_root.find("li.active");

				var new_tab_btn	= new_btn.find("a:first");
				var old_tab_btn	= old_btn.find("a:first");

				var new_name	= new_tab_btn.attr("tab");
				var old_name	= old_tab_btn.attr("tab");

				var new_tab		= $("#"+new_name);
				var twidth		= new_tab.width();
				var new_num		= new_tab_btn.attr("num");
				var old_num		= old_tab_btn.attr("num");

			//	var lwidth = tab.find(".list_level2_parent:first").outerWidth();
			//	new_tab.find(".details_level2_parent:first").width( twidth - lwidth -1 );
				pre( new_name +" -- "+ old_name );

				if( new_name != old_name ||	old_num != new_num ){
					if(old_name){
						closeTab( old_name, old_btn );
					}
					if($(".tab-pane.active").length > 2){
						$("#main_tabs").addClass("hide_title");
					}else{
						$("#main_tabs").removeClass("hide_title");
					}

					if( new_tab.is(".fill_parent")){
						var lwidth = 0;
						$(".tab-pane.active:not(.fill_parent)").each(function(index) {
							lwidth += parseInt($(this).outerWidth(), 10);
							pre($(this), $(this).outerWidth(), parseInt($(this).outerWidth(), 10));	
						});
					
						pre("outerWidth: " + lwidth);
						pre(swidth, $("body").innerWidth());
						pre("res: " +  Math.floor( $("body").innerWidth() - lwidth -1 ) );
						new_tab.width( Math.floor( $("body").innerWidth() - lwidth -1 ) );
					}
					new_btn.addClass("active");
					new_tab.addClass("active");			

					var parent		= nav_root.closest(".list_level2_parent");
					var theight		= sheight;
					scrollElementTo( parent, new_btn );

					var item		= tab_def_list[ new_name ];
					if(item.on_init ){
						item.on_init( new_num, new_tab_btn, new_tab, twidth, theight );
						item.on_init = null;
					}	  
					tab_def_list[ new_name ].on_show( new_num, new_tab_btn, new_tab, twidth, theight );
					new_tab.css( "height", sheight );
					var inner_scroll		= (new_tab.is(".inner_scroll")) ? new_tab : new_tab.find(".inner_scroll");
					inner_scroll.css( "min-height", sheight );
					//colorTabs(new_tab);
				}
		}, 2 );
	};

	var eventHandler = function( ev ){
		var btn			= $(this);
		setActive( btn );
	//	ev.stopPropagation();
	//	return false;
	};

	 var isTouch = 'ontouchstart' in window || 'msmaxtouchpoints' in window.navigator;
	 if(isTouch){
		$('ul.nav-tabs li').on('click', eventHandler );
	}else{
		$('ul.nav-tabs li').on('mousedown', eventHandler );
	}

	$('.menu_prev').on('mousedown', function(){
		var btn			= $(this);
		var selector	= btn.attr("parent");
		var current_btn	= $(selector).find("li.active");
		if( current_btn.length == 0 ){
			var next_btn	= $(selector).find("li:last");	// ostatni
			setActive(next_btn);
		}else{
			var next_btn	= current_btn.prev();
			if( next_btn.length == 0 ){
				next_btn	= current_btn.parent().find("li:last");	// ostatni
			}
			setActive(next_btn);
		}
	} );
	$('.menu_pour').on('mousedown', function(){
		alert("No glass");
	} );	
	$('.menu_next').on('mousedown', function(){
		var btn			= $(this);
		var selector	= btn.attr("parent");
		var current_btn	= $(selector).find("li.active");
		if( current_btn.length == 0 ){
			var next_btn	= $(selector).find("li:first");	// ostatni
			setActive(next_btn);
		}else{
			var next_btn	= current_btn.next();
			if( next_btn.length == 0 ){
				next_btn	= current_btn.parent().find("li:first");	// pierwszy
			}
			setActive(next_btn);
		}
	} );

	var scroll_object = false;
	var ev_important = false;

	function get_keys(obj ){
		var ret = [];
		for( var i in obj){
			ret.push( i );
		}
		return "{" + ret.join(",") + "}";
	}
	$(document).on('touchstart', function(ev) {
		var touchobj = ev.originalEvent.touches[0];

	//force
	//	relatedTarget
	//	target
	//	delegateTarget
	//	currentTarget

		if(ev.target.nodeName != "HTML"){
			var target			= $(ev.target);
			var nearMenuItem	= target.closest(".nav_menu_item");
			var nearScollable	= target.closest(".tabs-right");
			if(nearScollable.length){	//	menu - przesuwaj
				scroll_object = target;
		//		return;
			}
	//		pre("touchstart 8: "+ touchobj.target.nodeName + "/ " + $(touchobj.target).text() );
	//		pre("touchstart2: "+ ev.target.nodeName + "/ " + $(ev.target).text() );	
			
			if(nearMenuItem.length && !nearMenuItem.closest("li").hasClass("active")){		// przycisk, kliknij
//				nearMenuItem.click();
		//		ev.stopPropagation();
		//		return false;
			}else{
			}
			//pre("touchstart "+ get_keys(touchobj));
		}
	});

	$(document).on('touchend', function(ev) {
		var touchobj = ev.originalEvent.touches[0];
	//	pre("touchend "+ get_keys(ev.originalEvent)) ;
		/*
		var touchobj = ev.touches[0];
		if(ev.target.nodeName != "HTML"){
			pre("touchend1: "+ ev.target.nodeName + "/ " + $(ev.target).text() );
			pre("touchend2: "+ touchobj.target.nodeName + "/ " + $(touchobj.target).text() );
			if(scroll_object){
				scroll_object = false;
			}
		}*/
	});
});

var on_fl_close	= function( ev ){
	var btn 	= $(this);
	pre("on_fl_close");
	var fl		= btn.closest( "#full_box" );
	fl.stop();
//	btn.attr("disabled","disabled");
	fl.animate( { left: "-100%"}, 500, function() {
		fl.hide();
	});
};

function fullscreen( title, data ){
	var fl	= $("#full_box");
	var inited	= fl.data( "inited" );
	if( !inited ){
		fl.find(".fl_nav_back").click( on_fl_close );
		fl.data( "inited", true );
	}
	var in_data		= fl.find(".in_data:first");
	fl.stop();
	fl.find(".fl_nav_title:first").text( title );
	in_data.html( data );
	fl.show().animate( { left: "0%"}, 500, function() {
//		fl.find(".fl_nav_back").removeAttr("disabled");
	});
	setTimeout( function(){
		var data_height	= $(window).height() - fl.find(".navigation:first").outerHeight() - 1;
		in_data.height(data_height);	
	}, 50 );
	
	return fl; 
}

function x10(){
	  $.get("/rpc",{
	        command: "x10",
	        value: "1000"
	    }, function(dane){
	      //  alert("Dane otrzymane: " + dane);
	    }
	);
}

function loadObj( id, type, onready ){
	$.get("/loader",{
	      id	: id,
	      type	: type
	  }, function(dane){
		  onready( dane );
	  }
	);
}

function loadsth(){
	$.get("/rpc",{
	      command: "x-10",
	      value: "1000"
	  }, function(dane){
	    //  alert("Dane otrzymane: " + dane);
	  }
	);
}

function x010(){
	$.get("/rpc",{
	      command: "x-10",
	      value: "1000"
	  }, function(dane){
	    //  alert("Dane otrzymane: " + dane);
	  }
	);
}
function colorTabs( parent ){
	var links = parent.find(".nav_menu_item[tab]");
	pre("links");
	pre(parent);
	pre(links);
	if( links.length > 0 ){
		links.each( function(){
			var link		= $(this);
			var tab_name	= link.attr("tab");
			if( tab_def_list[ tab_name ] ){
				link.css("background-color", tab_def_list[ tab_name ].color );
			}
		});
	}
}






/*
fr.owlCarousel({
autoPlay : 5000,
navigation : false,
items : 1,
itemsDesktop : [1199,1],
itemsDesktopSmall : [979,1]
});*/
/*
$('a.nav_menu_item').on('shown.bs.tab', function (e) {		
	var old_tab_btn	= $(e.relatedTarget);
	var new_tab_btn	= $(e.target);
	var old_name	= old_tab_btn.attr("name");
	var new_name	= new_tab_btn.attr("name");

	setTimeout( function(){
		var old_tab_selector	= old_tab_btn.attr("tab");
		var new_tab_selector	= new_tab_btn.attr("tab");
		var new_tab		= $(new_tab_selector);
		var old_tab		= $(old_tab_selector);
		var twidth		= new_tab.width();
		var theight		= sheight;

		if( tab_def_list[ old_name ] ){
			var num = old_tab_btn.attr("num");
			tab_def_list[ old_name ].on_hide( num, old_tab_btn, old_tab, new_name, new_tab_btn );
		}
		if( tab_def_list[ new_name ] ){
			var num		= new_tab_btn.attr("num");	
			if(tab_def_list[ new_name ].on_init ){
				tab_def_list[ new_name ].on_init( num, new_tab_btn, new_tab, twidth, theight );
				tab_def_list[ new_name ].on_init = null;
			}	  
			tab_def_list[ new_name ].on_show( num, new_tab_btn, new_tab, twidth, theight );
		}
	}, 1 );		// other thread
});
*/
/*
$(document).on('touchcancel', function(ev) {
	if(scroll_object){
		scroll_object = false;
	}
});
$('a.nav_menu_item').on('mouseover', function (ev) {	
	//pre("touchmove "+ get_keys(ev.originalEvent)) ;
	//pre("mouseover: "+ ev.target.nodeName + "/ " + $(ev.target).text() );
});
$('a.nav_menu_item').on('dragover', function (ev) {	
	pre("dragover: "+ ev.target.nodeName + "/ " + $(ev.target).text() );
});
$('a.nav_menu_item').on('touchenter', function (ev) {	
	pre("touchenter: "+ ev.target.nodeName + "/ " + $(ev.target).text() );
});
*/
/*
if( navigator.userAgent.match(/Android/i) ) {
	pre("android");
	document.ontouchmove = function(event) {
	    event.preventDefault();
	};
}*/

/*
	var progress = setInterval(function() {
		var bar = pb.find(".progress-bar");
		if(pb.width() > 0){
		    if (bar.width() > pb.width() ) {
		        clearInterval(progress);
		        $('.progress').removeClass('active');
			    } else {
			        bar.width(bar.width()+10);
			    }
		    }
	}, 800);
*/

/*
$(window).scroll(function ( e ) {
    var currentScroll = $(window).scrollTop();
    pre("event")
    pre(e)
    pre("currentScroll " + currentScroll);
});*/

/*
$(document).on('touchmove', function(ev) {
//	pre("touchmove "+ get_keys(ev.originalEvent)) ;
	var touchobj = ev.originalEvent.touches[0];
	$("#touch_ui").css({
			width:	touchobj.radiusY,
			height:	touchobj.radiusX,
			top:	touchobj.pageY,
			left:	touchobj.pageX,
	});
	if(ev.target.nodeName != "HTML"){
//		pre("touchmove 2: "+ ev.target.nodeName + "/ " + $(ev.target).text() );
//		pre("touchmove 6: "+ touchobj.target.nodeName + "/ " + $(touchobj.target).text() );
	}
});*/
