package com.so.filesystem;

import java.io.*;

import javax.swing.JFileChooser;

public class Main {
    public static void main(String[] args) {

        String fileName = "txtGrande";
        String fileExt = "txt";

        FileSystemImplementation fileSystem = new FileSystemImplementation();
        
        fileSystem.remove(fileName);

        System.out.println("[MAIN] freespace: "+fileSystem.freeSpace());

        File arquivo = new File(fileName+"."+fileExt);

        byte[] data = new byte[(int)arquivo.length()];
        try(FileInputStream fis = new FileInputStream(arquivo)){
            fis.read(data);
        } catch (Exception e){
            e.printStackTrace();
        }

        fileSystem.create(fileName, data);

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

    public static File chooseFile(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione 1 arquivo");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);;
        fileChooser.showOpenDialog(null);
        return fileChooser.getSelectedFile();
    }

    public static void saveFile(byte[] data){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar arquivo");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.showSaveDialog(null);
        File file = fileChooser.getSelectedFile();
        try(FileOutputStream fos = new FileOutputStream(file)){
            fos.write(data);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main2(String[] args) {
        File file = chooseFile();

        byte[] data = new byte[(int)file.length()];
        try(FileInputStream fis = new FileInputStream(file)){
            fis.read(data);
        } catch (Exception e){
            e.printStackTrace();
        }
        
        saveFile(data);
    }
}
