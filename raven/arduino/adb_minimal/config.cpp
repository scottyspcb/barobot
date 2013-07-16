
// czy debugowac przez serial 0
#define DEBUG_OVER_SERIAL true

// czy Android jest na koncu USB
#define USE_ADB true

#define DEBUG_SERIAL_INPUT true
#define DEBUG_ADB_INPUT true
#define DEBUG_OUTPUT2ANDROID false
#define DEBUG_ADB2ANDROID false

// obsluga zrodel wejscia
#define SERIAL0_BOUND 115200
//#define SERIAL0_BOUND 9600


// Brakujace opisy dla pinów
const int PIN8 = 8;
const int PIN9 = 9;
const int PIN10 = 10;
const int PIN11 = 11;
const int PIN12 = 12;
const int PIN13 = 13;
const int PIN14 = 14;
const int PIN15 = 15;
const int PIN16 = 16;
const int PIN17 = 17;
const int PIN18 = 18;
const int PIN19 = 19;
const int PIN20 = 20;
const int PIN21 = 21;
const int PIN22 = 22;
const int PIN23 = 23;
const int PIN24 = 24;
const int PIN25 = 25;
const int PIN26 = 26;
const int PIN27 = 27;
const int PIN28 = 28;
const int PIN29 = 29;
const int PIN30 = 30;
const int PIN31 = 31;
const int PIN32 = 32;
const int PIN33 = 33;
const int PIN34 = 34;
const int PIN35 = 35;
const int PIN36 = 36;
const int PIN37 = 37;
const int PIN38 = 38;
const int PIN39 = 39;
const int PIN40 = 40;
const int PIN41 = 41;
const int PIN42 = 42;
const int PIN43 = 43;
const int PIN44 = 44;
const int PIN45 = 45;
const int PIN46 = 46;
const int PIN47 = 47;
const int PIN48 = 48;
const int PIN49 = 49;

// PINY dla ultradzwięków
#define PIN_ULTRA0_TRIG PIN24
#define PIN_ULTRA0_ECHO PIN22
#define PIN_ULTRA1_TRIG PIN28
#define PIN_ULTRA1_ECHO PIN26

// mozliwe stany uC (mikrokontrolera)
#define STATE_INIT 2
#define STATE_READY 4
#define STATE_BUSY 8
#define STATE_ERROR 32

// koniec stanu uC

// PIN dla diody statusu
#define STATUS_LED_PIN 13



