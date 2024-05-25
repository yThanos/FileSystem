package com.so.filesystem.test;

import javax.swing.*;

import com.so.filesystem.Archive;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private JPanel listPanel;
    private List<Archive> archives;

    public MainFrame() {
        setTitle("Lista de Arquivos");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Criar a lista de arquivos
        archives = new ArrayList<>();
        archives.add(new Archive("File1", "txt", 0, 100));
        archives.add(new Archive("File2", "jpg", 1, 200));
        archives.add(new Archive("File3", "pdf", 2, 300));

        // Configurar o painel de lista
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        updateListPanel();

        JScrollPane scrollPane = new JScrollPane(listPanel);

        // Configurar o layout do JFrame
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
    }

    private void updateListPanel() {
        listPanel.removeAll();
        for (Archive archive : archives) {
            ArchivePanel archivePanel = new ArchivePanel(archive,
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        archive.setName(archive.getName());
                        archive.setExt(archive.getExt());
                        archive.setPos(archive.getPos());
                        archive.setSize(archive.getSize());
                        JOptionPane.showMessageDialog(MainFrame.this, "Arquivo salvo: " + archive);
                    }
                },
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("Editando arquivo: " + archive);
                    }
                },
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        archives.remove(archive);
                        updateListPanel();
                        listPanel.revalidate();
                        listPanel.repaint();
                        JOptionPane.showMessageDialog(MainFrame.this, "Arquivo excluído: " + archive);
                    }
                }
            );
            archivePanel.setEditable(false);
            listPanel.add(archivePanel);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }    
}
