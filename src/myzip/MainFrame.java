package myzip;

import static myzip.Lib.readObjectFromFile;
import static myzip.Lib.writeObjectToFile;
import static myzip.Lib.writeToFileFromBuf;
import static myzip.Lib.readFullFileToBuf;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import myzip.Haffman.Node;

class MainFrame extends JFrame {

	// constructor
	public MainFrame(String title) {

		// define GUI components
		setMinimumSize(new Dimension(400, 300));
		setLocationByPlatform(true);
		setBounds(100, 100, 450, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 15, 125, 15, 125, 15 };
		gridBagLayout.rowHeights = new int[] { 15, 30, 15, 20, 30, 15, 20, 40, 15 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);
		setTitle(title);

		JButton bArchive = new JButton("Добавить в архив...");
		bArchive.setMargin(new Insets(2, 5, 2, 5));
		bArchive.setMinimumSize(new Dimension(125, 30));
		bArchive.setPreferredSize(new Dimension(125, 30));
		GridBagConstraints gbc_bArchive = new GridBagConstraints();
		gbc_bArchive.fill = GridBagConstraints.HORIZONTAL;
		gbc_bArchive.gridx = 1;
		gbc_bArchive.gridy = 1;
		getContentPane().add(bArchive, gbc_bArchive);

		JButton bExtract = new JButton("Извлечь из архива...");
		bExtract.setMargin(new Insets(2, 5, 2, 5));
		bExtract.setToolTipText("Извлечь файл из архива...");
		bExtract.setMinimumSize(new Dimension(125, 30));
		bExtract.setPreferredSize(new Dimension(125, 30));
		GridBagConstraints gbc_bExtract = new GridBagConstraints();
		gbc_bExtract.fill = GridBagConstraints.HORIZONTAL;
		gbc_bExtract.gridx = 3;
		gbc_bExtract.gridy = 1;
		getContentPane().add(bExtract, gbc_bExtract);

		JLabel lbProgress = new JLabel("Выполнение задачи");
		GridBagConstraints gbc_lbProgress = new GridBagConstraints();
		gbc_lbProgress.gridwidth = 3;
		gbc_lbProgress.anchor = GridBagConstraints.WEST;
		gbc_lbProgress.gridx = 1;
		gbc_lbProgress.gridy = 3;
		getContentPane().add(lbProgress, gbc_lbProgress);

		JProgressBar progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setValue(65);
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.fill = GridBagConstraints.BOTH;
		gbc_progressBar.gridwidth = 3;
		gbc_progressBar.gridx = 1;
		gbc_progressBar.gridy = 4;
		getContentPane().add(progressBar, gbc_progressBar);

		JLabel lbHistory = new JLabel("История");
		GridBagConstraints gbc_lbHistory = new GridBagConstraints();
		gbc_lbHistory.anchor = GridBagConstraints.WEST;
		gbc_lbHistory.gridx = 1;
		gbc_lbHistory.gridy = 6;
		getContentPane().add(lbHistory, gbc_lbHistory);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 7;
		getContentPane().add(scrollPane, gbc_scrollPane);

		JTextArea taHistory = new JTextArea();
		scrollPane.setViewportView(taHistory);

		// event handling
		bArchive.addActionListener(e -> {

			FileDialog fd = new FileDialog(this, "Выберите файл для сжатия", FileDialog.LOAD);
			fd.setVisible(true);
			if (fd.getFile() == null) {
				return;
			}
			Haffman fileArchive = new Haffman(fd);
			fileArchive.toArchive(taHistory);
		});

		bExtract.addActionListener(e -> {
			FileDialog fd = new FileDialog(this, "Выберите файл для извлечения из архива", FileDialog.LOAD);
			fd.setVisible(true);
			if (fd.getFile() == null) {
				return;
			}
			Haffman fileExtract = new Haffman(fd);
			try {
				fileExtract.toExtract(taHistory);
			} catch (ClassNotFoundException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
	}

}
