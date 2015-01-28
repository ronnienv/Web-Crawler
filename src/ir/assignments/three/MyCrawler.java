package ir.assignments.three;

import ir.assignments.two.a.Utilities;
import ir.assignments.two.b.WordFrequencyCounter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class MyCrawler extends WebCrawler {

	private final static HashMap<String, Integer> URLList = new HashMap<String, Integer>();
	private final static HashMap<String, Integer> subdomains = new HashMap<String, Integer>();
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
		return !FILTERS.matcher(href).matches() && href.contains(".ics.uci.edu/") && !URLExists;
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
			if(subdomains.containsKey(subdomain))
				subdomains.put(subdomain, subdomains.get(subdomain)+1);
			else
				subdomains.put(subdomain, 1);
		}
		System.out.println(subdomain);
		
		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String text = htmlParseData.getText();
			String html = htmlParseData.getHtml();
			URLList.put(url, text.length());
			List<WebURL> links = htmlParseData.getOutgoingUrls();
			
			//tokenize the text from the page and get the size
			ArrayList<String> tokenList = Utilities.tokenizeString(text);
			int wordsOnPage = tokenList.size();
			
			//if you find a page that is larger than the current largest one
			//reassign the appropriate variables
			if(wordsOnPage > largestPageSize){
				largestPageSize = wordsOnPage;
				largestPage = url;
			}

			//                    System.out.println("Text length: " + text.length());
			//                    System.out.println("Html length: " + html.length());
			System.out.println(WordFrequencyCounter.computeWordFrequencies(Utilities.tokenizeString(text), 500));
			System.out.println("Number of outgoing links: " + links.size());
		}
	}

	public String getSubdomain(String URL)
	{

		String[] s = URL.split("http://www");
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
		
		System.out.println("The largest page is: " + getLargestPage());
		
//		ArrayList<Frequency> topStopWords = new ArrayList<Frequency>();
		System.out.println("The stop words are: " + stopWords.toString());

	}
	
	public String getLargestPage(){
		return largestPage;
	}
	
	public int getLargestPageSize(){
		return largestPageSize;
	}
}