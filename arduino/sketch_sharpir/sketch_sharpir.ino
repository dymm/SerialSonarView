#include <SharpDistSensor.h>

#include <Stepper.h>
#include <SoftwareSerial.h>

/* Constante pour reception ordre */
const byte ORDER_PIN_ON = (byte)'1';
const byte ORDER_PIN_OFF = (byte)'2';
const byte MEASURE_ON_OFF = (byte)'3';

/* Constantes pour les broches */
const byte IR_ANALOG_INPUT_PIN = A0; // Broche TRIGGER entrée analogique pour capteur IR 4 - 30cm

// Window size of the median filter (odd number, 1 = no filtering)
const byte medianFilterWindowSize = 5;

// Create an object instance of the SharpDistSensor class
SharpDistSensor sensor(IR_ANALOG_INPUT_PIN, medianFilterWindowSize);

const byte MOTOR_IN1_PIN = 2;    //Broche controle moteur
const byte MOTOR_IN2_PIN = 3;    //Broche controle moteur
const byte MOTOR_IN3_PIN = 4;    //Broche controle moteur
const byte MOTOR_IN4_PIN = 5;    //Broche controle moteur
const int FULL_CIRCLE_IN_STEPS = 2048;

Stepper stepper(FULL_CIRCLE_IN_STEPS, MOTOR_IN2_PIN, MOTOR_IN4_PIN, MOTOR_IN3_PIN, MOTOR_IN1_PIN);

void sendData(int pos, unsigned int distance);
void readData();
byte twoCharToOneByte(char data[]);



byte measure_is_allowed = 0;
SoftwareSerial mavoieserie(11, 10);

void setup() {
  stepper.setSpeed(15); //Vitesse de 300 (max) réduire ce chiffre pour un mouvement plus lent
  //100 permet d'éavoir un couple élevé >300 le moteur vibre sans tourner
  
  /* Initialise le port série */
  Serial.begin(115200);
  mavoieserie.begin(9600);

}

void loop() {

  //readDataFromSoftSerial();
  readDataFromSerial();
  
  //Pour increments :
  // 2deg : delay 150 mini
  // 10deg : delay 200 mini
  if(measure_is_allowed == 0) {
    delay(150);
    return;
  }

  for(int i=0;i<1;i++) {
  int position = 0;
  int stepDiff = 10;
  for (position = 0; position <= 1040; position += stepDiff) { // goes from 0 degrees to 180 degrees
    stepper.step(stepDiff);
    uint16_t dictance_in_mm = sensor.getDist();
    sendData(position, dictance_in_mm);
  }
  stepDiff = -10;
  for (position = 1040; position >= 0; position += stepDiff) { // goes from 180 degrees to 0 degrees 
    stepper.step(stepDiff);
    uint16_t dictance_in_mm = sensor.getDist();
    sendData(position, dictance_in_mm);
  }
  }

  sendEndOfSweep();
}

void readDataFromSerial() {
  if (Serial.available()>0) {
    
    byte order = (byte) Serial.peek();
    
    if( (order == ORDER_PIN_ON || order == ORDER_PIN_OFF) && Serial.available()>=3) {
      char data[2];
      Serial.read();
      data[0] = Serial.read();
      data[1] = Serial.read();
      byte pin = twoCharToOneByte(data);
      if(order == ORDER_PIN_ON) digitalWrite(pin, HIGH);
      if(order == ORDER_PIN_OFF) digitalWrite(pin, LOW);
    }
    else if( order == MEASURE_ON_OFF && Serial.available()>=1) {
      Serial.read();
      char val = Serial.read();
      if(val >= '0' && val <= '9') {
        measure_is_allowed = val - '0';
      }
    }

  }
}

void readDataFromSoftSerial() {
  if (mavoieserie.available()>0) {
    byte order = (byte) mavoieserie.peek();
    
    if( (order == ORDER_PIN_ON || order == ORDER_PIN_OFF) && mavoieserie.available()>=3) {
      char data[2];
      mavoieserie.read();
      data[0] = mavoieserie.read();
      data[1] = mavoieserie.read();
      byte pin = twoCharToOneByte(data);
      if(order == ORDER_PIN_ON) digitalWrite(pin, HIGH);
      if(order == ORDER_PIN_OFF) digitalWrite(pin, LOW);
    }
    else if( order == MEASURE_ON_OFF && mavoieserie.available()>=1) {
      mavoieserie.read();
      char val = mavoieserie.read();
      if(val >= '0' && val <= '9') {
        measure_is_allowed = val - '0';
      }
    }

  }
}

byte twoCharToOneByte(char data[]) {
  byte nh = data[0] - '0';
  byte nb = data[1] - '0';
  byte result = (nh<<4) | nb;
  return result;
}

void sendData(int pos, uint16_t distance) {

    Serial.print(pos);
    Serial.print(F(";"));
    Serial.print(distance);
    Serial.print(F("\n"));
  #if 0
  mavoieserie.print(pos);
  mavoieserie.print(F(";"));
  mavoieserie.print(distance);
  mavoieserie.print(F("\n"));
  #endif
}
void sendEndOfSweep() {
  Serial.print(F("EOS\n"));
}


