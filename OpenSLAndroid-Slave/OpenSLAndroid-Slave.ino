#include <Arduino.h>
#include "BasicStepperDriver.h"

// SETUP ARDUINO PINS
#define PIN_DIR 12
#define PIN_STEP 11
#define PIN_LED_UV 8
#define PIN_ENDSTOP 9
#define PIN_BUTTON 10

// Motor steps per revolution. Most steppers are 200 steps or 1.8 degrees/step
// Since microstepping is set externally, make sure this matches the selected mode
// If it doesn't, the motor will move at a different RPM than chosen
// 1=full step, 2=half step etc.
#define MOTOR_STEPS 200
#define MICROSTEPS 2
#define RPM 90 // todo: implement jerk and acceleration
int _microns_Rev = 2000;
long _steps;
// 2-wire basic config, microstepping is hardwired on the driver
BasicStepperDriver stepper(MOTOR_STEPS, PIN_DIR, PIN_STEP);

// PRINTER PARAMS

int _trackZpos;

int _nrBaseLayers;
int _nrTotalLayers;
long _baselLayersMilis;
long _normalLayersMilis;
long _layerSliceHeightMicrons;
long _microns_Step;

const long _modePauseHeightMicrons = 60000;
const long _modeFillHeightMicrons = 60000;
const long _modePeelHeightMicrons = 7000;

void setup() {
  
// Declare pins as Outputs
pinMode(LED_BUILTIN, OUTPUT);
pinMode(PIN_STEP, OUTPUT);
pinMode(PIN_DIR, OUTPUT);
pinMode(PIN_LED_UV, OUTPUT);
pinMode(PIN_ENDSTOP, INPUT_PULLUP);

// Initialize Z position tracker and define layer and exposure times
_trackZpos = 0;
_nrBaseLayers = 4;
_nrTotalLayers = 40;
_baselLayersMilis = 40000;
_normalLayersMilis = 8000;
_layerSliceHeightMicrons = 50;
_microns_Step = _microns_Rev/(MOTOR_STEPS*MICROSTEPS);

// initiate stepper library
stepper.begin(RPM, MICROSTEPS);
  
Serial.begin(9600);

}

void loop() {

// Lift plate in order to fill the vat with resin (runs once)
doBootInit();
enterFillVatMode();
doPrint(_nrBaseLayers, _nrTotalLayers, _baselLayersMilis, _normalLayersMilis);
enterIdleMode();
  
}

void enterFillVatMode(){
  
  Serial.println("Moving up...");
  
  _steps = _modeFillHeightMicrons/_microns_Step*-1;
  
  stepper.move(_steps);
  doLedToggle();
  Serial.println("Fill Vat Now and press start when ready...");
  
  while(waitForInput()){
    }

  doLedToggle();  
  Serial.println("Moving down...");
  stepper.move(-_steps);
  
  }
  
void enterPauseMode(){
  Serial.println("Pausing...");
  stepper.move(_steps);
  Serial.println("Paused...");
  }

void enterWaitMode(String msg = "Waiting user input..."){
  Serial.println(msg);
  doLedToggle();
  while(waitForInput()){
  delay(20);
  }
}

void enterIdleMode(){
  Serial.println("\nIdleMode...");
  while(waitForInput()){
  doLedToggle();
  delay(1000);
  }
}

void doPrint(int bExposureIterations, int tExposureIterations, long bExposureMilis, long nExposureMilis){

  doPeelMove();
  
  int nExposureIterations = tExposureIterations-bExposureIterations;
  float estimatePrintingTime = bExposureIterations*(bExposureMilis)+nExposureIterations*(nExposureMilis);

  Serial.println("\n Starting print of "+(String)tExposureIterations+" total layers");
  Serial.println("  estimate printing time "+(String)(estimatePrintingTime)+" miliseconds");
  Serial.println("\n Burning "+(String)bExposureIterations+" base layers x "+(String)(bExposureMilis)+" miliseconds");
    
  for (int i = 1; i <= bExposureIterations; i++){
    Serial.println("  Printing layer "+(String)i+" @"+(String)(_trackZpos)+" microns");
    digitalWrite(LED_BUILTIN, HIGH);
    digitalWrite(PIN_LED_UV, HIGH);
    delay(bExposureMilis);
    digitalWrite(LED_BUILTIN, LOW);
    digitalWrite(PIN_LED_UV, LOW);
    Serial.println("  Cooling down...");
    delay(bExposureMilis/2);
    doPeelMove();
    enterWaitMode(" Change layer and press go...");
    }
  
  Serial.println("\n Burning "+(String)(nExposureIterations)+" normal layers x "+(String)(nExposureMilis)+" miliseconds");
    
  for (int i = 1; i <= nExposureIterations; i++){
    Serial.println("  Printing layer "+(String)i+" @"+(String)(_trackZpos)+" microns");
    digitalWrite(LED_BUILTIN, HIGH);
    digitalWrite(PIN_LED_UV, HIGH); 
    delay(nExposureMilis);
    digitalWrite(LED_BUILTIN, LOW);
    digitalWrite(PIN_LED_UV, LOW);
    Serial.println("  Cooling down...");
    delay(bExposureMilis/2);
    doPeelMove();
    enterWaitMode(" Change layer and press go...");
    }
  }
  
void doPeelMove(){
  
  Serial.println("   Peeling...");  
  
  _trackZpos = _trackZpos + _layerSliceHeightMicrons;
  
  _steps = _modePeelHeightMicrons/_microns_Step*-1;
  Serial.println(_steps);
  stepper.move(_steps);


  
  _steps = (_modePeelHeightMicrons - _layerSliceHeightMicrons)/_microns_Step;
  Serial.println(_steps);
  stepper.move(_steps);
  


  
  }

void doUvToggle(int layerMilis){

  }

void doBootInit(){
  digitalWrite(LED_BUILTIN, LOW);
  doLedToggle(); delay(500); 
  doLedToggle(); delay(200); 
  doLedToggle(); delay(500); 
  doLedToggle(); delay(200);
  doLedToggle(); delay(500);
  doLedToggle(); delay(800);
  doLedToggle();
  Serial.println("Boot OK - Waiting for input...");
  while(waitForInput()){
  }

  digitalWrite(LED_BUILTIN, LOW);
}

void doLedToggle(){
  digitalWrite(LED_BUILTIN, !digitalRead(LED_BUILTIN));
  }
  
boolean waitForInput(){
  delay(20);
  if ( digitalRead(PIN_ENDSTOP) == HIGH){ 
    return true; 
    }
  return false;
  }
