# JPyIPC | Java-Python IPC

Implements communication of Java and Python processes via TCP

Protocol described in [protocol.md](protocol.md) file.

In order to handle requests and send responses, you need to implement the server-side logic in Python.
An example implementation can be found in the `python-server` directory.

## BasicClient

Currently, the project offers a single-thread implementation.
However, it has the potential to leverage parallel computations
if the server allows for it, enabling the creation of a multi-thread implementation
without any alterations to the protocol.

### Construction and Connection
By default, connects to `127.0.0.1:25565`, but on construction can be provided with different endpoint

```java
var client = new BasicClient();
// or
var client = new BasicClient("127.0.0.1", 26676);
```

In order to make any requests, it is necessary to establish a connection by calling the `client.connect()` method.\
Once this is done, the socket will be opened and the handshake will be executed.

### MESSAGEs

`BasicClient` provides 2 blocking methods: `sendTextMessage` and `sendExpressionMessage`.

 * `sendTextMessage` blocks execution until Python responds
 * `sendExpressionMessage` also blocks execution, but also returns `ExpressionResponse` which contains 
   status code and text.
   * If status code is 0, text contains computation result
   * Otherwise, text contains error message

```java
// Send text:
client.sendTextMessage('text!');

// Send expression and get the response
var response = client.sendExpressionMessage('some expression');
System.out.println(response.getStatus());
System.out.println(response.getText());
```

### Disconnections

```java
client.close();
```

This call sends shutdown request and closes the socket.


## Exception handling
The listed methods have the potential to throw `IOException` and, sometimes, `RuntimeException`.
More specific information can be found in the methods' Javadoc.


## Tests
You can run all tests using `./gradlew :test`, but...

### TestWithJavaServer
These tests use Java server implementation and do not require anything else.

You can run them using `./gradlew :test --tests TestWithJavaServer`

### TestWithPythonServer
These tests require python server to already be working. **_It won't pass if server is down_**

 * Run the server: `python -m python-server` (in background or separate terminal)
 * Run the test: `./gradlew :test --tests TestWithPythonServer`

These tests are designed to expect specific behavior from the server.
Therefore, if you are implementing your own server,
you may need to modify the tests to align with your implementation.
This could involve adapting the test cases to suit the expected behavior of your server.


## Build the library

Run: 
 * `./gradlew :jar` or
 * `./gradlew :build` if you want to run tests
