package com.cubes.learningcubes;

import java.util.ArrayList;
import java.util.Arrays;

/* Model class representing a block set */
public class BlockSet {
	ArrayList<Block> set;
	String name;
	long id;
	long remoteId;
	boolean enabled;
	static int ENABLED = 1;
	static int DISABLED = 0;
	
	public BlockSet(String name, boolean enabled, ArrayList<Block> set, long id, long remoteId) {
		this.id = id;
		this.name = name;
		this.enabled = enabled;
		if (set == null || set.size() == 0) {
			this.set = new ArrayList<Block>();
		} else {
			this.set = set;
		}
		this.remoteId = remoteId;
	}
	
	public BlockSet(String name, boolean enabled, ArrayList<Block> set) {
		this(name, enabled, set, 0, 0);
	}
	
	public void add(Block newBlock) {
		this.set.add(newBlock);
	}
	
	public void addAll(Block[] list) {
		for (Block b : list) {
			add(b);
		}
	}
	
	
	public Block get(String blockId) {
		for (Block item : set) {
			if (item.tagId == blockId) {
				return item;
			}
		}
		return null;
	}
	
	public Block[] asArray() {
		Block[] blockList = new Block[set.size()];
		for (int i = 0; i < set.size(); i++) {
			blockList[i] = set.get(i);
		}
		return blockList;
	}
	
	public int size() {
		return set.size();
	}
}
