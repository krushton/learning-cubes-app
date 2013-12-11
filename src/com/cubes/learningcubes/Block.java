package com.cubes.learningcubes;

/* Model class representing a single block */
public class Block {
	String text;
	String tagId;
	long id;
	long blockSetId; 
	String localUrl;
	String remoteUrl;
	
	public Block(String text, String tagId, long blockSetId, long id, String localUrl, String remoteUrl) {
		this.text = text;
		this.tagId = tagId;
		this.blockSetId = blockSetId;
		this.id = id;
		this.localUrl = localUrl;
		this.remoteUrl = remoteUrl;
	}
	
	public Block(String text, String tagId, long blockSetId, String localUrl, String remoteUrl) {
		this.text = text;
		this.tagId = tagId;
		this.blockSetId = blockSetId;
		this.localUrl = localUrl;
		this.remoteUrl = remoteUrl;
	}
}
