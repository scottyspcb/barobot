javascript:{
var sum = 0;
var title	= $("#title a").text();
var pledged = $("#pledged").attr("data-pledged");
$(".NS-projects-reward").each(function(){
	var box = $(this);
	var bac = box.find(".num-backers").first().text();
	var pri = box.find(".money").first().text();
	pri = pri.replace("$","");
	pri = pri.replace("£","");
	pri = pri.replace(",","");
	bac = bac.replace(" backers","");
	bac = bac.replace(" backer","");
	bac	= bac.trim();
	var tt = pri * bac;
	sum += tt;
});alert(title + "\nSuma prezentów: " + sum + "\nDodatkowe wpłaty: " + Math.round(((pledged/sum)-1)*1000)/10+"%");};void(0);
