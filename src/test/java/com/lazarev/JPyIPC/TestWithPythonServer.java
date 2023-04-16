package com.lazarev.JPyIPC;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * These are the same tests, but they need an external server
 * You can use `python-server`, run `python -m python-server` in repo's root directory
 */
public class TestWithPythonServer {

    @Test
    void textTest() {
        var client = new BasicClient();
        assertDoesNotThrow(client::connect);
        assertDoesNotThrow(() -> client.sendTextMessage("Hello"));
        assertDoesNotThrow(() -> client.sendTextMessage("Hello again!"));
        assertDoesNotThrow(() -> client.sendTextMessage("die"));
    }

    @Test
    public void testExpression() {
        var client = new BasicClient();

        final String DEFAULT_RESPONSE = "garbage";

        assertDoesNotThrow(client::connect);
        assertDoesNotThrow(() ->
                assertEquals(DEFAULT_RESPONSE, client.sendExpressionMessage("Hello!!").getText()));
        assertDoesNotThrow(() ->
                assertEquals(DEFAULT_RESPONSE, client.sendExpressionMessage("Hello again!!").getText()));
        assertDoesNotThrow(() ->
                assertEquals(DEFAULT_RESPONSE, client.sendExpressionMessage("die").getText()));
    }

}
