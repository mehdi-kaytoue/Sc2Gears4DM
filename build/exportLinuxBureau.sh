#!/bin/sh

cd ../src
javac -cp ../lib/Sc2gears-plugin-api-4.2.jar:../lib/jdom-2.0.5.jar *.java 
jar cf Sc2Gears4DM.jar *.class
rm *.class

mv Sc2Gears4DM.jar ../build

cd ../build
