package com.so.FileSystem;

import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        System.out.println("initializing file system...");
        Disk disk = new Disk();
        byte[] data = disk.read(0);
        System.out.println(new String(data, StandardCharsets.UTF_8));
    }
}
