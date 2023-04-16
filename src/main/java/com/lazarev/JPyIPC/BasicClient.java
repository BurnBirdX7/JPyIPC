package com.lazarev.JPyIPC;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BasicClient {

    public static final String DEFAULT_IP = "127.0.0.1";
    public static final int DEFAULT_PORT = 25565;
    private final byte[] SERVER_HELLO = "Py2J".getBytes(StandardCharsets.US_ASCII);
    private final byte[] CLIENT_HELLO = "J2Py".getBytes(StandardCharsets.US_ASCII);

    private final String ip;
    private final int port;
    private final Socket socket = new Socket();
    private int lastId = 0;

    public BasicClient() {
        this(DEFAULT_IP, DEFAULT_PORT);
    }

    public BasicClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * connect to the socket
     * @throws IOException if error occurred during connection
     */
    public void connect() throws IOException {
        socket.connect(new InetSocketAddress(this.ip, this.port));

        var output = socket.getOutputStream();
        output.write(CLIENT_HELLO);
        output.flush();

        var input = socket.getInputStream();
        var res = input.readNBytes(4);

        if (!Arrays.equals(SERVER_HELLO, res)) {
            socket.close();
            throw new IOException("Server sent wrong hello");
        }
    }

    /**
     * Send MESSAGE of type 'Text' to Python process
     * Client must be connected.
     *
     * @param text - text to send to Python process
     * @throws IOException on socket errors
     * @throws RuntimeException if response cannot match the request
     */
    public void sendTextMessage(String text) throws IOException {
        var requestPacket = new RequestPacket('T', lastId++, text);
        requestPacket.writeToStream(socket.getOutputStream());
        var responsePacket = ResponsePacket.readFromStream(socket.getInputStream());

        if (requestPacket.id != responsePacket.id) {
            throw new RuntimeException(
                    "Unexpected error: Got response with wrong ID: (req)%d != (res)%d"
                            .formatted(requestPacket.id, responsePacket.id));
        }

        if (!responsePacket.ok()) {
            throw new RuntimeException(
                    "Unexpected error: server responded to the text message, but with error status: %d"
                            .formatted(responsePacket.status));
        }
    }

    /**
     * Sends MESSAGE of type 'Expression' to Python process
     * Client must be connected
     *
     * @param expression - expression to send to Python to compute
     * @return object that contains results of the computation
     * @throws IOException on socket error
     * @throws RuntimeException on id mismatch in request and response
     */
    public ExpressionResponse sendExpressionMessage(String expression) throws IOException {
        var requestPacket = new RequestPacket('E', lastId++, expression);
        requestPacket.writeToStream(socket.getOutputStream());
        var responsePacket = ResponsePacket.readFromStream(socket.getInputStream());
        if (responsePacket.id != requestPacket.id) {
            throw new RuntimeException(
                    "Unexpected error: Got response with wrong ID: (req)%d != (res)%d"
                            .formatted(requestPacket.id, responsePacket.id));
        }

        return responsePacket;
    }

    /**
     * Sends shutdown request and closes the socket
     * @throws IOException thrown if error occurred during request or socket couldn't be close
     */
    public void close() throws IOException {
        var killRequest = new RequestPacket('K', lastId, "");
        killRequest.writeToStream(socket.getOutputStream());
        var killResponse = ResponsePacket.readFromStream(socket.getInputStream());
        socket.close();

        if (!killResponse.ok()) {
            throw new RuntimeException(
                    "Strange response to a kill request, status code: %d, text: \"%s\""
                            .formatted(killResponse.status, killResponse.text)
            );
        }
    }

    /**
     * class RequestPacket
     * represents J2Py - Request Packet
     * <p>
     * Can be created with constructor or read from stream
     * <p>
     * Possibility of reading from stream was added to make testing easier
     */
    static class RequestPacket {
        byte type;
        int id;
        String text;

        private RequestPacket() {}

        RequestPacket(char type, int id, String text) {
            this.type = (byte)type;
            this.id = id;
            this.text = text;
        }


        void writeToStream(OutputStream stream) throws IOException {
            var dataStream = new DataOutputStream(stream);
            var bytes = text.getBytes(StandardCharsets.UTF_8);

            dataStream.writeByte(type);
            dataStream.writeInt(id);
            dataStream.writeInt(bytes.length);
            dataStream.write(bytes);

            dataStream.flush();
        }

        static RequestPacket readFromStream(InputStream stream) throws IOException {
            var dataStream = new DataInputStream(stream);
            var packet = new RequestPacket();

            packet.type = dataStream.readByte();
            packet.id = dataStream.readInt();
            int length = dataStream.readInt();
            byte[] encoded = dataStream.readNBytes(length);

            packet.text = new String(encoded, StandardCharsets.UTF_8);
            return packet;
        }
    }


    /**
     * class ResponsePacket
     * represents Py2J - Response Packet
     * <p>
     * Can be created with constructor or read from stream
     * <p>
     * Possibility of creating a packet with constructor and writing it to a stream
     * was added to make testing easier
     */
    static class ResponsePacket implements ExpressionResponse {
        int id;
        int status;
        String text;

        private ResponsePacket() {}

        ResponsePacket(int id, int status, String text) {
            this.id = id;
            this.status = status;
            this.text = text;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public int getStatus() {
            return status;
        }

        boolean ok() {
            return status == 0;
        }

        void writeToStream(OutputStream stream) throws IOException {
            var dataStream = new DataOutputStream(stream);
            dataStream.writeInt(id);
            dataStream.writeByte(status);

            var bytes = text.getBytes(StandardCharsets.UTF_8);
            dataStream.writeInt(bytes.length);
            dataStream.write(bytes);
            dataStream.flush();
        }

        static ResponsePacket readFromStream(InputStream stream) throws IOException {
            var dataStream = new DataInputStream(stream);
            var packet = new ResponsePacket();
            packet.id = dataStream.readInt();
            packet.status = dataStream.readByte();

            int length = dataStream.readInt();
            byte[] encoded = dataStream.readNBytes(length);
            packet.text = new String(encoded, StandardCharsets.UTF_8);
            return packet;
        }
    }

}
