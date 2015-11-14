import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeMap;

public class compression {
	static HashMap<String, String> specific = new HashMap<>();
	public static void compress(TreeMap<String,TermProperties> collection,HashSet<String> specificTerms, File file, File file2){
		StringBuffer mainString = new StringBuffer();
		long startCompressTime = System.currentTimeMillis();
		try {

			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			PrintWriter writer = new PrintWriter(file2, "UTF-8");
			ArrayList<String> words = new ArrayList<String>();
			int count = 0;
			for(Entry<String, TermProperties> entry: collection.entrySet()){
				if(count==0)
					raf.write(mainString.length());
				if(count<8){
					if(specificTerms.contains(entry.getKey())){
						String s = null;
						words.add(entry.getKey());
						byte [] g1 = gamma(entry.getValue().docFrequency);
						raf.write(g1);
						s +=g1.length+" ";
						HashMap<String, Integer> tempPostingFile= entry.getValue().postingFileInfo;
						int termFreq = 0;
						int invertedlist = 0;
						for( Entry<String, Integer> list:tempPostingFile.entrySet()){
							byte [] g = gamma(Integer.parseInt(list.getKey()));
							raf.write(g);
							invertedlist += g.length;
							termFreq += list.getValue();
							byte [] d = delta(list.getValue());
							raf.write(d);
							invertedlist += d.length;
						}
						System.out.println(entry.getKey()+" "+"Doc Frequency-"+entry.getValue().docFrequency+" "+"Term Frequency-"+termFreq+" inverted list length(in bytes)"+invertedlist);
					}else{
						words.add(entry.getKey());
						byte [] g1 = gamma(entry.getValue().docFrequency);
						raf.write(g1);
						HashMap<String, Integer> tempPostingFile= entry.getValue().postingFileInfo;
						for( Entry<String, Integer> list:tempPostingFile.entrySet()){
							byte [] g = gamma(Integer.parseInt(list.getKey()));
							raf.write(g);
							byte [] d = delta(list.getValue());
							raf.write(d);
						}
					}
					count++;
				}if(count==8){
					count = 0;
					String compress8 = dictionaryCompress(words);
					words = new ArrayList<String>();
					mainString.append(compress8);
				}
			}
			raf.close();
			writer.append(mainString);
			writer.close();
			System.out.println(file.getName()+" "+file.length()/1024+"kilobytes");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		long endcompressedTime = System.currentTimeMillis();
		long elapsedTime = (long) (endcompressedTime - startCompressTime);
		System.out.println("Time taken to create"+"\t"+file.getName()+"\t"+elapsedTime+" ms");
	}

	public static String dictionaryCompress(ArrayList<String> words){
		String [] words_Array = new String[words.size()];
		words_Array =  (String[]) words.toArray(words_Array);
		String s1 = frontcoding(words_Array,0);
		String s2 = frontcoding(words_Array,4);
		return s1+s2;
	}

	public static String frontcoding(String s[], int a) {
		int i = 0, min_len;
		String s1 = new String();
		min_len = min(s[a].length(), s[a+1].length(), s[a+2].length(), s[a+3].length());
		while (i < min_len) {
			if (s[a].charAt(i) == s[a+1].charAt(i) & s[a].charAt(i) == s[a+2].charAt(i) & s[a].charAt(i) == s[a+3].charAt(i)) {
				i++;
			} else {
				break;
			}
		}

		if (i >= 2) {
			s1 = Integer.toString(s[a].length()) + s[a].substring(0, i) + "*" + s[a].substring(i) + "1◊" + s[a+1].substring(i) + "2◊" + s[a+2].substring(i) + "3◊" + s[a+3].substring(i);
		} else {
			s1 = Integer.toString(s[a].length()) + s[a];
			s1 = s1 + Integer.toString(s[a+1].length()) + s[a+1];
			s1 = s1 + Integer.toString(s[a+2].length()) + s[a+2];
			s1 = s1 + Integer.toString(s[a+3].length()) + s[a+3];
		}
		return s1;
	}


	public static int min(int a, int b, int c, int d) {
		int i, j;
		if (a >= b) 
			i = b;
		else 
			i = a;

		if (c >= d) 
			j = d;
		else 
			j = c;

		if (i >= j) 
			return j;
		else 
			return i;

	}

	public static byte[] StringtoBytes(String fin) throws UnsupportedEncodingException {
		BitSet bits = new BitSet(fin.length());
		for (int i = 0; i < fin.length(); i++) {
			if (fin.charAt(i) == '1') {
				bits.set(i);
			}
		}

		byte[] bytes = new byte[(bits.length() + 7) / 8];
		for (int i = 0; i < bits.length(); i++) {
			if (bits.get(i)) {
				bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
			}
		}

		return bytes;
	}

	public static byte[] gamma(int id) throws UnsupportedEncodingException {
		String id_string = Integer.toBinaryString(id);
		String fin = new String();
		for (int i = 1; i < id_string.length(); i++) {
			fin = fin + "1";
		}

		fin = fin + "0" + id_string.substring(1);
		byte[] bytes = StringtoBytes(fin);
		return bytes;
	}

	public static byte[] delta(int id) throws UnsupportedEncodingException {
		String id_string_complete = Integer.toBinaryString(id);
		int len = id_string_complete.length();
		String id_string = Integer.toBinaryString(len);
		String fin = new String();
		for (int i = 1; i < id_string.length(); i++) {
			fin = fin + "1";
		}

		fin = fin + "0" + id_string.substring(1);//gamma
		fin = fin + id_string_complete.substring(1);//delta
		BitSet bits = new BitSet(fin.length());
		byte[] bytes = StringtoBytes(fin);
		return bytes;
	}
}
