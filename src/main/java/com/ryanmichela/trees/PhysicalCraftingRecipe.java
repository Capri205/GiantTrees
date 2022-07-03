/*
 * Copyright (C) 2014 Ryan Michela
 * Copyright (C) 2016 Ronald Jack Jenkins Jr.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ryanmichela.trees;

import java.util.Arrays;
import org.bukkit.Material;

/**
 * PhysicalCraftingRecipe represents the Material pattern for a tree and
 * is used to match against the blocks laid out horizontally in the world
 */
public class PhysicalCraftingRecipe {

  private String treename = null;
  private Material[][] pattern = new Material[5][5];
  private Material coreblock = null;
  private boolean domatchrotations = false;

  /**
   * Creates a PhysicalCraftingRecipe from a 2D pattern of materials.
   * The pattern must be a 5x5 square.
   *
   * @param treename - Name of the tree - matches tree filename
   * @param coreblock - The material of the central core block
   * @param pattern - The rectangle of Materials to match.
   */
  public PhysicalCraftingRecipe(String treename, Material coreblock, final Material[][] pattern) {

    this.treename = treename;
    this.coreblock = coreblock;
    
    // create a copy of the pattern for this recipe and determine if materials are all the same
    // so we don't have to waste time rotating the matrix when trying to match the pattern
    Material prevmaterial = pattern[0][0];
    for (int x = 0; x < 5; x++) {
    	for (int z = 0; z < 5; z++) {
    		this.pattern[x][z] = pattern[x][z];
    		if (!pattern[x][z].equals(prevmaterial)) {
    			if (!pattern[x][z].equals(this.coreblock)) {
    				this.domatchrotations = true;
    			}
    		}
    		if (!pattern[x][z].equals(this.coreblock)) {
    			prevmaterial = pattern[x][z];
    		}
    	}
    }
  }

  /**
   * Sets the name of the tree for this recipe
   * 
   * @param treename
   * 
   * @return void
   */
  public void setTreeName(String treename) {
	  this.treename = treename;
  }
  
  /*
   * Returns the name of the tree for this recipe
   * 
   * @param none
   * 
   * @return treename
   */
  public String getTreeName() {
	  return this.treename;
  }

  /**
   * Determines if this PhysicalCraftingRecipe matches the blocks in the world.
   *
   * @param coreblock - the core block clicked
   * @return true or false if a match was found
   */
  public boolean matches(Material[][] worldblocks) {
    boolean patternmatch = false;
    int rotations = 0;

	if (Arrays.deepEquals(worldblocks, this.pattern)) {
		patternmatch = true;
	}
	// rotate through 270 degrees (3 x 90 degree rotations) as we've already compared one orientation
	if (domatchrotations) {
		while (rotations < 3 && !patternmatch) {
			this.pattern = this.rotateCW(this.pattern);
			rotations++;
			if (Arrays.deepEquals(worldblocks, this.pattern)) {
				patternmatch = true;
			}
		}
    }

    return patternmatch;
  }
  
  /*
   * Transposes an array 90 degrees clockwise (fixed 5x5 matrix)
   * 
   * @param matrix
   * @return matrix rotated 90 degrees clockwise
   */
  private Material[][] rotateCW(Material[][] matrix) {
	    Material[][] ret = new Material[5][5];
	    for (int r = 0; r < 5; r++) {
	        for (int c = 0; c < 5; c++) {
	            ret[c][5-1-r] = matrix[r][c];
	        }
	    }
	    return ret;
	}
}
