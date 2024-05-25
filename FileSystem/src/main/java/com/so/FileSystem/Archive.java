package com.so.filesystem;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import lombok.Data;

@Data
public class Archive {
    /**
     * Nome do arquivo, 8 bytes.
     */
    private String name;
    /**
     * Extensão do arquivo, 3 bytes.
     */
    private String ext;
    /**
     * Tamanho do arquivo, 4 bytes.
     */
    private Integer size;
    /**
     * Posição do primeiro bloco do arquivo no disco, 4 bytes.
     */
    private Integer pos;

    public Archive(String name, String ext, Integer size, Integer pos) {
        this.name = name;
        if(this.name.length() > 8){
            this.name = name.substring(0, 8);
        } else if (this.name.length() < 8){
            do{
                this.name += " ";
            } while(this.name.length() < 8);
        }
        this.ext = ext;
        if(this.ext.length() > 3){
            this.ext = ext.substring(0, 3);
        } else if(this.ext.length() < 3){
            do{
                this.ext += " ";
            } while(this.ext.length() < 3);
        }
        this.size = size;
        this.pos = pos;
    }

    /**
     * Converte o objeto Archive para um array de bytes.
     * @return byte array do objeto Archive para ser salvo no bloco 0
     */
    public byte[] toByteArray(){
        byte[] data = new byte[19];
        System.arraycopy(name.getBytes(StandardCharsets.ISO_8859_1), 0, data, 0, 8);
        System.arraycopy(ext.getBytes(StandardCharsets.ISO_8859_1), 0, data, 8, 3);
        System.arraycopy(intToBytes(size), 0, data, 11, 4);
        System.arraycopy(intToBytes(pos), 0, data, 15, 4);
        return data;
    }

    /**
     * Converte um inteiro para um array de bytes.
     * @param i inteiro a ser convertido.
     * @return array de bytes.
     */
    private static byte[] intToBytes(int i){
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(i);
        return bb.array();
    }

    /**
     * Cria um objeto Archive a partir do array de bytes pego do Disk.
     * @param data byte array com os dados do arquivo.
     * @return um objeto Archive. (nome, extensao, tamanho, posicao)
     */
    public static Archive fromByteArray(byte[] data){
        String name = new String(data, 0, 8, StandardCharsets.ISO_8859_1);
        String ext = new String(data, 8, 3, StandardCharsets.ISO_8859_1);
        int size = ByteBuffer.wrap(data, 11, 4).getInt();
        int pos = ByteBuffer.wrap(data, 15, 4).getInt();
        return new Archive(name, ext, size, pos);
    }

}
