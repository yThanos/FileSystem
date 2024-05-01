package com.so.FileSystem;

import java.io.*;

public class Main {
    public static void main(String[] args) {

        String fileName = "pdf";
        String fileExt = "pdf";

        FileSystemImplementation fileSystem = new FileSystemImplementation();
        
        //fileSystem.remove(fileName);

        System.out.println("[MAIN] freespace: "+fileSystem.freeSpace());

        File arquivo = new File(fileName+"."+fileExt);

        byte[] data = new byte[(int)arquivo.length()];
        try(FileInputStream fis = new FileInputStream(arquivo)){
            fis.read(data);
        } catch (Exception e){
            e.printStackTrace();
        }

        //fileSystem.create(fileName, data);

        fileSystem.append(fileName, data);

        System.out.println("reading file...");
        byte[] dataRead = fileSystem.read2(fileName, 0, -1);
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
