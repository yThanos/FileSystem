package com.so.FileSystem;

import java.util.ArrayList;
import java.util.List;

public class TesteAula {
    private Integer iDeTodos = 0;
    private String vez = "a";
    private boolean cb, ca = false;


    private class InnerClassA implements Runnable {
        private int i = 0;

        @Override
        public void run() {
            while (i < 10000000L) {
                ca = true;
                vez = "B";
                while (vez.equals("B") && cb){

                }
                i++;
                TesteAula.this.iDeTodos++;
                ca = false;
            }
        }
    }

    private class InnerClassB implements Runnable {
        private int i = 0;

        @Override
        public void run() {
            while (i < 10000000L) {
                cb = true;
                vez = "A";
                while (vez.equals("A") && ca){

                }
                i++;
                TesteAula.this.iDeTodos++;
                cb = false;
            }
        }
    }

    public TesteAula() throws InterruptedException {
        init();
    }

    public void init() throws InterruptedException {
        List<Thread> threads = new ArrayList<>();

        InnerClassA ia = new InnerClassA();
        InnerClassB ib = new InnerClassB();

        Thread ta = new Thread(ia);
        Thread tb = new Thread(ib);

        threads.add(ta);
        threads.add(tb);

        ta.start();
        tb.start();

        monitor(threads, ia, ib);
    }

    public void monitor(List<Thread> ts, InnerClassA ia, InnerClassB ib) throws InterruptedException {
        for(Thread t: ts){
            t.join();
        }
        int total = ia.i + ib.i;

        System.out.println("I_GlOBAL: "+iDeTodos+" I_SUM: "+ total +"\nDifference: "+(total - iDeTodos));
    }

    public static void main(String[] args) throws InterruptedException {
        new TesteAula();
    }
}
