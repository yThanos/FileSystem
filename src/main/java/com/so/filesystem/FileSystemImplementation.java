package com.so.filesystem;

import com.so.filesystem.model.Archive;
import com.so.filesystem.model.Disk;
import com.so.filesystem.model.FileSystem;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author
 * User: Fraporti & Magro
 * Date: 26/05/2024
 * Time: 11:40
 *
 */
public class FileSystemImplementation implements FileSystem {

    /**
     * Instancia de Disco a ser usada pelo sistema de arquivos para gerenciar os arquivos.
     */
    private final Disk disk;

    /**
     * Lista de arquivos no sistema de arquivos, basicamente bloco 0, dizendo aonde estão os arquivos no disco.
     */
    @Getter
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
                break;//se der erro provalvemente tentou transofrmar em arquivo onde não tinha mais nada entao para
            }
        }
        
        System.out.println(this.archives);

        byte[] bytesFat = this.disk.read(1);//lê o bloco 1 aonde tem a FAT

        this.FAT = bytesToFat(bytesFat);//converte o array de bytes para uma lista de inteiros

    }

    @Override
    public void create(String fileName, byte[] data) {
        if(data.length > freeSpace()){
            throw new IllegalArgumentException("Tamanho do arquivo maior que o espaço livre");
        }
        String extension = formatExtension(fileName);
        fileName = formatFileName(fileName);//formata o nome do arquivo
        for(Archive archive : this.archives){//checa se ja tem um arquivo com esse nome, talvez de pra simplente sobreescrever
            if(archive.getName().equals(fileName)){
                throw new IllegalArgumentException("[FSI.create] File already exists");
            }
        }

        int nextBlock = nextFreeBlock();//pega o proximo bloco livre
        int length = data.length;//tamanho do array de dados
        
        System.out.println("[FSI.create] Next block: " + nextBlock);

        archives.add(new Archive(fileName, extension,  length, nextBlock));//adiciona o arquivo na lista de arquivos

        System.out.println(archives);

        int dataPos = 0;//posição do dado no array de dados

        do{
            FAT.set(nextBlock, 69);//preenche com qualquer coisa para não pegar o mesmo bloco na proxima vez

            byte[] parteData = new byte[Disk.BLOCk_SIZE];//cria um array de bytes com o tamanho de um bloco
            System.arraycopy(data, dataPos, parteData, 0,
                    Math.min(length, Disk.BLOCk_SIZE));//copia os dados do array de dados para o array de bytes
            
            disk.write(nextBlock, parteData);//escreve o bloco
            
            length -= Disk.BLOCk_SIZE;//atualiza o tamanho do array de dados
            dataPos += Disk.BLOCk_SIZE;//atualiza a posição do dado no array de dados
            
            System.out.println("[FSI.create] Writing block: " + nextBlock);
            System.out.println("[FSI.create] Data remaining length: " + length);
            System.out.println("[FSI.create] Disk size: " + dataPos);

            int current = nextBlock;//salva o bloco atual

            if(length <= 0){//se o acabou de gravar tudo
                nextBlock = 1;//o proximo bloco é 1
            } else {
                nextBlock = nextFreeBlock();//pega o proximo bloco livre
            }
            FAT.set(current, nextBlock);//atualiza o bloco atual com o valor do proximo bloco

        } while(length > 0);//repete até acabar gravar tudo
        updateFat();//atualiza a FAT
        updateIndex();//atualiza o bloco 0 com a nova lista de arquivos

    }

    @Override
    public void append(String fileName, byte[] data) {
        if(data.length > freeSpace()){
            throw new IllegalArgumentException("Tamano do arquivo maior que o espaço restante");
        }
        fileName = formatFileName(fileName);//formata o nome do arquivo
        for(Archive archive : this.archives){//procura o arquivo
            if(archive.getName().equals(fileName)){
                int currentBlock = archive.getPos();//pega o bloco inicial do arquivo
                int nextBlock = currentBlock;

                do {//pega o ultimo bloco
                    currentBlock = nextBlock;
                    nextBlock = FAT.get(nextBlock);
                    if (nextBlock == 1){
                        nextBlock = currentBlock;
                    }
                } while(FAT.get(nextBlock) > 1);

                System.out.println("[FSI.append] Current block: " + currentBlock + " Next block: " + nextBlock);
                
                int lastLength = archive.getSize() % Disk.BLOCk_SIZE;//tamanho do ultimo bloco

                currentBlock = nextBlock;//salva o bloco atual

                System.out.println("[FSI.append] Last block length: " + lastLength);

                byte[] lastBlock = disk.read(currentBlock);//lê o ultimo bloco
                byte[] newBlock = new byte[Disk.BLOCk_SIZE];//cria um array de bytes com o tamanho de um bloco
                System.arraycopy(lastBlock, 0, newBlock, 0, lastLength);//copia o ultimo bloco para o novo bloco
                System.arraycopy(data, 0, newBlock, lastLength,
                        Math.min((Disk.BLOCk_SIZE - lastLength), data.length));//copia os dados do array de dados para o novo bloco
                disk.write(currentBlock, newBlock);

                int length = data.length - (Disk.BLOCk_SIZE - lastLength);//tamanho do array de dados

                System.out.println("[FSI.append] Data for nextBlock: " + length);

                System.out.println("[FSI.append] Last block: " + nextBlock);
                nextBlock = nextFreeBlock();//pega o proximo bloco livre
                System.out.println("[FSI.append] Next free block: " + nextBlock);

                int dataPos = Disk.BLOCk_SIZE - lastLength;//posição do dado no array de dados
                if(length > 0){
                    FAT.set(currentBlock, nextBlock);//atualiza o bloco atual com o valor do proximo bloco
                    do{
                        FAT.set(nextBlock, 69);
    
                        System.out.println("[FSI.append] Writing block: " + nextBlock);
                        byte[] parteData = new byte[Disk.BLOCk_SIZE];//cria um array de bytes com o tamanho de um bloco
                        System.arraycopy(data, dataPos, parteData, 0,
                                Math.min(length, Disk.BLOCk_SIZE));//copia os dados do array de dados para o array de bytes
            
                        System.out.println("[FSI.append] Data remaining length: " + length);
                        
                        disk.write(nextBlock, parteData);//escreve o bloco
            
                        dataPos += Disk.BLOCk_SIZE;//atualiza a posição do dado no array de dados
                        
                        length -= Disk.BLOCk_SIZE;//atualiza o tamanho do array de dados
            
                        int current = nextBlock;//salva o bloco atual
            
                        if(length < 0){//se o acabou de gravar tudo
                            nextBlock = 1;//o proximo bloco é 1
                        } else {
                            nextBlock = nextFreeBlock();//pega o proximo bloco livre
                        }
            
                        FAT.set(current, nextBlock);//atualiza o bloco atual com o valor do proximo bloco
                    } while(length >= 0);//repete até acabar gravar tudo
                }
                archive.setSize(archive.getSize() + data.length);//atualiza o tamanho do arquivo
                updateFat();//atualiza a FAT
                updateIndex();//atualiza o bloco 0
            }
        }
    }

    @Override
    public void remove(String fileName) {//acho que é só remover do bloco 0 e do bloco 1, os dados no arquivo em si, podem ficar perdidos e depois são sobreescritos
        fileName = formatFileName(fileName);//formata o nome do arquivo
        for(Archive archive : this.archives){
            if(archive.getName().equals(fileName)) {
                int nextBlock = archive.getPos();//pega o bloco inicial do arquivo
                do {
                    System.out.println("[FSI.remove] Removing block: " + nextBlock);
                    int current = nextBlock;//salva o bloco atual
                    nextBlock = FAT.get(current);//pega o proximo bloco
                    FAT.set(current, 0);//remove o bloco atual
                    if(nextBlock == 1){//se o proximo bloco for 1 acabou
                        FAT.set(nextBlock, 0);//remove o proximo atual
                    }
                } while(nextBlock > 1);//se o proximo bloco for 1 acabou
                archives.remove(archive);//Como remove o arquivo do bloco 0?, n sei a posição exata do arquivo no bloco 0, ou sei?, pelo nome?
                updateIndex();//atualiza o bloco 0
                updateFat();//atualiza a bloco 1
                return;
            }
        }
        System.out.println("[FSI.remove] File not found");
    }

    @Override
    public int freeSpace() {
        int free = 0;//contador de blocos livres
        for(Integer block: FAT){
            if(block == null || block == 0){//se o bloco for nulo incrementa o contador
                free ++;
            }
        }
        return free * Disk.BLOCk_SIZE;
    }

    private int nextFreeBlock(){
        for(int i = 2; i < Disk.BLOCKS_NUM; i++){// começa do 2 pq o 0 e o 1 são reservados 0 indices e 1 FAT
            if(FAT.get(i) == 0){//se o bloco for nulo significa que está livre
                System.out.println("[FSI.nextFreeBlock] Free block: " + i);
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

    /**
     * codigo do GPT, works
     * @return array de bytes da FAT
     */
    private byte[] fatToBytes() {
        byte[] byteArray = new byte[FAT.size() * Integer.BYTES];
        for (int i = 0; i < FAT.size(); i++) {
            int value = FAT.get(i);
            byteArray[i * Integer.BYTES] = (byte) ((value >> 24) & 0xFF);
            byteArray[i * Integer.BYTES + 1] = (byte) ((value >> 16) & 0xFF);
            byteArray[i * Integer.BYTES + 2] = (byte) ((value >> 8) & 0xFF);
            byteArray[i * Integer.BYTES + 3] = (byte) (value & 0xFF);
        }
        return byteArray;
    }
    
    /**
     * codigo do gpt, works
     * @param byteArray array de bytes da FAT
     * @return lista de inteiros da FAT
     */
    private List<Integer> bytesToFat(byte[] byteArray) {
        List<Integer> integerList = new ArrayList<>();
        for (int i = 0; i < byteArray.length; i += Integer.BYTES) {
            int value = 0;
            value |= (byteArray[i] & 0xFF) << 24;
            value |= (byteArray[i + 1] & 0xFF) << 16;
            value |= (byteArray[i + 2] & 0xFF) << 8;
            value |= (byteArray[i + 3] & 0xFF);
            integerList.add(value);
        }
        return integerList;
    }

    /**
     * Formata o nome do arquivo para 8 bytes
     * @param fileName nome do arquivo
     * @return nome do arquivo formatado
     */
    private String formatFileName(String fileName) {
        fileName = fileName.split("\\.")[0];
        if(fileName.length() > 8){
            fileName = fileName.substring(0, 8);
        } else if (fileName.length() < 8){
            StringBuilder fileNameBuilder = new StringBuilder(fileName);
            do{
                fileNameBuilder.append(" ");
            } while(fileNameBuilder.length() < 8);
            fileName = fileNameBuilder.toString();
        }
        return fileName;
    }

    private String formatExtension(String fileName){
        fileName = fileName.split("\\.")[1];
        if(fileName.length() > 3){
            fileName = fileName.substring(0, 3);
        } else if(fileName.length() < 3){
            StringBuilder fileNameBuilder = new StringBuilder(fileName);
            do{
                fileNameBuilder.append(" ");
            } while(fileNameBuilder.length() < 3);
            fileName = fileNameBuilder.toString();
        }
        return fileName;
    }

    @Override
    public byte[] read(String fileName, int offset, int limit) {
        fileName = formatFileName(fileName);//formata o nome do arquivo
        System.out.println("[FSI.read2] Reading file: " + fileName);
        for(Archive archive : this.archives){//procura o arquivo
            if(archive.getName().equals(fileName)){
                System.out.println("[FSI.read2] Archive found: " + archive);
                if(offset > archive.getSize() || limit > archive.getSize()){//se o offset for maior que o tamanho do arquivo retorna null
                    return null;
                }
                if(limit == -1){//se o limite for -1 o limite é o tamanho do arquivo
                    limit = archive.getSize();
                }

                System.out.println("[FSI.read2]: passou das validacoes");

                List<Integer> blocos = new ArrayList<>();//lista de blocos
                int current = archive.getPos();//bloco atual
                do{
                    blocos.add(current);//adiciona o bloco na lista de blocos
                    System.out.println("[FSI.read2] Reading block: " + current);
                    current = FAT.get(current);//pega o proximo bloco
                    System.out.println("[FSI.read2] Next block: " + current);
                } while(current != 1);//repete até acabar os blocos

                for(int i = 0; i < 10; i++){
                    System.out.print(FAT.get(i) + " ");
                }

                System.out.println("\n[FSI.read2] Blocks: " + blocos);

                int initialBlock = blocos.get((int) (double) (offset / Disk.BLOCk_SIZE));//bloco inicial
                int initialOffset = offset % Disk.BLOCk_SIZE;//resto do bloco inicial

                System.out.println("[FSI.read2] Initial block: " + initialBlock);
                System.out.println("[FSI.read2] Initial offset: " + initialOffset);

                int p1 = (int) (double) (limit / Disk.BLOCk_SIZE);
                System.out.println(p1);

                int finalBlock = blocos.get(p1);//bloco final
                int finalOffset = limit % Disk.BLOCk_SIZE;//resto do bloco final

                System.out.println("[FSI.read2] Final block: " + finalBlock);
                System.out.println("[FSI.read2] Final offset: " + finalOffset);

                byte[] arquivo = new byte[limit - offset];//cria um array de bytes com o tamanho total

                int pos = 0;//posição no array de bytes
                
                if(initialBlock == finalBlock){//se o bloco inicial for igual ao bloco final
                    byte[] parteArchive = disk.read(initialBlock);//lê o bloco
                    System.arraycopy(parteArchive, initialOffset, arquivo, 0, finalOffset - initialOffset);//copia o bloco para o array de bytes
                    return arquivo;
                }

                int bloco = initialBlock;
                
                do{
                    byte[] parteArchive = disk.read(bloco);//lê o bloco
                    if(bloco == initialBlock){//se o bloco for o bloco inicial
                        System.arraycopy(parteArchive, initialOffset, arquivo, pos, Disk.BLOCk_SIZE - initialOffset);//copia o bloco para o array de bytes
                        pos += Disk.BLOCk_SIZE - initialOffset;//atualiza a posição
                    } else if(bloco == finalBlock){//se o bloco for o bloco final
                        System.arraycopy(parteArchive, 0, arquivo, pos, finalOffset);//copia o bloco para o array de bytes
                        break;
                    } else {
                        System.arraycopy(parteArchive, 0, arquivo, pos, Disk.BLOCk_SIZE);//copia o bloco para o array de bytes
                        pos += Disk.BLOCk_SIZE;//atualiza a posição
                    }
                    bloco = FAT.get(bloco);//pega o proximo bloco
                } while (bloco > 1);//repete até acabar os blocos

                return arquivo;
            }
        }
        return null;
    }

    /**
     * @deprecated use FileSystemImplementation.read() instead
     * @param fileName nome do arquivo
     * @return byte[] do arquivo ou null se n tiver aqruivo
     */
    @Deprecated
    public byte[] read2(String fileName) {
        fileName = formatFileName(fileName);//formata o nome do arquivo
        for(Archive archive : this.archives){//procura o arquivo
            if(archive.getName().equals(fileName)){
                System.out.println("[FSI.read] Archive found: " + archive.getName());
                int nextBlock = archive.getPos();//pega o bloco inicial do arquivo
                int currentBlock = nextBlock;
                int blocks = 0;//contador de blocos
                while(nextBlock > 1) {
                    blocks ++;//incrementa o contador de blocos
                    nextBlock = FAT.get(nextBlock);//pega o proximo bloco
                }//repete até acabar os blocos
                System.out.println("[FSI.read] Necessary blocks: " + blocks);
                byte[] data = new byte[blocks * Disk.BLOCk_SIZE];//cria um array de bytes com o tamanho total dos blocos
                int destPos = 0;//posição de destino no array de bytes
                do{
                    //System.out.println("[FSI.read] Reading block: " + nextBlock);
                    byte[] parteArchive = disk.read(currentBlock);//lê o bloco
                    System.arraycopy(parteArchive, 0, data, destPos, parteArchive.length);//copia o bloco para o array de bytes
                    destPos += Disk.BLOCk_SIZE;//atualiza a posição de destino
                    currentBlock = FAT.get(currentBlock);//pega o proximo bloco
                } while(currentBlock > 1);//repete até acabar os blocos
                System.out.println(data.length);

                return data;
            }
        }
        return null;
    }
}
