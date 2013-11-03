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
#define I2C_ADR_TROLLEY 0x09
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
#define PROGRAMMER_DEVICE_TYPE 0x13
#define PROGRAMMER_VERSION 0x01
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
	#define PIN_MAINBOARD_LEFT_RESET 14

#endif

/*------------------ IPANEL -------------------*/
#if IS_IPANEL
	#define IPANEL_COMMON_ANODE true		// sterowanie plusem? false gdy sterowaniem minusem

	// domyslen ustawienie mocy silnika Z
	// pozycja jechania do góry i czas jechania
	#define IPANEL_SERVOZ_UP_POS 2200

	// pozycja jechania w dó³ i czas jechania
	#define IPANEL_SERVOZ_DOWN_POS 900

	//#define PIN_IPANEL_SELF_RESET 7		// ppin 13

	#define PIN_IPANEL_HALL_X A0		// ppin 23
	#define PIN_IPANEL_HALL_Y A1		// ppin 24
	#define PIN_IPANEL_WEIGHT A2		// ppin 25

	#define PIN_IPANEL_SERVO_Y 5		// ppin 11
	#define PIN_IPANEL_SERVO_Z 6		// ppin 12

	#define PIN_UPANEL_LED0_NUM	2		// ppin 4
	#define PIN_UPANEL_LED1_NUM	3		// ppin 5
	#define PIN_UPANEL_LED2_NUM	4		// ppin 6
	#define PIN_UPANEL_LED3_NUM	7		// ppin 13
	#define PIN_UPANEL_LED4_NUM	8		// ppin 14
	#define PIN_UPANEL_LED5_NUM	9		// ppin 15
	#define PIN_UPANEL_LED6_NUM	10		// ppin 16
	#define PIN_UPANEL_LED7_NUM	17		// ppin 26


#endif
/*------------------ PROGRAMMER -------------------*/
#if IS_PROGRAMMER


#endif
/*------------------ UPANEL -------------------*/

#if IS_UPANEL

	#define UPANEL_COMMON_ANODE true		// sterowanie plusem? false gdy sterowaniem minusem

	#define PIN_UPANEL_LEFT_RESET 14
	#define PIN_UPANEL_POKE 5

	#define PIN_UPANEL_LED0_NUM	4
	#define PIN_UPANEL_LED1_NUM	5
	#define PIN_UPANEL_LED2_NUM	6
	#define PIN_UPANEL_LED3_NUM	7
	#define PIN_UPANEL_LED4_NUM	8
	#define PIN_UPANEL_LED5_NUM	9
	#define PIN_UPANEL_LED6_NUM	16
	#define PIN_UPANEL_LED7_NUM	17

	#define PIN_UPANEL_LED0_MASK	digital_pin_to_bit_mask_PGM+PIN_UPANEL_LED0_NUM
	#define PIN_UPANEL_LED1_MASK	digital_pin_to_bit_mask_PGM+PIN_UPANEL_LED1_NUM
	#define PIN_UPANEL_LED2_MASK	digital_pin_to_bit_mask_PGM+PIN_UPANEL_LED2_NUM
	#define PIN_UPANEL_LED3_MASK	digital_pin_to_bit_mask_PGM+PIN_UPANEL_LED3_NUM
	#define PIN_UPANEL_LED4_MASK	digital_pin_to_bit_mask_PGM+PIN_UPANEL_LED4_NUM
	#define PIN_UPANEL_LED5_MASK	digital_pin_to_bit_mask_PGM+PIN_UPANEL_LED5_NUM
	#define PIN_UPANEL_LED6_MASK	digital_pin_to_bit_mask_PGM+PIN_UPANEL_LED6_NUM
	#define PIN_UPANEL_LED7_MASK	digital_pin_to_bit_mask_PGM+PIN_UPANEL_LED7_NUM
#endif












