// -----------
// Gdzie jest android? Tylko jedna z tych 3 opcji moz byc wlaczona na raz
// czy Android jest na koncu BT (Serial3)
#define USE_BT true
// czy Android jest na koncu USB
#define USE_ADB false
// udawaj androida po serialu
#define USE_SERIAL0 false
// -----------


// czy debugowac przez BT
#define DEBUG_OVER_BT false

// czy debugowac przez Serial0
#define DEBUG_OVER_SERIAL true  //

#define DEBUG_ADB2ANDROID false  //


#define I2C_ENABLED false
#define I2C_BUFF_LENGTH 10


#define DEBUG_SERIAL_INPUT false
#define DEBUG_BT_INPUT false
#define DEBUG_ADB_INPUT false
#define DEBUG_OUTPUT2ANDROID true    //

#define I2C_MASTER_ADDR 0x100
#define I2C_DEVICE_VERSION 0x11     // v1.0
#define I2C_DEVICE_TYPE 0x12        // mainboard


// obsluga zrodel wejscia
#define SERIAL0_BOUND 115200
//#define BT_BOUND 9600
#define BT_BOUND 115200
#define BT_DEV_NAME "barobotA"
//#define SERIAL0_BOUND 9600

// ycz usyzwac sterownika 2 czy 4 pinowego
#define SERVOX4PIN true
#define SERVOY4PIN false

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

// INDEX pinów zeby sie nie pomylić
// 0  TX0     I/O    SERIAL0
// 1  RX0     I/O    SERIAL0
// 2  PWM     IN     INT0    // WOLNY INT
// 3  PWM     IN     INT1    // IRRZ_PIN Krańcowy Z
// 4  PWM     OUT    STEPPER_Z_PWM
// 5  PWM     OUT    STATUS_LED03 - LED od tacki
// 6  PWM     OUT    STATUS_LED04
// 7  PWM     OUT    STATUS_LED05
// 8  PWM     OUT    STATUS_LED06
// 9  PWM     OUT    STATUS_LED07
// 10  PWM    OUT    STATUS_LED08
// 11  PWM    OUT    STATUS_LED09
// 12  PWM    OUT    STATUS_LED_PIN
// 13  PWM    OUT    STATUS_LED01
// 14  TX3    I/O    serial3 BT
// 15  RX3    I/O    serial3 BT
// 16  TX2 
// 17  RX2
// 18  TX1    IN    INT5    // Enkoder Y
// 19  RX1    IN    INT4    // Enkoder X
// 20  i2c?   IN    INT3
// 21  i2c?   IN    INT2
// 22         IN   ultrasonic 0  PIN_ULTRA0_TRIG
// 23         IN   ultrasonic 0  PIN_ULTRA0_ECHO 
// 24         
// 25         
// 26         IN   ultrasonic 1  PIN_ULTRA1_TRIG  
// 27         IN   ultrasonic 1  PIN_ULTRA0_ECHO  
// 28         IN   Endstop X    IRRX_PIN  
// 29         IN   Endstop Y    IRRY_PIN
// 30         OUT  STATUS_LED02
// 30
// 31  
// 32
// 33  
// 34
// 35  
// 36
// 37  
// 38  
// 39  
// 30          OUT STEPPER_Z_ENABLE
// 31   
// 32  
// 33  
// 34  
// 35  
// 36  
// 37  
// 38  
// 39  
// 40          stepper Y Step // STEPPER_Y_STEP0 
// 41          stepper Y DIR  // STEPPER_Y_STEP1 
// 42                         // STEPPER_Y_STEP2 
// 43                         // STEPPER_Y_STEP3 

// 44          enable_pin_x   // STEPPER_X_ENABLE
// 45          enable_pin_Y   // STEPPER_Y_ENABLE

// 46          stepper X Step / STEPPER_X_STEP0 
// 47          stepper X DIR  / STEPPER_X_STEP1
// 48          stepper X      / STEPPER_X_STEP2
// 49          stepper X      / STEPPER_X_STEP3


