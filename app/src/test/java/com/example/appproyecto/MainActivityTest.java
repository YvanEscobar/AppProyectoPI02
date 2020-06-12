package com.example.appproyecto;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;

public class MainActivityTest {

    private MainActivity mainActivity;

    @Before
    public void setUp() throws Exception {
        mainActivity = new MainActivity();
    }

    @Test
    public void onCreate() {
    }

    @Test
    public void suma() {
        int resultado = mainActivity.suma(3, 2);
        assertEquals(5, resultado);
        //double decimal = (double) mainActivity.suma((int) 2.3, 4);
        //assertEquals(6.3, decimal, 0.0);
        //CompletableFuture future;

    }


}