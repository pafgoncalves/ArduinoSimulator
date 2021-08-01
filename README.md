# ArduinoSimulator

An Arduino simulator specifically designed for the teaching area.

It consists of three projects:
- [**ArduinoSimulator**](https://github.com/pafgoncalves/ArduinoSimulator/tree/master/ArduinoSimulator): where all the microcontroller features are implemented, namely the AVR Instruction Set, the microcontroller peripherals, FLASH and SRAM. It is this module that executes the microcontroller code, exposes methods to change the value of the pins and launches events when their state is changed internally.
- [**ArduinoSimulatorWeb**](https://github.com/pafgoncalves/ArduinoSimulator/tree/master/ArduinoSimulatorWeb): this is the web application that manages users, projects and presents the user interface.
- [**ArduinoSimulatorProgrammer**](https://github.com/pafgoncalves/ArduinoSimulator/tree/master/ArduinoSimulatorProgrammer): this module is a software that is installed in the Arduino IDE and that replaces the program that performs the programming of the device (avrdude).


[installation manual](https://github.com/pafgoncalves/ArduinoSimulator/blob/master/instalation_manual.pdf)

[user manual](https://github.com/pafgoncalves/ArduinoSimulator/blob/master/user_manual.pdf)
