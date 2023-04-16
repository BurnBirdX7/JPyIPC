# JPyIPC | Java-Python IPC

Implements communication of Java and Python processes via TCP

Protocol described in [protocol.md](protocol.md) file.

To get requests and respond, python process must implement server side,
example implementation provided in `python-server` directory.

## BasicClient

At the moment project provides single thread implementation, but in general,
if server allows for parallel computations, multi-thread implementation can
be created with no changes to the protocol 

### Construction and Connection
By default, connects to `127.0.0.1:25565`, but on construction can be provided with different endpoint

```java
var client = new BasicClient();
// or
var client = new BasicClient("127.0.0.1", 26676);
```

Before any requests are made, connection must be established

```java
client.connect();
```

### MESSAGEs

`BasicClient` provides 2 blocking methods: `sendTextMessage` and `sendExpressionMessage`.

 * `sendTextMessage` blocks execution until Python responds
 * `sendExpressionMessage` also blocks execution, but also returns `ExpressionResponse` which contains 
   status code and text.
   * If status code is 0, text contains computation result
   * Otherwise, text contains error message

### Disconnections

```java
client.close();
```

This call sends shutdown request and closes the socket.


## Tests
You can run all tests using `./gradlew :test`, but...

### TestWithJavaServer
These tests use Java server implementation and do not require anything else.

You can run them using `./gradlew :test --tests TestWithJavaServer`

### TestWithPythonServer
These tests require python server to already be working. **_It won't pass if server is down_**

 * Run the server: `python -m python-server` (in background or separate terminal)
 * Run the test: `./gradlew :test --tests TestWithPythonServer`

These tests expect certain behaviour from the server, so if you are implementing your server
you need to edit the test to suit your implementation.


## Build the library

`./gradlew :jar`
 * or `./gradlew :build` if you want to run the tests
