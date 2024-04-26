package com.so.FileSystem;

import java.io.*;

public class Main {
    public static void main(String[] args) {

        String fileName = "arquivoGrande";
        String fileExt = "csv";

        System.out.println("initializing file system...");
        FileSystemImplementation fileSystem = new FileSystemImplementation();
        System.out.println("file system initialized");
        
        /*System.out.println("creating file...");
        System.out.println(fileSystem.freeSpace());
        File arquivo = new File(fileName+"."+fileExt);
        byte[] data = new byte[(int)arquivo.length()];
        try(FileInputStream fis = new FileInputStream(arquivo)){
            fis.read(data);
        } catch (Exception e){
            e.printStackTrace();
        }
        fileSystem.create(fileName+"."+fileExt, data);
        System.out.println("file created");*/

        System.out.println("reading file...");
        byte[] dataRead = fileSystem.read(fileName+"", 0, 0);
        System.out.println("file read");
        System.out.println("Saving file...");
        try(FileOutputStream fos = new FileOutputStream(fileName+"Copy."+fileExt)){
            fos.write(dataRead);
        } catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("file saved");
    }
}
