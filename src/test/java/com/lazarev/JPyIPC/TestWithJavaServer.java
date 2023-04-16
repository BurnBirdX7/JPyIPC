package com.lazarev.JPyIPC;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


public class TestWithJavaServer {

    private static ServerSocket serverSocket;
    private static Thread serverThread;
    private final static String DEFAULT_RESPONSE = ClientRunnable.DEFAULT_RESPONSE;
    private static void listen() {

        try {
            System.out.println("Started accepting");
            int num = 0;

            while (!serverSocket.isClosed()) {
                var runnable = new ClientRunnable(serverSocket.accept());
                System.out.println("Accepted connection");

                new Thread(runnable, "ClientThread-" + num).start();
                ++num;
            }

        } catch (IOException e) {
            System.out.println("Server down");
            System.out.println(e.getMessage());
        }
    }

    @BeforeAll
    public static void setupServer() throws IOException {
        serverSocket = new ServerSocket(25565);
        serverThread = new Thread(TestWithJavaServer::listen);
        serverThread.start();
    }


    @AfterAll
    public static void tearDownServer() throws IOException, InterruptedException {
        serverSocket.close();
        serverThread.join();
    }


    @Test
    public void testText() {
        var client = new BasicClient();

        assertDoesNotThrow(client::connect);
        assertDoesNotThrow(() -> client.sendTextMessage("Hello!!"));
        assertDoesNotThrow(() -> client.sendTextMessage("Hello again!!"));
        assertDoesNotThrow(() -> client.sendTextMessage("die"));
    }

    @Test
    public void testExpression() {
        var client = new BasicClient();

        assertDoesNotThrow(client::connect);
        assertDoesNotThrow(() ->
                assertEquals(DEFAULT_RESPONSE, client.sendExpressionMessage("Hello!!").getText()));
        assertDoesNotThrow(() ->
                assertEquals(DEFAULT_RESPONSE, client.sendExpressionMessage("Hello again!!").getText()));
        assertDoesNotThrow(() ->
                assertEquals(DEFAULT_RESPONSE, client.sendExpressionMessage("die").getText()));
    }
}


/**
 * Simple class that incapsulate
 */
class ClientRunnable implements Runnable {
    private final Socket socket;

    final static String DEFAULT_RESPONSE = "garbage";

    ClientRunnable(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // Handshake
            var input = socket.getInputStream();
            var res = input.readNBytes(4);
            String stringRes = new String(res, StandardCharsets.US_ASCII);

            System.out.print("Got handshake message: ");
            System.out.println(stringRes);

            var output = socket.getOutputStream();
            output.write("Py2J".getBytes(StandardCharsets.US_ASCII));
            output.flush();

            boolean run = true;
            while (run) {
                var request = BasicClient.RequestPacket.readFromStream(input);
                if (request.text.equals("die")) {
                    run = false;
                }

                System.out.println("Got message:");
                System.out.println("\ttype: " + (char)request.type);
                System.out.println("\tID: " + request.id);
                System.out.println("\ttext: " + request.text);

                var response = new BasicClient.ResponsePacket(request.id, 0, DEFAULT_RESPONSE);
                response.writeToStream(output);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Client worker finished!");
    }
}
