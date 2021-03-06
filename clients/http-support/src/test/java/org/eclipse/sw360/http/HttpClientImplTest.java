/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for {@code HttpClientImpl}. This class focuses on functionality
 * that is not covered by the integration test class.
 */
public class HttpClientImplTest {
    /**
     * The client to be tested.
     */
    private HttpClientImpl httpClient;

    @Before
    public void setUp() {
        httpClient = new HttpClientImpl(new OkHttpClient(), new ObjectMapper());
    }

    /**
     * Returns a request object with some defined properties.
     *
     * @return the test request
     */
    private static Request testRequest() {
        return new Request.Builder().url("http://test.org/foo").build();
    }

    /**
     * Returns a mock for a response processor.
     *
     * @return the response processor mock
     */
    private static ResponseProcessor<Object> createProcessorMock() {
        @SuppressWarnings("unchecked")
        ResponseProcessor<Object> mockProcessor = mock(ResponseProcessor.class);
        return mockProcessor;
    }

    /**
     * Creates a mock for a Call object that is prepared to return some meta
     * information about itself. This is needed to deal with logging output
     * generated by the HTTP client implementation.
     *
     * @return the mock Call object
     */
    private static Call createCallMock() {
        Call call = mock(Call.class);
        when(call.request()).thenReturn(testRequest());
        return call;
    }

    /**
     * Creates a mock for a response that is prepared to return some
     * information. This is needed to deal with logging output generated by the
     * HTTP client implementation.
     *
     * @return the mock response object
     */
    private static Response createResponseMock() {
        Response response = mock(Response.class);
        when(response.code()).thenReturn(200);
        when(response.request()).thenReturn(testRequest());
        return response;
    }

    /**
     * Expects that the given future has failed and returns the causing
     * exception.
     *
     * @param future the future to check
     * @return the exception that caused the future to fail
     */
    private static Throwable extractException(CompletableFuture<?> future) {
        try {
            Object result = future.join();
            throw new AssertionError("Future did not fail, but returned " + result);
        } catch (CompletionException e) {
            return e.getCause();
        }
    }

    /**
     * Checks that the given future has failed with the passed in exception.
     *
     * @param future the future to check
     * @param expEx  the expected exception
     */
    private static void expectFailure(CompletableFuture<?> future, Throwable expEx) {
        assertThat(extractException(future)).isEqualTo(expEx);
    }

    @Test
    public void testResponseIsClosed() throws IOException {
        Object result = new Object();
        Response response = createResponseMock();
        CompletableFuture<Object> future = new CompletableFuture<>();
        ResponseProcessor<Object> processor = createProcessorMock();
        when(processor.process(any())).thenReturn(result);

        Callback callback = httpClient.createCallback(processor, future);
        callback.onResponse(createCallMock(), response);
        assertThat(future.join()).isEqualTo(result);
        verify(response).close();
    }

    @Test
    public void testResponseIsClosedEvenIfProcessorThrowsAnException() throws IOException {
        Response response = createResponseMock();
        CompletableFuture<Object> future = new CompletableFuture<>();
        IOException exception = new IOException("Boo");
        ResponseProcessor<Object> processor = createProcessorMock();
        when(processor.process(any())).thenThrow(exception);

        Callback callback = httpClient.createCallback(processor, future);
        callback.onResponse(createCallMock(), response);
        expectFailure(future, exception);
        verify(response).close();
    }

    @Test
    public void testResponseIsClosedEvenIfProcessorThrowsARuntimeException() throws IOException {
        Response response = createResponseMock();
        CompletableFuture<Object> future = new CompletableFuture<>();
        IllegalStateException exception = new IllegalStateException("Runtime boo");
        ResponseProcessor<Object> processor = createProcessorMock();
        when(processor.process(any())).thenThrow(exception);

        Callback callback = httpClient.createCallback(processor, future);
        callback.onResponse(createCallMock(), response);
        expectFailure(future, exception);
        verify(response).close();
    }

    @Test
    public void testCallbackErrorHandling() {
        CompletableFuture<Object> future = new CompletableFuture<>();
        IOException exception = new IOException("Request failed miserably");

        Callback callback = httpClient.createCallback(createProcessorMock(), future);
        callback.onFailure(createCallMock(), exception);
        expectFailure(future, exception);
    }
}
