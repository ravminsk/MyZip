package myzip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Haffman {

	public DataBuf code(byte[] inBuf, String ext) {
		// Count the frequency of letters Map <letter, weight>
		HashMap<Byte, Integer> charsMap = new HashMap<Byte, Integer>();
		for (Byte b : inBuf) {
			charsMap.putIfAbsent(b, 0);
			charsMap.put(b, charsMap.get(b) + 1);
		}
		List<Node> listNodeCode = createLiteraCode(charsMap);

		Collections.sort(listNodeCode);
		//System.out.println(listNodeCode.toString());// for debugging
		int bufSizeNoZip = inBuf.length;
		int bufSizeZip = 0;
		for (Node tmpNode : listNodeCode) {
			bufSizeZip = bufSizeZip + tmpNode.getWeight() * tmpNode.getCodeLengthBit();
		}

		if ((bufSizeZip % 8) != 0) {
			bufSizeZip = bufSizeZip / 8 + 1;// encoded text size
		} else {
			bufSizeZip = bufSizeZip / 8; // encoded text size
		}

		DataBuf myDataBuf = new DataBuf(bufSizeZip, bufSizeNoZip, ext);
		// To quickly find the codes, copy the ListNodecode to Map
		// ListNodecode does not use later
		// fill the light version of the Map in the object myDataBuf.  It will save in a compressed file
		HashMap<Byte, Node> codeMap = new HashMap<Byte, Node>();

		listNodeCode.forEach(tmp -> codeMap.put(tmp.getValue(), tmp));
		listNodeCode.forEach(tmp -> myDataBuf.putcodeMapLight(tmp.getValue(), tmp.getWeight()));

		int codeMapLightSize = myDataBuf.getcodeMapLight().size() * (Byte.BYTES + Integer.BYTES);
		/*System.out.println("encoded file size " + 
				(1372 + 		//wrong header size value. !!!!!! 
				ext.length() +
				Integer.BYTES +
				Integer.BYTES +
				bufSizeZip +
				codeMapLightSize) + " bytes");*/   // You may not use it

		int[] bitMask = new int[Integer.SIZE];
		for (int i = 0; i < Integer.SIZE; i++) {
			bitMask[i] = 1 << (i);
		}
		int i8 = 0, i32, outBufIndex = 0;
		for (byte inBufIndex : inBuf) {
			Node codeNode = codeMap.get(inBufIndex);
			int codeInt = codeNode.getCodeInt();
			int codeLength = codeNode.getCodeLengthBit();
			for (i32 = codeLength - 1; i32 >= 0; i32--) {
				if ((bitMask[i32] & codeInt) != 0) {
					myDataBuf.byteBuf[outBufIndex] = (byte) (myDataBuf.byteBuf[outBufIndex] | 1);
				}
				i8++;
				if (i8 == 8) {
					i8 = 0;
					outBufIndex++;
					if (outBufIndex == bufSizeZip) {
						return myDataBuf;
					}
				}
				myDataBuf.byteBuf[outBufIndex] = (byte) (myDataBuf.byteBuf[outBufIndex] << 1);
			}
		}
		myDataBuf.byteBuf[outBufIndex] = (byte) (myDataBuf.byteBuf[outBufIndex] << (7 - i8));
		return myDataBuf;
	}

	public byte[] deCode(DataBuf inObject) {
		if (inObject == null) {
			return null;
		}
		byte[] outBuf = new byte[inObject.getBufSizeNoZip()];
		List<Node> listNodeCode = createLiteraCode(inObject.getcodeMapLight());
		// copy ListNodeCode in Map for fast searching by codStr  
		// ListNodeCode does not use later
		HashMap<String, Node> codeMap = new HashMap<String, Node>();
		for (Node tmpNode : listNodeCode) {
			codeMap.putIfAbsent(tmpNode.getCodeStr(), tmpNode);
		}

		int i8 = 0, outBufIndex = 0, codeSize = 1;
		String codedStr = "";
		for (byte byteInBuf : inObject.byteBuf) {

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

	public List<Node> createLiteraCode(HashMap<Byte, Integer> charsMap) {
		// ListNode - to form a binary tree with temporary nodes
		List<Node> listNode = new ArrayList<Node>();
		charsMap.forEach((byteValue, intWeight) -> {
			listNode.add(new Node(byteValue, intWeight));
		});
		// copy all references to the leaves of the tree in ListNodecode
		// there will be remember the codes of letters
		List<Node> listNodeCode = new ArrayList<Node>();
		listNodeCode.addAll(listNode);

		// Formation of codes of letters.  There will be 1 node In listNode 
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
		return listNodeCode;
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

		public String getCodeStr() {
			return codeStr;
		}

		public int getCodeInt() {
			return codeInt;
		}

		public int getCodeLengthBit() {
			return codeLengthBit;
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
}
