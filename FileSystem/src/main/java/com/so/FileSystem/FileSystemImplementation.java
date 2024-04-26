package com.so.FileSystem;

import java.util.ArrayList;
import java.util.List;

public class FileSystemImplementation implements FileSystem {
    /**
     * Instancia de Disco a ser usada pelo sistema de arquivos para gerenciar os arquivos.
     */
    private final Disk disk;

    /**
     * Lista de arquivos no sistema de arquivos, basicamente bloco 0, dizendo aonde estão os arquivos no disco.
     */
    private final List<Archive> archives;

    /**
     * Mapa de alocação de arquivos, dizendo aonde estão os arquivos no disco.
     */
    private List<Integer> FAT = new ArrayList<>(Disk.BLOCKS_NUM);

    /**
     * Construtor do sistema de arquivos, inicializa o disco e carrega os arquivos do bloco 0 e 1.
     */
    public FileSystemImplementation() {
        this.disk = new Disk();//inicializa o disco
        this.archives = new ArrayList<>();//inicializa a lista de arquivos
        byte[] bytes = this.disk.read(0);//lê o bloco 0 aonde tem os indices
        for(int i = 0; i < bytes.length; i += 19){
            try{
                byte[] archiveBytes = new byte[19];//cria um array de bytes com o tamanho de um arquivo
                System.arraycopy(bytes, i, archiveBytes, 0, 19);//copia os dados do array de bytes para o array de bytes do arquivo
                if(archiveBytes[0] == 0){//se o primeiro byte for 0 significa que não tem mais arquivos
                    break;
                }
                Archive archive = Archive.fromByteArray(archiveBytes);//cria um arquivo a partir do array de bytes
                this.archives.add(archive);//adiciona o arquivo na lista de arquivos
            } catch (Exception e){
                e.printStackTrace();
                break;//se der erro provalvemente tentou transofrmar em arquivo onde não tinha mais nada entao para
            }
        }
        //vou ter que fazer um for para preencher a FAT com os valores do bloco 1
        byte[] bytesFat = this.disk.read(1);//lê o bloco 1 aonde tem a FAT

        this.FAT = bytesToFat(bytesFat);//converte o array de bytes para uma lista de inteiros
    }

    @Override
    public void create(String fileName, byte[] data) {
        if (data.length > Disk.BLOCk_SIZE) {//checa se tem espaço suficiente para o arquivo
            throw new IllegalArgumentException("[FSI.create] No free space");
        }

        for(Archive archive : this.archives){//checa se ja tem um arquivo com esse nome, talvez de pra simplente sobreescrever
            if(archive.getName().equals(fileName)){
                throw new IllegalArgumentException("[FSI.create] File already exists");
            }
        }

        int nextBlock = nextFreeBlock();//pega o proximo bloco livre
        System.out.println("[FSI.create] Next block: " + nextBlock);
        int dataPos = 0;//posição do dado no array de dados

        archives.add(new Archive(fileName, "txt",  data.length, nextBlock));//adiciona o arquivo na lista de arquivos
        updateIndex();//atualiza o bloco 0 com a nova lista de arquivos

        System.out.println(archives);
        //System.out.println(FAT);

        do{
            byte[] parteData = new byte[Disk.BLOCk_SIZE];//cria um array de bytes com o tamanho de um bloco
            System.arraycopy(data, dataPos, parteData, 0, data.length > Disk.BLOCk_SIZE ? Disk.BLOCk_SIZE : data.length);//copia os dados do array de dados para o array de bytes
            disk.write(nextBlock, parteData);//escreve o bloco
            dataPos += Disk.BLOCk_SIZE;//atualiza a posição do dado no array de dados
            int current = nextBlock;//salva o bloco atual

            if(dataPos >= data.length){//se o acabou de gravar tudo
                nextBlock = -1;//o proximo bloco é -1
            } else {
                nextBlock = nextFreeBlock();//pega o proximo bloco livre
            }

            FAT.set(current, nextBlock);//atualiza o bloco atual com o valor do proximo bloco
        } while(dataPos < data.length);//repete até acabar gravar tudo

        updateFat();//atualiza a FAT

    }

    @Override
    public void append(String fileName, byte[] data) {

    }

