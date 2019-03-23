


#define MAXBUFFER 100
char buffer[MAXBUFFER];
byte bufferpos = 0;


#include <SoftwareSerial.h>
SoftwareSerial BTserial(2, 3); // RX | TX

#define CommDev Serial


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

int _trackZpos = 0;

int _nrBaseLayers;
int _nrTotalLayers;
long _baselLayersMilis;
long _normalLayersMilis;
long _layerSliceHeightMicrons;
long _microns_Step;

const long _modePauseHeightMicrons = 60000;
const long _modeFillHeightMicrons = 60000;
const long _modePeelHeightMicrons = 7000;


void doLedToggle(int delayTime){
  digitalWrite(LED_BUILTIN, !digitalRead(LED_BUILTIN));
  delay(delayTime);
}

void doViniciusCode() {
  digitalWrite(LED_BUILTIN, LOW);
  doLedToggle(500);
  doLedToggle(200);
  doLedToggle(500);
  doLedToggle(200);
  doLedToggle(500);
  doLedToggle(800);
  doLedToggle(0);
}

void enterFillVatMode(){

  CommDev.println("Moving up...");

  _steps = _modeFillHeightMicrons/_microns_Step*-1;

  stepper.move(_steps);
  doLedToggle(0);
  CommDev.println("Fill Vat Now and press start when ready...");

  while(waitForInput()){
    }

  doLedToggle(0);
  CommDev.println("Moving down...");
  stepper.move(-_steps);

  }

void enterPauseMode(){
  CommDev.println("Pausing...");
  stepper.move(_steps);
  CommDev.println("Paused...");
  }

void enterWaitMode(String msg = "Waiting user input..."){
  CommDev.println(msg);
  doLedToggle(0);
  while(waitForInput()){
  delay(20);
  }
}

void enterIdleMode(){
  CommDev.println("\nIdleMode...");
  while(waitForInput()){
  doLedToggle(0);
  delay(1000);
  }
}

void _Wait(long _millis) {
  CommDev.print("MSG:WAITING ");
  CommDev.print(_millis);
  CommDev.println(" millis");
  delay(_millis);
}

void doPrintLayer(int layer) {
    //Serial.println("  Printing layer "+(String)i+" @"+(String)(_trackZpos)+" microns");
    
    digitalWrite(LED_BUILTIN, HIGH);
    
    CommDev.println("MSG:UVLED ON");
    digitalWrite(PIN_LED_UV, HIGH);
    long waittime = 0;
    if (layer <= _nrBaseLayers) {
        waittime=_baselLayersMilis;
    } else {
        waittime=_normalLayersMilis;
    }
    _Wait(waittime);
    digitalWrite(LED_BUILTIN, LOW);
    CommDev.println("MSG:UVLED OFF");
    digitalWrite(PIN_LED_UV, LOW);
    CommDev.println("MSG:Cooling down");
    _Wait(waittime/2);
    CommDev.println("MSG:Peeling");
    doPeelMove();
    //enterWaitMode(" Change layer and press go...");
}

void doMoveZAxis(long centimilli) {
    long _steps = centimilli/_microns_Step;
    stepper.move(_steps);
}

void doPrint(int bExposureIterations, int tExposureIterations){

//  doPeelMove();

  int nExposureIterations = tExposureIterations-bExposureIterations;
  float estimatePrintingTime = bExposureIterations*(_baselLayersMilis)+nExposureIterations*(_normalLayersMilis);

  CommDev.println("\n Starting print of "+(String)tExposureIterations+" total layers");
  CommDev.println("  estimate printing time "+(String)(estimatePrintingTime)+" miliseconds");
  CommDev.println("\n Burning "+(String)bExposureIterations+" base layers x "+(String)(_baselLayersMilis)+" miliseconds");

    for(int i=1; i<=tExposureIterations;i++) {
        doPrintLayer(i);
    }
  }

void doPeelMove(){

//  Serial.println("   Peeling...");

  _trackZpos = _trackZpos + _layerSliceHeightMicrons;

  _steps = _modePeelHeightMicrons/_microns_Step;
  stepper.move(-_steps);

  _steps = (_modePeelHeightMicrons - _layerSliceHeightMicrons)/_microns_Step;
  stepper.move(_steps);
  }

