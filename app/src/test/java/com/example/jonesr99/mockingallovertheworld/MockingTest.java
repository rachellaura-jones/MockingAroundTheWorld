package com.example.jonesr99.mockingallovertheworld;

import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class MockingTest {
    @Test
    public void givenIHaveAnInterfaceThenICanInstantiateAMock() throws Exception {
        TestInterface mockObject = Mocking.mock(TestInterface.class);
        assertNotNull(mockObject);
    }


    public interface TestInterface {

    }

    private static class Mocking {

        public static <T> T mock(Class<T> clazz) throws IllegalAccessException, InstantiationException {
            return (T) java.lang.reflect.Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    return null;
                }
            });
        }
    }


}