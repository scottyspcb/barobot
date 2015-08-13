# Technical details #


An Android tablet attached to Barobot frame serves as user interface. We choose a low cost unit as there will be limited need for processor power and fancy graphics.

Tablet communicates via USB/RS232 with custom made master PCB with ATMega 328 microcontroller (yep, that is the same chip as in Arduino Uno). Barobot features a distributed architecture which means that there are several small PCBs in different places rather than one big one. All the PCBs communicate via I2C and ISP protocols. One of the advantages of this setup is that there are over 120 individually operated LEDs that can illuminate the frame and bottles in a myriad of different ways (simple elegance mode? disco italiano? you name it!)

No robot is complete without software. Barobot Android application is under heavy development but so far includes over 1500 drinks. User defines what bottles are attached to the dispensers and program filters the whole drinks database to show only drinks that can be created using loaded liquids. Of course you can add your own drinks too.


On the microchip side the software will allow for driving hardware and collecting sensor inputs. Anybody will be able to change it using well known Arduino language and console. On top of that a clever trick we used allows for easy firmware update of the whole distributed system. So if you feel less programing saavy you will be able to just press a button on Android to update the all the microchips with newest firmware.



More info:
http://barobot.com/barobot/