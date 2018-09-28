package myzip;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

// contains a data structure for writing to a file
public class DataBuf implements Serializable {

	private int bufSizeZip;// compressed data size
	private int bufSizeNoZip;// size of uncompressed data
	private String ext;// extension of the source file
	public byte[] byteBuf;// contains compressed data

	private HashMap<Byte, Integer> codeMapLight = new HashMap<Byte, Integer>(); // contains value-weight

	public DataBuf(int bufSizeZip, int bufSizeNoZip, String ext) {
		byteBuf = new byte[bufSizeZip];
		Arrays.fill(byteBuf, (byte) 0);
		this.bufSizeZip = bufSizeZip;
		this.bufSizeNoZip = bufSizeNoZip;
		this.ext = ext;
	}

	public int getBufSizeNoZip() {
		return bufSizeNoZip;
	}

	public int getBufSizeZip() {
		return bufSizeZip;
	}

	public String getExt() {
		return ext;
	}

	public HashMap<Byte, Integer> getcodeMapLight() {
		return codeMapLight;
	}

	public void putcodeMapLight(byte Value, int Weight) {
		codeMapLight.put(Value, Weight);
	}

}
