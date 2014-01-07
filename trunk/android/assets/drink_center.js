var tab_def_list = {

	fav:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){
			$("#list_container").height( sheight );
		},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	choose:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){
			$("#type_list").height( sheight );
		},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){
			var lwidth = tab.find(".list_level2_parent:first").outerWidth();
			tab.find(".details_level2_parent:first").width(  swidth - lwidth  );
			console.log("choose w: " +swidth );
		},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	history:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){
			$("#history_list").height( sheight );
		},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){
			var lwidth = tab.find(".list_level2_parent:first").outerWidth();
			console.log("history w: " + tab.width() );
			console.log("screen w: " +lwidth );
			console.log("details w: " + (tab.width() - lwidth) );
			console.log(tab);		
			console.log(tab.find(".list_level2_parent:first") );
			console.log(tab.find(".details_level2_parent:first") );		

			tab.find(".details_level2_parent:first").width( tab.width() - lwidth -1 );
		},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	lucky:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	create:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	settings:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){
			$("#settings_list").height( sheight  );
		},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	off:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){
			$("#off_list").height( sheight );
		},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	fav_item:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	history_item:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	type_item:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	lucky_item:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	setup_bottle	:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	setup_light:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	setup_sound:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	demo_mode:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	setup_advanced:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	setup_calibrate:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	goto_shop:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	goto_project:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	goto_about:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	off_stop:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	off_app:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	off_robot:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	},
	off_all:{
		on_load		: function( swidth, sheight ){},
		on_init		: function( num, tab_btn, tab, swidth, sheight ){},
		on_show		: function( num, tab_btn, tab, swidth, sheight ){},
		on_hide		: function( num, tab_btn, tab, new_tab_name, new_tab ){},
		on_scroll	: function( num, tab_btn, tab, top, left ){},
		on_destroy	: function( num, tab_btn, tab ){},
	}
};
 
$(document).ready(function() {
	var mt = $("#main_tabs")
	var fr = $("#frontslider");
    var pb	= $('#animated');
    $('.dropdown').hover(function() {
        $(this).addClass('open');
    }, function() {
        $(this).removeClass('open');
    });
	var w = $(".tabbable").innerWidth()- mt.outerWidth() - 20;
	console.log("screen: " + $(document).width() + "/" + $(document).height() );
	/*
	fr.owlCarousel({
		autoPlay : 5000,
		navigation : false,
		items : 1,
		itemsDesktop : [1199,1],
		itemsDesktopSmall : [979,1]
	});*/
	$(".tab-content").css( "width", w );
	$("#frontslider .owl-item img").css( "width", w-1 );
	fr.width( w );

    $(window).scroll(function ( e ) {
        var currentScroll = $(window).scrollTop();
        console.log("event")
        console.log(e)
        console.log("currentScroll " + currentScroll);
    });
    $("#drink_list_parent").scroll(function () {
        var currentScroll = $("#drink_list_parent").scrollTop();
        console.log("drink_list_parent " + currentScroll);
    });
    $("#drink_list").scroll(function () {
        var currentScroll = $("#drink_list").scrollTop();
        console.log("drink_list " + currentScroll);
    });
    $("#drink_list").parent().scroll(function () {
        var currentScroll = $("#drink_list").parent().scrollTop();
        console.log("drink_listpar " + currentScroll);
    });
 
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
	$("#main_tabs li a").mousedown( function(){
		//$(this).click();
	});
	var swidth	= $(window).width() - mt.outerWidth();
	var sheight	= $(window).height();
	for( var tab_name in tab_def_list){
		if(tab_def_list[tab_name].on_load){
			tab_def_list[ tab_name ].on_load( swidth, sheight );
			tab_def_list[ tab_name ].on_load = null;			
		}
	}
	$('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {	
		var old_tab_btn	= $(e.relatedTarget);
		var new_tab_btn	= $(e.target);
		var old_name	= old_tab_btn.attr("name");
		var new_name	= new_tab_btn.attr("name");
		var old_tab_selector	= old_tab_btn.attr("href");
		var new_tab_selector	= new_tab_btn.attr("href");
		var new_tab		= $(new_tab_selector);
		var old_tab		= $(old_tab_selector);

		if( tab_def_list[ old_name ] ){
			var num = old_tab_btn.attr("num");
			tab_def_list[ old_name ].on_hide( num, old_tab_btn, old_tab, new_name, new_tab_btn );
		}
		if( tab_def_list[ new_name ] ){
			var num		= new_tab_btn.attr("num");	
			if(tab_def_list[ new_name ].on_init ){
				tab_def_list[ new_name ].on_init( num, new_tab_btn, new_tab, swidth, sheight );
				tab_def_list[ new_name ].on_init = null;
			}	  
			tab_def_list[ new_name ].on_show( num, new_tab_btn, new_tab, swidth, sheight );
		}
	});
	$(document).on('touchmove', function(ev) {
		console.log("touchmove: "+ ev.target.nodeName + "/ " + $(ev.target).text() );
		console.log("pageX "+ev.pageX + " " +ev.pageY );
	});

	$(document).on('touchstart', function(ev) {
		console.log("touchstart: "+ ev.target.nodeName + "/ " + $(ev.target).text() );
		console.log("pageX "+ev.pageX + " " +ev.pageY );
	});
	$(document).on('touchend', function(ev) {
		console.log("touchend: "+ ev.target.nodeName + "/ " + $(ev.target).text() );
		console.log("pageX "+ev.pageX + " " +ev.pageY );
	});

/*
    if( navigator.userAgent.match(/Android/i) ) {
    	console.log("android");
    	document.ontouchmove = function(event) {
    	    event.preventDefault();
    	};
    }*/
});

function x10(){
	  $.get("/rpc",{
	        command: "x10",
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