//ANALOGS:
// A0    // potencjomentr
// A1
// A2    // miernik wagi podlaczony do multipleksera
// A3    
// A4  
// A5  
// A6  
// A7  
// A8  
// A9  
// A10  
// A11  
// A12  
// A13  
// A14  
// A15  



// udawane analogi
// A20 distance 0
// A21 distance 1


// PINY dla wag
#define PIN_WAGA_ANALOG A2
//#define PIN_WAGA_BUTTLES_ANALOG A3

#define IRRX_PIN 28
#define IRRY_PIN 29
#define IRRZ_PIN 3

// PINY dla ultradzwięków
#define PIN_ULTRA0_TRIG PIN22
#define PIN_ULTRA0_ECHO PIN23
#define PIN_ULTRA1_TRIG PIN26
#define PIN_ULTRA1_ECHO PIN27
#define MAX_DISTANCE 200
// powtarzaj 5 razy odczyt
#define DIST_REPEAT 1

// mozliwe stany uC (mikrokontrolera)
#define STATE_INIT 2
#define STATE_READY 4
#define STATE_BUSY 8
#define STATE_ERROR 32

// koniec stanu uC

// kody osi
#define NO_AXIS 0
#define AXIS_X 2
#define AXIS_Y 4
#define AXIS_Z 8
// koniec kody osi

// PINY dla stepperow
#define STEPPER_X_STEP PIN46
#define STEPPER_X_DIR PIN47
#define STEPPER_X_STEP0 PIN46
#define STEPPER_X_STEP1 PIN47
#define STEPPER_X_STEP2 PIN48
#define STEPPER_X_STEP3 PIN49

#define STEPPER_X_ENABLE PIN44
#define STEPPER_Y_ENABLE PIN45
//#define STEPPER_Z_ENABLE PIN30

#define STEPPER_Y_STEP PIN40
#define STEPPER_Y_DIR PIN41
#define STEPPER_Y_STEP0 PIN40
#define STEPPER_Y_STEP1 PIN41
#define STEPPER_Y_STEP2 PIN42
#define STEPPER_Y_STEP3 PIN43

#define STEPPER_Z_PWM PIN4

// Mnoznik pozycji progamowej na sprzetowa
#define STEPPER_X_MUL  1
#define STEPPER_Y_MUL  1

// PINy dla wyjść podświetlenia
#define STATUS_LED01 PIN13
#define STATUS_LED02 PIN30
#define STATUS_LED03 PIN5
#define STATUS_LED04 PIN6
#define STATUS_LED05 PIN7
#define STATUS_LED06 PIN8
#define STATUS_LED07 PIN9
#define STATUS_LED08 PIN10
#define STATUS_LED09 PIN11
// ktory to LED od szklanyki (jeden z powyzszych)
#define STATUS_GLASS_LED STATUS_LED05

// domyslen ustawienie mocy silnika Z
// pozycja jechania do góry i czas jechania
#define SERVOZ_UP_POS 2200

// pozycja jechania w dół i czas jechania
#define SERVOZ_DOWN_POS 900

// domyslnie ustawienie mocy silnikow Xy
#if SERVOX4PIN==true
  #define SPEEDX 400
  #define ACCELERX 1050
  #define XLENGTH 1700
#else
  #define SPEEDX 4000
  #define ACCELERX 9000
  #define XLENGTH 12700
#endif

// domyslnie ustawienie mocy silnikow Xy
#if SERVOY4PIN==true
  #define YLENGTH 600
  #define SPEEDY 800
  #define ACCELERY 1050
#else
  #define SPEEDY 4000
  #define ACCELERY 3000
  #define YLENGTH 10000  
#endif

// kierunki dla serva Z
#define DIR_UP 1
#define DIR_DOWN 2
#define DIR_STOP 0

// czy wylączaj stepper X gdy zajechal za miejsce?
#define STEPPERX_READY_DISABLE true
// czy wylączaj stepper Y gdy zajechal za miejsce?
#define STEPPERY_READY_DISABLE true

/*
#define STEPPERX_ADD_X_LOW 3
#define STEPPERX_ADD_X_HIGH 3
#define STEPPERX_ADD_Y_LOW 3
#define STEPPERX_ADD_Y_HIGH 3
*/
