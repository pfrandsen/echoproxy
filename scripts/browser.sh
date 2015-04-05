#!/bin/bash

# use echoproxy and the Chrome browser to list urls in a browser session

pushd ..
mvn clean package
clear

port=1337
key=7331
echoLog=target/echo.log
cmd1="java -jar target/echoproxy-1.0.jar -port ${port} -echoFile ${echoLog} -shutdownKey 7331"

echo "Starting echo proxy: ${cmd1}"
`exec x-terminal-emulator 1>&2 -e $cmd1`
echo
sleep 2s # let proxy start (separate shell)

echo
echo "Important! Chrome must be shut down"
echo "ie. _not_ running in background (as is default) and no open windows"
echo "else --proxy-server command line option has no effect"
echo

google-chrome --incognito --proxy-server="localhost:${port}" http://google.com &