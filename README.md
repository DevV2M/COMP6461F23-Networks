# COMP 6461 - Computer Networks and Protocols

## Lab Assignment

---

### Group Members:

- Vithu Maheswaran - 27052715
- Shafiq Imtiaz - 40159305

---

## Structure

```
<root>
├── data
├── src
│   └── main
│       └── java
│           └── echo
│               ├── cURLClient.java
│               └── httpLibrary.java
│               └── HttpServer.java
└── README.MD
```

## COMMANDS

# A1 - HTTP LIBRARY + CURL CLIENT

### Help

- `httpc help`
- `httpc help get`
- `httpc help post`

### GET

- `httpc get -v -h Content-Type:application/json 'http://httpbin.org/get?course=networking&assignment=1'`
- `httpc get -v http://httpbin.org/status/418`
- `httpc get http://httpbin.org/status/418`
- `httpc get -v http://httpbin.org/status/418 -o ./teapot.txt`

- `httpc get http://localhost:8080/teapot.txt`
- `httpc get http://localhost:8080/`

### POST

- `httpc post -v -h Content-Type:application/json --d '{"Assignment": 1,"Vithu": 1}' http://httpbin.org/post`
- `httpc post -v -h Content-Type:application/json --d '{"Assignment": 1,"Vithu": "Student"}' http://httpbin.org/post`
- `httpc post -v -h Content-Type:application/json --d 'hello' http://httpbin.org/post`

### POST FILE

- `httpc post -v -f ./text.txt 'http://httpbin.org/post' -o postFileTest.txt`

## BONUS TASK

### Redirect GET

- `httpc get -v http://httpbin.org/status/301`
- `httpc get -v http://httpbin.org/status/304`

### Redirect POST

- `httpc post -v --d '{:}' http://httpbin.org/status/301`

## ADDITIONAL TESTING API

### [Postman Collection](https://www.postman.com/postman/workspace/published-postman-templates/folder/631643-9a4c3bce-30f7-a496-c9ec-78afecbf1545?ctx=documentation)

- `httpc get -v http://postman-echo.com/get?foo1=bar1&foo2=bar2`
- `httpc post -v --d '{:}' http://postman-echo.com/post`

---

# A2 - HTTP SERVER

## Start the http server

- `httpfs -p 8080` // default directory is the current directory
- `httpfs -p 8080 -d data` // custom directory

## GET

- `httpc get http://localhost:8080/`
- `httpc get -v http://localhost:8080/teapot` // teapot.txt when -d not specified
- `httpc get -v http://localhost:8080/text` // text.txt when -d not specified
- `httpc get -v -h Accept:text/plain http://localhost:8080/`
- `httpc get -v -h Accept:text/plain http://localhost:8080/teapot`

## POST

- `httpc post -v -f ./pikachu.txt 'http://localhost:8080/postFile.txt'`
- `httpc post -v --d 'Hello Pikachu' 'http://localhost:8080/postFile.txt'`

---

## Using with IDE

In `Intellij`, you can simply open the project and run the application.
or you can create a JAR file and run it from the command line.

## Using JAR

1. Navigate to the project source directory `cd out/artifacts/httpc_jar`
2. Run the application using the JAR file `java -jar httpc.jar`

## Using with Command Line

1. Navigate to the project source directory `cd src/main/java/echo`
2. Compile the source code
3. Create a manifest file named manifest.txt with the following content:
4. Package the compiled code and manifest into a JAR file
5. Run the application using the JAR file

## Requirement

[Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)