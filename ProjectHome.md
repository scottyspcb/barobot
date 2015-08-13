# Barobot source repository #

An open source cocktail mixing robot can handle up to 12 bottles of liquors, and pour you a perfect drink with mililiter accuracy.

This site is a file repository of the project. All files are accessible for open-source community. Main project site: http://barobot.com

On Google Code here you can read about technical aspects.

### Barobot construction principles ###
  * Dosing accurate amounts of ingredients
  * Adequate shaking/mixing capability
  * Any popular glass and bottle should fit
  * Should handle liquors, juices, carbonated liquids, milk and syrups
  * No cleaning in between drinks (eg. Pinacolada residues cannot spoil Whiskey and Coke poured next)
  * Easy cleaning after the party
  * Frame produced in deterministic way made of popular material
  * Electronics built of popular parts
  * A touchscreen with user interface
  * Easy to assemble at home without advanced skills or tools
  * As easy as possible to operate by unskilled users (eg. slightly intoxicated people late during a party)
  * Can stay in cabinet with alcohol bottles loaded for a long time without need for any interaction
  * Illumination of the cocktail ingredients
  * LED illumination should add WOW factor to the party
  * Upgradeable software and firmware
  * Open source/open hardware
  * Remote cocktail ordering
  * Running on 12V so that it can work from car accumulator
  * Adjustable user interface touchscreen position
  * 12 bottle capability
  * Intuitive user interface that can suggest drinks
  * One person should be able to move it
  * Should fit into 90cm/36’’ cabinet or a sedan car
  * Easily extendable on software and hardware side
  * One load should be enough for ~100 drinks
  * Easy to resupply ingredients during a party
  * And most important: as cost effective as possible

For a year we worked on mechanics, electronics, software and usability. During that time we made numerous iterations and wrote tons of code. Finally we believe we fulfilled all the requirements.

## 3D model (open hardware) ##
You can download Barobot 3D model in PDF format and see it at your own screen
https://barobot.googlecode.com/svn/trunk/hardware/design/Barobot3D.PDF (Acrobat Reader is needed)

Sources are in folder /trunk/hardware/design


