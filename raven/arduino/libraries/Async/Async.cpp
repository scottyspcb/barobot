
#include "Async.h"
#include <string.h>
#include <Adb.h>

Async::Async(int time){      // powtarzaj co TIME milisekund
  this->counter     = 0;
  this->repeat_time = time;
  this->repeat      = -1;
  this->enabled     = false;
  this->lastTime    = millis();
}
Async::Async(int time, int repeat){     // powtarzaj co TIME milisekund, REPEAT razy
  this->counter     = 0;
  this->enabled     = false;
  this->repeat_time = time;
  this->repeat      = repeat;
  this->lastTime    = millis();
}

void Async::onTick(String (*dFunction)(String data, int repeat)){
//  this->onTickF = dFunction;
}
//void Async::onReady(void (*dFunction)(String in,String res, int repeat)){
  //this->onReadyF = dFunction;
//}
void Async::Enable(){
  this->enabled     = true;
}

void Async::Init(void (*dFunction)(String in, int repeat), String in ){
  dFunction( in, repeat );
}

void Async::Destroy(){
 // this->onReadyF = NULL;
 // this->onTickF = NULL;
  this->repeat = 0;
  this->enabled     = false;
}

void Async::run(){
      if(this->repeat == -1 ){            // powtarzaj ile sie da 
      //  if(this->onTickF){
       //     this->onTickF("SET CARRET 12,34", this->repeat );
      //  }
      }else if(this->repeat == 0 ){      // koniec
       // if(this->onReadyF){
       //     this->onReadyF("SET CARRET 7,34", "", this->repeat );            // wykonaj tylko tylko onReadyF
       // }
        this->enabled = false;
      }else{                            // jakas wartosc >0
        String res = "";
       // if(this->onTickF){
       //     res = this->onTickF("SET CARRET 12,34", this->repeat);
       // }
        this->repeat--;
     //   if(this->repeat == 0 && this->onReadyF){              // jesli to konczy sprawe to zakoncz
     //     this->onReadyF("SET CARRET 7,34", res, this->repeat );
          this->repeat = -1;      // zakoncz wykonywanie
          this->enabled = false;
     //   }
      }
}

void Async::Update(){                   // sprawdz czy nalezy uruchomic
    if(this->enabled){
      return;
    }
    unsigned int now    = millis();
    if( now > this->lastTime + this->repeat_time ){
      this->run();
      this->lastTime    = now;
    }
}
void Async::Update(unsigned int now){                   // sprawdz czy nalezy uruchomic dla podanego czasu
    if( this->enabled && now > this->lastTime + this->repeat_time ){
      this->run();
      this->lastTime    = now;
    }
}

/*
Przyklad:
  Async* a;
  Async* b;
  a = new Async( 40, 20 );      // co 40ms, 20 razy
  a->onTick( in_40ms );         // co wykonac za kazdym razem
  a->onReady( ready_40ms );     // co wykonac na koncu
  a->Update();

  b = new Async( 1, 1 );       // raz, najszybciej jak sie da
  a->onReady( ready_40ms );     // co wykonac na koncu
  a->Update();
*/

