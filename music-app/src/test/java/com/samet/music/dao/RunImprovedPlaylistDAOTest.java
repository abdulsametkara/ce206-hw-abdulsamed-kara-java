package com.samet.music.dao;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Runner class for ImprovedPlaylistDAOTest
 */
public class RunImprovedPlaylistDAOTest {
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(ImprovedPlaylistDAOTest.class);
        
        if (result.wasSuccessful()) {
            System.out.println("All tests passed successfully!");
            System.out.println("Total tests run: " + result.getRunCount());
            System.out.println("Time: " + result.getRunTime() + "ms");
        } else {
            System.out.println("Tests failed!");
            System.out.println("Total tests run: " + result.getRunCount());
            System.out.println("Failed tests: " + result.getFailureCount());
            
            for (Failure failure : result.getFailures()) {
                System.out.println(failure.toString());
                System.out.println(failure.getTrace());
            }
        }
    }
} 