package myzip;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

// содержит структуру данных для записи в файл
public class DataBuf implements Serializable {

	private int bufSizeZip;// размер сжатых данных
	private int bufSizeNoZip;// размер неcжатых данных
	private String ext;//расширение исходного файла
	public byte[] byteBuf;// содержит данные в сжатом виде
	
	private HashMap<Byte, Integer> kodMapLight = new HashMap<Byte, Integer>();	// содержит символ-вес

	public DataBuf(int bufSizeZip, int bufSizeNoZip,String ext) {
		byteBuf = new byte[bufSizeZip];
		Arrays.fill(byteBuf, (byte) 0);
		this.bufSizeZip = bufSizeZip;
		this.bufSizeNoZip = bufSizeNoZip;
		this.ext=ext;
	}

	public int getBufSizeNoZip() {	return bufSizeNoZip;}
	public int getBufSizeZip() {	return bufSizeZip;}
	public String getExt() {	    return ext;}
	public HashMap<Byte, Integer> getKodMapLight() {return kodMapLight;}
	public void putKodMapLight(byte Value, int Weight) {
		kodMapLight.put(Value, Weight);
	}

}
