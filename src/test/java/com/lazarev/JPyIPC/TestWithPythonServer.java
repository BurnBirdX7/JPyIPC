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
        assertDoesNotThrow(client::close);
    }

    @Test
    public void testExpression() {
        var client = new BasicClient();

        String expr1 = "Expression!!";
        String expr2 = "Expression again!!";
        String expr3 = "Expression 3";

        assertDoesNotThrow(client::connect);
        assertDoesNotThrow(() ->
                assertEquals(expr1+expr1, client.sendExpressionMessage(expr1).getText()));
        assertDoesNotThrow(() ->
                assertEquals(expr2+expr2, client.sendExpressionMessage(expr2).getText()));
        assertDoesNotThrow(() ->
                assertEquals(expr3+expr3, client.sendExpressionMessage(expr3).getText()));
        assertDoesNotThrow(client::close);
    }

}
