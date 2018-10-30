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
			taHistory.append("Выбран файл для сжатия: " + fd.getDirectory() + fd.getFile() + "\n");

			long start = System.currentTimeMillis();// for debugging

			String fileName = fd.getFile().replaceFirst("[.][^.]+$", "");
			String fileExt = fd.getFile().substring(fileName.length() + 1);
			int maxSize =65536; // inBuf size
			ObjectOutputStream oos = null;
			DataInputStream dis = null;
			DataBufHeader dbHeader = new DataBufHeader(maxSize, maxSize, fileExt);
			byte[] inBuf = null;
			byte[] outBuf = null;

			try {
				oos = new ObjectOutputStream(new FileOutputStream(new File(fd.getDirectory() + "test.myz")));
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			// fill frequency table
			try {
				dis = new DataInputStream(new FileInputStream(new File(fd.getDirectory() + fd.getFile())));
				do {
					if (dis.available() < maxSize) {
						inBuf = new byte[dis.available()];
					} else {
						inBuf = new byte[maxSize];
					}
					dis.read(inBuf);
					Haffman.fillFreqTable(inBuf, dbHeader.charsFreqMap);
				} while (dis.available() > 0);
				List<Node> listNodeCode = createLiteraCode(dbHeader.charsFreqMap);
				dis.close();

			} catch (IOException e2) {
				e2.printStackTrace();
			}
			// write freq table in a file
			try {
				oos.writeObject(dbHeader);
			} catch (IOException e3) {
				e3.printStackTrace();
			}
			// code data and write in a file
			try {
				dis = new DataInputStream(new FileInputStream(new File(fd.getDirectory() + fd.getFile())));
				do {
					if (dis.available() < maxSize) {
						inBuf = new byte[dis.available()];
					} else {
						inBuf = new byte[maxSize];
					}
					dis.read(inBuf);
					taHistory.append("in "+inBuf.length+"\n");
					outBuf = new Haffman().codeBuffer(inBuf, dbHeader.charsFreqMap);
					oos.write(outBuf);
					taHistory.append("out "+outBuf.length+"\n");

				} while (dis.available() > 0);
				dis.close();
				oos.flush();
				oos.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}

			// read part of file, coding, write part file
			/*
			 * try { dis = new DataInputStream(new FileInputStream(new
			 * File(fd.getDirectory() + fd.getFile()))); while (dis.read(inBuf) != -1) { //
			 * reading part of file
			 * 
			 * // coding part of file
			 * 
			 * // Writing part of file
			 * 
			 * } dis.close(); oos.flush(); oos.close(); } catch (IOException e2) {
			 * e2.printStackTrace(); }
			 */

			taHistory.append(maxSize + "  " + Long.toString(System.currentTimeMillis() - start) + "ms \n");

			/*
			 * DataBuf outObject = new Haffman().code(inBuf, fileExt); boolean result =
			 * writeObjectToFile(fd.getDirectory() + fileName + ".myz", outObject);
			 * 
			 * if (result == true) { taHistory.append("Создан архив " + fd.getDirectory() +
			 * fileName + ".myz" + "\n"); taHistory.append("Время " +
			 * (System.currentTimeMillis() - start) + "ms \n"); } else {
			 * taHistory.append("Ошибка при записи файла \n"); }
			 */

			/*
			 * byte[] inBuf=null;; try { inBuf = readFullFileToBuf(fd.getDirectory() +
			 * fd.getFile()); } catch (IOException e1) {
			 * taHistory.append("ошибка чтения файла " + fd.getFile()+ "\n");
			 * e1.printStackTrace(); } DataBuf outObject = new Haffman().code(inBuf,
			 * fileExt); boolean result = writeObjectToFile(fd.getDirectory() + fileName +
			 * ".myz", outObject);
			 * 
			 * if (result == true) { taHistory.append("Создан архив " + fd.getDirectory() +
			 * fileName + ".myz" + "\n"); taHistory.append("Время " +
			 * (System.currentTimeMillis() - start) + "ms \n"); } else {
			 * taHistory.append("Ошибка при записи файла \n"); }
			 */

		});

		bExtract.addActionListener(e -> {
			FileDialog fd = new FileDialog(this, "Выберите файл для извлечения из архива", FileDialog.LOAD);
			fd.setVisible(true);
			if (fd.getFile() == null) {
				return;
			}

			taHistory.append("Выбран файл для извлечения: " + fd.getDirectory() + fd.getFile() + "\n");
			String fileName = fd.getFile().replaceFirst("[.][^.]+$", "");
			long start = System.currentTimeMillis();
			boolean result = false;
			DataBuf inObject = null;

			try {
				inObject = readObjectFromFile(fd.getDirectory() + fd.getFile());
			} catch (ClassNotFoundException | IOException e1) {
				taHistory.append("Файл " + fd.getFile() + " поврежден или имеет неизвестный формат \n");
				e1.printStackTrace();
				return;
			}

			byte[] outBuf = new Haffman().deCode(inObject);
			result = writeToFileFromBuf(fd.getDirectory() + fileName + "." + inObject.getExt(), outBuf);
			if (result == true) {
				taHistory.append("Извлечен файл " + fd.getDirectory() + fileName + "." + inObject.getExt() + "\n");
				taHistory.append("Время " + (System.currentTimeMillis() - start) + "ms \n");
			} else {
				taHistory.append("Файл поврежден или имеет неизвестный формат" + "\n");
			}

		});

	}

}
