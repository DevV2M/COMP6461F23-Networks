# COMP 6461 - Computer Networks and Protocols

## Assignment 1

## Structure

```
<root>
├── src
│   └── main
│       └── java
│           └── echo
│               ├── cURLClient.java
│               └── httpLibrary.java
└── README.MD
```

## cURL Commands

### Help

- `httpc help`
- `httpc help get`
- `httpc help post`

### Get

- `httpc get -v -h Content-Type:application/json 'http://httpbin.org/get?course=networking&assignment=1'`
- `httpc get -v http://httpbin.org/status/418`
- `httpc get http://httpbin.org/status/418`
- `httpc get -v http://httpbin.org/status/418 -o ./teapot.txt`

### Redirect sample

- `httpc get -v http://httpbin.org/status/301`
- `httpc get -v http://httpbin.org/status/304`

### Redirect post

- `httpc post -v --d '{:}' http://httpbin.org/status/301`

### Post in-line data

- `httpc post -v -h Content-Type:application/json --d '{"Assignment": 1,"Vithu": 1}' http://httpbin.org/post`
- `httpc post -v -h Content-Type:application/json --d '{"Assignment": 1,"Vithu": "Student"}' http://httpbin.org/post`
- `httpc post -v -h Content-Type:application/json --d 'hello' http://httpbin.org/post`

### Post file

- `httpc post -v -f ./text.txt 'http://httpbin.org/post' -o postFileTest.txt`

### [Postman Collection](https://www.postman.com/postman/workspace/published-postman-templates/folder/631643-9a4c3bce-30f7-a496-c9ec-78afecbf1545?ctx=documentation)

- `httpc get -v http://postman-echo.com/get?foo1=bar1&foo2=bar2`
- `httpc post -v --d '{:}' http://postman-echo.com/post`

## Compile and package

1. Open the terminal and cd to the `netsample` directory
2. Run `mvn package` to compile and package this example
   You should see two jar files in the `target` directory: one with all dependencies (we use logging and argument parse
   libraries); one without dependencies.

## Run the sample applications

Use the following the commands to run applications.

## Requirement

[Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

## Using with IDE

You can either Intellij, Eclipse, or Netbeans to run, and extend these examples.