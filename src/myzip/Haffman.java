package myzip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Haffman {

	public DataBuf code(byte[] inBuf, String ext) {
		// подсчет частоты букв Map<буква,вес>
		HashMap<Byte, Integer> charsMap = new HashMap<Byte, Integer>();
		for (Byte b : inBuf) {
			charsMap.putIfAbsent(b, 0);
			charsMap.put(b, charsMap.get(b) + 1);
		}
		List<Node> listNodeKod = createLiteraCod(charsMap);

		Collections.sort(listNodeKod);
		System.out.println(listNodeKod.toString());//debugging
		int bufSizeNoZip = inBuf.length;
		int bufSizeZip = 0;
		for (Node tmpNode : listNodeKod) 
			bufSizeZip = bufSizeZip + tmpNode.getWeight() * tmpNode.getKodLegthBit();
		
		if ((bufSizeZip % 8) != 0)
			  bufSizeZip = bufSizeZip / 8 + 1;// размер закодированного текста
		else  bufSizeZip = bufSizeZip / 8; // размер закодированного текста

		DataBuf myDataBuf = new DataBuf(bufSizeZip, bufSizeNoZip, ext);
		// для быстрого поиска кодов копируем ListNodeKod в Map
		// ListNodeKod в дальнейшем не используем
		// заполняем лайт версию Map в объекте myDataBuf которую сохраним в сжатом файле
		HashMap<Byte, Node> kodMap = new HashMap<Byte, Node>();

		listNodeKod.forEach(tmp -> kodMap.put(tmp.getValue(), tmp));
		listNodeKod.forEach(tmp -> myDataBuf.putKodMapLight(tmp.getValue(), tmp.getWeight()));

		int kodMapLightSize = myDataBuf.getKodMapLight().size() * (Byte.BYTES + Integer.BYTES);
		System.out.println("размер сжатого файла " +
				(1372 + // заголовок сериализации ????? каждый раз разный..
				ext.length()+
				Integer.BYTES + 
				Integer.BYTES + 
				bufSizeZip + 
				kodMapLightSize) + " байт");

		int[] bitMask = new int[Integer.SIZE];
		for (int i = 0; i < Integer.SIZE; i++) {
			bitMask[i] = 1 << (i);
		}
		int i8 = 0, i32, outBufIndex = 0;
		for (byte inBufIndex : inBuf) {

			Node kodNode = kodMap.get(inBufIndex);
			int kodInt = kodNode.getKodInt();
			int kodLength = kodNode.getKodLegthBit();
			for (i32 = kodLength - 1; i32 >= 0; i32--) {
				if ((bitMask[i32] & kodInt) != 0) {
					myDataBuf.byteBuf[outBufIndex] = (byte) (myDataBuf.byteBuf[outBufIndex] | 1);
				}
				i8++;
				if (i8 == 8) {
					i8 = 0;
					outBufIndex++;
					if (outBufIndex == bufSizeZip)
						return myDataBuf;
				}
				myDataBuf.byteBuf[outBufIndex] = (byte) (myDataBuf.byteBuf[outBufIndex] << 1);
			}
		}
		myDataBuf.byteBuf[outBufIndex] = (byte) (myDataBuf.byteBuf[outBufIndex] << (7 - i8));
		return myDataBuf;
	}

	public byte[] deCode(DataBuf inObject) {
		byte[] outBuf = new byte[inObject.getBufSizeNoZip()];
		List<Node> listNodeKod = createLiteraCod(inObject.getKodMapLight());
		// для быстрого поиска по кодам копируем ListNodeKod в Map.
		// ListNodeKod в дальнейшем не используем
		HashMap<String, Node> kodMap = new HashMap<String, Node>();
		for (Node tmpNode : listNodeKod) {
			kodMap.putIfAbsent(tmpNode.getKodStr(), tmpNode);
		}

		int i8 = 0, outBufIndex = 0, kodSize = 1;
		String kodStr = "";
		for (byte byteInBuf : inObject.byteBuf) {

			for (i8 = 0; i8 < 8; i8++) {
				if (outBufIndex == outBuf.length)
					return outBuf;

				if ((byteInBuf & 0b10000000) == 0)
					kodStr = kodStr + "0";
				else
					kodStr = kodStr + "1";

				if (kodMap.containsKey(kodStr)) {
					outBuf[outBufIndex] = kodMap.get(kodStr).getValue();
					outBufIndex++;
					kodStr = "";
				}
				byteInBuf = (byte) (byteInBuf << 1);
			}
		}
		return outBuf;
	}

	public List<Node> createLiteraCod(HashMap<Byte, Integer> charsMap) {
		// ListNode - для формирования бинарного дерева с временными узлами
		List<Node> listNode = new ArrayList<Node>();
		charsMap.forEach((byteValue, intWeight) -> {
			listNode.add(new Node(byteValue, intWeight));
		});
		// ListNodeKod - копируем сюда все ссылки на листья дерева. здесь будут
		// запоминаться коды букв.
		List<Node> listNodeKod = new ArrayList<Node>();
		listNodeKod.addAll(listNode);

		// формирование кодов букв. в listNode в итоге останется 1 узел.
		// listNode в дальнейшем не используем
		while (listNode.size() > 1) {
			Collections.sort(listNode);
			Node tmpNode = new Node(listNode.get(0), listNode.get(1));
			tmpNode.node0.createKod(tmpNode.node0, "0");
			tmpNode.node1.createKod(tmpNode.node1, "1");
			listNode.add(tmpNode);
			listNode.remove(1);
			listNode.remove(0);
		}
		return listNodeKod;
	}

	// класс для реализации бинарного дерева
	class Node implements Comparable<Node> {
		private int kodInt = 0; // значение кода в виде числа
		private int kodLengthBit = 0; // длина кода в битах
		private String kodStr = ""; // значение кода в строковом виде (в релизе убрать)
		public Node node0 = null; // ссылка на родительский узел 0
		public Node node1 = null; // ссылка на родительский узел 1
		private byte value = 0; // кодируемый знак
		private int weight = 0; // вес знака

		// конструктор для начальной таблицы знаков
		public Node(byte value, int weight) {
			super();
			this.value = value;
			this.weight = weight;
		}

		// конструктор для построения временных узлов дерева
		public Node(Node n0, Node n1) {
			super();
			this.weight = n0.getWeight() + n1.getWeight();
			node0 = n0;
			node1 = n1;
		}

		// для сортировки List по весу узлов
		@Override
		public int compareTo(Node o) {
			return this.getWeight() - o.getWeight();
		}

		// при добавлении нового узла обходит все листья и добавляет 0 или 1 к коду
		// (рекурсия)
		// подсчитывет длину кода
		public void createKod(Node pNode, String bitKoda) {
			if (pNode.node0 != null)
				pNode.node0.createKod(pNode.node0, bitKoda);
			if (pNode.node1 != null)
				pNode.node1.createKod(pNode.node1, bitKoda);
			if ((pNode.node0 == null) && (pNode.node1 == null)) {
				this.kodStr = bitKoda + this.kodStr;
				this.kodLengthBit++;
				this.kodInt = Integer.parseInt(this.getKodStr(), 2);
			}

		}

		public String getKodStr() {	return kodStr;}
		public int getKodInt() {	return kodInt;}
		public int getKodLegthBit() {return kodLengthBit;}
		public byte getValue() {	return value;}
		public int getWeight() {	return weight;}

		@Override
		public String toString() {
			return value + "(" + (char) value + ") " + "	kod=" + kodStr + "\n";
		}

	}
}
