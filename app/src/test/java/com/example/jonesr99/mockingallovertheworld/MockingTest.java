package com.example.jonesr99.mockingallovertheworld;

import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class MockingTest {
    @Test
    public void givenIHaveAnInterfaceThenICanInstantiateAMock() throws Exception {
        Mocked<TestInterface> mockObject = Mocking.mock(TestInterface.class);
        assertNotNull(mockObject);
    }

    @Test
    public void givenMyInterfaceHasAMethodICanVerifyItsInvocation() throws Exception {
        Mocked<MethodTestInterface> mockObject = Mocking.mock(MethodTestInterface.class);

        mockObject.get().one();
        mockObject.verifyCalled().one();
    }


    public interface TestInterface {

    }

    public interface MethodTestInterface {
        public void one();
    }

    private static class Mocking {
        public static <T> Mocked<T> mock(Class<T> clazz) throws IllegalAccessException, InstantiationException {
            return new Mocked(java.lang.reflect.Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    return null;
                }
            }));
        }
    }


    private static class Mocked<T> {
        private T mockedObject;

        public Mocked(T mockedObject) {
            this.mockedObject = mockedObject;
        }

        public T get() {
            return mockedObject;
        }

        public T verifyCalled() {
            return null;
        }
    }
}