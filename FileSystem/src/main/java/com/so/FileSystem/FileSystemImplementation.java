package com.so.FileSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSystemImplementation implements FileSystem {
    /**
     * Instancia de Disco a ser usada pelo sistema de arquivos para gerenciar os arquivos.
     */
    private final Disk disk;

    /**
     * Lista de arquivos no sistema de arquivos, basicamente bloco 0, dizendo aonde estão os arquivos no disco.
     */
    private List<Archive> archives;

    /**
     * Mapa de alocação de arquivos, dizendo aonde estão os arquivos no disco.
     */
    private List<Integer> FAT = new ArrayList<>(Disk.BLOCKS_NUM);

    /**
     * Construtor do sistema de arquivos, inicializa o disco e carrega os arquivos do bloco 0.
     */
    public FileSystemImplementation() {
        this.disk = new Disk();
        byte[] bytes = this.disk.read(0);
        for(int i = 0; i < bytes.length; i += 19){
            Archive archive = Archive.fromByteArray(bytes);
            this.archives.add(archive);
        }

    }

    @Override
    public void create(String fileName, byte[] data) {

    }

    @Override
    public void append(String fileName, byte[] data) {

    }

    //ToDo: ler na FAT o bloco inicial, e ir lendo os blocos seguintes. pedir pro rafael o que é o offset e o limit exatamente e ver se faz sentido
    @Override
    public byte[] read(String fileName, int offset, int limit) {
        for(Archive archive : this.archives){
            if(archive.getName().equals(fileName)){
                int nextBlock = archive.getPos();
                int blocks = 0;
                do {
                    blocks ++;
                    nextBlock = FAT.get(nextBlock);
                } while(nextBlock != 0);
                byte[] data = new byte[blocks * Disk.BLOCk_SIZE];
                int destPos = 0;
                do{
                    byte[] parteArchive = disk.read(nextBlock);
                    System.arraycopy(parteArchive, 0, data, destPos, parteArchive.length);
                    destPos += Disk.BLOCk_SIZE;
                    nextBlock = FAT.get(nextBlock);
                } while(nextBlock != 0);
                return data;
            }
        }
        return null;
    }

    @Override
    public void remove(String fileName) {
        for(Archive archive : this.archives){
            if(archive.getName().equals(fileName)) {
                int nextBlock = archive.getPos();
                do {
                    int current = nextBlock;
                    nextBlock = FAT.get(nextBlock);
                    FAT.remove(current);
                } while(nextBlock != 0);
                archives.remove(archive);
            }
        }
    }

    @Override
    public int freeSpace() {
        int free = 0;
        for(Integer block: FAT){
            if(block == null){
                free ++;
            }
        }
        return free;
    }
    
}
