#!/bin/bash

FILE=$1

~/.arduino15/packages/arduino/tools/avr-gcc/4.9.2-atmel3.5.4-arduino2/bin/avr-objdump --no-show-raw-insn -m avr -D $FILE > ${FILE}.asm_avr

