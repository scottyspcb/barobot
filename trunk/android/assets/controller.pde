SpinArm arm;

// zmienne uniwersalne:
var height3		= window.innerHeight - 10;
int width5		= window.innerWidth;
var srodek		= int(height3/2);
var dol			= int(height3);
int fps			= 10;
int dot_size	= 5;
int dimms		= 10;
int inited		= 0;		// ile wymiarów danych
int skalax		= 4;
int interval_size= 0;


//tablice w zale¿nosci od wymiaru
int[] lastX		= new int[dimms];		// 10 wymiarów max
int[] agvs		= new int[dimms];		// 10 wymiarów max
int[] skala		= new int[dimms];
int[] ymax		= new int[dimms];
int[] ymin		= new int[dimms];
int[] sigma2	= new int[dimms];

color[] palette=new color[dimms];
palette[0]=color(0,255,0);
palette[1]=color(0,0,255);
palette[2]=color(255,255,0);
palette[3]=color(255,255,255);
palette[4]=color(128,0,0);
palette[5]=color(128,0,255);

int data_width							= int((width5 -10) / skalax);
int  buffer_width						= width5;		// tak jakby skalax = 1
int[ dimms ][ buffer_width ] history	= new int[dimms][buffer_width];

var reversed		= 1;		// 1 lub -1
boolean localmin	= true;
boolean draw_dots	= false;
boolean draw_lines	= true;
boolean draw_columns= false;


void setup() {
	size(width5, height3);
	background(0, 0, 0);

//	arm	= new SpinArm(width5/2, height/2, 0.01);
	stroke(255);     // Set line drawing color to monochrome white
	frameRate(fps);

	//line(0, srodek, width5, srodek);
	stroke(204, 102, 0);

	//line(0, srodek, 100, 100);
	//noLoop();

	for( int d=0; d<dimms;d++){
		sigma2[d]	= 0;
		agvs[d]		= 0;
		lastX[d]	= 0;
		ymin[d]		= 10000;
		ymax[d]		= -10000;
		skala[d]	= dol / ( ymin[d] - ymax[d] );
	}
}


void readInput( vals ) {
	int[] nums = int(split(vals, ','));
	inited = nums.length;
	for (int d		= 0; d < nums.length; d++) {		// dla kazdego wymiaru
		int index	= (lastX[d] % data_width);
		int val		= int(nums[ d ] * reversed);
		history[ d ][ index ]	= val;
		if(!localmin){
			ymax[d]		= max( ymax[d], val );
			ymin[d]		= min( ymin[d], val );
		}
	//	println(val);
		lastX[d]++;
		interval_size++;
	}
}

void getValue( d, rawY ) {		// podaje liczbê px od do³u ekranu (do³u wykresu)
	//int y1	= dol - int(rawY * skala)-ymin;
	// ymin powinien byæ zawsze na pozycji 0 wykresu czyli "dol" ekranu
	return dol + (rawY - ymin[d]) * skala[d];
}

void draw() {
	background(0, 0, 0);

	/*
	line(0, srodek, width5, srodek);
	int draw_min = getValue(ymin);
	int draw_max = getValue(ymax);
	stroke(255,255,0);
	line(0, draw_min-10, width5, draw_min);		// dolne min
	stroke(0,255,255);
	line(0, draw_max+10, width5, draw_max);		// dolne max
*/

	for (int d = 0; d < inited; d++) {		// dla ka¿xego wymiaru
		skala[d]		= dol / ( ymin[d] - ymax[d] );

		int newymin	= 10000;
		int newymax	= -10000;
			
		stroke(palette[ d ]);
		
		//line(0, agvs[d], width5, agvs[d]);		// srednia
		int sigma		= int(sqrt(sigma2[d]/data_width) * 1);	// wariancja
		sigma2[d]		= 0;

		int odch1		= getValue(d,agvs[d] + sigma);	// zaznacz srednia + sigma 
		line(0, odch1, width5, odch1);		// srednia
			
		int odch2		= getValue(d,agvs[d] - sigma);	// zaznacz srednia - sigma 
		line(0, odch2, width5, odch2);		// srednia

		
	//	println("sigma" + sigma);
	//	println("agv: "+agvs[d]);

		int old_y		= getValue(d, history[ d ][ 0 ]);
		int old_x		= 0;
		int agv_sum		= 0;

		// wysokoœc jest height3
		// zakres jest ymin do ymax czyli szrokosc zakresu to ymax - ymin
		// ymax - ymin to height3

		textSize(12);
		int zakres = ymax[d]- ymin[d];
		for (int i = 0; i < 20; i++) {
			int val		= int( ymax[d] - zakres/20 * i);
			int new_y	= int(getValue(d, val));
			text("" + val, 5, new_y); 			
		
		}
		
		

		
		for (int i = 0; i < data_width; i++) {	
			int val		= history[ d ][ i ];
			int new_y	= getValue(d, val);
			int new_x	= i* skalax;
			agv_sum		+=	val;

			if(draw_dots){
				ellipse(new_x,new_y,dot_size,dot_size);
			}
			if(draw_lines){
				line( old_x, old_y, new_x, new_y );
			}
			if(draw_columns){
				rectMode(CORNERS);
				fill(palette[ d ] );
				rect( old_x, dol, new_x, new_y);
			}
			/*
			//if(val > agvs[d] + sigma || val < agvs[d] - sigma){
				textSize(12);
				text("" +  val, new_x, new_y); 
			//}
			*/

			old_y		= new_y;
			old_x		= new_x;
			sigma2[d]	+= ((agvs[d] - val ) * (agvs[d] - val ));

			if( localmin ){
				newymax		= max( newymax, val);
				newymin		= min( newymin, val);
			}
		}
		agvs[d]	=  int(agv_sum / data_width);
		if( localmin ){
			ymax[d]	= newymax;
			ymin[d]	= newymin;
		}
	}	
}









// zmiany ustawien

void dots() {
	draw_dots = !draw_dots;
}
void lines() {
	draw_lines = !draw_lines;
}
void column() {
	draw_columns = !draw_columns;
}

void changefps( val ) {
	fps = val;
	frameRate(fps);
	println(fps);
}

void changex( val ) {
	skalax		= val;
	if( skalax > 10 ){
		skalax = 10;
	}
	if( skalax < 1 ){
		skalax = 1;
	}
	data_width		= int((width5 -10) / skalax);	
}

void toggleLocalMin() {	
	localmin = !localmin;
}

void clear() {
	reversed	= 0 - reversed;
}

void new_interval() {		// zacznij pokazywc od lewej, skaluj na d³ugosc okresu jesli to mozliwe
	lastX[0]	= 0;
	lastX[1]	= 0;
	lastX[2]	= 0;
	float skalax2	= width5 / interval_size;		// skaluj tak zeby tyle wynikow miescilo sie na jednym wykresie
	if( skalax2 > 1 ){
		skalax			= floor(skalax);
		data_width		= int((width5 -10) / skalax);
	}
	interval_size = 0;
}

void reverseY() {
	reversed	= 0 - reversed;
}

void sethighspeed() {
	fps	= 30;
}
void toggleDimm( name ) {
	for (int d = 0; d < inited; d++) {		// dla ka¿xego wymiaru
	}
	data_width = 1;
}
