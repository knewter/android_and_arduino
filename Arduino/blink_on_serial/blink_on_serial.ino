int led = 13;          // Internal LED / pin 13
int inByte = 0;        // incoming serial byte
int onMessage = 49;    // expected "on" message.  This is a "1" character.
int offMessage = 48;   // expected "off" message.  This is a "0" character.

void setup()
{
  // initialize the digital pin as an output.
  pinMode(led, OUTPUT); 

  // start serial port at 9600 bps:
  Serial.begin(9600);
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
  digitalWrite(led, HIGH);  
}

void turnOff() {
  digitalWrite(led, LOW);
}
