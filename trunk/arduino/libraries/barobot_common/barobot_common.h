#ifndef BAROBOT_COMMON
	#define BAROBOT_COMMON	
	#include <Arduino.h>

	#define PPD0		0
	#define PPD1		1
	#define PPD2		2
	#define PPD3		3
	#define PPD4		4
	#define PPD5		5
	#define PPD6		6
	#define PPD7		7
	#define PPB0		8
	#define PPB1		9
	#define PPB2		10
	#define PPB3		11
	#define PPB4		12
	#define PPB5		13
	#define PPC0		14
	#define PPC1		15
	#define PPC2		16
	#define PPC3		17
	#define PPC4		18
	#define PPC5		19
	
	/*------------------------------    i2c     ------------------------------*/
	//#define I2C_ADR_MASTER 0x01
	#define I2C_ADR_MAINBOARD 0x01
	#define I2C_ADR_RESERVED 0x06
	#define I2C_ADR_PROGRAMMER 0x07
	//#define I2C_ADR_TROLLEY 0x09
	#define I2C_ADR_CARRET 0x0A
	#define I2C_ADR_USTART 0x0B
	#define I2C_ADR_UEND 0x70

	/*------------------------------ MAINBOARD  ------------------------------*/
	#define MAINBOARD_DEVICE_TYPE 0x10
	#define MAINBOARD_VERSION 0x01
	#define MAINBOARD_F_CPU 16000000
	#define MAINBOARD_CPU "atmega328"
	#define MAINBOARD_SERIAL0_BOUND 115200


	/*------------------------------ CARRET (carret)    ------------------------------*/
	#define CARRET_DEVICE_TYPE 0x11
	#define CARRET_VERSION 0x01
	#define CARRET_F_CPU 16000000
	#define CARRET_CPU "atmega328"
	#define CARRET_SERIAL0_BOUND 115200


	/*------------------------------ PROGRAMMER ------------------------------*/
	#define PROGRAMMER_DEVICE_TYPE 0x14
	#define PROGRAMMER_VERSION 0x01
	#define PROGRAMMER_F_CPU 12000000
	#define PROGRAMMER_CPU "atmega8"


	/*------------------------------ OTHER ------------------------------*/
	#define COUNT_UPANEL 12
	#define COUNT_UPANEL_ONBOARD_LED 8
	#define COUNT_CARRET 1
	#define COUNT_CARRET_ONBOARD_LED 8


	/*------------------ MAINBOARD -------------------*/
	#if IS_MAINBOARD
		#define CPU_F MAINBOARD_F_CPU
		#define MAINBOARD_USE_BT false
		#define MAINBOARD_USE_SERIAL0 true
		#define SERIAL0_BOUND MAINBOARD_SERIAL0_BOUND
		
		#define MAINBOARD_BT_BOUND 115200
		#define MAINBOARD_BT_DEV_NAME "barobotA"

		// czy wyl¹czaj stepper X gdy zajechal za miejsce?
		#define MAINBOARD_STEPPER_READY_DISABLE true
		#define MAINBOARD_SERVO_4PIN false
		#define MAINBOARD_BUFFER_LENGTH 3

		// domyslnie ustawienie mocy silnikow Xy
		#if MAINBOARD_SERVO_4PIN==true
			#define MAINBOARD_SPEEDX 400
			#define MAINBOARD_ACCELERX 950
			#define MAINBOARD_XLENGTH 1700
			#define PIN_MAINBOARD_STEPPER_STEP0 14			// A0
			#define PIN_MAINBOARD_STEPPER_STEP1 15			// A1
			#define PIN_MAINBOARD_STEPPER_STEP2 16			// A2
			#define PIN_MAINBOARD_STEPPER_STEP3 17			// A3
		#else
			// driver 4-pin (LMxxxx)
			#define MAINBOARD_SPEEDX 2500
			#define MAINBOARD_ACCELERX 9000
			#define MAINBOARD_XLENGTH 12700
			#define PIN_MAINBOARD_STEPPER_ENABLE 14			// A0
			#define PIN_MAINBOARD_STEPPER_STEP 4
			#define PIN_MAINBOARD_STEPPER_DIR 5
		#endif

		#define PIN_MAINBOARD_SDA SDA			// arduino 18 A4
		#define PIN_MAINBOARD_SCL SCL			// arduino 19 A5

		// Set as INPUT to allow programming over ISP
		#define PIN_MAINBOARD_SCK SCK			// 13
		#define PIN_MAINBOARD_MISO MISO			// 11
		#define PIN_MAINBOARD_MOSI MOSI			// 12

		#define PIN_MAINBOARD_FREE_PIN		A6
		#define PIN_MAINBOARD_TABLET_PWR	A7
	//	#define VPIN_TEMP_MB	8

		// reset index = 4
		#define PIN_PROGRAMMER_RESET_UPANEL_FRONT PPD2	//	02
		// reset index = 3
		#define PIN_PROGRAMMER_RESET_UPANEL_BACK PPB0	//	08
		// reset index = 2
		#define PIN_PROGRAMMER_RESET_CARRET PPD7			//	7	
		// reset index = 1
		#define PIN_PROGRAMMER_RESET_MASTER PPC3			// A3/D17

		// PIN 10 czyli SlaveSelect (SS) musi byæ jako output gdziekolwiek aby SPI dzia³a³o
		#define PIN_PROGRAMMER_LED_LIGHT   	PPD3			//	03
		// 	Lights up if something goes wrong
		#define PIN_PROGRAMMER_LED_ERROR   	PPB2			//	10
		// 	shows the programmer is running
		#define PIN_PROGRAMMER_LED_ACTIVE	PPD6			//	06
		// 	In communication with the slave
		#define PIN_PROGRAMMER_LED_STATE    PPB1			//	09
		
		#if IS_PROGRAMMER	
			#define HWVER		2
			#define SWMAJ		1
			#define SWMIN		18
			#define PTIME       30
			#define STK_OK      0x10
			#define STK_FAILED  0x11
			#define STK_UNKNOWN 0x12
			#define STK_INSYNC  0x14
			#define STK_NOSYNC  0x15
			#define CRC_EOP     0x20
			#define beget16(addr) (*addr * 256 + *(addr+1) )

			//#define PROGRAMMER_SERIAL0_BOUND 115200
			#define PROGRAMMER_SERIAL0_BOUND 19200
		//	#define PROGRAMMER_METHOD_PIN	true
		//	#define PROGRAMMER_METHOD_RPC	false
		#endif
	#endif

	/*------------------ CARRET -------------------*/
	#if IS_CARRET

		/*
			Komponenty:

			HALL_X	: getValue
			HALL_Y	: getValue

			SERVO_Y	: setSpeed, setPos, getPos
			SERVO_Z	: setSpeed, setPos, getPos

			WEIGHT_SENSOR	getValue,
			8xLED			set PWMUP,PWMDOWN,FadeUp, FadeDown,TimeUp,TimeDown
			I2C				getValue, test_slave
		*/

		#define CARRET_COMMON_ANODE false		// sterowanie plusem? false gdy sterowaniem minusem
		#define CARRET_BUFFER_LENGTH 6			// i2c input buffer
		#define SERIAL0_BOUND MAINBOARD_SERIAL0_BOUND
			
		// domyslen ustawienie mocy silnika Z
		// pozycja jechania do góry i czas jechania
		#define CARRET_SERVOZ_UP_POS 2200

		// pozycja jechania w dó³ i czas jechania
		#define CARRET_SERVOZ_DOWN_POS 900

		#define PIN_CARRET_SDA SDA
		#define PIN_CARRET_SCL SCL
		
		// set as INPUT to allow programming over ISP
		#define PIN_CARRET_SCK 13			// dip pin 17,
		#define PIN_CARRET_MISO 11			// dip pin 18,
		#define PIN_CARRET_MOSI 12			// dip pin 19,

		#define PIN_CARRET_HALL_X A3		// dip pin 23 A0,	Q 23
		#define PIN_CARRET_HALL_Y A2		// dip pin 24 A1,	Q 24
		#define PIN_CARRET_WEIGHT A0		// dip pin 25 A2,	Q 25

		#define PIN_CARRET_CURRENTY A6		// dip pin ?? A6,	Q 19
		#define PIN_CARRET_CURRENTZ A7		// dip pin ?? A7,	Q 22
		#define PIN_CARRET_TEMP A8			// interanl, no pin

		
		#define INNER_CODE_HALL_X 0
		#define INNER_CODE_HALL_Y 1
		#define INNER_CODE_WEIGHT 2
		#define INNER_CODE_CURRENT_X 3
		#define INNER_CODE_CURRENT_Y 4
		#define INNER_CODE_TEMP 5

		#define PIN_CARRET_SERVO_Y PPB2		// 10
		#define PIN_CARRET_SERVO_Z PPB1		// 9

		#define PIN_PANEL_LED0_NUM	PPD5
		#define PIN_PANEL_LED1_NUM	PPD4		
		#define PIN_PANEL_LED2_NUM	PPD3
		#define PIN_PANEL_LED3_NUM	PPD2
		#define PIN_PANEL_LED4_NUM	PPD6
		#define PIN_PANEL_LED5_NUM	PPD7
		#define PIN_PANEL_LED6_NUM	PPC1
		#define PIN_PANEL_LED7_NUM	PPB0

		#define PIN_PANEL_LED_OFF_WHEN		0b00001000

		#define LED_TOP_RED					PIN_PANEL_LED0_NUM
		#define LED_TOP_GREEN				PIN_PANEL_LED1_NUM
		#define LED_TOP_BLUE				PIN_PANEL_LED2_NUM
		#define LED_TOP_WHITE				PIN_PANEL_LED3_NUM
		#define LED_BOTTOM_RED				PIN_PANEL_LED7_NUM
		#define LED_BOTTOM_GREEN			PIN_PANEL_LED4_NUM
		#define LED_BOTTOM_BLUE				PIN_PANEL_LED5_NUM
		#define LED_BOTTOM_WHITE			PIN_PANEL_LED6_NUM

		// EEPROM content:
		/*
		0x00	- NEUTRAL_VALUE kopia 0
		0x01	- NEUTRAL_VALUE kopia 1
		*/

	#endif






	/*------------------------------  UPANEL    ------------------------------*/

	#define UPANEL_DEVICE_TYPE 0x13
	#define UPANEL_VERSION 0x01
	#define UPANEL_F_CPU 8000000
	#define UPANEL_CPU "atmega8"
	#define UPANEL_SERIAL0_BOUND 115200

	#ifdef IS_UPANEL

		#define CPU_F UPANEL_F_CPU

		#define UPANEL_COMMON_ANODE false		// sterowanie minusem? false gdy sterowaniem minusem
		#define SERIAL0_BOUND UPANEL_SERIAL0_BOUND

		// set as INPUT do allow programming over ISP
		#define PIN_UPANEL_SCK SCK				// dip pin 17
		#define PIN_UPANEL_MISO MISO			// dip pin 18
		#define PIN_UPANEL_MOSI MOSI			// dip pin 19

		#define PIN_UPANEL_LEFT_RESET 14		// dip pin 23
		#define PIN_UPANEL_POKE 16

		#define PIN_UPANEL_SDA SDA
		#define PIN_UPANEL_SCL SCL

		#define PIN_PANEL_LED0_NUM	PPD5
		#define PIN_PANEL_LED1_NUM	PPD3
		#define PIN_PANEL_LED2_NUM	PPD4
		#define PIN_PANEL_LED3_NUM	PPC3

		#define PIN_PANEL_LED4_NUM	PPB0
		#define PIN_PANEL_LED5_NUM	PPD7
		#define PIN_PANEL_LED6_NUM	PPB1
		#define PIN_PANEL_LED7_NUM	PPD6
		
		#define PIN_PANEL_LED0_OFF_WHEN	0
		#define PIN_PANEL_LED1_OFF_WHEN	0
		#define PIN_PANEL_LED2_OFF_WHEN	0
		#define PIN_PANEL_LED3_OFF_WHEN	0
		
		#define PIN_PANEL_LED4_OFF_WHEN	1
		#define PIN_PANEL_LED5_OFF_WHEN	1
		#define PIN_PANEL_LED6_OFF_WHEN	1
		#define PIN_PANEL_LED7_OFF_WHEN	0

		#define PIN_PANEL_LED_OFF_WHEN		0b10001111

		#define LED_TOP_RED					PIN_PANEL_LED0_NUM
		#define LED_TOP_GREEN				PIN_PANEL_LED1_NUM
		#define LED_TOP_BLUE				PIN_PANEL_LED2_NUM
		#define LED_TOP_WHITE				PIN_PANEL_LED3_NUM
		#define LED_BOTTOM_RED				PIN_PANEL_LED4_NUM
		#define LED_BOTTOM_GREEN			PIN_PANEL_LED5_NUM
		#define LED_BOTTOM_BLUE				PIN_PANEL_LED6_NUM
		#define LED_BOTTOM_WHITE			PIN_PANEL_LED7_NUM

		// EEPROM content:
		/*
		0x00	- i2c adres kopia 0
		0x01	- i2c adres kopia 1
		0x02	- i2c adres kopia 2

		0xF0	- starts m³odsze
		0xF1	- starts starsze

		0xF2	- i2c errors m³odsze
		0xF3	- i2c errors starsze

		0xFA	- watchdog m³odsze
		0xFB	- watchdog starsze
		*/
		
	#endif

	#if 1
	#define DEBUGINIT(sth) (Serial.begin(SERIAL0_BOUND))
	#define DEBUG(sth) (Serial.print(String(sth)))
	#define DEBUGLN(sth) (Serial.println(String(sth)))
	#define DEBUG_ON	true
	#else
	#define DEBUGINIT(sth)
	#define DEBUG(sth)
	#define DEBUGLN(sth)
	#define DEBUG_ON	false
	#endif

	// tyle dodaæ aby liczby bajtowe z zakresu 0-50 by³y widoczne w podgl¹dzie magistrali UART.
	#define VISIBLER A

	// typy b³êdów:
	#define 	T_UNKNOWN_E		0x05
	#define 	T_ENGINE_E		0x06
	#define 	T_WRITE_I2C_E	0x07	
	#define 	T_READ_I2C_E	0x08

	// inne
	#define DRIVER_DIR_FORWARD 	32
	#define DRIVER_DIR_BACKWARD 64
	#define DRIVER_DIR_STOP		0

	#if HAS_LEDS
		typedef struct{ 
		  uint8_t pin;      // hardware I/O port and pin for this channel
		  volatile uint8_t *outport;
		  uint8_t pinmask;
		  uint8_t current_pwm;
		  uint8_t fadeup;      // 0- 15
		  uint8_t fadedown;    // 0- 15
		  uint8_t pwmup;       // PWM przy UP
		  uint8_t pwmdown;     // PWM przy DOWN
		  uint8_t timedown;    // Czas trwania UP
		  uint8_t timeup;      // Czas trwania DOWN
		} PWMChannel;

		volatile PWMChannel _pwm_channels[COUNT_UPANEL_ONBOARD_LED]= {
			{LED_TOP_RED,		0,0,0,0,0},
			{LED_TOP_GREEN,		0,0,0,0,0},
			{LED_TOP_BLUE,		0,0,0,0,0},
			{LED_TOP_WHITE,		0,0,0,0,0},
			{LED_BOTTOM_RED,	0,0,0,0,0},
			{LED_BOTTOM_GREEN,	0,0,0,0,0},
			{LED_BOTTOM_BLUE,	0,0,0,0,0},
			{LED_BOTTOM_WHITE,	0,0,0,0,0}
		};
		boolean prog_mode  = false;    // czy magistrala jest w trybie programowania?
		boolean prog_me    = false;    // czymam zamiar programowaæ mnie?
		
		#define PIN_PANEL_LED0_MASK	digital_pin_to_bit_mask_PGM+PIN_PANEL_LED0_NUM
		#define PIN_PANEL_LED1_MASK	digital_pin_to_bit_mask_PGM+PIN_PANEL_LED1_NUM
		#define PIN_PANEL_LED2_MASK	digital_pin_to_bit_mask_PGM+PIN_PANEL_LED2_NUM
		#define PIN_PANEL_LED3_MASK	digital_pin_to_bit_mask_PGM+PIN_PANEL_LED3_NUM
		#define PIN_PANEL_LED4_MASK	digital_pin_to_bit_mask_PGM+PIN_PANEL_LED4_NUM
		#define PIN_PANEL_LED5_MASK	digital_pin_to_bit_mask_PGM+PIN_PANEL_LED5_NUM
		#define PIN_PANEL_LED6_MASK	digital_pin_to_bit_mask_PGM+PIN_PANEL_LED6_NUM
		#define PIN_PANEL_LED7_MASK	digital_pin_to_bit_mask_PGM+PIN_PANEL_LED7_NUM
		
		
		void set_pin( byte pin, boolean value );

		
	#endif