void doUvToggle(int layerMilis){

  }

void doBootInit(){
  
  doViniciusCode();
  CommDev.println("Boot OK - Waiting for input...");
  while(waitForInput()){
  }

  digitalWrite(LED_BUILTIN, LOW);
}

boolean waitForInput(){
  delay(20);
  if ( digitalRead(PIN_ENDSTOP) == HIGH){
    return true;
    }
  return false;
  }




void GoHomeCmd() {
  CommDev.print("MSG:EXECUTING GOHOME - MOVING ");
  long _steps = _trackZpos/_microns_Step;
  CommDev.println(-_steps);
  
  stepper.move(-_steps);
  _trackZpos = 0;
}

void SetHomeCmd() {
  CommDev.println("MSG:EXECUTING SETHOME - TRACKZPOS=0");
  _trackZpos = 0;
}

void printLayer(int layer) {
  CommDev.print("MSG:EXECUTING PRINTLAYER - ");
  CommDev.println(layer);
  doPrintLayer(layer);
}

void moveZAxis(long centmm) {
  CommDev.print("MSG:EXECUTING MOVEZAXIS - ");
//  CommDev.println(centmm);
  _trackZpos += centmm;
  long _steps = centmm/_microns_Step;
  CommDev.println(_steps);
  stepper.move(_steps);
}

void PrintLayerCmd(const char *layer) {
  char *endptr = NULL;
  int layerNum = strtol(layer, &endptr, 0);
  if (layer == endptr) { // Invalid number
    CommDev.println("MSG:INVALID LAYER");
  } else {
    printLayer(layerNum);
  }
}

void  MoveZAxisCmd(const char *centmm) {
  char *endptr = NULL;
  long centmmNum = strtol(centmm, &endptr, 0);
  if (centmm == endptr) { // Invalid number
    CommDev.println("MSG:INVALID VALUE");
    CommDev.println("OK");
  } else {
    moveZAxis(centmmNum);
  }
}

void processBuffer(const char *command) {

  if (strncmp(command, "HOME", 4) == 0) {
    GoHomeCmd();
  } else if (strncmp(command, "LAYR", 4) == 0) {
    // Get Layer Number
    PrintLayerCmd(command+4);
  } else if (strncmp(command, "SETH", 4) == 0) {
    SetHomeCmd();
  } else if (strncmp(command, "MOVE", 4) == 0) {
    MoveZAxisCmd(command+4);
  } else {
    CommDev.print("INVALID COMMAND - ");
    CommDev.println(command);
  }
}


void setup() {
  // put your setup code here, to run once:
  CommDev.begin(9600);

// Declare pins as Outputs
pinMode(LED_BUILTIN, OUTPUT);
pinMode(PIN_STEP, OUTPUT);
pinMode(PIN_DIR, OUTPUT);
pinMode(PIN_LED_UV, OUTPUT);
pinMode(PIN_ENDSTOP, INPUT_PULLUP);

// Initialize Z position tracker and define layer and exposure times

_nrBaseLayers = 4;
_nrTotalLayers = 40;
_baselLayersMilis = 40000;
_normalLayersMilis = 8000;
_layerSliceHeightMicrons = 50;
_microns_Step = _microns_Rev/(MOTOR_STEPS*MICROSTEPS);

// initiate stepper library
stepper.begin(RPM, MICROSTEPS);

}

void loop() {
  // put your main code here, to run repeatedly:
  if (CommDev.available()) {
    int input = CommDev.read();
    if (input == '\n') {
      buffer[bufferpos] = 0;
      processBuffer(buffer);
      bufferpos = 0;
      CommDev.println("OK");      
    } else if ((input >31) && (input < 126)) {
        if (bufferpos < MAXBUFFER-1) {
          buffer[bufferpos++] = input;
        } else {
          buffer[bufferpos] = 0;
          processBuffer(buffer);
          bufferpos = 0;
          CommDev.println("OK");
        }
    }

  }
}
