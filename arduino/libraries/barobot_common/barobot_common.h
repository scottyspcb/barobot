/*
ATMEGA8 / ARDUINO
                  +-\/-+
            PC6  1|    |28  PC5 (A5/ D19) (SDA)
  RX  (D0)  PD0  2|    |27  PC4 (A4/ D18) (SCL)
  TX  (D1)  PD1  3|    |26  PC3 (A3/ D17)
 INT0 (D2)  PD2  4|    |25  PC2 (A2/ D16)
 INT1 (D3)  PD3  5|    |24  PC1 (A1/ D15)
 XCK  (D4)  PD4  6|    |23  PC0 (A0/ D14)
            VCC  7|    |22  GND
            GND  8|    |21  AREF
 XTAL       PB6  9|    |20  AVCC
 XTAL       PB7 10|    |19  PB5 (D13) SCK
      (D5)  PD5 11|    |18  PB4 (D12) MISO
 AIN0 (D6)  PD6 12|    |17  PB3 (D11) MOSI PWM
 AIN1 (D7)  PD7 13|    |16  PB2 (D10) SS PWM
      (D8)  PB0 14|    |15  PB1 (D9) PWM      
                  +----+
*/

/*------------------------------    i2c     ------------------------------*/
//#define I2C_ADR_MASTER 0x01
#define I2C_ADR_MAINBOARD 0x01
#define I2C_ADR_RESERVED 0x06
#define I2C_ADR_PROGRAMMER 0x07
//#define I2C_ADR_TROLLEY 0x09
#define I2C_ADR_IPANEL 0x0A
#define I2C_ADR_USTART 0x0B
#define I2C_ADR_UEND 0x70

/*------------------------------ MAINBOARD  ------------------------------*/
#define MAINBOARD_DEVICE_TYPE 0x10
#define MAINBOARD_VERSION 0x01
#define MAINBOARD_F_CPU 16000000
#define MAINBOARD_CPU atmega328"


/*------------------------------ IPANEL     ------------------------------*/
#define IPANEL_DEVICE_TYPE 0x11
#define IPANEL_VERSION 0x01
#define IPANEL_F_CPU == 16000000
#define IPANEL_CPU "atmega328"

/*------------------------------ TROLLEY    ------------------------------*/
#define TROLLEY_DEVICE_TYPE 0x13
#define TROLLEY_VERSION 0x01
#define TROLLEY_F_CPU 8000000
#define TROLLEY_CPU "atmega8"



/*------------------------------ PROGRAMMER ------------------------------*/
#define PROGRAMMER_DEVICE_TYPE 0x14
#define PROGRAMMER_VERSION 0x01
#define PROGRAMMER_F_CPU 12000000
#define PROGRAMMER_CPU "atmega8"

/*------------------------------  UPANEL    ------------------------------*/
#define UPANEL_DEVICE_TYPE 0x13
#define UPANEL_VERSION 0x01
#define UPANEL_F_CPU 8000000
#define UPANEL_CPU "atmega8"


/*------------------------------ OTHER ------------------------------*/
#define COUNT_UPANEL 12
#define COUNT_UPANEL_ONBOARD_LED 8
#define COUNT_IPANEL 1
#define COUNT_IPANEL_ONBOARD_LED 8

