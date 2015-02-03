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

		//this list is a user defined list that is imported and exported
		seeds.remove(url.toString());

		String href = url.getURL().toLowerCase();
		boolean URLExists = URLList.containsKey(href);

		//add traps to this list as we come across them
		return !FILTERS.matcher(href).matches() && href.contains(".ics.uci.edu/") && !URLExists && !href.contains("calendar.ics.uci.edu/") && !href.contains("?") 
				&& !href.contains("ftp") && !href.contains("fano"); 
	}

	//this method is similar to above, but takes a string instead of weburl
	public boolean shouldVisit(String url){

		seeds.remove(url.toString());

		String href = url.toLowerCase();
		boolean URLExists = URLList.containsKey(href);

		return !FILTERS.matcher(href).matches() && href.contains(".ics.uci.edu/") && !URLExists && !href.contains("calendar.ics.uci.edu/") && !href.contains("?") 
				&& !href.contains("ftp") && !href.contains("fano"); 
	}


	/**
	 * This function is called when a page is fetched and ready 
	 * to be processed by your program.
	 */
	@Override
	public void visit(Page page) {

		//when we stop and restart the crawler, we add new seeds which don't seem to go through the
		//shouldVisist method.
		//This part is here to make sure they go through it no matter what.
		if(!shouldVisit(page.getWebURL()))
			return;

		String url = page.getWebURL().getURL().toLowerCase();

		//used for logging purposes to make sure it's still working/not in any traps
		pageNumber++;

		if(pageNumber % 500 == 0)
		{
			System.out.println(pageNumber + " URL: " + url);

		}

		//determines if given url is part of a subdomain and updates corresponding list to reflect if it is
		String subdomain = getSubdomain(url);
		if(!subdomain.isEmpty())
		{
			//recreates subdomain with entire domain for logging purposes
			String subdomainURL = "http://" +subdomain + ".ics.uci.edu/";
			if(subdomains.containsKey(subdomainURL))
				subdomains.put(subdomainURL, subdomains.get(subdomainURL)+1);
			else
				subdomains.put(subdomainURL, 1);
		}

		//does text parsing for tokenizing
		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();

			String text = htmlParseData.getText();
			List<WebURL> links = htmlParseData.getOutgoingUrls();

			//adds all the links to the list for later output
			for(WebURL link: links){
				seeds.put(link.toString(), 1);
			}

			//tokenizes all text on the page and pairs it with URL
			ArrayList<String> tokenList = Utilities.tokenizeString(text);
			int wordsOnPage = tokenList.size();
			URLList.put(url, wordsOnPage);

			//add all tokens to master token list
			List<Frequency> currentTokens = WordFrequencyCounter.computeWordFrequencies(tokenList);

			//maps token list to individual URL
			urlMapper.put(url, (ArrayList<Frequency>) currentTokens);

		}
	}

	public void printToFile()
	{
		System.out.println("Printing to files");

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

			//output visited pages to file
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

	//load data from files into Crawler memory space
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

	//determines if given url is part of a subdomain or not
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

}