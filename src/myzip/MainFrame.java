package myzip;

import static myzip.Lib.readFromFileToBuf;
import static myzip.Lib.readObjectFromFile;
import static myzip.Lib.writeObjectToFile;
import static myzip.Lib.writeToFileFromBuf;

import java.awt.FileDialog;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

class MainFrame extends JFrame {

	private static final int DEFAULT_WIDTH = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 3);
	private static final int DEFAULT_HEIGHT = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 3);

	// элементы основного окна
	JPanel panel3 = new JPanel();
	JButton bArchive = new JButton(" Добавить в архив... ");
	JButton bExtract = new JButton(" Извлечь из архива... ");

	JPanel panel4 = new JPanel();
	public JTextArea txtArea = new JTextArea(6, 50);

	JPanel panel5 = new JPanel();

	// конструктор для инициализации окна
	public MainFrame(String title) {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle(title);
		setLocationByPlatform(true);

		panel3.add(bArchive);
		panel3.add(bExtract);

		panel4.add(new JScrollPane(txtArea));

		panel5.setLayout(new GridLayout(2, 1));
		panel5.add(panel3);
		panel5.add(panel4);

		add(panel5);
		pack();

		// обработка нажатий кнопок
		bArchive.addActionListener(e -> {
			FileDialog fd = new FileDialog(this, "Выберите файл для сжатия", FileDialog.LOAD);
			fd.setVisible(true);
			if (fd.getFile() == null)
				return;

			String fileName = fd.getFile().replaceFirst("[.][^.]+$", "");
			String fileExt = fd.getFile().substring(fileName.length() + 1);
			txtArea.append("Выбран файл для сжатия: " + fd.getDirectory() + fd.getFile() + "\n");
			long start = System.currentTimeMillis();
			byte[] inBuf = readFromFileToBuf(fd.getDirectory() + fd.getFile());
			DataBuf outObject = new Haffman().code(inBuf, fileExt);
			boolean result = writeObjectToFile(fd.getDirectory() + fileName + ".myz", outObject);

			if (result == true) {
				txtArea.append("Записан файл " + fd.getDirectory() + fileName + ".myz" + "\n");
				txtArea.append("Время " + (System.currentTimeMillis() - start) + "ms \n");
			} else {
				txtArea.append("Ошибка при записи файла");
			}
		});

		bExtract.addActionListener(e -> {
			FileDialog fd = new FileDialog(this, "Выберите файл для извлечения из архива", FileDialog.LOAD);
			fd.setVisible(true);
			if (fd.getFile() == null)
				return;

			txtArea.append("Выбран файл для извлечения: " + fd.getDirectory() + fd.getFile() + "\n");
			String fileName = fd.getFile().replaceFirst("[.][^.]+$", "");
			long start = System.currentTimeMillis();
			boolean result = false;
			DataBuf inObject = readObjectFromFile(fd.getDirectory() + fd.getFile());
			byte[] outBuf = new Haffman().deCode(inObject);
			if ((outBuf != null)&&(inObject!=null)) {
				result = writeToFileFromBuf(fd.getDirectory() + fileName + "." + inObject.getExt(), outBuf);
			}

			if (result == true) {
				txtArea.append("Извлечен файл " + fd.getDirectory() + fileName + "." + inObject.getExt() + "\n");
				txtArea.append("Время " + (System.currentTimeMillis() - start) + "ms \n");
			} else {
				txtArea.append("Файл поврежден или имеет неизвестный формат" + "\n");
			}

		});

	}

}
