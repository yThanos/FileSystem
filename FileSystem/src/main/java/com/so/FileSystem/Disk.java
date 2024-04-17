package com.so.FileSystem;

import java.io.File;
import java.io.RandomAccessFile;

import lombok.SneakyThrows;

public class Disk {
    private static final String ARCHIVE_NAME = "fat32.data";
    public static final int BLOCKS_NUM = 16 * 1024;
    private static final int BLOCk_SIZE = 64 * 1024;
    private RandomAccessFile disk;

    @SneakyThrows
    public Disk(){
        File f = new File(ARCHIVE_NAME);
        this.disk = new RandomAccessFile(f, "rws");
        this.disk.setLength(BLOCKS_NUM * BLOCk_SIZE);
    }

    public byte[] read(int block){
        if(block < 0 || block >= BLOCKS_NUM){
            throw new IllegalArgumentException("Invalid block number");
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

    public void write(int block, byte[] data){
        if(block < 0 || block >= BLOCKS_NUM){
            throw new IllegalArgumentException("Invalid block number");
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
