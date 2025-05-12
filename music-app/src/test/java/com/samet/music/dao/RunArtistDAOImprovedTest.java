package com.samet.music.dao;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Runner class for ArtistDAOImprovedTest
 */
public class RunArtistDAOImprovedTest {
    public static void main(String[] args) {
        System.out.println("Running ArtistDAOImprovedTest...");
        
        Result result = JUnitCore.runClasses(ArtistDAOImprovedTest.class);
        
        System.out.println("\nTest Summary:");
        System.out.println("Total tests run: " + result.getRunCount());
        System.out.println("Tests passed: " + (result.getRunCount() - result.getFailureCount()));
        System.out.println("Tests failed: " + result.getFailureCount());
        System.out.println("Test success rate: " + String.format("%.2f%%", 
            (double)(result.getRunCount() - result.getFailureCount()) / result.getRunCount() * 100));
        
        if (!result.getFailures().isEmpty()) {
            System.out.println("\nFailures:");
            for (Failure failure : result.getFailures()) {
                System.out.println("- " + failure.getTestHeader() + ": " + failure.getMessage());
                System.out.println("  Location: " + failure.getDescription().getClassName() + "." + 
                    failure.getDescription().getMethodName());
                System.out.println("  Trace: " + failure.getTrace());
                System.out.println();
            }
        }
        
        System.out.println("\nTest completion time: " + result.getRunTime() + "ms");
        
        if (result.wasSuccessful()) {
            System.out.println("\n✅ All tests PASSED!");
        } else {
            System.out.println("\n❌ Some tests FAILED!");
        }
    }
} 