/*------------------ MAINBOARD -------------------*/
#if IS_MAINBOARD

	#define MAINBOARD_USE_BT false
	#define MAINBOARD_USE_SERIAL0 true
	#define MAINBOARD_SERIAL0_BOUND 115200

	#define MAINBOARD_BT_BOUND 115200
	#define MAINBOARD_BT_DEV_NAME "barobotA"

	// czy wyl¹czaj stepper X gdy zajechal za miejsce?
	#define MAINBOARD_STEPPER_READY_DISABLE true
	#define MAINBOARD_SERVO_4PIN false

	#define MAINBOARD_BUFFER_LENGTH 10

	// domyslnie ustawienie mocy silnikow Xy
	#if MAINBOARD_SERVO_4PIN==true
	  #define MAINBOARD_SPEEDX 400
	  #define MAINBOARD_ACCELERX 1050
	  #define MAINBOARD_XLENGTH 1700
	#else
	  #define MAINBOARD_SPEEDX 4000
	  #define MAINBOARD_ACCELERX 9000
	  #define MAINBOARD_XLENGTH 12700
	#endif

	//#define MAINBOARD_SERIAL0_BOUND 9600
	#define PIN_MAINBOARD_STEPPER_STEP PIN46
	#define PIN_MAINBOARD_STEPPER_DIR PIN47
	#define PIN_MAINBOARD_STEPPER_STEP0 PIN46
	#define PIN_MAINBOARD_STEPPER_STEP1 PIN47
	#define PIN_MAINBOARD_STEPPER_STEP2 PIN48
	#define PIN_MAINBOARD_STEPPER_STEP3 PIN49
	#define PIN_MAINBOARD_STEPPER_ENABLE PIN44
	
	
	#if IS_PROGRAMMER
		// Put an LED (with resistor) on the following pins:
		// 9: Heartbeat   - shows the programmer is running
		// 8: Error       - Lights up if something goes wrong (use red if that makes sense)
		// 7: Programming - In communication with the slave
		
		#define PIN_PROGRAMMER_LED_ACTIVE	7		// dip 13
		#define PIN_PROGRAMMER_LED_ERROR   	8		// dip 14
		#define PIN_PROGRAMMER_LED_STATE    9		// dip 15
		
		//#define PIN_PROGRAMMER_RESET_MAINBOARD 2	// dip 4
		#define PIN_PROGRAMMER_RESET_UPANEL 9		// dip ??
		#define PIN_PROGRAMMER_RESET_IPANEL 8		// dip ??

		
		#define PROG_FLICKER true
		
	#endif

#endif

/*------------------ IPANEL -------------------*/
#if IS_IPANEL

	/*
		Komponenty:

		HALL_X
		HALL_Y

		SERVO_Y
		SERVO_Z

		WEIGHT_SENSOR
		NEXT_RESET		???
		I2C

	*/

	#define IPANEL_COMMON_ANODE true		// sterowanie plusem? false gdy sterowaniem minusem
	#define IPANEL_BUFFER_LENGTH 10
		
	// domyslen ustawienie mocy silnika Z
	// pozycja jechania do góry i czas jechania
	#define IPANEL_SERVOZ_UP_POS 2200

	// pozycja jechania w dó³ i czas jechania
	#define IPANEL_SERVOZ_DOWN_POS 900

	#define PIN_IPANEL_LED_TEST 4		// dip pin ??
	
	//#define PIN_IPANEL_SELF_RESET 7	// dip pin 13

	#define PIN_IPANEL_HALL_X A0		// dip pin 23
	#define PIN_IPANEL_HALL_Y A1		// dip pin 24
	#define PIN_IPANEL_WEIGHT A2		// dip pin 25

	#define PIN_IPANEL_SERVO_Y 5		// dip pin 11
	#define PIN_IPANEL_SERVO_Z 6		// dip pin 12

	#define PIN_IPANEL_LED0_NUM	2		// dip pin 4
	#define PIN_IPANEL_LED1_NUM	3		// dip pin 5
	#define PIN_IPANEL_LED2_NUM	4		// dip pin 6
	#define PIN_IPANEL_LED3_NUM	7		// dip pin 13
	#define PIN_IPANEL_LED4_NUM	8		// dip pin 14
	#define PIN_IPANEL_LED5_NUM	9		// dip pin 15
	#define PIN_IPANEL_LED6_NUM	10		// dip pin 16
	#define PIN_IPANEL_LED7_NUM	17		// dip pin 26

	// Organizacja pamiêci:
	/*
	0x00	- NEUTRAL_VALUE kopia 0
	0x01	- NEUTRAL_VALUE kopia 1

	*/


#endif

/*------------------ UPANEL -------------------*/

