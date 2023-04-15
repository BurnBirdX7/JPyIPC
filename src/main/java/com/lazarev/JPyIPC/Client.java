package com.lazarev.JPyIPC;

import java.io.IOException;
import java.util.function.Consumer;

public interface Client {

    class Response {
        public String text;
        public int status;

        public boolean ok() {
            return status == 0;
        }
    }

    void connect() throws IOException;

    void sendTextMessage(String text, Runnable callback);

    void sendExpressionMessage(String expression, Consumer<Response> callback);

}
