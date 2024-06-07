package com.so.filesystem.aula;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TesteProdutor {
    private static final int MAX_LENGTH = 30;
    private List<Integer> buffer = new ArrayList<>(MAX_LENGTH);
    public TesteProdutor(){
        new Thread(new Produtor()).start();
        new Thread(new Consumidor()).start();
    }

    public class Produtor implements Runnable{
        private Random rnd = new Random();
        @Override
        public void run() {
            while (true){
                int i = rnd.nextInt();
                buffer.add(i);
                System.out.println("[PRODUTOR] Produziu o N: " + i);
            }
        }
    }
    public class Consumidor implements Runnable{

        @Override
        public void run() {
            while (true){
                int i = buffer.remove(0);
                System.out.println("[CONSUMIDOR] Consumiu o N: " + i);
            }
        }
    }

    public static void main(String[] args) {
        new TesteProdutor();
    }
}
