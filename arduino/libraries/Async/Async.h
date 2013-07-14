/*
  Test.h - Test library for Wiring - description
  Copyright (c) 2006 John Doe.  All right reserved.
*/

// ensure this library description is only included once
#ifndef async_h
#define async_h

#include "Arduino.h"

class Async{
    public:
      Async(int time, int repeat);                  // powtarzaj co TIME milisekund, REPEAT razy, po tym raz onReady
      Async(int time);                              // powtarzaj co TIME milisekund, nie uruchamia nigdy onReady
      void Update();                                // sprawdz czy nalezy uruchomic
      void Enable();                                // sprawdz czy nalezy uruchomic
      void Update(unsigned int now);                // sprawdz czy nalezy uruchomic dla podanego czasu
      void Init(void (*dFunction2)(String in, int repeat), String in );                // inicjuj zadanie
      void onTick(String (*dFunction2)(String in, int repeat));    // ustaw co zrobic gdy nadszedl czas
      void onReady(void (*dFunction2)(String in,String res, int repeat));   // ustaw co zrobic gdy zakonczono
      void Destroy();                               // zakoncz wykonywanie
     private:
      unsigned int counter;
      bool enabled;
      unsigned int repeat_time;
      unsigned int lastTime;
      void run();
      int repeat;
      String (*onTickF)(String data, int repeat);
      void (*onReadyF)(String in,String res, int repeat);
};

#endif

