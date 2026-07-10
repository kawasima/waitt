package net.unit8.waitt.feature.admin;

import org.junit.Test;

import static org.junit.Assert.*;

public class ResponseUtilsTest {

    @Test
    public void acceptsLoopbackOrigins() {
        assertTrue(ResponseUtils.isLocalOrigin("http://localhost"));
        assertTrue(ResponseUtils.isLocalOrigin("http://localhost:8080"));
        assertTrue(ResponseUtils.isLocalOrigin("http://127.0.0.1:1192"));
        assertTrue(ResponseUtils.isLocalOrigin("http://[::1]:3000"));
        assertTrue(ResponseUtils.isLocalOrigin("HTTP://LocalHost:8080")); // case-insensitive host
    }

    @Test
    public void rejectsLookalikeAndForeignOrigins() {
        // The prefix-match bug: a host that merely starts with "localhost".
        assertFalse(ResponseUtils.isLocalOrigin("http://localhost.attacker.example"));
        assertFalse(ResponseUtils.isLocalOrigin("http://evil.example"));
        assertFalse(ResponseUtils.isLocalOrigin("https://notlocalhost"));
        assertFalse(ResponseUtils.isLocalOrigin(null));
        assertFalse(ResponseUtils.isLocalOrigin("garbage"));
    }
}
