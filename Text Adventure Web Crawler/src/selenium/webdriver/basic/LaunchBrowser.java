package selenium.webdriver.basic;


import java.util.ArrayList;
import java.io.FileWriter;
import java.io.File; 
import java.io.FileInputStream; //Needed if input is read from a file
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import com.google.gson.Gson;


/*
 * LaunchBrowser is used by the android application to harvest the text adventure stories.
 * The application needs a ChromeDriver to run properly .
 * This was originally created in the summer 2022 before the work on the Android application side
 * had begun.
 */

public class LaunchBrowser {
	
	//chromeDriver file location
	private static final String webDriverPath = "C:\\Java testing\\Text Adventure Web Crawler\\chromedriver.exe";
	//Text Adventure save directory
	private static final String gameFileDirectory = "C:\\Users\\majav\\Tiedostot\\TextAdventures\\";
	//Maximum amount of empty pages before we stop looking for new ones
	private static final int max_empty_pages = 50;
	//Adventure URL address
	private static final String storyURL = "https://chooseyourstory.com/Stories/top.aspx";
	//Webdriver
	private static WebDriver driver = null;
	//Webdriver options
	private static ChromeOptions options = new ChromeOptions();
	//Active game info list used in saving the game information
	private static String[] currentGameInfo;
	
	
	public static void main(String[] args) throws FileNotFoundException {

		
		
		//AUTOMATED (DISABLE OTHERWISE):
		//System.setIn(new FileInputStream("Automate_cmd.txt"));
				
		Scanner myObj = new Scanner(System.in);
		boolean notLoaded = true;
		boolean looping = true;		
		
		currentGameInfo = new String[8];
		String[][] listOfGames = new String[101][];
		String command = null;
		boolean noInput = false;
		System.setProperty("webdriver.chrome.driver",webDriverPath);
		options = new ChromeOptions();
		options.addArguments("--auto-open-devtools-for-tabs");
		
		//Makes the browser run in a Windowless state
		//options.addArguments("headless");
			
		
		while (looping==true) {
			boolean selector = true;
			while (selector==true){
					
				if (notLoaded) {
					 
					driver = new ChromeDriver(options);
					listOfGames = getGameList(driver);
					notLoaded = false;
				}
				
				//Print the list of games
				printGameList(listOfGames);
				
				System.out.println("Type the index number of the story you want to load or 'quit' to  exit");
				
				//
				//SELECTING THE GAME INDEX
				//
				
				noInput = true;
				int selectedGame = 0;
				while (noInput) {
					command = myObj.nextLine();
					if (command.equals("quit")) {
						System.out.println("Closing the program");
						driver.quit();
						looping = false;
					}
					else if (isNumeric(command)==false) {
						System.out.println("Input must be a whole number.");
					} else if (0 <= Integer.parseInt(command) && Integer.parseInt(command)<listOfGames.length){
						selectedGame =  Integer.parseInt(command);
						noInput = false;
					}
					else {
						System.out.println("Please input a valid index.");
					}
				
				}
				
				
				
				
				
				//Copy Game information to gameInfo
				for (int i = 0; i<listOfGames[selectedGame].length;i++) {
					currentGameInfo[i]=listOfGames[selectedGame][i];
				}
				
				//Go to game URL
				driver.get(currentGameInfo[currentGameInfo.length-2]);
				
				//Print game description
				String description = driver.findElement(By.xpath("/html/body/div[2]/div[1]/form/div[5]")).getText();
				System.out.println();
				System.out.println(currentGameInfo[1]);
				System.out.println(description);
				
				
				//Add game description to currentGameInfo
				currentGameInfo[currentGameInfo.length-1] = description;
				
				
				//SELECTING WHETHER TO PLAY THE SELECTED ADVENTURE OR NOT
				
				
				System.out.println("\t"+"\t" + "Would you like to 'play' " + currentGameInfo[1] + " or to go 'back'?");
				noInput = true;
				
				//Continue until the user gives a valid input
				while (noInput) {
					
					System.out.println("Commands: 'play', 'quit' and 'back'");
					command = myObj.nextLine();
					if (command.equals("quit")) {
						System.out.println("Closing the program");
						driver.quit();
						
						//Close the program
						looping = false;
					}
					else if (command.equals("back")) {
						System.out.println("Back");
						
						//Exit input loop
						noInput = false;
					}
					else if (command.equals("play")) {
						
						//Click on the play button
						WebElement open_game_button = driver.findElement(By.xpath("/html/body/div[2]/div[1]/form/div[6]/input"));
						String game_address = open_game_button.getAttribute("onclick").split("'")[1];
						String gameUrl = game_address.substring(16,game_address.length());
						
						//Start the story harvester
						storyHarvester(driver,gameUrl);
						
						//Exit input loop
						noInput = false;
					}
					else {
						System.out.println("Your command: '" + command + "' is invalid.");
					}
				
				}
				
				
			}
		}
		driver.quit();
		myObj.close();
			
			
	}

    
	//Used to input JavaScript commands into the browser console
	private static WebDriver consoleCommand(WebDriver driver, String Command) {	
		JavascriptExecutor js = ((JavascriptExecutor) driver);
		js.executeScript(Command);
		try {
			Thread.sleep(000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return driver;
	}

	
	
    //Used to check whether the String is numeric
	//Return true if it is and otherwise false
	private static boolean isNumeric(String strNum) {
	    if (strNum == null) {
	        return false;
	    }
	    try {
	        double d = Double.parseDouble(strNum);
	        if (d % 1 == 0) {
	    	    return true;
    	    }
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return false;
    }

    //Used to save an object to a specific file name
	//objects saved are expected to be JSON files
	private static boolean saveFile(Object map,String path) {
        FileWriter writer = null;
        try {
        	writer = new FileWriter(new File(path));
        	Gson gson = new Gson();
            gson.toJson(map, writer);
			writer.flush();
			writer.close();
            return true;
        } catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR WHILE SAVING");
			return false;
		}		
	}


	//Used to print the list of Games
	private static void printGameList(String[][] listOfGames) {
		System.out.println("------------------------------------------------------------------------------------------------------------");
		for (int i = 0; i< listOfGames.length;i++) {
			String[] row = listOfGames[i];
			System.out.format("%-10s%-75s%-20s%-10s%-15s%-15s\n",row[0], row[1], row[2], row[3], row[4], row[5]);

		}
		System.out.println();
		System.out.println();
	}
	
	//Used to gather the list of all possible games from the URL below
	private static String[][] getGameList(WebDriver driver) {
		driver.get(storyURL);
		String[][] listOfGames = new String[101][];
		int index = 0;
		listOfGames[index] = new String[] {"Index","Title", "Author", "Length", "Difficulty", "Rating","url"};
		WebElement table = driver.findElement(By.id("MainContentPlaceHolder_StoriesGridView"));
		List<WebElement> allRows = table.findElements(By.tagName("tr"));
		
		for (WebElement row : allRows) {
			if (index!=0) {
				String title = row.findElement(By.xpath("./td[1]")).getText();
				title = title.substring(0, Math.min(title.length(), 70));
				
				String author = row.findElement(By.xpath("./td[2]")).getText();
				author = author.substring(0, Math.min(author.length(), 20));
				
				String length = row.findElement(By.xpath("./td[3]/img")).getAttribute("alt");	
				length = length.substring(0, Math.min(length.length(), 1));
				
				String difficulty = row.findElement(By.xpath("./td[4]/img")).getAttribute("alt");
				difficulty = difficulty.substring(0, Math.min(difficulty.length(), 1));
				
				String rating = row.findElement(By.xpath("./td[5]")).getText();
				
				String url = row.findElement(By.xpath("./td[1]/a")).getAttribute("href");
				
				listOfGames[index] = new String[] {Integer.toString(index), title,author,length,difficulty,rating, url};
			}
			index++;
			
		}
		return listOfGames;
	}
	
	
	
	//This is the part of the program that harvests the adventure information by going through webpage indexes
	//until it meets max_empty_pages amount of empty pages in row indicating that the story is over
	//The return value indicated whether the adventure was read successfully or not.
	private static boolean storyHarvester(WebDriver driver,String gameUrl) {
		
		
		//Try catch in case the application runs into something it does not know how to handle
		try {
			int serverError= 0;
			boolean finished = false;
			boolean successfulRead = true;
			driver.get("https://chooseyourstory.com/story/viewer/default.aspx?StoryId=" + gameUrl);

			WebElement optionsMenu = null;
			
			//Initial page
			int i = 0;
			HashMap<String, String> storyText = new HashMap<String, String>();
			HashMap<String, ArrayList<String>> storyChoices = new HashMap<String, ArrayList<String>>();
			HashMap<String, ArrayList<String>> storyGoTo = new HashMap<String, ArrayList<String>>();
			HashMap<String, String> storyTitles = new HashMap<String, String>();
			
			//We continue iterating until all of the pages are visited or until items or other objects
			//that cannot be implemented into the Android application are met
			while (!finished) {
				
				i++;
				//Reset the driver after 1000 pages
				//this is due to the driver slowing down to a crawl after a while
				if (i %1000==0) {
					driver.quit();
					driver = new ChromeDriver(options);
					driver.get("https://chooseyourstory.com/story/viewer/default.aspx?StoryId=" + gameUrl);
					
				}
				
				//JS Console command for the next page
				String Command = "PostBack('FollowLink','" + Integer.toString(i) + "');";
				
				String pageText = "";
				String pageTitle = "";
				
				//Empty page => Go back (unless enough are met one after another)
				if (driver.getTitle().equals("Link Resultset not loaded")) {
					serverError++;
					System.out.println("serverError has been increased to " + Integer.toString(serverError));
					driver.navigate().back();
					if (serverError == max_empty_pages) {
						System.out.println("End reached!");
						successfulRead = true;
						finished = true;
					}
					System.out.println("Page "+ Integer.toString(i-1) + " does not exist");
					driver = consoleCommand(driver,Command);
					// CASE 1: INVALID PAGE NUMBER
					//STOP
					
					
				}

				//Items in game
				else if (driver.findElements(By.xpath("/html/body/div[3]/table[2]")).size() >0 ||
						driver.findElements(By.xpath("/html/body/div[3]/table/tbody/tr//*")).size() >0) {
					System.out.println("ITEM FOUND");
					System.out.println("STORY CANNOT BE LOADED. PLEASE TRY ANOTHER ONE.");
					successfulRead = false;
					finished = true;
					
				}
				
				//Save progress page => Go back
				else if (driver.findElements(By.xpath("/html/body/form/div[4]/div/p[1]")).size() >0) {
						driver.navigate().back();
						driver = consoleCommand(driver,Command);
				}
				
				//End screen => Go back
				else if (driver.findElement(By.xpath("/html/body/form")).getAttribute("action").
						equals("https://chooseyourstory.com/story/viewer/rate.aspx?StoryId="+gameUrl)){
					serverError = 0;
					System.out.println("Page: "+ Integer.toString(i-1) + " is the end screen.");
					driver.navigate().back();
					driver = consoleCommand(driver,Command);
					pageTitle = "The end.";
					pageText = "You've now completed a path in game " + currentGameInfo[1];
					// CASE 2: RATE SCREEN
					//GO TO PREVIOUS PAGE 
					//CONTINUE WITH CONSOLE
				}
				
				//Normal page => Get text and continue to next page
				else {
					serverError = 0;
					String pageName = "page " + Integer.toString(i-1);
					List<String> pname = new ArrayList<String>();
					pname.add(pageName);
					System.out.println(pageName);
					// CASE 3: NORMAL
					//SAVE PAGE TEXT AND CHILDREN
					//CONTINUE WITH CONSOLE
					
					// CASE 4: NO OPTIONS
					//SAVE PAGE TEXT WITH NO CHILDREN
					//CONTINUE WITH CONSOLE
					
					
					
					//Get page title
					pageTitle += driver.findElement(By.xpath("/html/body/div[2]/h1")).getText();
					
					//Get page text body element
					List<WebElement> txtBody = driver.findElements(By.xpath("/html/body/div[3]/div[1]"));
					
					//Append all of the elements to pageText
					for (WebElement paragraph: txtBody) {
						pageText += paragraph.getText() + "\n";
					}
					storyText.put(pageName,pageText);
					ArrayList<String> choices = new ArrayList<String>();
					List<WebElement> optionslist = new ArrayList<WebElement>();
					ArrayList<String> pageGoTo = new ArrayList<String>();
					optionsMenu =  driver.findElement(By.xpath("/html/body/div[3]/ul"));
					optionslist = optionsMenu.findElements(By.tagName("a"));
					
					//For debugging. This means that the page has no pages to go to from this one
					if (optionslist.size()==0) {
						System.out.println("No options");
						
					}
					else {
						//Page has choices for next page
						
						System.out.println("Possible choices:");
						for (WebElement item: optionslist) {
							
							//Add page to current page's pageGoTo array
							String optioncode = "page " + item.getAttribute("onclick").split("'")[3];
							choices.add(optioncode);
							pageGoTo.add(item.getText());
							
							//Print page code
							System.out.print(optioncode.substring(4,optioncode.length()) + " - ");
							System.out.println(item.getText());
						}
					}
					
					//Append all of the page information to the corresponding story maps with a key being
					//"page " + [pagenumber]
					storyGoTo.put(pageName, choices);
					storyChoices.put(pageName, pageGoTo);
					storyTitles.put(pageName, pageTitle);
					driver = consoleCommand(driver,Command);
				}

			}
				
			//Save results if the story was successfully read meaning that no disallowed objects were met
			if (successfulRead ) {
				//Get directory and file paths
				String filePath = gameFileDirectory + currentGameInfo[1].replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
				String filePathTitles = filePath + "\\"+ "storyTitles.json";
				String filePathStory = filePath + "\\"+ "storyText.json";
				String filePathChoices = filePath + "\\"+ "storyChoices.json";
				String filePathGoTo = filePath + "\\"+ "storyGoTo.json";
				String fileGameInfo = filePath + "\\" + "gameInfo.json";
				
				//Add a adventure directory
				File directory = new File(filePath); 
			    boolean res = directory.mkdir();
			    
			    if(res) {
			      System.out.println("The directory has been created.");
			    }
			    else {
			        System.out.println("The directory already exists.");
			    }
			    
			    //Save files
			    saveFile(storyText,filePathStory);
			    saveFile(storyChoices,filePathChoices);
			    saveFile(storyGoTo,filePathGoTo);
			    saveFile(storyTitles,filePathTitles);
			    saveFile(currentGameInfo,fileGameInfo);
			    
			    
			    //Sleep before continuing to the main menu
				//This can be removed
			    try {
					
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
		    	//After fully successful run return true to indicate that 
			    // the information was gathered and saved successfully
				return true;
				
			}
		} catch(Exception e) {
			e.printStackTrace();
			
		}
		return false;
		
	}
}



