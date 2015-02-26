# EchoProxy

A simple pass-through http proxy that echoes the request uri for all requests going through it.
In addition to echoing to the console it can be configured to echo to a file.

## Use-case example

The proxy was developed to get a simple way of capturing dependencies in wsdl files, i.e., "recursively" find
all the resources (schemas) that a wsdl depends on. The pseudo code for analyzing a set of wsdl's is given below.

    start echo proxy (with file logging)
    remove log file (to make sure log is empty)

    for each wsdl do
      run wsimport on wsdl with proxy set to echo proxy
      move log file "wsdl name".log

    stop echo proxy

A log file for each wsdl listing its dependencies will now be available. See scripts/testrun.sh for example.

## Building

    mvn clean package

## Test

    cd scripts
    ./testrun.sh (or testrun.bat)

## Run

    java -jar echoproxy.jar &lt;options&gt;

## Command line options

Run java -jar echoproxy.jar -help (e.g., java -jar target/echoproxy-1.0.jar -help) to get help on command line options.

The proxy does not need any arguments to run. The most important options are:

* port &lt;int&gt; - the port that proxy listens on (default 9901)
* echoFile &lt;path&gt; - path to log file (uri's are appended to this file)
* httpProxy &lt;proxyhost:proxyport&gt; - required if the echo proxy need to forward requests to another proxy

If a log file is used it will be created by the proxy if it does not exist. If it is deleted/moved while the proxy
is running it will be recreated.

To suppress output to console just redirect stdout to /dev/null
