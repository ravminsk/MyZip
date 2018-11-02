package myzip;

import java.awt.FileDialog;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.JTextArea;

import myzip.Haffman.Node;

public class Haffman implements Serializable{

	class ObjBuf implements Serializable{
		private int sizeNoZip = 0;
		byte[] buf = null;

		public int getSizeNoZip() {
			return sizeNoZip;
		}

		public void setSizeNoZip(int sizeNoZip) {
			this.sizeNoZip = sizeNoZip;
		}
	}

	// class to implement a binary tree
	class Node implements Comparable<Node> {
		private int codeInt = 0; // the value of the code as a number
		private int codeLengthBit = 0; // length of the code in bits
		private String codeStr = ""; // code value in string form
		public Node node0 = null; // reference to the parent node 0
		public Node node1 = null; // reference to the parent node 1
		private byte value = 0; // encoded character
		private int weight = 0; // character weight

		// constructor for the initial character table
		public Node(byte value, int weight) {
			super();
			this.value = value;
			this.weight = weight;
		}

		// constructor for building temporary nodes of a tree
		public Node(Node n0, Node n1) {
			super();
			this.weight = n0.getWeight() + n1.getWeight();
			node0 = n0;
			node1 = n1;
		}

		// to sort the List by the weight of the nodes
		@Override
		public int compareTo(Node o) {
			return this.getWeight() - o.getWeight();
		}

		// when adding a new node it traverses all leaves and adds 0 or 1 to the code
		// (recursion)
		// count the length of the code
		public void createCode(Node pNode, String bitcodea) {
			if (pNode.node0 != null) {
				pNode.node0.createCode(pNode.node0, bitcodea);
			}
			if (pNode.node1 != null) {
				pNode.node1.createCode(pNode.node1, bitcodea);
			}
			if ((pNode.node0 == null) && (pNode.node1 == null)) {
				this.codeStr = bitcodea + this.codeStr;
				this.codeLengthBit++;
				this.codeInt = Integer.parseInt(this.getCodeStr(), 2);
			}

		}

		public int getCodeInt() {
			return codeInt;
		}

		public int getCodeLengthBit() {
			return codeLengthBit;
		}

		public String getCodeStr() {
			return codeStr;
		}

		public byte getValue() {
			return value;
		}

		public int getWeight() {
			return weight;
		}

		@Override
		public String toString() {
			return value + "(" + (char) value + ") " + "	code=" + codeStr + "\n";
		}

	}

	private String fileDir;
	private String fileExt;
	private String fileFullPath;
	private String fileName;
	private final int MAX_BUF_SIZE = 1000000;

	public Haffman(FileDialog fd) {
		this.fileDir = fd.getDirectory();
		this.fileName = fd.getFile().replaceFirst("[.][^.]+$", "");
		this.fileExt = fd.getFile().substring(fileName.length() + 1);
		this.fileFullPath = fileDir + fileName + "." + fileExt;
	}

	// -------------------------------------------------------------------------------------------
	private byte[] codeBuffer(byte[] inBuf, HashMap<Byte, Node> codeMap) {

		int bufSizeZip = 0;
		byte[] outBuf = null;

		for (byte key : inBuf) {
			bufSizeZip += codeMap.get(key).codeLengthBit;
		}

		if ((bufSizeZip % 8) != 0) {
			bufSizeZip = bufSizeZip / 8 + 1;// encoded data size
		} else {
			bufSizeZip = bufSizeZip / 8; // encoded data size
		}
		outBuf = new byte[bufSizeZip];

		int[] bitMask = new int[Integer.SIZE];
		for (int i = 0; i < Integer.SIZE; i++) {
			bitMask[i] = 1 << (i);
		}

		int i8 = 0, i32 = 0, outBufIndex = 0;
		for (byte inBufIndex : inBuf) {
			Node codeNode = codeMap.get(inBufIndex);
			int codeInt = codeNode.getCodeInt();
			int codeLength = codeNode.getCodeLengthBit();
			for (i32 = codeLength - 1; i32 >= 0; i32--) {
				if ((bitMask[i32] & codeInt) != 0) {
					outBuf[outBufIndex] = (byte) (outBuf[outBufIndex] | 1);
				}
				i8++;
				if (i8 == 8) {
					i8 = 0;
					outBufIndex++;
					if (outBufIndex == bufSizeZip) {
						return outBuf;
					}
				}
				outBuf[outBufIndex] = (byte) (outBuf[outBufIndex] << 1);
			}
		}
		outBuf[outBufIndex] = (byte) (outBuf[outBufIndex] << (7 - i8));
		return outBuf;
	}

