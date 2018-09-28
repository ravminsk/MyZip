package myzip;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Lib {

	public static byte[] readFromFileToBuf(String fileName) throws IOException {
		DataInputStream dis = new DataInputStream(new FileInputStream(new File(fileName)));
		// System.out.println("readFromFileToBuf read " + fileName); //for debugging
		return dis.readAllBytes();
	}

	public static DataBuf readObjectFromFile(String fileName) throws ClassNotFoundException, IOException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(fileName)));
		return (DataBuf) ois.readObject();
		//System.out.println("readObjectFromFile read " + fileName); //for debugging
	}

	public static boolean writeToFileFromBuf(String fileName, byte[] outBuf) {
		if (outBuf == null) {
			return false;
		}
		try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(fileName)));) {
			//System.out.println("запись в файл " + fileName);//for debugging
			dos.write(outBuf);
			dos.flush();
			dos.close();
			return true;
		} catch (IOException e) {
			//System.out.println("ошибка чтения файла " + fileName); // for debugging
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
			//System.out.println("запись в файл " + fileName);// for debugging
			return true;
		} catch (IOException e) {
			//System.out.println("ошибка чтения файла " + fileName);// for debugging
			e.printStackTrace();
			return false;
		}

	}
}
