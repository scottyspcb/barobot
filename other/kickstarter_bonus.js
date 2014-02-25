javascript:{
var sum = 0;
var title	= $("#title a").text();
var pledged = $("#pledged").attr("data-pledged");
$(".NS-projects-reward").each(function(){
	var box = $(this);
	var bac = box.find(".num-backers").first().text();
	var pri = box.find(".money").first().text();
	box.find(".money").first().text();
	pri = pri.replace("$","");
	pri = pri.replace("£","");
	pri = pri.replace(",","");
	bac = bac.replace(" backers","");
	bac = bac.replace(" backer","");
	bac	= bac.trim();
	var tt = pri * bac;
	sum += tt;
	var procOfPledged	= Math.round(tt/pledged * 1000);
	box.find("h5").append( " czyli <i>"+ tt + " $</i> (" + procOfPledged/10+"%)" );
});
var proc	= Math.round(((pledged/sum)-1)*1000)/10;
alert(title + "\nSuma prezentów: " + Math.round(pledged-sum) + "\nDodatkowe wpłaty: " + Math.round(proc)+"%");};void(0);
