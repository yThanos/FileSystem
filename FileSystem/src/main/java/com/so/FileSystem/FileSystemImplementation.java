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
    private List<Archive> archives;

    /**
     * Mapa de alocação de arquivos, dizendo aonde estão os arquivos no disco.
     */
    private List<Integer> FAT = new ArrayList<>(Disk.BLOCKS_NUM);

    /**
     * Construtor do sistema de arquivos, inicializa o disco e carrega os arquivos do bloco 0 e 1.
     */
    public FileSystemImplementation() {
        this.disk = new Disk();//inicializa o disco
        byte[] bytes = this.disk.read(0);//lê o bloco 0 aonde tem os indices
        for(int i = 0; i < bytes.length; i += 19){
            try{
                Archive archive = Archive.fromByteArray(bytes);//cria um arquivo a partir do array de bytes
                this.archives.add(archive);//adiciona o arquivo na lista de arquivos
                } catch (Exception e){
                e.printStackTrace();
                break;//se der erro provalvemente tentou transofrmar em arquivo onde não tinha mais nada entao para
            }
        }
        //vou ter que fazer um for para preencher a FAT com os valores do bloco 1

    }

    @Override
    public void create(String fileName, byte[] data) {
        if (data.length > (freeSpace() * Disk.BLOCk_SIZE)) {//checa se tem espaço suficiente para o arquivo
            throw new IllegalArgumentException("No free space");
        }
        for(Archive archive : this.archives){//checa se ja tem um arquivo com esse nome, talvez de pra simplente sobreescrever
            if(archive.getName().equals(fileName)){
                throw new IllegalArgumentException("File already exists");
            }
        }

        int nextBlock = nextFreeBlock();//pega o proximo bloco livre
        int dataPos = 0;//posição do dado no array de dados

        archives.add(new Archive(fileName, "txt",  data.length, nextBlock));//adiciona o arquivo na lista de arquivos
        updateIndex();//atualiza o bloco 0 com a nova lista de arquivos

        //um contador e um Math.roundUp para saber qual o ultimo bloco, ultimo bloco leva o valor -1
        do{
            byte[] parteData = new byte[Disk.BLOCk_SIZE];//cria um array de bytes com o tamanho de um bloco
            System.arraycopy(data, dataPos, parteData, 0, Disk.BLOCk_SIZE);//copia os dados do array de dados para o array de bytes
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

    }

    @Override
    public void append(String fileName, byte[] data) {

    }

    //ToDo: ler na FAT o bloco inicial, e ir lendo os blocos seguintes. pedir pro rafael o que é o offset e o limit exatamente e ver se faz sentido
    @Override
    public byte[] read(String fileName, int offset, int limit) {
        for(Archive archive : this.archives){//procura o arquivo
            if(archive.getName().equals(fileName)){
                int nextBlock = archive.getPos();//pega o bloco inicial do arquivo
                int blocks = 0;//contador de blocos
                do {
                    blocks ++;//incrementa o contador de blocos
                    nextBlock = FAT.get(nextBlock);//pega o proximo bloco
                } while(nextBlock != -1);//repete até acabar os blocos
                byte[] data = new byte[blocks * Disk.BLOCk_SIZE];//cria um array de bytes com o tamanho total dos blocos
                int destPos = 0;//posição de destino no array de bytes
                do{
                    byte[] parteArchive = disk.read(nextBlock);//lê o bloco
                    System.arraycopy(parteArchive, 0, data, destPos, parteArchive.length);//copia o bloco para o array de bytes
                    destPos += Disk.BLOCk_SIZE;//atualiza a posição de destino
                    nextBlock = FAT.get(nextBlock);//pega o proximo bloco
                } while(nextBlock != 0);//repete até acabar os blocos
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
                updateFat();
            }
        }
    }

    @Override
    public int freeSpace() {
        int free = 0;//contador de blocos livres
        for(Integer block: FAT){
            if(block == null){//se o bloco for nulo incrementa o contador
                free ++;
            }
        }
        return free;
    }

    private int nextFreeBlock(){
        for(int i = 2; i < Disk.BLOCKS_NUM; i++){// começa do 2 pq o 0 e o 1 são reservados 0 indices e 1 FAT
            if(FAT.get(i) == null){
                return i;
            }
        }
        return -1;
    }

    private void updateIndex(){
        byte[] bytes = new byte[Disk.BLOCk_SIZE];
        int pos = 0;
        for(Archive archive : this.archives){
            byte[] archiveBytes = archive.toByteArray();
            System.arraycopy(archiveBytes, 0, bytes, pos, archiveBytes.length);
            pos += archiveBytes.length;
        }
        disk.write(0, bytes);
    }

    private void updateFat(){
        byte[] bytes = new byte[Disk.BLOCk_SIZE];

        disk.write(1, bytes);
    }
}
