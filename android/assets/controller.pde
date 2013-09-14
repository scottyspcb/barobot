SpinArm arm;

var height3		= $(window).height() - $("#menu").height() - 20;
int width5		= $(window).width() - 1;
var srodek		= int(height3/2);
var dol			= int(height3);
int lastx1		= 0;
int lasty1		= dol;

int lastx2		= 0;
int lasty2		= dol;

float y			= 100;
int fps			= 10;
int ymax		= 10;
int ymin		= 100;

int skalax		= 5;

void setup() {
	size(width5, height3);
	background(0, 0, 0);

//	arm	= new SpinArm(width5/2, height/2, 0.01);
	stroke(255);     // Set line drawing color to monochrome white
	frameRate(fps);   // Set up draw() to be called 30 times per second

	//line(0, srodek, width5, srodek);
	stroke(204, 102, 0);

	//line(0, srodek, 100, 100);
	//noLoop();
}

int dimms	= 10;
int[] lastX = new int[dimms];		// 10 wymiarów max
int[] lastY = new int[dimms];		// 10 wymiarów max
float[] y_size = new float[dimms];		// 10 wymiarów max
int inited = 0;		// ile wymiarów danych


color[] palette=new color[dimms];
palette[0]=color(0,255,0);
palette[1]=color(0,0,255);
palette[2]=color(255,255,0);
palette[3]=color(255,255,255);
palette[4]=color(128,0,0);
palette[5]=color(128,0,255);

//int data_width = 400;//(width5 -20) / 10;
int data_width = int((width5 -20) / skalax);
int[ dimms ][ data_width ] history = new int[dimms][data_width];

void toggleDimm( name ) {

}

void readInput( val ) {
	int[] nums = int(split(val, ','));
	inited = nums.length;
	for (int i		= 0; i < nums.length; i++) {
		int newX	= 1 + lastX[ i ] ;
		int index	= (newX % (int(data_width)));
		lastX[ i ]	= newX;

		history[ i ][ index ]		= nums[ i ];
	//	y_size[ i ]	= 5;//data_width / newX;
		lastX[ i ]	= (( newX )% (int( data_width )));
		ymax		= max( ymax, nums[ i ]);
		ymin		= min( ymin, nums[ i ]);
	}
	//draw3();
}

void draw34() {

}

void draw() {
	float zakres	= ymax - ymin;
	float skala		= height3 / zakres;

	background(0, 0, 0);

//	line(0, srodek, width5, srodek);

	stroke(255,255,0);

	line(0, (dol - ymin) * skala-5, width5, (dol - ymin) * skala-5);		// dolne min
	stroke(0,255,255);

	line(0, (dol - ymax) * skala+5, width5, (dol - ymax) * skala+5);		// dolne max

	for (int d = 0; d < inited; d++) {
		stroke(palette[ d ]);

	//	println(data_width);

		int old_y		= 0;
		int old_x		= 0;
		int startx		= 0;
		int stopx		= data_width;

		// wysokoœc jest height3
		// zakres jest ymin do ymax czyli szrokosc zakresu to ymax - ymin
		// ymax - ymin to height3
		
		for (int i = 0; i < data_width; i++) {	
			int new_y	= history[ d ][ i ];
			
			int x1	= old_x* skalax;
			int x2	= i* skalax;

			int y1	= int((dol - old_y) * skala);
			int y2	= int((dol - new_y) * skala);

			line( x1, y1, x2, y2 );

			old_y		= new_y;
			old_x		= i;
			if( i>= lastX[d] ){
			//	break;
			}
		}
	}
}
