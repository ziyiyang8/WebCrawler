import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Store frequency of words in all html pages
 * 
 */
public class FrequencyAnalysis {
	public HashMap<String, Integer> wordFrequency;	// count for each word occurrence
	public TreeMap<String, Integer> sorted;	// sorted to get rankings
	
	public FrequencyAnalysis()
	{
		wordFrequency = new HashMap<String, Integer>();
	}
	
	// Comparator for TreeMap to sort by values
	static class ValueComparator implements Comparator<String>{
		 
		HashMap<String, Integer> map = new HashMap<String, Integer>();
	 
		public ValueComparator(HashMap<String, Integer> map){
			this.map.putAll(map);
		}
	 
		@Override
		public int compare(String s1, String s2) {
			if(map.get(s1) >= map.get(s2)){
				return -1;
			}else{
				return 1;
			}	
		}
	}
	
	public void getCount() throws IOException
	{
		File dir = new File("repository");
		File[] repository = dir.listFiles();
		if (repository != null)
		{
			for (File page : repository)
			{
				Document doc = Jsoup.parse(page, "UTF-8");				
				String text = doc.text();
				
				String [] tokens = text.split("\\s+");
				for (int i = 0; i < tokens.length; i++)
				{
					// get only words with alphanumeric characters.
					if (tokens[i].matches("[A-Za-z0-9]+"))
					{
						if (!wordFrequency.containsKey(tokens[i]))
							wordFrequency.put(tokens[i], 1);
						else
							wordFrequency.put(tokens[i], wordFrequency.get(tokens[i]) + 1);
					}
				}
			}
		}
	}
	
	public static TreeMap<String, Integer> sortMapByValue(HashMap<String, Integer> map){
		Comparator<String> comparator = new ValueComparator(map);
		TreeMap<String, Integer> result = new TreeMap<String, Integer>(comparator);
		result.putAll(map);
		return result;
	}
	
	// write words and their counts to output.txt sorted by their ranks
	public void print() throws FileNotFoundException, UnsupportedEncodingException 
	{
		int rank = 1;
		sorted = sortMapByValue(wordFrequency);
		PrintWriter writer = new PrintWriter("output.csv", "UTF-8");
		writer.println("rank" + "," + "frequency" + "," + "log(r)" + "," + "log(f)");
		PrintWriter top100Writer = new PrintWriter("Top100.txt", "UTF-8");
		for (Entry<String, Integer> e : sorted.entrySet())
		{
			writer.println(rank + "," + e.getValue() + "," + Math.log10(rank *1.0) + "," + Math.log10(e.getValue() * 1.0));
			if (rank <= 100)
				top100Writer.println(rank + "," + e.getKey() + "," + e.getValue());
			rank++;
		}
		writer.close();
		top100Writer.close();
	}
	
	public static void main(String[] args) {
		FrequencyAnalysis fa = new FrequencyAnalysis();
		try {
			fa.getCount();
			fa.print();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
