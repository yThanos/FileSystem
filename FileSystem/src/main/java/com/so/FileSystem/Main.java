package com.so.FileSystem;

import java.io.*;

public class Main {
    public static void main(String[] args) {

        String fileName = "pitch";
        String fileExt = "mp4";

        System.out.println("initializing file system...");
        FileSystemImplementation fileSystem = new FileSystemImplementation();
        System.out.println("file system initialized");
        
        //fileSystem.remove(fileName);
        /*System.out.println("creating file...");
        System.out.println("[MAIN] freespace: "+fileSystem.freeSpace());
        File arquivo = new File(fileName+"."+fileExt);
        byte[] data = new byte[(int)arquivo.length()];
        try(FileInputStream fis = new FileInputStream(arquivo)){
            fis.read(data);
        } catch (Exception e){
            e.printStackTrace();
        }*/
        //fileSystem.create("teste", "texto de teste para inserção".getBytes());
        System.out.println("file created");

        //System.out.println("appending file...");
        fileSystem.append("teste", " Kappa texto de teste para append".getBytes());
        //System.out.println("file appended");

        System.out.println("reading file...");
        byte[] dataRead = fileSystem.read2("teste", 0, -1);
        System.out.println("file read");
        System.out.println(new String(dataRead));
        /*System.out.println("Saving file...");
        try(FileOutputStream fos = new FileOutputStream(fileName+"Copy."+fileExt)){
            fos.write(dataRead);
        } catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("file saved");*/
    }
}
