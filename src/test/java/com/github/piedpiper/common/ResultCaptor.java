package com.github.piedpiper.common;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ResultCaptor<T> implements Answer<T> {
    private T result = null;
    public T getResult() {
        return result;
    }

    @SuppressWarnings("unchecked")
	@Override
    public T answer(InvocationOnMock invocationOnMock) throws Throwable {
        result = (T) invocationOnMock.callRealMethod();
        return result;
    }
}