## Rendering ##
![http://barobot.googlecode.com/svn-history/r415/trunk/marketing/renderings/barobot_r1.jpg](http://barobot.googlecode.com/svn-history/r415/trunk/marketing/renderings/barobot_r1.jpg)

## Real foto ##
![http://barobot.googlecode.com/svn-history/r415/trunk/marketing/realfoto/double.jpg](http://barobot.googlecode.com/svn-history/r415/trunk/marketing/realfoto/double.jpg)

## Real photo after deployment in test environment ##
![http://barobot.googlecode.com/svn/trunk/marketing/realfoto/barobot_czer_kuch.jpg](http://barobot.googlecode.com/svn/trunk/marketing/realfoto/barobot_czer_kuch.jpg)

(More photos on Facebook or our site http://barobot.com)


## The project consists of a few aspects ##
  * Technical solutions
  * Hardware
  * Electronics
  * Firmware
  * Android application


## Technical solutions ##
  * all the bolts unified to 2 sizes
  * all the elements that touch food are certified food-safe
  * any amount of liquid can be poured, a fraction of dose is determined by timing of actuator action
  * rigid acrylic glass frame that can hold 12 large bottles without flexing
  * distributed system of microchips connected by I2C and ISP bus and governed by Android tablet
  * stepper motor and servos allow for precision moves with adequate speed
  * PCBs are splash proof due to spray coating and location in closed compartments – we had some serious early prototype disasters and electronic components were never harmed
  * regulated tablet holder allows for changing angle of view, there is also alternative tablet position at the back of the machine
  * timing belt and mechanics hidden from view
  * closed-loop positioning system
  * automatic calibration

## Mechanics ##
Frame was designed in Solidworks and is made exclusively from flat laser cut elements 6mm and 10mm in thickness. All parts fit like LEGO blocks and form a rigid and precise 3d structure.
After a lot of experiments we used acrylic glass (PMMA) as the material of choice. Laser cutting source files are at the bottom of the page. Once cut the parts can be assembled using step-by-step manual from http://barobot.com/assets/manuals/Barobot_assembly_guide_v03.pdf


### So how does it work? ###
Barobot pours drinks by moving carriage with glass under one of 12 bottles.
Each bottle has a dispenser – a common bar optic easily obtainable in bartender shops. Barobot uses both 20ml dispensers for spirits and 50ml dispensers for soft drinks (imperial measurement dispensers are available too).

Bottles are set in upside down position in two rows and we let gravity do the work by using a well proven solution.
The dispensers are capable of holding almost any bottle size starting with 0,3l (10 fl. oz) up to party-size Coca-Cola. A wide range of bottleneck sizes fit too. In fact we haven’t seen anything that doesn’t fit except bottles that have plastic insertion in the neck. And those can be easily extracted.

Similarly any glass or even cup will do as long as it fits under the dispenser i.e. is lower than 17cm (6 3/4”). All but the very tallest champaign glasses are fine.


### Moving ###
Glass moves around in two axes – X axis operated by stepper motor and Y by high torque RC model servo. The second “Y axis” is really a curve. We decided that it is easier to create circular than linear motion due to space and cost constraints.
Second servomotor operates “Z axis” which is really just a name for actuator pushing dispenser triggers to pour liquids into glass beneath. It was challenging to find carriage and actuator geometry that would work for both front and back row at the same time. But that is much more cost effective than electro-valve or actuator for each bottle.

![http://barobot.com/assets/img_small/IMG_7944c.jpg](http://barobot.com/assets/img_small/IMG_7944c.jpg)


### Can we shake the drink? ###
To a degree. Liquids fall into the glass from high enough to mix well. Additionally carriage rapid movements can additionally shake the cocktail. Shaking intensity is limited by center of gravity height and shape of the glass. Such mixing is more than enough for most cocktail amateurs. At the same time we know that there are people who differentiate stirred and shaked mixtures.

## Electronics ##
PCBs use popular components easily obtainable in any major electronics shop.

Main board and carriage board are based on one ATMEGA328 each (the same as Arduino hence Barobot is Arduino compatible in terms of programming). They are responsible for collecting and relaying information from sensors as well as giving commands to actuators (motor and servos).

![https://barobot.googlecode.com/svn/trunk/resources/architecture1.png](https://barobot.googlecode.com/svn/trunk/resources/architecture1.png)

The other 12 boards are called u-panels with tiny ATmega8 microprocessors. Their main purpose is operating 96 LEDs on top of the robot (for bottle and Barobot interior illumination). Each u-panel board has 4 RGB and 4 white LEDs. Half of them illuminate bottles and the other half robot interior. Each LED is individually controlled using PWM.


All the PCBs communicate via I2C and ISP protocols in a distributed manner. One of the advantages of this setup is that all those independently operated LEDs that can illuminate the frame and individual bottles in a myriad of different ways. It allows for easy extension of the robot by adding another PCB to the bus.
Additionally existing boards have spare PINs sockets. These can be used to add extra sensors/peripherals (RFID, range sensor, displays etc.).

High level functions including displaying user interface are performed by Android 7” tablet due to the fact that it is easier to implement “business” logic in Java on Android than in microcontroller’s C++;

Tablet is connected to main board via PL2303 and USB/RS232. Tablet serves as command and control center for all the microchips. Tablet's WIFI can be used to access Barobot menu remotely via another smartphone or tablet.

While robot works just fine, there is Murphy’s Law plaque hanging in our lab as a friendly reminder. Our main principle is that in the worst case scenario Barobot can fail to pour a drink or it can even pour a wrong drink, but absolutely nothing should ever spill. After all we have have 12 bottles full of liquid hanging upside down within frame.

Barobot utilizes a series of fail-safe detectors:
X and Y axes have closed-loop control systems. There are magnets placed in strategic positions and polarizations to tell the machine about current glass position. Magnets are detected by Hall sensors and Barobot will start pouring only the glass is directly under a bottle.
Each magnet is placed in position that carriage has to reach to successfully pour from a given bottle. This feature allows for automatic calibration of the machine.

Here is a graph of magnetic field along X axis.
![https://barobot.googlecode.com/svn/trunk/resources/hallx.png](https://barobot.googlecode.com/svn/trunk/resources/hallx.png)

Notice that outermost magnets are stronger and serve as endswitches.


Glass detection is performed by weight sensor located under glass platform. Again no pouring unless glass weight is detected. This sensor doubles as a pouring detection. If Barobot attempts to pour and glass weight is not changing then something went wrong. Presumably there is no more liquid in the bottle.

At the moment weight sensor has only partial support in the code.

Finally Y and Z servos have current sensors. If there is obstacle preventing Barobot to reach desired position and current consumption is high for a prolonged time an error message is produced.

## Firmware ##
Once started all the boards auto-negotiate I2C bus addresses which are later stored in eeproms. Firmware collects data from sensors and relays them to Android. It also receives and interprets commands for LEDs and actuators.

All the commands and communication exchanged between microcontrollers and Android are done asynchronously. So tablet doesn’t have to wait for robot reply/task completion at any moment.
More info about API http://barobot.com/topic/barobot-java-api/


It was important to make all the firmware easily upgradable. Hence ISP bus connecting all the boards. Main board after sending the right command becomes a programmer for other boards (it is compatible with **stk500** or **Arduino**). Additionally mainboard has **optiboot** bootloader so that it can be programmed itself.


Each non-mainboard PCB can be reseted remotely over I2C. Firmware programming is done by chain-resetting boards along I2C bus. This prevented us from the cable nightmare of star architecture. Programming uses the same PINs as normal work so no additional cables are needed.

So to sum up: a new firmware package can be downloaded on the tablet and then one-click uploaded to all Barobot boards.

Firmware can be modified eg. in standard Arduino IDE application.

## Android application ##
Application was written in Java in standard **Eclipse** environment.

It is divided into a few smaller components to allow for integration with different hardware devices or software packages. Components:
  * com.barobot.audio		- sound analyzer implementation
  * com.barobot.audio.example – standalone example of music light show
  * com.barobot.common – config and java interfaces
  * com.barobot.hardware.devices – Barobot modules logic
  * com.barobot.hardware.serial – low level communication via USB, FTDI, or PL2303.
  * com.barobot.isp – programmer lib
  * com.barobot.parser – high level communication using synchronized threads and more
  * com.barobot.web – sofa server
  * main\_app – main tablet app android
All the connections between components are defined to be as easy to work with as possible.


### Connection Android-PCBs ###
There is native Android serial port implementation but it hard to use.
Therefor we used library that solves this problem on majority of Android devices
https://github.com/mik3y/usb-serial-for-android

We tried hard to turn any potential pouring problem into programming task. And we succeeded. All the sensors are accessible from user interface.
Application is based on sqlite database. It holds information on cocktail ingredients, drink recipes, LED light-themes etc. We have a plan to allow synchronization of drink recipes between robots.
User first action is to put bottles and define what ingredient is located in which of the 12 slots. Then application filter drink recipes database against available ingredients and shows only cocktails that can be created with current set of bottles.

Of course at any moment user can add his/her own cocktail recipe or pour improvised drink.


Potential issues when building Barobot on your own

  * only certain kind of dispensers from UK based company fits at the moment
  * PCBs use both sides and require milling
  * At least 80W laser is necessary to cut 10mm acrylic glass with enough quality
  * Not all the tablets can connect via PL2303
  * Cost of around $1800-$2000 for one machine when created individually (if your time is worthless, which is not true)


## Is such a robot going to take work from bartenders? ##
No. Bartending and drink creation is both art and liquid pouring. Our robot can only mix cocktail according to a recipe. It won’t talk with you about troublesome day, it won’t tell a joke and you can’t pick it up. Remember Star Trek Enterprise bar? Drinks were produced by replicator but alive bartender was still there. The art of bartending is here to stay.


### We plan to add much more features like ###
  * sharing party statistics via social networks (for self-confident users, we have in mind the confidentiality of this data)
  * integrate with RFID chips to make users account with their list of favourite drinks or history ("should I serve the usual, sir?"),
  * better handling the load cell to measure weight of liquid (now we can detect a glass only)
  * redesign Led diodes to show ingredients of the drink in better way
  * simple web platform for drinks synchronization
  * more experiments with materials
  * more experiments with power (robot needs ~5A@12V, we want to decrease it )

# What's next? #

You can have a Cocktail Mixing Robot at your home. It is not a science-fiction.

http://barobot.com/store/