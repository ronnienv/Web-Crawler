package ir.assignments.three;

import ir.assignments.two.a.Frequency;
import ir.assignments.two.a.Utilities;
import ir.assignments.two.b.WordFrequencyCounter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class MyCrawler extends WebCrawler {

	private final static HashMap<String, Integer> URLList = new HashMap<String, Integer>();
	private final static HashMap<String, Integer> subdomains = new HashMap<String, Integer>();
	private final static HashMap<String, Integer> totalTokenList = new HashMap<String, Integer>();
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" 
			+ "|png|tiff?|mid|mp2|mp3|mp4"
			+ "|wav|avi|mov|mpeg|ram|m4v|pdf" 
			+ "|rm|smil|wmv|swf|wma|zip|rar|gz))$");
	private String largestPage = "";
	private int largestPageSize = 0;

	/**
	 * You should implement this function to specify whether
	 * the given url should be crawled or not (based on your
	 * crawling logic).
	 */
	@Override
	public boolean shouldVisit(WebURL url) {
		String href = url.getURL().toLowerCase();
		boolean URLExists = URLList.containsKey(href);
		return !FILTERS.matcher(href).matches() && href.contains(".ics.uci.edu/") && !URLExists && !href.contains("calendar.ics.uci.edu/") && !href.contains("?=") 
				&& !href.contains("ftp") && !href.contains("fano"); 
	}

	/**
	 * This function is called when a page is fetched and ready 
	 * to be processed by your program.
	 */
	@Override
	public void visit(Page page) {          
		String url = page.getWebURL().getURL();

		System.out.println("URL: " + url);
		String subdomain = getSubdomain(url);

		if(!subdomain.isEmpty())
		{
			String subdomainURL = "http://" +subdomain + ".ics.uci.edu/";
			if(subdomains.containsKey(subdomainURL))
				subdomains.put(subdomainURL, subdomains.get(subdomainURL)+1);
			else
				subdomains.put(subdomainURL, 1);
		}
		System.out.println(subdomain);

		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String text = htmlParseData.getText();
			String html = htmlParseData.getHtml();

			List<WebURL> links = htmlParseData.getOutgoingUrls();

			ArrayList<String> tokenList = Utilities.tokenizeString(text);
			int wordsOnPage= tokenList.size();
			URLList.put(url, wordsOnPage);

			//add all tokens to master token list
			List<Frequency> currentTokens = WordFrequencyCounter.computeWordFrequencies(tokenList);
			for(int i = 0; i < currentTokens.size(); i++)
			{
				Frequency f = currentTokens.get(i);
				int value = f.getFrequency();
				if(totalTokenList.containsKey(f.getText()))
					value += totalTokenList.get(f.getText());
				totalTokenList.put(f.getText(), value);
			}

			//                    System.out.println("Text length: " + text.length());
			//                    System.out.println("Html length: " + html.length());
//			System.out.println(currentTokens);
//			System.out.println("Number of outgoing links: " + links.size());

		}
	}

	public String getSubdomain(String URL)
	{
		String[] s;
		if(URL.contains("http://www."))
			s = URL.split("http://www");
		else
			s = URL.split("http://");
		String[] s2 =  s[1].split(".ics.uci.edu/");
		return s2[0];
	}

	public void printEndResults(HashMap<String, Integer> stopWords) {
		System.out.println("Number of unique pages: " + URLList.size());

		int max = 0;
		String maxKey = "";
		for(String s: URLList.keySet())
		{
			if(URLList.get(s) > max)
			{
				max = URLList.get(s);
				maxKey = s;
			}
		}

		System.out.println("The page : " + maxKey + " has the longest text size of " + max + " words");

		System.out.println("There are " + subdomains.size() + " subdomains");
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new File("Subdomains.txt"));
			for(String s: subdomains.keySet())
			{
				pw.write(s + ", " + subdomains.get(s) + "\n");
			}
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		PriorityQueue<Frequency> topWords = new PriorityQueue<Frequency>(totalTokenList.size(), WordFrequencyCounter.freqComparator);

		//adds tokens to top 500 list if they aren't in the list of stop words
		for(String s : totalTokenList.keySet())
		{
			if(!stopWords.containsKey(s))
				topWords.add(new Frequency(s,totalTokenList.get(s)));
		}
		

		
		try {
			pw = new PrintWriter(new File("CommonWords.txt"));
			for(int i = 500; i > 0 && !topWords.isEmpty(); i--)
			{
				pw.write(topWords.poll() + ", " + " \n");
			}

			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
//		System.out.println("The stop words are: " + stopWords.toString());


	}
}