package com.samet.music.dao;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Runner class for PlaylistDAOTest
 */
public class RunPlaylistDAOTest {
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(PlaylistDAOTest.class);
        
        if (result.wasSuccessful()) {
            System.out.println("All tests passed successfully!");
            System.out.println("Total tests run: " + result.getRunCount());
        } else {
            System.out.println("Tests failed!");
            System.out.println("Total tests run: " + result.getRunCount());
            System.out.println("Failed tests: " + result.getFailureCount());
            
            for (Failure failure : result.getFailures()) {
                System.out.println(failure.toString());
            }
        }
    }
} 