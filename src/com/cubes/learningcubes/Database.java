package com.cubes.learningcubes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;


public class Database {

	private static HashMap<String, Integer> sessionLog;

	
	public static BlockSet[] blockSets = {
		new BlockSet(1, "Alphabet Blocks", true),
		new BlockSet(2, "Math Blocks", false)
	};

	public static Session[] sessions = { 
		new Session(new Date(), 2000, 10, 12, "Spelling animals", 1, 123),
		new Session(new Date(), 1000, 20, 30, "Spelling animals", 1, 124),
		new Session(new Date(), 2200, 16, 20, "Simple addition", 2, 125),
		new Session(new Date(), 1800, 12, 20, "Simple addition", 2, 125),
	};
	
	public static Lesson[] lessons = { 
		new Lesson("Spelling animals", "A game for practicing spelling with common animal names", 1, 1, null),
		new Lesson("Simple addition", "Adding of single digit numbers", 2, 2, null)
	};
	
	static {
		HashMap<String,String> questions = new HashMap<String, String>();
		questions.put("How do you spell cat?", "{c}{a}{t}");
		questions.put("How do you spell dog?", "{d}{o}{g}");
		questions.put("How do you spell fish?", "{f}{i}{s}{h}");
		questions.put("How do you spell frog?", "{f}{r}{o}{g}");
		questions.put("How do you spell horse?", "{h}{o}{r}{s}{e}");
		questions.put("How do you spell goat?", "{g}{o}{a}{t}");
		questions.put("How do you spell pig?", "{p}{i}{g}");
		questions.put("How do you spell mouse?", "{m}{o}{u}{s}{e}");
		questions.put("How do you spell rooster?", "{r}{o}{o}{s}{t}{e}{r}");
		questions.put("How do you spell lizard?", "{l}{i}{z}{a}{r}{d}");
		questions.put("How do you spell eagle?", "{e}{a}{g}{l}{e}");
		questions.put("How do you spell antelope?", "{a}{n}{t}{e}{l}{o}{p}{e}");	
		lessons[0].questions = questions;
		
		HashMap<String,String> moreQuestions = new HashMap<String, String>();
		moreQuestions.put("What is 2 plus 2?", "{4}");
		moreQuestions.put("What is 6 plus 3?", "{9}");
		moreQuestions.put("What is 3 plus 1?", "{4}");
		moreQuestions.put("What is 1 plus 1?", "{2}");
		moreQuestions.put("What is 5 plus 1?", "{6}");
		moreQuestions.put("What is 3 plus 2?", "{5}");
		moreQuestions.put("What is 0 plus 1?", "{1}");
		moreQuestions.put("What is 1 plus 8?", "{9}");
		moreQuestions.put("What is 2 plus 4?", "{6}");
		moreQuestions.put("What is 6 plus 1?", "{7}");
		moreQuestions.put("What is 3 plus 3?", "{6}");
		moreQuestions.put("What is 3 plus 6?", "{9}");
		moreQuestions.put("What is 7 plus 1?", "{8}");
		moreQuestions.put("What is 4 plus 0?", "{4}");
		moreQuestions.put("What is 0 plus 0?", "{0}");
		moreQuestions.put("What is 4 plus 4?", "{8}");
		lessons[1].questions = moreQuestions;
		
		sessionLog = new HashMap<String, Integer>();
		sessionLog.put("How do you spell cat?", 1);
		sessionLog.put("How do you spell dog?", 0);
		sessionLog.put("How do you spell fish?", 1);
		sessionLog.put("How do you spell frog?", 1);
		sessionLog.put("How do you spell hors?", 1);
		sessionLog.put("How do you spell goat?", 1);
		sessionLog.put("How do you spell pig?", 1);
		sessionLog.put("How do you spell mouse?", 1);
		sessionLog.put("How do you spell rooster?", 1);
		sessionLog.put("How do you spell lizard?", 1);
		sessionLog.put("How do you spell eagle?", 1);
		sessionLog.put("How do you spell antelope?", 0);	
		
		sessions[0].log = sessionLog;
		sessions[1].log = sessionLog;
		sessions[2].log = sessionLog;
		
		Block[] spellingBlocks = {
			new Block("a", "1234-abcd"),
			new Block("b", "2038-abcd"),
			new Block("c", "3038-abcd"),
			new Block("d", "4958-abcd"),
			new Block("e", "4759-abcd"),
			new Block("f", "9274-abcd"),
			new Block("g", "1480-abcd"),
			new Block("h", "1939-abcd"),
			new Block("i", "3859-abcd"),
			new Block("j", "1037-abcd"),
			new Block("k", "4957-abcd"),
			new Block("l", "1947-abcd"),
			new Block("m", "4058-abcd"),
			new Block("n", "7284-abcd"),
			new Block("o", "1589-abcd"),
			new Block("p", "2657-abcd"),
			new Block("q", "2044-abcd"),
			new Block("r", "4758-abcd"),
			new Block("s", "3948-abcd"),
			new Block("t", "4899-abcd"),
			new Block("u", "2828-abcd"),
			new Block("v", "3931-abcd"),
			new Block("w", "0000-abcd"),
			new Block("x", "4950-abcd"),
			new Block("y", "1111-abcd"),
			new Block("z", "3859-abcd")
		};
		
		blockSets[0].addAll(spellingBlocks);
		
		Block[] mathBlocks = {
			new Block("0", "1234-abcd"),
			new Block("1", "2038-abcd"),
			new Block("2", "3038-abcd"),
			new Block("3", "4958-abcd"),
			new Block("4", "4759-abcd"),
			new Block("5", "9274-abcd"),
			new Block("6", "1480-abcd"),
			new Block("7", "1939-abcd"),
			new Block("8", "3859-abcd"),
			new Block("9", "1037-abcd"),
			new Block("*", "4957-abcd"),
			new Block("%", "1947-abcd"),
			new Block("/", "4058-abcd"),
			new Block("+", "7284-abcd"),
			new Block("-", "1589-abcd")
		};
		
		blockSets[1].addAll(mathBlocks);
		
		lessons[0].setBlockSet(0);
		
	}

}
