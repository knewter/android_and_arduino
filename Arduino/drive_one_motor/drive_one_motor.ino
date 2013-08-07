#include <Servo.h>

Servo leftMotor;        // create servo for the left motor
int leftMotorPin = 11;  // Left Motor on pin 11
Servo rightMotor;       // create servo for the right motor
int rightMotorPin = 12; // Right Motor on pin 12
int inByte = 0;         // incoming serial byte
int onMessage = 49;     // expected "on" message.  This is a "1" character.
int offMessage = 48;    // expected "off" message.  This is a "0" character.

void setup()
{
  leftMotor.attach(leftMotorPin);
  rightMotor.attach(rightMotorPin);

  // start serial port at 115200 bps:
  Serial.begin(115200);
  while (!Serial) {
    ; // This is only required if you have a Leonardo
  }
  establishContact(); // Sends a byte until the serial port is
                      // connected and at least a single byte has
                      // been sent across it.
}

void loop()
{
  // if we get a valid byte, read analog ins:
  if (Serial.available() > 0) {
    // get incoming byte:
    inByte = Serial.read();
    if(inByte == onMessage){
      turnOn();
    }
    if(inByte == offMessage){
      turnOff();
    }
  }
}

void establishContact() {
  while (Serial.available() <= 0) {
    Serial.print('A');   // send a capital A
    delay(300);
  }
}

void turnOn() {
  leftMotor.write(100);  // Forward 1/8-ish
  rightMotor.write(100);  // Forward 1/8-ish
}

void turnOff() {
  leftMotor.write(90);
  rightMotor.write(90);
}
