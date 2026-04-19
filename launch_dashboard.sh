#!/bin/bash

export DISPLAY=:0
xset s off
xset -dpms
xset s noblank

sudo chmod 666 /sys/class/backlight/10-0045/brightness

cd "$(dirname "$0")"
java -jar target/dashboard-rpi-1.0-SNAPSHOT-jar-with-dependencies.jar
