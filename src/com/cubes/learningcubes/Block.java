package com.cubes.learningcubes;

/* Model class representing a single block */
public class Block {
	String text;
	String tagId;
	long id;
	long blockSetId; 
	
	public Block(String text, String tagId, long blockSetId, long id) {
		this.text = text;
		this.tagId = tagId;
		this.blockSetId = blockSetId;
		this.id = id;
	}
	
	public Block(String text, String tagId, long blockSetId) {
		this.text = text;
		this.tagId = tagId;
		this.blockSetId = blockSetId;
	}
}
