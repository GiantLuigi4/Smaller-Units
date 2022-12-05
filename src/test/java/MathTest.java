import tfc.smallerunits.utils.math.Math1D;

import java.util.HashMap;
import java.util.TreeSet;

public class MathTest {
	public static void main(String[] args) {
		System.out.println("-- values --");
		int scl = 16;
		HashMap<Integer, Integer> counts = new HashMap<>();
		for (int i = -(scl * 2); i <= (scl * 2); i++) {
			String iStr = "" + i;
			while (iStr.length() < 3) iStr = " " + iStr;
			iStr = iStr.replace("-", " ");
			if (i < 0) {
				iStr = "-" + iStr.substring(1);
			}
//			if (i < 10) System.out.print(" ");
			System.out.print(iStr + ": ");
			int cnt;
			System.out.println(cnt = Math1D.getChunkOffset(i, scl));
			Integer i1 = counts.getOrDefault(cnt, null);
			if (i1 == null) counts.put(cnt, i1 = Integer.valueOf(0));
			counts.replace(cnt, i1 + 1);
		}
		TreeSet<Integer> set = new TreeSet<>(counts.keySet());
		System.out.println("-- counts --");
		for (Integer integer : set) {
			System.out.print(integer + ": ");
			System.out.println(counts.get(integer));
		}
	}
}
