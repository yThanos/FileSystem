package com.so.filesystem.aula;

import java.util.concurrent.Semaphore;

import lombok.SneakyThrows;

public class TesteSemaforo {
    private final Semaphore semaforo = new Semaphore(1);
    private char[] buffer = new char[200];

    @SneakyThrows
    public TesteSemaforo() {
        new Thread(new Consumidor()).start();
        new Thread(new Produtor()).start();
        Thread.sleep(100);
        System.out.println(buffer);
    }

    public class Produtor implements Runnable {

        @Override
        @SneakyThrows
        public void run() {
            int i = 1;
            while (i<200) {
                semaforo.acquire();
                buffer[i] = 'a';
                semaforo.release();
                i+=2;
            }
        }
    }

    public class Consumidor implements Runnable {

        @Override
        @SneakyThrows
        public void run() {
            int i = 0;
            while (i < 200) {
                semaforo.acquire();
                buffer[i] = 'b';
                semaforo.release();
                i+=2;
            }
        }
    }

    public static void main(String[] args) {
        new TesteSemaforo();
    }
}
