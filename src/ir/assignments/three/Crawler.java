package ir.assignments.three;

import ir.assignments.two.a.Frequency;
import ir.assignments.two.a.Utilities;
import ir.assignments.two.b.WordFrequencyCounter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class Crawler extends WebCrawler {

	private final static HashMap<String, Integer> seeds = new HashMap<String, Integer>();
	private final static HashMap<String, Integer> URLList = new HashMap<String, Integer>();
	private final static HashMap<String, Integer> subdomains = new HashMap<String, Integer>();
	private final static HashMap<String, Integer> totalTokenList = new HashMap<String, Integer>();
	private final static HashMap<String, ArrayList<Frequency>> urlMapper = new HashMap<String, ArrayList<Frequency>>();
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" 
			+ "|png|tiff?|mid|mp2|mp3|mp4"
			+ "|wav|avi|mov|mpeg|ram|m4v|pdf|ppt|pptx" 
			+ "|rm|smil|wmv|swf|wma|zip|rar|gz))$");
	private static int pageNumber = 1;
	/**
	 * You should implement this function to specify whether
	 * the given url should be crawled or not (based on your
	 * crawling logic).
	 */
	@Override
	public boolean shouldVisit(WebURL url) {

		//		System.out.println(url.getURL());
		seeds.remove(url.toString());

		String href = url.getURL().toLowerCase();
		boolean URLExists = URLList.containsKey(href);

		return !FILTERS.matcher(href).matches() && href.contains(".ics.uci.edu/") && !URLExists && !href.contains("calendar.ics.uci.edu/") && !href.contains("?=") 
				&& !href.contains("ftp") && !href.contains("fano"); 
	}

	public boolean shouldVisit(String url){
		
		seeds.remove(url.toString());
		
		String href = url.toLowerCase();
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

		if(!shouldVisit(page.getWebURL()))
			return;
		
		String url = page.getWebURL().getURL().toLowerCase();
		
		pageNumber++;
		
		if(pageNumber % 500 == 0)
		{
			System.out.println(pageNumber + " URL: " + url);
			
		}

		String subdomain = getSubdomain(url);
		if(!subdomain.isEmpty())
		{
			String subdomainURL = "http://" +subdomain + ".ics.uci.edu/";
			if(subdomains.containsKey(subdomainURL))
				subdomains.put(subdomainURL, subdomains.get(subdomainURL)+1);
			else
				subdomains.put(subdomainURL, 1);
		}

		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String text = htmlParseData.getText();
			String html = htmlParseData.getHtml();

			List<WebURL> links = htmlParseData.getOutgoingUrls();

			for(WebURL link: links){
				seeds.put(link.toString(), 1);
			}

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
			//			System.out.println(totalTokenList);

			//maps token list to individual URL
			urlMapper.put(url, (ArrayList<Frequency>) currentTokens);
			//                    System.out.println("Text length: " + text.length());
			//                    System.out.println("Html length: " + html.length());
			//			System.out.println(currentTokens);
			//			System.out.println("Number of outgoing links: " + links.size());

		}
	}

	public void printToFile()
	{
		System.out.println("Printing to files");
		//if we have hit an increment of 25 pages, print out the current state
		//so we can run from where we left off later
		PrintWriter pw = null;
		try {

			//output the queue of urls to travel to file
			pw = new PrintWriter(new File("queue.txt"));
			pw.print("");
			for(String url: seeds.keySet()){
				pw.write(url+ "\n");
			}
			pw.close();

			//output subdomains visited to file
			pw = new PrintWriter(new File("PreSubdomains.txt"));
			for(String subdomain: subdomains.keySet()){
				pw.write(subdomain+ " " + subdomains.get(subdomain) + "\n");
			}
			pw.close();

			//output visited pages in file
			pw = new PrintWriter(new File("visited.txt"));
			for(String url: URLList.keySet()){
				pw.write(url+ " " + URLList.get(url) + "\n");
			}
			pw.close();

			//output urlMapper to preindex
			pw = new PrintWriter(new BufferedWriter(new FileWriter("PreIndex.txt", true)));
			for(String key: urlMapper.keySet()){
				pw.write(key);
				ArrayList<Frequency> al = urlMapper.get(key);
				for(Frequency f : al)
				{
					pw.write(" " + f.getText() + " " + f.getFrequency());
				}
				pw.write("\n");
			}
			pw.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done printing to files");
	}

	public void loadData()
	{
		try {

			Scanner s = new Scanner(new File("visited.txt"));

			while(s.hasNext())
			{
				URLList.put(s.next(), s.nextInt());
			}

			s.close();

			s = new Scanner(new File("PreSubdomains.txt"));

			while(s.hasNext())
			{
				subdomains.put(s.next(), s.nextInt());
			}

			s.close();

			System.out.println("Loaded!");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public String getSubdomain(String URL)
	{
		if(URL.equals("http://www.ics.uci.edu/"))
			return "";
		String[] s;
		if(URL.contains("http://www"))
			s = URL.split("http://www");
		else
			s = URL.split("http://");
		String[] s2 =  s[1].split(".ics.uci.edu/");
		return s2[0];
	}

	public void printEndResults(HashMap<String, Integer> stopWords) {
		//		System.out.println("Number of unique pages: " + URLList.size());

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

		ArrayList<String> al = new ArrayList<String>();
		for(String s: subdomains.keySet())
		{
			al.add(s);
		}

		String[] subdomainsPrint = Arrays.copyOf(al.toArray(), al.size(), String[].class);
		Arrays.sort(subdomainsPrint);

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new File("Subdomains.txt"));
			for(int i = 0; i < subdomainsPrint.length; i++)
			{
				String subdomain = subdomainsPrint[i];
				pw.write(subdomain + ", " + subdomains.get(subdomain) + "\n");
			}
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		PriorityQueue<Frequency> topWords = new PriorityQueue<Frequency>(totalTokenList.size(), WordFrequencyCounter.freqComparator);

		//adds tokens to top 500 list if they aren't in the list of stop words
		for(String s : totalTokenList.keySet())
		{
			//if the top word is > 1 character add it
			if(!stopWords.containsKey(s) && s.length() > 1)
				topWords.add(new Frequency(s,totalTokenList.get(s)));
		}

		try {
			pw = new PrintWriter(new File("CommonWords.txt"));
			for(int i = 500; i > 0 && !topWords.isEmpty(); i--)
			{
				pw.write(topWords.poll() + " \n");
			}

			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();

		}
		//		System.out.println("The stop words are: " + stopWords.toString());


	}
}