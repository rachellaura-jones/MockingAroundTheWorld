package com.example.jonesr99.mockingallovertheworld;

import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class MockingTest {
    @Test
    public void givenIHaveAnInterfaceThenICanInstantiateAMock() throws Exception {
        MockedWrapper<TestInterface> mockObject = Mocker.mock(TestInterface.class);
        assertNotNull(mockObject);
    }

    @Test
    public void givenMyInterfaceHasAMethodICanVerifyItsInvocation() throws Exception {
        MockedWrapper<MethodTestInterface> mockObject = Mocker.mock(MethodTestInterface.class);
        mockObject.get().one();
        mockObject.verifyCalled().one();
    }

    @Test (expected = AssertionError.class)
    public void givenMyMethodIsNotCalledAssertionErrorIsThrown() throws Exception {
        MockedWrapper<MethodTestInterface> mockedObject = Mocker.mock(MethodTestInterface.class);
        mockedObject.verifyCalled().one();
    }

    public interface TestInterface {

    }

    public interface MethodTestInterface {
        void one();
    }

    private static class Mocker {
        public static <T> MockedWrapper<T> mock(Class<T> clazz) throws IllegalAccessException, InstantiationException {
            MockInvocationHandler invocationHandler = new MockInvocationHandler();
            Object proxyInstance = Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, invocationHandler);
            return new MockedWrapper(proxyInstance, invocationHandler);
        }

    }

    private static class MockInvocationHandler implements InvocationHandler {

        private ArrayList<Method> called = new ArrayList<>();

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            called.add(method);
            return null;
        }

        public boolean hasBeenCalled(Method method) {
            return called.contains(method);
        }
    }

    private static class MockedWrapper<T> {

        private final T mockedObject;
        private final MockInvocationHandler invocationHandler;

        public MockedWrapper(T mockedObject, MockInvocationHandler invocationHandler) {
            this.mockedObject = mockedObject;
            this.invocationHandler = invocationHandler;
        }

        public T get() {
            return mockedObject;
        }

        public T verifyCalled() {
            return createProxy(new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    assertTrue(MockedWrapper.this.invocationHandler.hasBeenCalled(method));
                    return null;
                }
            });
        }

        private T createProxy(InvocationHandler invocationHandler) {
            return (T) Proxy.newProxyInstance(getClass().getClassLoader(), mockedObject.getClass().getInterfaces(), invocationHandler);
        }
    }
}