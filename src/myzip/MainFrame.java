package myzip;

import static myzip.Lib.readFromFileToBuf;
import static myzip.Lib.readObjectFromFile;
import static myzip.Lib.writeObjectToFile;
import static myzip.Lib.writeToFileFromBuf;

import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

class MainFrame extends JFrame {

	private static final int DEFAULT_WIDTH = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 3);
	private static final int DEFAULT_HEIGHT = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 3);

	// define components of main frame
	
	JPanel panelButton = new JPanel();
	JButton bArchive = new JButton(" Добавить в архив... ");
	JButton bExtract = new JButton(" Извлечь из архива... ");

	JPanel paneTxtArea = new JPanel();
	public JTextArea txtArea = new JTextArea(6, 50);

	JPanel panelFrame = new JPanel();

	// constructor
	public MainFrame(String title) {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle(title);
		setLocationByPlatform(true);
		
		panelButton.setLayout(new FlowLayout(1,40,20));
		panelButton.add(bArchive);
		panelButton.add(bExtract);

		paneTxtArea.add(new JScrollPane(txtArea));

		panelFrame.setLayout(new GridLayout(2, 1));
		panelFrame.add(panelButton);
		panelFrame.add(paneTxtArea);

		add(panelFrame);
		pack();

		// event handling
		bArchive.addActionListener(e -> {
			FileDialog fd = new FileDialog(this, "Выберите файл для сжатия", FileDialog.LOAD);
			fd.setVisible(true);
			if (fd.getFile() == null) {
				return;
			}

			String fileName = fd.getFile().replaceFirst("[.][^.]+$", "");
			String fileExt = fd.getFile().substring(fileName.length() + 1);
			txtArea.append("Выбран файл для сжатия: " + fd.getDirectory() + fd.getFile() + "\n");
			long start = System.currentTimeMillis();
			byte[] inBuf=null;;
			try {
				inBuf = readFromFileToBuf(fd.getDirectory() + fd.getFile());
			} catch (IOException e1) {
				txtArea.append("ошибка чтения файла " + fd.getFile()+ "\n");
				e1.printStackTrace();
			}
			DataBuf outObject = new Haffman().code(inBuf, fileExt);
			boolean result = writeObjectToFile(fd.getDirectory() + fileName + ".myz", outObject);

			if (result == true) {
				txtArea.append("Создан архив " + fd.getDirectory() + fileName + ".myz" + "\n");
				txtArea.append("Время " + (System.currentTimeMillis() - start) + "ms \n");
			} else {
				txtArea.append("Ошибка при записи файла \n");
			}
		});

		bExtract.addActionListener(e -> {
			FileDialog fd = new FileDialog(this, "Выберите файл для извлечения из архива", FileDialog.LOAD);
			fd.setVisible(true);
			if (fd.getFile() == null) {
				return;
			}

			txtArea.append("Выбран файл для извлечения: " + fd.getDirectory() + fd.getFile() + "\n");
			String fileName = fd.getFile().replaceFirst("[.][^.]+$", "");
			long start = System.currentTimeMillis();
			boolean result = false;
			DataBuf inObject = null;
			
			try {
				inObject = readObjectFromFile(fd.getDirectory() + fd.getFile());
			} catch (ClassNotFoundException | IOException e1) {
				txtArea.append("Файл " + fd.getFile() + " поврежден или имеет неизвестный формат \n");
				e1.printStackTrace();
				return;
			}
			
			byte[] outBuf = new Haffman().deCode(inObject);
			result = writeToFileFromBuf(fd.getDirectory() + fileName + "." + inObject.getExt(), outBuf);
			if (result == true) {
				txtArea.append("Извлечен файл " + fd.getDirectory() + fileName + "." + inObject.getExt() + "\n");
				txtArea.append("Время " + (System.currentTimeMillis() - start) + "ms \n");
			} else {
				txtArea.append("Файл поврежден или имеет неизвестный формат" + "\n");
			}

		});

	}

}
