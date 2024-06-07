package com.so.filesystem.test;

import javax.swing.*;

import com.so.filesystem.model.Archive;
import com.so.filesystem.FileSystemImplementation;
import com.so.filesystem.util.StringUtils;

import java.awt.*;
import java.io.*;
import java.util.List;

public class MainFrame extends JFrame {
    private final JPanel listPanel;
    private final List<Archive> archives;
    private final FileSystemImplementation fsi;
    private final JLabel label;
    private Integer freeSpace;

    public MainFrame(FileSystemImplementation fis) {
        this.fsi = fis;
        setTitle("Lista de Arquivos");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        freeSpace = fis.freeSpace();
        label = new JLabel();
        label.setText("Espaço livre: " + StringUtils.formatFreeSpace(freeSpace));

        archives = fis.getArchives();

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        updateListPanel();

        JScrollPane scrollPane = new JScrollPane(listPanel);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

        JButton button = getButton();
        add(button, BorderLayout.SOUTH);
        add(label, BorderLayout.NORTH);
    }

    private JButton getButton() {
        JButton button = new JButton("Adicionar arquivo");
        button.addActionListener(
                e -> {
                    File file = chooseFile();
                    System.out.println(file.length());
                    byte[] data = new byte[(int) file.length()];
                    try(FileInputStream inp = new FileInputStream(file)){
                        inp.read(data);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    System.out.println(file.getName());
                    fsi.create(file.getName(), data);
                    updateFreeSpace();
                    updateListPanel();
                    listPanel.revalidate();
                    listPanel.repaint();
                }
        );
        return button;
    }

    private void updateFreeSpace(){
        freeSpace = fsi.freeSpace();
        label.setText("Espaço livre: " + StringUtils.formatFreeSpace(freeSpace));
    }

    private void updateListPanel() {
        listPanel.removeAll();
        for (Archive archive : archives) {
            ArchivePanel archivePanel = new ArchivePanel(archive,
                e -> {
                    byte[] data = fsi.read(archive.getName()+archive.getExt(), 0, -1);
                    saveFile(data, archive.getName()+archive.getExt());
                    JOptionPane.showMessageDialog(MainFrame.this, "Arquivo salvo: " + archive.getName()+archive.getExt());
                },
                e -> {
                    File file = chooseFile();
                    byte[] data = new byte[(int)file.length()];
                    try(FileInputStream fis = new FileInputStream(file)){
                        fis.read(data);
                    } catch (Exception err){
                        err.printStackTrace();
                    }
                    fsi.append(archive.getName(), data);
                    updateFreeSpace();
                    updateListPanel();
                    listPanel.revalidate();
                    listPanel.repaint();
                },
                e -> {
                    fsi.remove(archive.getName());
                    updateFreeSpace();
                    updateListPanel();
                    listPanel.revalidate();
                    listPanel.repaint();
                    JOptionPane.showMessageDialog(MainFrame.this, "Arquivo excluído: " + archive);
                }
            );
            listPanel.add(archivePanel);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    public static void main(String[] args) {
        FileSystemImplementation fsi = new FileSystemImplementation();
        SwingUtilities.invokeLater(() -> new MainFrame(fsi).setVisible(true));
    }

    public static File chooseFile(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione 1 arquivo");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);;
        fileChooser.showOpenDialog(null);
        return fileChooser.getSelectedFile();
    }

    public static void saveFile(byte[] data, String filename){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar arquivo");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setName(filename);
        fileChooser.showSaveDialog(null);
        File file = fileChooser.getSelectedFile();
        try(FileOutputStream fos = new FileOutputStream(file)){
            fos.write(data);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