#endif


// 32bit int
union byteint{
    byte bytes[4];
    int i;
};
// 16bit int
union byteword{
    byte bytes[2];
    word i;
};

/* 
ARDUINO DIP to TQFP
Arduino		DIP		TQFP32	Port	
0			2		30		PD0		RX
1			3		31		PD1		TX
2			4		32		PD2		INT0
3			5		1		PD3		INT1 PWM
4			6		2		PD4		XCK
5			11		9		PD5		PWM
6			12		10		PD6		AIN0 PWM
7			13		11		PD7		AIN1
8			14		12		PB0		
9			15		13		PB1		PWM
10			16		14		PB2		SS PWM
11			17		15		PB3		MOSI PWM
12			18		16		PB4		MISO
13			19		17		PB5		SCK
14	A0		23		23		PC0		ADC0
15	A1		24		24		PC1		ADC1
16	A2		25		25		PC2		ADC2
17	A3		26		26		PC3		ADC3
18	A4		27		27		PC4		ADC4 SDA
19	A5		28		28		PC5		ADC5 SCL
RESET		1		29		PC6
XTAL1		9		7		PB6		XTAL
XTAL2		10		8		PB7		XTAL
-	A6		-		19				ADC6
-	A7		-		22				ADC7


TQFP32
	pin01	PD3	arduino 03	PWM	- CONN1
	pin02	PD4	arduino 04
	pin03	GND
	pin04	VCC
	pin05	GND
	pin06	VCC
	pin07	PB6	arduino ??	XTAL1 
	pin08	PB7	arduino ??	XTAL2
	pin09	PD5	arduino 05	PWM			-SERVO_Y
	pin10	PD6	arduino 06	PWM			-SERVO_Z
	pin11	PD7	arduino 07				- 
	pin12	PB0	arduino 08				-
	pin13	PB1	arduino 09		
	pin14	PB2	arduino 10	PWM,SS

	pin15	PB3	arduino 11	MOSI		-CONN1
	pin16	PB4	arduino 12	MISO		-CONN1
	pin17	PB5	arduino 13	SCK			-CONN1
	pin18	AVCC
	pin19	ADC6	??
	pin20	AREF
	pin21	GND
	pin22	ADC7	??
	pin23	PC0	arduino A0/D14	ADC0	-HALL_X  
	pin24	PC1	arduino A1/D15	ADC1	-HALL_Y 
	pin25	PC2	arduino A2/D16	ADC2	-WEIGHT
	pin26	PC3	arduino A3/D17	ADC3	-
	pin27	PC4	arduino A4/D18	ADC4	-SDA	-CONN1
	pin28	PC5	arduino A5/D19	ADC5	-SCL	-CONN1
	pin29	PC6	arduino ??		RESET	-CONN1
	pin30	PD0	arduino 00		RX		-CONN2
	pin31	PD1	arduino 01		TX		-CONN2
	pin32	PD2	arduino 02		INT0	-

*/

