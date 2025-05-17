package com.samet.music;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * A simple test class that always passes its test.
 */
public class SimplePassingTest {

    /**
     * This test will always pass.
     */
    @Test
    public void testAlwaysPass() {
        // This assertion will always pass
        assertTrue("This test should always pass", true);
    }
} 