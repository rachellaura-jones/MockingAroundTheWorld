package com.example.jonesr99.mockingallovertheworld;

import android.support.annotation.NonNull;

import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
    public void givenMyMethodIsCalledThenICanVerify() throws Exception {
        MockedWrapper<MethodTestInterface> mockObject = Mocker.mock(MethodTestInterface.class);
        mockObject.get().one();
        mockObject.verifyCalled().one();
    }

    @Test
    public void givenMyMethodIsNotCalledAssertionErrorIsThrown() throws Exception {
        MockedWrapper<MethodTestInterface> mockedObject = Mocker.mock(MethodTestInterface.class);
        String errorMessage = null;
        try {
            mockedObject.verifyCalled().one();
        } catch (AssertionError e) {
            errorMessage = e.getMessage();
        }
        assertEquals("Expected one to be called with args: null, but there was no calls to this mock.", errorMessage);

    }

    @Test
    public void givenMyMethodIsNotCalledButAnotherArglessMethodWasThenAssertionErrorIsThrownWithDetails() throws Exception {
        MockedWrapper<MethodTestInterface> mockedObject = Mocker.mock(MethodTestInterface.class);
        String errorMessage = null;
        mockedObject.get().seven();
        try {
            mockedObject.verifyCalled().one();
        } catch (AssertionError e) {
            errorMessage = e.getMessage();
        }
        assertEquals("Expected one to be called with args: null, the calls with this mock were: \nseven", errorMessage);

    }

    @Test
    public void givenMyMethodIsNotCalledButAnotherArgedMethodWasThenAssertionErrorIsThrownWithDetails() throws Exception {
        MockedWrapper<MethodTestInterface> mockedObject = Mocker.mock(MethodTestInterface.class);
        String errorMessage = null;
        mockedObject.get().add(1,2);
        try {
            mockedObject.verifyCalled().one();
        } catch (AssertionError e) {
            errorMessage = e.getMessage();
        }
        assertEquals("Expected one to be called with args: null, the calls with this mock were: \nadd with args: 1 2 ", errorMessage);

    }

    @Test
    public void givenMyMethodIsCalledWithParamsThenICanVerify() throws Exception {
        MockedWrapper<MethodTestInterface> mockObject = Mocker.mock(MethodTestInterface.class);
        mockObject.get().add(1, 2);
        mockObject.verifyCalled().add(1, 2);
    }

    @Test (expected = AssertionError.class)
    public void givenMyMethodIsCalledWithIncorrectParamsAssertionErrorIsThrown() throws Exception {
        MockedWrapper<MethodTestInterface> mockObject = Mocker.mock(MethodTestInterface.class);
        mockObject.get().add(1, 1);
        mockObject.verifyCalled().add(1, 2);
    }

    @Test
    public void givenISpecifyAnIntegerReturnValueThenThatIsReturned() throws Exception {
        MockedWrapper<MethodTestInterface> mockObject = Mocker.mock(MethodTestInterface.class);
        mockObject.returns(7).seven();
        assertEquals(mockObject.get().seven(), 7);
    }

    @Test
    public void givenISpecifyAStringReturnValueThenThatIsReturned() throws Exception {
        MockedWrapper<MethodTestInterface> mockObject = Mocker.mock(MethodTestInterface.class);
        mockObject.returns("world").hello();
        assertEquals(mockObject.get().hello(), "world");
    }

    public interface TestInterface {
    }

    public interface MethodTestInterface {

        void one();
        void add(int numberOne, int numberTwo);
        int seven();
        String hello();
    }
    private static class Mocker {

        public static <T> MockedWrapper<T> mock(Class<T> clazz) throws IllegalAccessException, InstantiationException {
            MockInvocationHandler invocationHandler = new MockInvocationHandler();
            Object proxyInstance = Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, invocationHandler);
            return new MockedWrapper(proxyInstance, invocationHandler);
        }
    }

    private static class MethodCalledDetails {

        public final Method method;
        public final Object[] args;
        private MethodCalledDetails(Method method, Object[] args) {
            this.args = args;
            this.method = method;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MethodCalledDetails that = (MethodCalledDetails) o;

            if (method != null ? !method.equals(that.method) : that.method != null) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(args, that.args);

        }

        @Override
        public int hashCode() {
            int result = method != null ? method.hashCode() : 0;
            result = 31 * result + Arrays.hashCode(args);
            return result;
        }

    }
    private static class MockInvocationHandler implements InvocationHandler {

        private ArrayList<MethodCalledDetails> called = new ArrayList<>();

        private HashMap<MethodCalledDetails, Object> returnValues = new HashMap<>();
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            MethodCalledDetails methodCalledDetails = new MethodCalledDetails(method, args);
            called.add(methodCalledDetails);
            Object ret = returnValues.get(methodCalledDetails);
            if(ret==null) {
                ret = safeReturnNull(method.getReturnType());
            }
            return ret;
        }

        public boolean hasBeenCalled(Method method, Object[] args) {
            return called.contains(new MethodCalledDetails(method, args));
        }

        public List<MethodCalledDetails> getCalls() {
            return called;
        }

        public void setValueForMethod(Method method, Object[] args, Object retVal) {
            returnValues.put(new MethodCalledDetails(method, args), retVal);
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
                    String message = getErrorMessage(method, args);

                    assertTrue(message, invocationHandler.hasBeenCalled(method, args));
                    return null;
                }
            });
        }

        private String getErrorMessage(Method method, Object[] args) {
            String baseMessage = "Expected " + method.getName() + " to be called with args: " + args;

            if(mockWasCalled()) {
                return getErrorMessageForUncalledMock(baseMessage);
            } else {
                return getErrorMessageForCalledMock(baseMessage);
            }
        }

        private boolean mockWasCalled(){ return invocationHandler.called.size() == 0;}

        private String getErrorMessageForUncalledMock(String message) {
            return message + ", but there was no calls to this mock.";
        }

        private String getErrorMessageForCalledMock(String message) {
            message += ", the calls with this mock were: ";
            for(MethodCalledDetails methodCall : invocationHandler.called) {
                message += "\n" + methodCall.method.getName();
                if(methodCall.args != null) {
                    message += " with args: " + argsToString(methodCall.args);
                }
            }
            return message;
        }

        private String argsToString(Object[] args) {
            String result = "";
            for(Object arg : args) {
                result += arg.toString() + " ";
            }
            return result;
        }

        private T createProxy(InvocationHandler invocationHandler) {
            return (T) Proxy.newProxyInstance(getClass().getClassLoader(), mockedObject.getClass().getInterfaces(), invocationHandler);
        }

        public T returns(final Object retVal) {
            return createProxy(new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    invocationHandler.setValueForMethod(method, args, retVal);
                    return safeReturnNull(method.getReturnType());
                }
            });
        }

    }

    private static Object safeReturnNull(Class<?> returnType) {
        //boolean , byte , char , short , int , long , float and double
        if(returnType.equals(Boolean.class)) {
            return false;
        } else if (returnType.equals(byte.class)) {
            return (byte) 0;
        } else if (returnType.equals(char.class)) {
            return '\000';
        } else if (returnType.equals(short.class)) {
            return (short) 0;
        } else if (returnType.equals(int.class)) {
            return 0;
        } else if (returnType.equals(long.class)) {
            return 0L;
        } else if (returnType.equals(float.class)) {
            return  0f;
        } else if (returnType.equals(double.class)) {
            return  0d;
        }
        else return null;
    }
}