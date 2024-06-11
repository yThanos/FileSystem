package com.so.filesystem;

import javax.swing.SwingUtilities;

import com.so.filesystem.test.MainFrame;

public class Main {

    private static final FileSystemImplementation fsi = new FileSystemImplementation();;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame(fsi).setVisible(true));
        
        // para testar o uso do limit e offset no read basta descomentar a linha abaixo
        testeLimitOffset();
    }

    private static void testeLimitOffset() {
        String filename = "speech.txt";
        fsi.remove(filename);

        String texto = """
                Pirates are Evil? Marines are righteous? These terms have always changed throughout the course of history.
                Kid's who have never seen peace and kid's who have never seen war have different values!
                Those who stand at the top determine what's wrong and what's right! This very place is a neutral ground!
                Justice will prevail you say, of course it will. Those who win the war, become the Justice.""";
        fsi.create(filename, texto.getBytes());

        byte[] data = fsi.read(filename, 0, 100);
        System.out.println("-=+=".repeat(40)+"-");
        System.out.println("Primeiros 100 bytes:");
        System.out.println(new String(data));
        System.out.println("-=+=".repeat(40)+"-");

        data = fsi.read(filename, 100, -1);
        System.out.println("-=+=".repeat(40)+"-");
        System.out.println("A partir dos primeiros 100 bytes ate o fim:");
        System.out.println(new String(data));
        System.out.println("-=+=".repeat(40)+"-");

        String append = "\nThis was the Doflammingo's speech at Marineford during the war.";
        fsi.append("speech.txt", append.getBytes());

        data = fsi.read("speech.txt", 0, -1);
        System.out.println("-=+=".repeat(40)+"-");
        System.out.println("Texto completo com append:");
        System.out.println(new String(data));
        System.out.println("-=+=".repeat(40)+"-");
    }
}