    //ToDo: ler na FAT o bloco inicial, e ir lendo os blocos seguintes. pedir pro rafael o que é o offset e o limit exatamente e ver se faz sentido
    @Override
    public byte[] read(String fileName, int offset, int limit) {
        for(Archive archive : this.archives){//procura o arquivo
            if(archive.getName().equals(fileName)){
                System.out.println("[FSI.read] Archive found: " + archive.getName());
                int nextBlock = archive.getPos();//pega o bloco inicial do arquivo
                int currentBlock = nextBlock;
                int blocks = 0;//contador de blocos
                while(nextBlock != -1) {
                    blocks ++;//incrementa o contador de blocos
                    nextBlock = FAT.get(nextBlock);//pega o proximo bloco
                } ;//repete até acabar os blocos
                System.out.println("[FSI.read] Necessary blocks: " + blocks);
                byte[] data = new byte[blocks * Disk.BLOCk_SIZE];//cria um array de bytes com o tamanho total dos blocos
                int destPos = 0;//posição de destino no array de bytes
                do{
                    System.out.println("[FSI.read] Reading block: " + currentBlock);
                    byte[] parteArchive = disk.read(currentBlock);//lê o bloco
                    System.arraycopy(parteArchive, 0, data, destPos, parteArchive.length);//copia o bloco para o array de bytes
                    destPos += Disk.BLOCk_SIZE;//atualiza a posição de destino
                    currentBlock = FAT.get(currentBlock);//pega o proximo bloco
                } while(currentBlock != -1);//repete até acabar os blocos
                System.out.println(data.length);
                return data;
            }
        }
        return null;
    }

    @Override
    public void remove(String fileName) {
        for(Archive archive : this.archives){
            if(archive.getName().equals(fileName)) {
                int nextBlock = archive.getPos();//pega o bloco inicial do arquivo
                do {
                    Integer current = nextBlock;//salva o bloco atual
                    FAT.set(current, null);//remove o bloco atual
                    nextBlock = FAT.get(current);//pega o proximo bloco
                } while(nextBlock != -1);//se o proximo bloco for -1 acabou
                archives.remove(archive);//Como remove o arquivo do bloco 0?, n sei a posição exata do arquivo no bloco 0, ou sei?, pelo nome?
                updateIndex();//atualiza o bloco 0
                updateFat();//atualiza a bloco 1
            }
        }
    }

    @Override
    public int freeSpace() {
        int free = 0;//contador de blocos livres
        for(Integer block: FAT){
            if(block == 0 || block == null){//se o bloco for nulo incrementa o contador
                free ++;
            }
        }
        return free;
    }

    private int nextFreeBlock(){
        for(int i = 2; i < Disk.BLOCKS_NUM; i++){// começa do 2 pq o 0 e o 1 são reservados 0 indices e 1 FAT
            if(FAT.get(i) == null || FAT.get(i) == 0){//se o bloco for nulo significa que está livre
                return i;
            }
        }
        return -1;
    }

    private void updateIndex(){
        byte[] bytes = new byte[Disk.BLOCk_SIZE];//cria um array de bytes com o tamanho de um bloco
        int pos = 0;//posição no array de bytes
        for(Archive archive : this.archives){
            byte[] archiveBytes = archive.toByteArray();//converte o arquivo para um array de bytes
            System.arraycopy(archiveBytes, 0, bytes, pos, archiveBytes.length);//copia o array de bytes para o array de bytes do bloco
            pos += archiveBytes.length;//atualiza a posição
        }
        disk.write(0, bytes);//escreve o bloco 0
    }

    private void updateFat(){
        byte[] fatBytes = fatToBytes();//converte a FAT para um array de bytes
        disk.write(1, fatBytes);//escreve a FAT no bloco 1
    }

    private byte[] fatToBytes() {
        byte[] byteArray = new byte[FAT.size()];//cria um array de bytes com o tamanho da FAT
        for (int i = 0; i < FAT.size(); i++) {
            byteArray[i] = FAT.get(i).byteValue();//converte a lista de inteiros para um array de bytes
        }
        return byteArray;
    }
    
    private List<Integer> bytesToFat(byte[] byteArray) {
        List<Integer> integerList = new ArrayList<>();//cria uma lista de inteiros
        for (byte b : byteArray) {
            try {
                integerList.add((int) b);//converte o array de bytes para uma lista de inteiros
            } catch (Exception e) {
                e.printStackTrace();
                break;//se der erro para
            }
        }
        return integerList;
    }
}
