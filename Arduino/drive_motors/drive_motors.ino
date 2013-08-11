#include <Servo.h>

Servo leftMotor;          // create servo for the left motor
int leftMotorPin = 11;    // Left Motor on pin 11
Servo rightMotor;         // create servo for the right motor
int rightMotorPin = 12;   // Right Motor on pin 12
int inByte = 0;           // incoming serial byte
int onMessage = '1';      // expected "on" message.
int offMessage = '0';     // expected "off" message.
int leftMotorValue = 90;  // start off at 0%
int rightMotorValue = 90; // start off at 0%

void setup() {
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

void loop() {
  // if we get a valid byte, read analog ins:
  if (Serial.available() > 0) {
    // get incoming byte:
    inByte = Serial.read();
    handleCommand(inByte);
  }
//  drive();
}

void drive() {
  debug();
}

void debug() {
  Serial.print(leftMotorValue);
  Serial.println(rightMotorValue);
}

void handleCommand(char inByte) {
  debug();
  if(inByte <= 'u' && inByte >= 'a'){
    Serial.println("aaa");
    updateLeftMotor(inByte);
  }
  if(inByte <= 'U' && inByte >= 'A'){
    Serial.println("bbb");
    updateRightMotor(inByte);
  }
}

void updateLeftMotor(char value){
  leftMotorValue = leftMotorValueMapped(value);
}

void updateRightMotor(char value){
  rightMotorValue = rightMotorValueMapped(value);
}

void establishContact() {
  while (Serial.available() <= 0) {
    Serial.print(rightMotorValueMapped('B')); // send a capital A
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

// Protocol for motor speeds:
int leftMotorValueMapped(char encoded){
  switch(encoded){
    case 'a':
      return 0;   // -100%
    case 'b':
      return 9;   // -90%
    case 'c':
      return 18;  // -80%
    case 'd':
      return 27;  // -70%
    case 'e':
      return 36;  // -60%
    case 'f':
      return 45;  // -50%
    case 'g':
      return 54;  // -40%
    case 'h':
      return 63;  // -30%
    case 'i':
      return 72;  // -20%
    case 'j':
      return 81;  // -10%
    case 'k':
      return 90;  // 0%
    case 'l':
      return 99;  // 10%
    case 'm':
      return 108; // 20%
    case 'n':
      return 117; // 30%
    case 'o':
      return 126; // 40%
    case 'p':
      return 135; // 50%
    case 'q':
      return 144; // 60%
    case 'r':
      return 153; // 70%
    case 's':
      return 162; // 80%
    case 't':
      return 171; // 90%
    case 'u':
      return 180; // 100%
  }
}

int rightMotorValueMapped(char encoded){
  switch(encoded){
    case 'A':
      return 0;   // -100%
    case 'B':
      return 9;   // -90%
    case 'C':
      return 18;  // -80%
    case 'D':
      return 27;  // -70%
    case 'E':
      return 36;  // -60%
    case 'F':
      return 45;  // -50%
    case 'G':
      return 54;  // -40%
    case 'H':
      return 63;  // -30%
    case 'I':
      return 72;  // -20%
    case 'J':
      return 81;  // -10%
    case 'K':
      return 90;  // 0%
    case 'L':
      return 99;  // 10%
    case 'M':
      return 108; // 20%
    case 'N':
      return 117; // 30%
    case 'O':
      return 126; // 40%
    case 'P':
      return 135; // 50%
    case 'Q':
      return 144; // 60%
    case 'R':
      return 153; // 70%
    case 'S':
      return 162; // 80%
    case 'T':
      return 171; // 90%
    case 'U':
      return 180; // 100%
  }
}
