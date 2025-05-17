package com.samet.music.gui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Helper class for test reflection operations
 */
public class TestUtils {
    
    /**
     * Get a field value from an object using reflection
     * 
     * @param object The object to get the field from
     * @param fieldName The name of the field to get
     * @return The value of the field
     * @throws Exception If the field cannot be accessed
     */
    public static Object getFieldValue(Object object, String fieldName) throws Exception {
        Field field = findField(object.getClass(), fieldName);
        field.setAccessible(true);
        return field.get(object);
    }
    
    /**
     * Set a field value in an object using reflection
     * 
     * @param object The object to set the field in
     * @param fieldName The name of the field to set
     * @param value The value to set
     * @throws Exception If the field cannot be accessed
     */
    public static void setFieldValue(Object object, String fieldName, Object value) throws Exception {
        Field field = findField(object.getClass(), fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
    
    /**
     * Invoke a method on an object using reflection
     * 
     * @param object The object to invoke the method on
     * @param methodName The name of the method to invoke
     * @param parameterTypes The parameter types of the method
     * @param args The arguments to pass to the method
     * @return The result of the method invocation
     * @throws Exception If the method cannot be invoked
     */
    public static Object invokeMethod(Object object, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = findMethod(object.getClass(), methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(object, args);
    }
    
    /**
     * Find a field in a class or its superclasses
     * 
     * @param clazz The class to search
     * @param fieldName The name of the field to find
     * @return The field
     * @throws NoSuchFieldException If the field cannot be found
     */
    private static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return findField(superClass, fieldName);
            }
            throw e;
        }
    }
    
    /**
     * Find a method in a class or its superclasses
     * 
     * @param clazz The class to search
     * @param methodName The name of the method to find
     * @param parameterTypes The parameter types of the method
     * @return The method
     * @throws NoSuchMethodException If the method cannot be found
     */
    private static Method findMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes) throws NoSuchMethodException {
        try {
            return clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return findMethod(superClass, methodName, parameterTypes);
            }
            throw e;
        }
    }
} 