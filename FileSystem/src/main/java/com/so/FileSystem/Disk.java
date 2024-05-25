package com.so.filesystem;

import java.io.File;
import java.io.RandomAccessFile;

import lombok.SneakyThrows;

public class Disk {
    /**
     * Nome do arquivo que simula o disco.
     */
    private static final String ARCHIVE_NAME = "fat32.data";
    /**
     * Número de blocos no disco.
     */
    public static final int BLOCKS_NUM = 16 * 1024;
    /**
     * Tamanho de cada bloco.
     */
    public static final int BLOCk_SIZE = 64 * 1024;
    /**
     * Arquivo que simula o disco.
     */
    private final RandomAccessFile disk;

    @SneakyThrows
    public Disk(){
        File f = new File(ARCHIVE_NAME);
        this.disk = new RandomAccessFile(f, "rws");
        this.disk.setLength(BLOCKS_NUM * BLOCk_SIZE);
    }

    /**
     * Lê um bloco do disco.
     * @param block número do bloco a ser lido.
     * @return byte array com os dados do bloco.
     */
    public byte[] read(int block){
        if(block < 0 || block >= BLOCKS_NUM){
            throw new IllegalArgumentException("[DISK.read] Invalid block number");
        }
        byte[] data = new byte[BLOCk_SIZE];
        try {
            this.disk.seek(block * BLOCk_SIZE);
            this.disk.read(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * Escreve um bloco no disco.
     * @param block número do bloco a ser escrito.
     * @param data byte array dados a serem escritos.
     */
    public void write(int block, byte[] data){
        if(block < 0 || block >= BLOCKS_NUM){
            throw new IllegalArgumentException("[DISK.write] Invalid block number");
        }
        if(data.length > BLOCk_SIZE){
            throw new IllegalArgumentException("Data is too big");
        }
        try {
            this.disk.seek(block * BLOCk_SIZE);
            this.disk.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
