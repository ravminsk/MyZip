package myzip;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Lib {

	public static byte[] readFromFileToBuf(String fileName) {
		try (DataInputStream dis = new DataInputStream(new FileInputStream(new File(fileName)));) {
			System.out.println("чтение файла " + fileName);
			return dis.readAllBytes();
		} catch (IOException e) {
			System.out.println("ошибка чтения файла " + fileName);
			e.printStackTrace();
			return null;
		}
	}

	public static DataBuf readObjectFromFile(String fileName) {
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(fileName)));) {
			System.out.println("чтение из файла " + fileName);
			return (DataBuf) ois.readObject();
		} catch (IOException e) {
			System.out.println("Файл поврежден или имеет неизвестный формат" + fileName);
			e.printStackTrace();
		} catch (ClassNotFoundException e1) {
			System.out.println("не найден класс DataBuf");
			e1.printStackTrace();
		}
		return null;

	}

	public static boolean writeToFileFromBuf(String fileName, byte[] outBuf) {
		if (outBuf == null) {
			return false;
		}
		try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(fileName)));) {
			System.out.println("запись в файл " + fileName);
			dos.write(outBuf);
			dos.flush();
			dos.close();
			return true;
		} catch (IOException e) {
			System.out.println("ошибка чтения файла " + fileName);
			e.printStackTrace();
			return false;
		}
	}

	public static boolean writeObjectToFile(String fileName, DataBuf dataToWrite) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(fileName)));
			oos.writeObject(dataToWrite);
			oos.flush();
			oos.close();
			System.out.println("запись в файл " + fileName);
			return true;
		} catch (IOException e) {
			System.out.println("ошибка чтения файла " + fileName);
			e.printStackTrace();
			return false;
		}

	}
}