	private HashMap<Byte, Node> createCodeMap(HashMap<Byte, Integer> charsFreqMap) {
		// ListNode - to form a binary tree with temporary nodes
		List<Node> listNode = new ArrayList<Node>();
		charsFreqMap.forEach((byteValue, intWeight) -> {
			listNode.add(new Node(byteValue, intWeight));
		});
		// copy all references to the leaves of the tree in ListNodecode
		// there will be remember the codes of letters
		List<Node> listNodeCode = new ArrayList<Node>();
		listNodeCode.addAll(listNode);

		// Formation of codes of letters. There will be 1 node In listNode
		// listNode is not used in the future
		while (listNode.size() > 1) {
			Collections.sort(listNode);
			Node tmpNode = new Node(listNode.get(0), listNode.get(1));
			tmpNode.node0.createCode(tmpNode.node0, "0");
			tmpNode.node1.createCode(tmpNode.node1, "1");
			listNode.add(tmpNode);
			listNode.remove(1);
			listNode.remove(0);
		}
		// To quickly find the codes, copy the ListNodecode to Map
		// ListNodecode does not use later
		HashMap<Byte, Node> codeMap = new HashMap<Byte, Node>();
		listNodeCode.forEach(tmp -> codeMap.put(tmp.getValue(), tmp));

		return codeMap;
	}

	private void fillFreqTable(byte[] inBuf, HashMap<Byte, Integer> charsMap) {
		for (Byte b : inBuf) {
			charsMap.putIfAbsent(b, 0);
			charsMap.put(b, charsMap.get(b) + 1);
		}
	}

	public void toArchive(JTextArea taHistory) {

		try {
			// fill frequency table
			DataInputStream streamForCountData = new DataInputStream(new FileInputStream(new File(this.fileFullPath)));
			int size = Math.min(streamForCountData.available(), MAX_BUF_SIZE);
			HashMap<Byte, Integer> charsFreqMap = new HashMap<Byte, Integer>();
			while (size > 0) {
				byte[] inBuf = new byte[size];
				streamForCountData.read(inBuf);
				fillFreqTable(inBuf, charsFreqMap);
				size = Math.min(streamForCountData.available(), MAX_BUF_SIZE);
			}
			streamForCountData.close();

			// write frequency table and fileExt in a file
			ObjectOutputStream streamOutFile = 	new ObjectOutputStream(
												new FileOutputStream(new File(this.fileDir + this.fileName + ".myz")));
			streamOutFile.writeObject(this.fileExt);
			streamOutFile.writeObject(charsFreqMap);

			// create code table from frequency table)
			HashMap<Byte, Node> codeMap = createCodeMap(charsFreqMap);

			// code data and write in a file
			DataInputStream streamInData = new DataInputStream(new FileInputStream(new File(this.fileFullPath)));

			size = Math.min(streamInData.available(), MAX_BUF_SIZE);
			while (size > 0) {
				byte[] inBuf = new byte[size];
				streamInData.read(inBuf);
				taHistory.append("in " + inBuf.length + "   ");// for debug
				ObjBuf outObjBuf = new ObjBuf();
				outObjBuf.setSizeNoZip(size);
				outObjBuf.buf = codeBuffer(inBuf, codeMap);
				streamOutFile.writeObject(outObjBuf);
				taHistory.append("out " + outObjBuf.buf.length + "\n");// for debug
				size = Math.min(streamInData.available(), MAX_BUF_SIZE);
			}
			streamInData.close();
			streamOutFile.flush();
			streamOutFile.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void toExtract(JTextArea taHistory) {
		taHistory.append("Выбран файл для извлечения: " + fileFullPath + "\n");

		try {
			ObjectInputStream streamInFile = new ObjectInputStream(new FileInputStream(new File(fileFullPath)));

			// read file .ext and charsFrequency table
			this.fileExt = (String) streamInFile.readObject();
			@SuppressWarnings("unchecked")
			HashMap<Byte, Integer> charsFreqMap = (HashMap<Byte, Integer>) streamInFile.readObject();

			// create code table from frequency table)
			HashMap<Byte, Node> codeMap = createCodeMap(charsFreqMap);

			// decode data and write in a file
			DataOutputStream streamOutFile = new DataOutputStream(new FileOutputStream
																 (new File(	this.fileDir + 
																			this.fileName + "1"+	
																			this.fileExt)));
			ObjBuf inObjBuf= new ObjBuf();
			byte[] outBuf = null;
			do {
				inObjBuf = (ObjBuf) streamInFile.readObject();
				if (inObjBuf.buf != null) {
					outBuf = deCodeBuffer(inObjBuf.sizeNoZip, inObjBuf.buf,codeMap);
					streamOutFile.write(outBuf);
				}
			} while (inObjBuf.sizeNoZip==MAX_BUF_SIZE);

			streamOutFile.flush();
			streamOutFile.close();
			streamInFile.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	
	private byte[] deCodeBuffer(int sizeNoZip, byte[] inBuf, HashMap<Byte, Node> codeMap) {

		byte[] outBuf = new byte[sizeNoZip];
		int i8 = 0, outBufIndex = 0;
		String codedStr = "";
		for (byte byteInBuf : inBuf) {
			for (i8 = 0; i8 < 8; i8++) {
				if (outBufIndex == outBuf.length) {
					return outBuf;
				}
				if ((byteInBuf & 0b10000000) == 0) {
					codedStr = codedStr + "0";
				} else {
					codedStr = codedStr + "1";
				}
			
				if (codeMap.containsKey(codedStr)) {
					outBuf[outBufIndex] = codeMap.get(codedStr).getValue();
					outBufIndex++;
					codedStr = "";
				}
				byteInBuf = (byte) (byteInBuf << 1);
			}
		}
		return outBuf;
	}
}
