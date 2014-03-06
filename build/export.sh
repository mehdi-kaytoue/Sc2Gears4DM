#!/bin/sh

cd ../src
javac -cp ../lib/Sc2gears-plugin-api-4.2.jar:../lib/jdom-2.0.5.jar *.java 
jar cf PluginXML.jar *.class
rm *.class

mv PluginXML.jar ../build

cd ../build

mkdir -p /home/guillaume/PartageLinux/PluginXML

rm -f /home/guillaume/PartageLinux/PluginXML/Sc2gears-plugin.xml
rm -f /home/guillaume/PartageLinux/PluginXML/PluginXML.jar

cp Sc2gears-plugin.xml /home/guillaume/PartageLinux/PluginXML
cp PluginXML.jar /home/guillaume/PartageLinux/PluginXML



