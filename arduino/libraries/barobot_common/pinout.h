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