#if IS_UPANEL

	#define UPANEL_COMMON_ANODE true		// sterowanie plusem? false gdy sterowaniem minusem

	#define PIN_UPANEL_LEFT_RESET 14
	#define PIN_UPANEL_POKE 3			// dip pin 5

	#define PIN_UPANEL_LED_TEST 4		// pin 6
		
	#define PIN_UPANEL_LED0_NUM	4		// dip pin 6
	#define PIN_UPANEL_LED1_NUM	5		// dip pin 11
	#define PIN_UPANEL_LED2_NUM	6		// dip pin 12
	#define PIN_UPANEL_LED3_NUM	7		// dip pin 13
	#define PIN_UPANEL_LED4_NUM	8		// dip pin 14
	#define PIN_UPANEL_LED5_NUM	9		// dip pin 15
	#define PIN_UPANEL_LED6_NUM	16		// dip pin 25 A2
	#define PIN_UPANEL_LED7_NUM	17		// dip pin 26 A3

	#define PIN_UPANEL_LED0_MASK	digital_pin_to_bit_mask_PGM+PIN_UPANEL_LED0_NUM
	#define PIN_UPANEL_LED1_MASK	digital_pin_to_bit_mask_PGM+PIN_UPANEL_LED1_NUM
	#define PIN_UPANEL_LED2_MASK	digital_pin_to_bit_mask_PGM+PIN_UPANEL_LED2_NUM
	#define PIN_UPANEL_LED3_MASK	digital_pin_to_bit_mask_PGM+PIN_UPANEL_LED3_NUM
	#define PIN_UPANEL_LED4_MASK	digital_pin_to_bit_mask_PGM+PIN_UPANEL_LED4_NUM
	#define PIN_UPANEL_LED5_MASK	digital_pin_to_bit_mask_PGM+PIN_UPANEL_LED5_NUM
	#define PIN_UPANEL_LED6_MASK	digital_pin_to_bit_mask_PGM+PIN_UPANEL_LED6_NUM
	#define PIN_UPANEL_LED7_MASK	digital_pin_to_bit_mask_PGM+PIN_UPANEL_LED7_NUM
	
		
	// Organizacja pamiêci:
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



/* 
DIP28
	pin01	PC6	arduino ??		RESET	- CONN1
	pin02	PD0	arduino 00		RX		- CONN2
	pin03	PD1	arduino 01		TX		- CONN2
	pin04	PD2	arduino 02		INT0	- SWITCH
	pin05	PD3	arduino 03		INT1	- CONN1
	pin06	PD4	arduino 04		-
	pin07	VCC
	pin08	GND	
	pin09	PB6	arduino ??		XTAL1 
	pin10	PB7	arduino ??		XTAL2	
	pin11	PD5	arduino 05		-
	pin12	PD6	arduino 06		-
	pin13	PD7	arduino 07		-
	pin14	PB0	arduino 08		- 
	pin15	PB1	arduino 09				-SERVO_X
	pin16	PB2	arduino 10		SS		-SERVO_Y
	pin17	PB3	arduino 11		MOSI	-CONN1
	pin18	PB4	arduino 12		MISO	-CONN1
	pin19	PB5	arduino 13		SCK		-CONN1
	pin20	AVCC	
	pin21	AREF	
	pin22	GND	
	pin23	PC0	arduino A0/D14	ADC0	HALL_X 
	pin24	PC1	arduino A1/D15	ADC1	HALL_Y 
	pin25	PC2	arduino A2/D16	ADC2	WEIGHT
	pin26	PC3	arduino A3/D17	ADC3	
	pin27	PC4	arduino A4/D18	ADC4	SDA	-CONN1
	pin28	PC5	arduino A5/D19	ADC5	SCL	-CONN1

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


	#define PIN_IPANEL_HALL_X A0		// ppin 23
	#define PIN_IPANEL_HALL_Y A1		// ppin 24
	#define PIN_IPANEL_WEIGHT A2		// ppin 25

	#define PIN_IPANEL_SERVO_Y 5		// ppin 9
	#define PIN_IPANEL_SERVO_Z 6		// ppin 10

	#define PIN_UPANEL_LED0_NUM	2		// ppin 32
	#define PIN_UPANEL_LED1_NUM	3		// ppin 1
	#define PIN_UPANEL_LED2_NUM	4		// ppin 2
	#define PIN_UPANEL_LED3_NUM	7		// ppin 11
	#define PIN_UPANEL_LED4_NUM	8		// ppin 12
	#define PIN_UPANEL_LED5_NUM	9		// ppin 13
	#define PIN_UPANEL_LED6_NUM	10		// ppin 14
	#define PIN_UPANEL_LED7_NUM	17		// ppin 26

*/
