package com.cubes.learningcubes;

import java.util.ArrayList;
import java.util.Arrays;

/* Model class representing a block set */
public class BlockSet {
	ArrayList<Block> set;
	String name;
	int id;
	boolean enabled;
	
	public BlockSet(int id, String name, boolean enabled, ArrayList<Block> set) {
		this.id = id;
		this.name = name;
		this.enabled = enabled;
		if (set == null || set.size() == 0) {
			this.set = new ArrayList<Block>();
		} else {
			this.set = set;
		}
	}
	
	public BlockSet(int id, String name, boolean enabled) {
		this.id = id;
		this.name = name;
		this.enabled = enabled;
		this.set = new ArrayList<Block>();
	}
	
	public BlockSet(int id, String name, boolean enabled, Block[] set) {
		this.id = id;
		this.name = name;
		if (set == null || set.length == 0) {
			this.set = new ArrayList<Block>();
		} else {
			this.set.addAll(Arrays.asList(set));
		}
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
			if (item.id == blockId) {
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
