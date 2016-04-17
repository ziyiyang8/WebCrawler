import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *  Goes through all downloaded pages in repository folder and removes noise
 *  such as main navigation bar and advertisements
 *  
 *  The main algorithm used is found in this paper:
 *  https://www3.nd.edu/~tweninge/pubs/WH_TIR08.pdf
 *
 */
public class ContentProcessor {	
	
	ArrayList<String> lines;		// keeps track of each line of an html page
	/**
	 * Initializes the ContentProcessor
	 * 
	 * @throws Exception
	 */
	public ContentProcessor() throws Exception
	{
		lines = new ArrayList<String>();
	}
	
	/**
	 * Start the noise removal process.
	 * Iterates through all files in repository to get Tag-To-Text Ratio (TTR), smooth the ratio, then categorize the TTR
	 * into clusters using kMeans clustering algorithm.
	 * 
	 * @throws Exception
	 */
	public void removeNoise() throws Exception
	{
		ArrayList<Double> TTR;
		ArrayList<Double> smoothTTR;
		ArrayList<Boolean> clusterFlag;
		FileWriter fw;
		File dir = new File("repository");
		File[] repository = dir.listFiles();
		// loop through each file in repository folder
		if (repository != null) 
		{
		    for (File page : repository) 
		    {
		    	System.out.println("Processing " + page.getName());
		    	TTR = calculateTTR(page);
		    	smoothTTR = smooth(TTR, 1);	   
		    	clusterFlag = kMeans(smoothTTR,3);
		    	
		    	// get only content lines of html page
		    	String goodHtml = "";
		    	for (int i=0;i<lines.size();i++) 
		    	{
		    		// if true that means line has content
		    		if (clusterFlag.get(i)) 
		    			goodHtml += lines.get(i);
		    	}
		    	// write to file with noise removed
				fw = new FileWriter(page, false);
				fw.write(goodHtml);
				fw.close();
		    }
		    System.out.println("Finished processing.");
		} 
		else 
			throw new Exception ("repository folder not found");
	}
	
	/**
	 * kMeans clustering algorithm
	 * https://en.wikipedia.org/wiki/K-means_clustering
	 * 
	 * @param smoothTTR
	 * @param k num of clusters
	 * @return
	 */
	public ArrayList<Boolean> kMeans (ArrayList<Double> smoothTTR, int k)
	{
		ArrayList<Integer> clusterPos = new ArrayList<Integer>();
		ArrayList<Boolean> preserved = new ArrayList<Boolean>();
		double[] clusterVal = new double[k];
		int[] clusterCount = new int[k];
		double avgVal = 0.0;
		Random rn = new Random();
	
		for (int i=0;i<smoothTTR.size();i++) {
			int tmp = rn.nextInt(k);
			clusterPos.add(tmp);
			avgVal += smoothTTR.get(i);
		}
		
		avgVal /= smoothTTR.size();
		
		while (true) {
			// calculate the initial value for the clusters
			for (int i=0;i<k;i++) {
				clusterCount[i] = 0;
				clusterVal[i] = 0.0;
			}
			
			for (int i=0;i<smoothTTR.size();i++) {
				clusterCount[clusterPos.get(i)]++;
				clusterVal[clusterPos.get(i)] += smoothTTR.get(i);
			}
			
			for (int i=0;i<k;i++) 
				clusterVal[i] /= clusterCount[i];
			
			boolean clusterChange = false;
			
			// reassigning the cluster elements
			for (int i=0;i<smoothTTR.size();i++) {
				int oldInd = clusterPos.get(i);
				double dist = 0.0;
				int minInd = -1;
				for (int j=0;j<k;j++) {
					double tdist = Math.abs(smoothTTR.get(i)-clusterVal[j]);
					if (minInd == -1 || dist>tdist) {
						minInd = j;
						dist = tdist;
					}
				}
				if (oldInd!=minInd) clusterChange = true;
				clusterPos.set(i,minInd);
			}
			
			if (!clusterChange) break;
		}
		
		for (int i=0;i<k;i++) {
			clusterCount[i] = 0;
			clusterVal[i] = 0.0;
		}
		
		for (int i=0;i<smoothTTR.size();i++) {
			clusterCount[clusterPos.get(i)]++;
			clusterVal[clusterPos.get(i)] += smoothTTR.get(i);
		}
		
		for (int i=0;i<k;i++) 
			clusterVal[i] /= clusterCount[i];
		
		boolean[] goodCluster = new boolean[k];
		for (int i=0;i<k;i++) {
			if (clusterVal[i]>avgVal) goodCluster[i] = true;
			else goodCluster[i] = false;
		}
		
		for (int i=0;i<smoothTTR.size();i++) {
			if (goodCluster[clusterPos.get(i)]) preserved.add(true);
			else preserved.add(false);
		}
		
		return preserved;
	}
	
	/**
	 * This method will calculate the TTR of each line of html page.
	 * 
	 * @param page
	 * @throws IOException 
	 */
	public ArrayList<Double> calculateTTR(File page) throws IOException
	{
		FileWriter output = null;
		BufferedReader br;
		ArrayList<Double> pageTTR = new ArrayList<Double>();
		// make sure lines is empty
		lines.clear();
		
		try {
			// use Jsoup to parse a html page in repository folder
			Document doc = Jsoup.parse(page, "UTF-8");
						
			// remove all script, remark, and style tags
			doc.select("script").remove();
			doc.select("remark").remove();
			doc.select("style").remove();
			
			String strippedContent = doc.outerHtml();
			
			// update page with scripts and remarks and style tags removed
			output = new FileWriter(page, false);
			output.write(strippedContent);
			
			// read html page line by line to calculate each line's Text To Tag (TTR) Ratio
			br = new BufferedReader(new FileReader(page));
			String line;
			while ((line = br.readLine()) != null)
			{
				Document docLine = Jsoup.parse(line);
				// if line has no next, then skip it
				if (docLine.text().equals(""))
					continue;
				// save each line of file
				lines.add(docLine.outerHtml());
				// number of non-tag characters in line
				int x = docLine.text().length();
				// number of tags in line
				int y = 0;
				for (int i = 0; i < docLine.outerHtml().length(); i++)
				{
					// if we encounter open tag character then increment count
					if (docLine.outerHtml().charAt(i) == '<')
						y++;
				}
				// divide by 2 to take into account closing tags
				y /= 2;
				// add the ratio to our TTR array
				if (y == 0)
					pageTTR.add(x * 1.0);
				else
					pageTTR.add(x * 1.0/y);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally
		{
			if (output != null)
				output.close();
		}
		return pageTTR;
	}
	
	/**
	 * Smooths the TTR to prevent important content area from being removed accidentally.
	 * A higher TTR will be lowered and a lower TTR will be increased to average out the TTRs.
	 * 
	 * @param TTR
	 * @param radius
	 * @return
	 */
	private ArrayList<Double> smooth(ArrayList<Double> TTR, int radius)
	{
		ArrayList<Double> smoothTTR = new ArrayList<Double>();
		// iterate through TTR array
		for (int i = 0; i < TTR.size(); i++)
		{
			double sum = 0.0;
			int count = 0;
			// get the neighbors of current index (the radius) 
			for (int j = Math.max(0, i-radius); j <= Math.min(i+radius, TTR.size()-1); j++)
			{
				sum += TTR.get(j);
				count++;
			}
			// calculate the average and add to smoothTTR array
			smoothTTR.add(sum/count);
		}
		return smoothTTR;
	}
	
	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ContentProcessor cp = new ContentProcessor();
			cp.removeNoise();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}