/*


boolean i2c_getValue(  byte slave_address, byte pin ){      // zwraca 2 bajty. typ na m³odszych bitach, versja na starszych
  out_buffer[0]  = METHOD_GETVALUE;
  out_buffer[1]  = pin;
  byte error = writeRegisters(slave_address, 2, true );
  if(!error){
    readRegisters( slave_address, 1 );
    byte res = in_buffer[0];    // = wersja
    if( res == 0 ){
      return false;
    }
    if( res == 0xFF ){
       return true;
    }
    // todo. zwróc true ale raportuj warning
    return true;
  }
  return false;
}

void i2c_reloadAddress( byte slave_address, byte new_addr ){			// Zmieñ adres I2c, musi byæ podane co najmniej 4 razy zeby zadzia³a³o. (2 bajty)
  out_buffer[0]  = METHOD_RESETSLAVEADDRESS;
  out_buffer[1]  = new_addr;
  writeRegisters(slave_address, 2, true );    // powtarzaj
  writeRegisters(slave_address, 2, true );
  writeRegisters(slave_address, 2, true );
  writeRegisters(slave_address, 2, true );
  writeRegisters(slave_address, 2, true );
}


void i2c_resetCycles( byte slave_address ){
  out_buffer[0]  = METHOD_RESETCYCLES;
  writeRegisters(slave_address, 1, true );
}
void i2c_setPWM( byte slave_address, byte pin, byte level ){		                              // Wpisz wype³nienie PWM do LEDa ( 3 bajty )
  out_buffer[0]  = METHOD_SETPWM;
  out_buffer[1]  = pin;
  out_buffer[2]  = level;
  writeRegisters(slave_address, 3, true );
}
void i2c_setTime( byte slave_address, byte pin, byte on_time, byte off_time ){			            // Czas pomiêdzy kolejnym zapaleniem i Czas od zapalenia do zgaszenia ( 4 bajty )
  out_buffer[0]  = METHOD_SETTIME;
  out_buffer[1]  = pin;
  out_buffer[2]  = on_time;
  out_buffer[3]  = off_time;
  writeRegisters(slave_address, 3, true );
}
void i2c_setFading( byte slave_address,  byte pin, byte level_in, byte level_out ){			       // Czas i kierunek zanikania PWMa	( 4 bajty )
  out_buffer[0]  = METHOD_SETFADING;
  out_buffer[1]  = pin;
  out_buffer[2]  = level_in;
  out_buffer[2]  = level_out;
  writeRegisters(slave_address, 2, true );
}










X5,40,200
X55,60,-1250

D:\PROG\arduino-1.0.5\hardware/tools/avr/bin/avrdude -CD:\PROG\arduino-1.0.5\hardware/tools/avr/etc/avrdude.conf -v -v -v -v -D -patmega8 -cstk500v1 -P\\.\COM40 -b19200 -Uflash:w:c:\temp\build7005077114599572471.tmp\atmega8_i2c_slave.cpp.hex:i -Ulock:w:0x3F:m -Uhfuse:w:0xc4:m -Ulfuse:w:0xe4:m
D:\PROG\arduino-1.0.5\hardware/tools/avr/bin/avrdude -CD:\PROG\arduino-1.0.5\hardware/tools/avr/etc/avrdude.conf -v -v -v -v -D -patmega8 -cstk500v1 -P\\.\COM43 -b19200 -Ulock:w:0x3F:m -Uhfuse:w:0xc4:m -Ulfuse:w:0xe4:m
D:\PROG\arduino-1.0.5\hardware/tools/avr/bin/avrdude -CD:\PROG\arduino-1.0.5\hardware/tools/avr/etc/avrdude.conf -v -v -v -v -patmega8 -cstk500v1 -P\\.\COM40 -b19200 -Uflash:w:c:\temp\build7005077114599572471.tmp\atmega8_i2c_slave.cpp.hex:i



D:\PROG\arduino-1.0.5\hardware/tools/avr/bin/avrdude -CD:\PROG\arduino-1.0.5\hardware/tools/avr/etc/avrdude.conf -v -v -v -v -patmega8 -cstk500v1 -P\\.\COM43 -b19200 -Uflash:w:c:\temp\build7005077114599572471.tmp\atmega8_i2c_slave.cpp.hex:i

-------- ATMEGA 8 FUSE OK --------
D:\PROG\arduino-1.0.5\hardware/tools/avr/bin/avrdude -CD:\PROG\arduino-1.0.5\hardware/tools/avr/etc/avrdude.conf -v -v -v -v -D -patmega8 -cstk500v1 -P\\.\COM40 -b19200 -Ulock:w:0x3F:m -Uhfuse:w:0xc4:m -Ulfuse:w:0xe4:m
-----------------------------

-------- ATMEGA 8 FUSE DEFAULT --------
D:\PROG\arduino-1.0.5\hardware/tools/avr/bin/avrdude -CD:\PROG\arduino-1.0.5\hardware/tools/avr/etc/avrdude.conf -v -v -v -v -D -patmega8 -cstk500v1 -P\\.\COM40 -b19200 -Ulock:w:0x3F:m -Uhfuse:w:0xD9:m -Ulfuse:w:0xE1:m
-----------------------------


-------- UPANEL ATMEGA 8 FUSE +BROWN +1024--------
D:\PROG\arduino-1.0.5\hardware/tools/avr/bin/avrdude -CD:\PROG\arduino-1.0.5\hardware/tools/avr/etc/avrdude.conf -v -v -v -v -D -patmega8 -cstk500v1 -P\\.\COM40 -b19200 -Ulock:w:0x3F:m -Uhfuse:w:0xC7:m -Ulfuse:w:0xA4:m
-----------------------------


-------- UPANEL ATMEGA 8 FUSE TEST -------
D:\PROG\arduino-1.0.5\hardware/tools/avr/bin/avrdude -CD:\PROG\arduino-1.0.5\hardware\tools\avr\etc\avrdude.conf -v -v -v -v -D -patmega8 -cstk500v1 -P\\.\COM39 -b19200 -U lfuse:r:-:h -U hfuse:r:-:h -U efuse:r:-:h -U lock:r:-:h
-----------------------------


-------- UPANEL ATMEGA 8 CODE --------
D:\PROG\arduino-1.0.5\hardware/tools/avr/bin/avrdude -CD:\PROG\arduino-1.0.5\hardware/tools/avr/etc/avrdude.conf -v -v -v -v -D -patmega8 -cstk500v1 -P\\.\COM39 -b19200 -Uflash:w:c:\temp\build3775116777284934834.tmp\barobot_upanel.cpp.hex:i 
-----------------------------





-------- CARRET ATMEGA 328 FUSE  --------
D:\PROG\arduino-1.0.5\hardware/tools/avr/bin/avrdude -CD:\PROG\arduino-1.0.5\hardware/tools/avr/etc/avrdude.conf -v -v -v -v -p m328p  -cstk500v1 -u -P\\.\COM39 -b19200 -U lfuse:w:0xFF:m -U hfuse:w:0xDB:m -U efuse:w:0x05:m
-----------------------------

-------- CARRET ATMEGA 328 CODE --------
D:\PROG\arduino-1.0.5\hardware/tools/avr/bin/avrdude -CD:\PROG\arduino-1.0.5\hardware/tools/avr/etc/avrdude.conf -v -v -v -v -p m328p -cstk500v1 -u -P\\.\COM39 -b19200 -Uflash:w:c:\temp\build3775116777284934834.tmp\barobot_upanel.cpp.hex:i 
-----------------------------


-------- CARRET ATMEGA 328 FUSE TEST --------
D:\PROG\arduino-1.0.5\hardware/tools/avr/bin/avrdude -CD:\PROG\arduino-1.0.5\hardware\tools\avr\etc\avrdude.conf -v -v -v -v -pm328p -cstk500v1 -P\\.\COM39 -b19200 -U lfuse:r:-:h -U hfuse:r:-:h -U efuse:r:-:h -U lock:r:-:h


D:\PROG\arduino-1.0.5\hardware/tools/avr/bin/avrdude -CD:\PROG\arduino-1.0.5\hardware\tools\avr\etc\avrdude.conf -v -v -v -v -patmega8 -cstk500v1 -P\\.\COM39 -b19200 


----------------------------- 



-------- UPANEL ATMEGA 8 FUSE TEST -------
D:\PROG\arduino-1.0.5\hardware/tools/avr/bin/avrdude -CD:\PROG\arduino-1.0.5\hardware\tools\avr\etc\avrdude.conf -v -v -v -v -D -patmega8 -cstk500v1 -P\\.\COM39 -b19200 -U lfuse:r:-:h -U hfuse:r:-:h -U efuse:r:-:h -U lock:r:-:h
-----------------------------




-------- MB ATMEGA 328 CODE --------
D:\PROG\arduino-1.0.5\hardware\tools\avr\bin\avrdude -C"D:\PROG\arduino-1.0.5\hardware\tools\avr\etc\avrdude.conf" -q -q  -patmega328p -carduino -P\\.\COM39 -b57600 -D -Uflash:w:"c:\workspace\barobot\arduino\projects2\barobot_mainboard\build\barobot_mainboard.hex":i









-----------------------------


*/


/*
void disable_pin( byte pin ){
    #if UPANEL_COMMON_ANODE
     *_pwm_channels[pin].outport &= ~(_pwm_channels[pin].pinmask);          // turn off the channel (set GND)
    #else
     *_pwm_channels[pin].outport |= _pwm_channels[pin].pinmask;              // turn off the channel (set VCC)
    #endif
}
void enable_pin( byte pin ){
   #if UPANEL_COMMON_ANODE
	  *_pwm_channels[pin].outport |= _pwm_channels[pin].pinmask;              // turn off the channel (set VCC)
   #else
	 *_pwm_channels[pin].outport &= ~(_pwm_channels[pin].pinmask);          // turn off the channel (set GND)
   #endif  
}
*/
