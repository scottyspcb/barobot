/* Add up and down scroll buttons to the side of a scrollable div */
var	scroll_opt = 0; // 0 = scroll up/down; 1 = page up/down 
var scroll_pos = 0; // 0 = side of div; 1 = top/bottom of div
var	scroll_slack = 10; // How many pixels will remain from the top or bottom of the div when page up/down


var sb_timer = 0;

function bindEvent(el, eventName, eventHandler) 
{
  // addEventListener for IE and FF
  if (el.addEventListener){ // FF
    el.addEventListener(eventName, eventHandler, false); 
  } else if (el.attachEvent){ // IE
    el.attachEvent('on'+eventName, eventHandler);
  }
} // end function bindEvent(el, eventName, eventHandler)


function scrollbutton(div_id)
{

	
	if (scroll_pos==0)
		middle_style = " style='position: relative; top: 42%'";
	else
		middle_style =  "";
		
	var div_to_scroll = document.getElementById(div_id);
	
	// Ie will not get offsetHeight until document is loaded so if !opt
	// then call this function again until offsetHeight is larger than 0
	if (div_to_scroll.offsetHeight == 0)
	{
		setTimeout(function (){ scrollbutton(div_id); }, 250);
		return;
	}
	
	var scroll_button_div = document.createElement("div");
	scroll_button_div.style.width = '18px';
	scroll_button_div.style.height = div_to_scroll.offsetHeight;
	//scroll_button_div.style.border = "1px solid black";
	scroll_button_div.style.cssFloat = 'right'; // FF
	scroll_button_div.style.styleFloat = 'right'; // IE
	//div_to_scroll.parentNode.insertBefore(scroll_button_div, div_to_scroll);

	// If I don't use outer_div then FF puts things to the right of the div slightly inside of it
	// Also with outer_div I can use float=right above
	var outer_div = document.createElement("div");
	if (scroll_pos==0) // if buttons on side of scrollable div
		outer_div.style.width = parseFloat(div_to_scroll.offsetWidth)+21+"px";
	else // if buttons on top and bottom of scrollable div
		outer_div.style.width = div_to_scroll.offsetWidth;
	outer_div.style.height = 'auto';
	//outer_div.style.border = "1px solid black"; 
	
	var up_arrow_div = document.createElement("div");
	up_arrow_div.style.border = "1px solid black";
	up_arrow_div.style.textAlign = "center";
	up_arrow_div.style.cursor = "pointer";
	up_arrow_div.style.backgroundColor = "#CCDDFF";
	if (scroll_pos==0) // if buttons on side of scrollable div
		up_arrow_div.style.height = Math.round(parseFloat(div_to_scroll.offsetHeight/2)) + "px";
	// verticalAlign = "middle" does not work so I had to put a relative div with top
	up_arrow_div.innerHTML = "<div"+middle_style+">&#9650;";
	
	// Make a copy of up_arrow_div into down_arrow_div
	down_arrow_div = up_arrow_div.cloneNode(false); // false means don't copy contents
	down_arrow_div.innerHTML = "<div"+middle_style+">&#9660;";
	
	//Put up arrow div and down arrow div inside of scroll_button_div
	scroll_button_div.appendChild(up_arrow_div);
	scroll_button_div.appendChild(down_arrow_div);	
	
	if (scroll_opt==1)
	{
		bindEvent(up_arrow_div, 'click', function () {
	  		div_to_scroll.scrollTop = div_to_scroll.scrollTop - div_to_scroll.offsetHeight + scroll_slack;
		});
		
		bindEvent(down_arrow_div, 'click', function () {
	  		div_to_scroll.scrollTop = div_to_scroll.scrollTop + div_to_scroll.offsetHeight - scroll_slack;
		});
	}
	else if (scroll_opt==0)
	{
		// mousedown and mouseup works great on blackberry but not other devices
		bindEvent(up_arrow_div, 'mousedown', function () {
			clearInterval(sb_timer);
			sb_timer = setInterval(function () {
	  		div_to_scroll.scrollTop = div_to_scroll.scrollTop - 10;
	  		}, 15);
	  		return;
		});
		
		bindEvent(down_arrow_div, 'mousedown', function () {
			clearInterval(sb_timer);
			sb_timer = setInterval(function () {
	  		div_to_scroll.scrollTop = div_to_scroll.scrollTop + 10;
	  		}, 15);
	  		return;
		});
		
		bindEvent(up_arrow_div, 'mouseup', function () {clearInterval(sb_timer);});
		bindEvent(down_arrow_div, 'mouseup', function () {clearInterval(sb_timer);});
		bindEvent(up_arrow_div, 'mouseout', function () {clearInterval(sb_timer);});
		bindEvent(down_arrow_div, 'mouseout', function () {clearInterval(sb_timer);});
		
		
		// iPhone and android does not use mousedown so we have to add touchstart event
		bindEvent(up_arrow_div, 'touchstart', function (event) {
			event.preventDefault();	// Prevents submenu from popping up because of touching screen for too long
			clearInterval(sb_timer);
			sb_timer = setInterval(function () {
	  		div_to_scroll.scrollTop = div_to_scroll.scrollTop - 10;
	  		}, 15);
	  		return;
		});
		
		bindEvent(down_arrow_div, 'touchstart', function (event) {
			event.preventDefault();	// Prevents submenu from popping up because of touching screen for too long
			clearInterval(sb_timer);
			sb_timer = setInterval(function () {
	  		div_to_scroll.scrollTop = div_to_scroll.scrollTop + 10;
	  		}, 15);
	  		return;
		});
		

		// iPhone and android does not use mouseup so we have to add touchend event
		bindEvent(up_arrow_div, 'touchend', function () {clearInterval(sb_timer);});
		bindEvent(down_arrow_div, 'touchend', function () {clearInterval(sb_timer);});
		//bindEvent(up_arrow_div, 'touchleave', function () {clearInterval(sb_timer);});
		//bindEvent(down_arrow_div, 'touchleave', function () {clearInterval(sb_timer);});
	
	}
	
	// Put outer div before div_to_scroll
	div_to_scroll.parentNode.insertBefore(outer_div, div_to_scroll);
	
	if (scroll_pos==0) // if buttons on side of scrollable div
	{
		// Put scroll_button_div inside of outer_div
		outer_div.appendChild(scroll_button_div);
		// Put div_to_scroll inside of outer_div
		outer_div.appendChild(div_to_scroll); 
	}
	else // if buttons on top and bottom of scrollable div
	{
		// Put up arrow div on top
		outer_div.appendChild(up_arrow_div);
		// Put div_to_scroll in middle
		outer_div.appendChild(div_to_scroll);
		// Put dn arrow div on bottom
		outer_div.appendChild(down_arrow_div);
	}
	
} // end function scrollbutton(div_id)

// This function inserts newNode after referenceNode
function insertAfter( referenceNode, newNode )
{
    referenceNode.parentNode.insertBefore( newNode, referenceNode.nextSibling );
}

// Have to use window.onload AND function below because IE
// will not get offsetHeight until document is loaded
/* window.onload = function (){
scrollbutton('quiz_div');
}; */

//scrollbutton('scrollbutton_div');
//</div"+middle_style+"></div"+middle_style+"></body></html>