package ir.assignments.three;

import ir.assignments.two.a.Frequency;
import ir.assignments.two.b.WordFrequencyCounter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {
	public static void main(String[] args) throws Exception {
		Scanner s = null;
		if(true)
		{
			printResults();
			return;
		}
	
		//		System.out.println(stopWords.toString());

		Date startTime = Calendar.getInstance().getTime();
		String crawlStorageFolder = "dump";
		int numberOfCrawlers = 100;

		CrawlConfig config = new CrawlConfig();
		config.setUserAgentString("UCI Inf141-CS121 crawler 34201703 22768608");
		config.setPolitenessDelay(300);
		config.setResumableCrawling(false);
		config.setMaxPagesToFetch(60000);
		config.setCrawlStorageFolder(crawlStorageFolder);
		/*
		 * Instantiate the controller for this crawl.
		 */
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
		/*
		 * For each crawl, you need to add some seed urls. These are the first
		 * URLs that are fetched and then the crawler starts following links
		 * which are found in these pages
		 */
		//            controller.addSeed("http://www.ics.uci.edu/~welling/");
		//            controller.addSeed("http://www.ics.uci.edu/~lopes/");
		Crawler c = new Crawler();
		c.loadData();
//				controller.addSeed("http://www.ics.uci.edu/");
		try {
			s = new Scanner(new File("queue.txt"));
			while(s.hasNext())
			{
				String url = s.next();
				if(c.shouldVisit(url))
					controller.addSeed(url.trim());
			}
			s.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		/*
		 * Start the crawl. This is a blocking operation, meaning that your code
		 * will reach the line after this only when crawling is finished.
		 */

		controller.start(c.getClass(), numberOfCrawlers);   
		c.printToFile();

		Date endTime = Calendar.getInstance().getTime();
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("metadata.txt", true)));
		pw.write(endTime.getTime() + " " + startTime.getTime() + " ");
		pw.close();
		System.out.println("The program took a total of : " + (endTime.getTime()-startTime.getTime())/1000/60 + " minutes");
		updateIndex();
		//c.printEndResults(stopWords);
	}
	
	public static void printResults()
	{
		HashMap<String, Integer> stopWords = new HashMap<String, Integer>();
		PrintWriter pw = null;
		
		try {
			//calculates total time taken
			Scanner s = new Scanner(new File("metadata.txt"));
			double sum = 0;
			while(s.hasNext())
			{
				sum += (s.nextLong() - s.nextLong());
			}
			s.close();
			System.out.println("The total time it took was " + sum/1000/60/60 + " hours");
			
			//finds url with the most words
			s = new Scanner(new File("visited.txt"));
			String maxString = s.next();
			int maxPages = s.nextInt();
			
			while(s.hasNext())
			{
				String tempString = s.next();
				int tempMax = s.nextInt();
				if(tempMax > maxPages)
				{
					maxString = tempString;
					maxPages = tempMax;
				}
			}
		
			System.out.println("The page " + maxString + " has the most words with " + maxPages);
			
			
			//gets stop words from file for later use
			s = new Scanner(new File("StopWords.txt"));
			while(s.hasNext())
			{
				stopWords.put(s.nextLine(), 1);
			}
			s.close();
			
			//create priority queue for all words
			PriorityQueue<Frequency> priorityQueue = new PriorityQueue<Frequency>(500, WordFrequencyCounter.freqComparator);
			s = new Scanner (new File("Index.txt"));
			HashMap<String, Integer> commonWords = new HashMap<String, Integer>();
			
			while(s.hasNextLine()){
				String line = s.nextLine();
				String[] splitLine = line.split(" ");
				String word = splitLine[0];
				int count = 0;
				
				//if word is appropriate length and not a stop word
				//calculate its count and word doesn't have 
				//a number in it, add it to the queue
				if(word.length() > 1 && !stopWords.containsKey(word) &&
						!word.matches("(.)*(\\d)(.)*")){
					for(int i = 2; i <= splitLine.length-1; i+=2){
						count += Integer.parseInt(splitLine[i]);
					}
					Frequency current = new Frequency(word, count);
					priorityQueue.offer(current);
				}
			}
			
			//calculates 500 most occuring words
			ArrayList<Frequency> topWords = new ArrayList<Frequency>();
			for(int i = 0; i < 500; i++){
				topWords.add(priorityQueue.poll());
			}
			
			//write most common words to file
			pw = new PrintWriter("CommonWords.txt");
			for(Frequency word: topWords)
			{
				pw.write(word.getText() + ":" + word.getFrequency() + "\n");
			}
			pw.close();
			
			//loads and sorts subdomains
			s = new Scanner(new File("PreSubdomains.txt"));
			HashMap<String, Integer> subdomains = new HashMap<String, Integer>();
			ArrayList<String> al = new ArrayList<String>();
			
			//populate subdomains
			while(s.hasNext())
			{
				String key = s.next();
				subdomains.put(key, s.nextInt());
				al.add(key);
			}

			String[] toSort = Arrays.copyOf(al.toArray(), al.size(), String[].class);
			Arrays.sort(toSort);
			pw = new PrintWriter("Subdomains.txt");
			for(String a: toSort)
			{
				pw.write(a + ", " + subdomains.get(a) + "\n");
			}
		
			pw.close();

			s.close();

			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static void updateIndex()
	{
		try {
			Scanner s = new Scanner(new File("PreIndex.txt"));
			Scanner s2 = new Scanner(new File("Index.txt"));
			HashMap<String, ArrayList<Frequency>> index = new HashMap<String, ArrayList<Frequency>>();

			//import index into memory
			while(s2.hasNextLine())
			{	
				Scanner line = new Scanner(s2.nextLine());
				String key = line.next();
				while(line.hasNext())
				{
					String url = line.next();
					int value = line.nextInt();
					if(index.containsKey(key))
					{
						ArrayList<Frequency> al = index.get(key);
						al.add(new Frequency(url, value));
					}
					else
					{
						ArrayList<Frequency> al = new ArrayList<Frequency>();
						al.add(new Frequency(url, value));
						index.put(key, al);
					}
				}
				line.close();
			}

			//import preindex and place into index
			while(s.hasNextLine())
			{	
				Scanner line = new Scanner(s.nextLine());
				String url = line.next();
				while(line.hasNext())
				{
					String key = line.next();
					int value = line.nextInt();
					if(index.containsKey(key))
					{
						ArrayList<Frequency> al = index.get(key);
						al.add(new Frequency(url, value));
					}
					else
					{
						ArrayList<Frequency> al = new ArrayList<Frequency>();
						al.add(new Frequency(url, value));
						index.put(key, al);
					}
				}
				line.close();
			}
			s.close();
			s2.close();
			PrintWriter pw = new PrintWriter("Index.txt");
			for(String key: index.keySet())
			{
				pw.write(key);
				ArrayList<Frequency> al = index.get(key);
				for(Frequency f : al)
				{
					pw.write(" " + f.getText() + " " + f.getFrequency());
				}
				pw.write("\n");
			}
			pw.close();


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		System.out.println("Index updated");
	}
}