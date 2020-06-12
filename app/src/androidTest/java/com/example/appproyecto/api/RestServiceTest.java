package com.example.appproyecto.api;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class RestServiceTest {

    RestService mRestService;

    @Before
    public void setUp() throws Exception {
        mRestService = RestService.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    @Test
    public void getInstance() {
        RestService restService = RestService.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext());
        assertEquals(mRestService, restService);
    }

    @Test
    public void validarUsuarioExistente() throws ExecutionException, InterruptedException {
        final CompletableFuture<String> future = new CompletableFuture<>();

        RestService.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext()).validarUsuario("usuario", "usuario", new RestService.ResponseListener() {
            @Override
            public void onResponse(String response) {
                future.complete(response);
            }

            @Override
            public void onError(String error) {
                future.complete("");
            }
        });

        assertTrue(future.get().length() > 0);
    }

    @Test
    public void validarUsuarioInexistente() throws ExecutionException, InterruptedException {
        final CompletableFuture<String> future = new CompletableFuture<>();

        RestService.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext()).validarUsuario("dwnidw", "ndiwndiw", new RestService.ResponseListener() {
            @Override
            public void onResponse(String response) {
                future.complete(response);
            }

            @Override
            public void onError(String error) {
                future.complete("Error");
            }
        });

        assertTrue(future.get().length() == 0);
    }

    @Test
    public void documentValidationExistente() throws ExecutionException, InterruptedException {
        final CompletableFuture<String> future = new CompletableFuture<>();

        RestService.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext()).documentValidation("10458148398", "002-000003", new RestService.ResponseListener() {
            @Override
            public void onResponse(String response) {
                future.complete(response);
            }

            @Override
            public void onError(String error) {
                future.complete("");
            }
        });

        assertTrue(future.get().length() > 0);
    }

    @Test
    public void documentValidationInexistente() throws ExecutionException, InterruptedException {
        final CompletableFuture<String> future = new CompletableFuture<>();

        RestService.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext()).documentValidation("10102020192", "000-000001", new RestService.ResponseListener() {
            @Override
            public void onResponse(String response) {
                future.complete(response);
            }

            @Override
            public void onError(String error) {
                future.complete(error);
            }
        });

        assertEquals("NO SE ENCONTRÓ DOCUMENTO", future.get());
    }

}