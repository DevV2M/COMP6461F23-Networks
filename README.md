# COMP6461F23-Networks Assignment 1
Computer Networks and Protocols

## Requirement
1. [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

## Structure
There is 1 application for the cURL and and hTTPLibrary.

## cURL Commands
Help:

`httpc help`

`httpc help get`

`httpc help post`

Get:

`httpc get -v -h Content-Type:application/json 'http://httpbin.org/get?course=networking&assignment=1'`

`httpc get -v 'http://httpbin.org/status/418'`

`httpc get http://httpbin.org/status/418`

Post in-line data:

`httpc post -v -h Content-Type:application/json --d '{"Assignment": 1,"Vithu": 1}' http://httpbin.org/post`

`httpc post -v -h Content-Type:application/json --d '{"Assignment": 1,"Vithu": "Student"}' http://httpbin.org/post`

`httpc post -v -h Content-Type:application/json --d 'hello' http://httpbin.org/post`

Post file:

`httpc post -v -f ./text.txt 'http://httpbin.org/post'`

## Compile and package
1. Open the terminal and cd to the `netsample` directory
2. Run `mvn package` to compile and package this example
You should see two jar files in the `target` directory: one with all dependencies (we use logging and argument parse libraries); one without dependencies.

## Run the sample applications
Use the following the commands to run applications.

## Using with IDE
You can either Intellij, Eclipse, or Netbeans to run, and extend these examples.
