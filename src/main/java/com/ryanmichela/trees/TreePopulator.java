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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.plugin.Plugin;

import com.ryanmichela.trees.rendering.TreeRenderer;

public class TreePopulator extends BlockPopulator {

  private final Plugin plugin;

  public TreePopulator(final Plugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void populate(final World world, final Random random, final Chunk chunk) {
    try {
      
      final Location refPoint = new Location(world,
    		  (chunk.getX() * 16) + random.nextInt(16),
    		  64,
              (chunk.getZ() * 16) + random.nextInt(16)
      );
      refPoint.setY(this.getHighestSoil(world.getHighestBlockAt(refPoint)));
      
      Biome biome = this.simplifyBiome(world.getBiome(refPoint.getBlockX(), refPoint.getBlockY(), refPoint.getBlockZ()));

      if (this.isAcceptableBiome(biome) && this.treeCanGrow(random)) {
        final String treeType = biome.name();
        
        // get list of available tree files with biome name in
        File [] biometreefiles = this.plugin.getDataFolder().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("biome.");
            }
        });
        ArrayList<String> availabletrees = new ArrayList<String>();
		String regex = "biome." + biome.name().toUpperCase() + "((-([0-9A-Z-_]*)){0,}).xml";
		Pattern pattern = Pattern.compile(regex);
		for ( int idx = 0; idx < biometreefiles.length; idx++ ) {
			Matcher match = pattern.matcher( biometreefiles[idx].toString() );
			if ( match.find() ) {
				availabletrees.add(match.group());
			}
		}
		        
        // pick a random file from set
		int randtreeidx = (int) ((Math.random() * (availabletrees.size() - 0)) + 0);
        
        //final File treeFile = new File(this.plugin.getDataFolder(), "biome."
        //        + treeType
        //        + ".xml");
        //final File rootFile = new File(this.plugin.getDataFolder(), "biome."
        //        + treeType
        //        + ".root.xml");
		File treeFile = null;
		File rootFile = null;
		treeFile = new File( this.plugin.getDataFolder(), availabletrees.get(randtreeidx) );
		rootFile = new File( this.plugin.getDataFolder(), availabletrees.get(randtreeidx).replace( ".xml", ".root.xml" ) );

        if ( treeFile.exists() && rootFile.exists() ) {
          final TreeRenderer renderer = new TreeRenderer(this.plugin);
          renderer.renderTree(refPoint, treeFile, rootFile, random.nextInt(), false);
        }
      }
    } catch (Exception ex) {
      plugin.getLogger().log(Level.SEVERE, "Exception caught generating giant tree", ex);
    }
  }

  int getHighestSoil(Block highestBlock) {
    while (highestBlock.getY() > 0
           && (highestBlock.getType() != Material.GRASS_BLOCK)
           && (highestBlock.getType() != Material.MUD)
           && (highestBlock.getType() != Material.MYCELIUM)
           && (highestBlock.getType().toString().endsWith("DIRT"))
           && (!highestBlock.getType().toString().endsWith("SAND"))
           && (!highestBlock.getType().toString().endsWith("TERRACOTTA"))) {
      highestBlock = highestBlock.getRelative(BlockFace.DOWN);
    }
    return highestBlock.getY();
  }

  private boolean isAcceptableBiome(final Biome biome) {
    return (biome == Biome.FOREST)
        || (biome == Biome.BIRCH_FOREST)
        || (biome == Biome.SWAMP)
        || (biome == Biome.MANGROVE_SWAMP)
        || (biome == Biome.JUNGLE)
        || (biome == Biome.BADLANDS)
        || (biome == Biome.DARK_FOREST)
        || (biome == Biome.TAIGA)
        || (biome == Biome.SAVANNA);
  }

  private Biome simplifyBiome(final Biome biome) {
    switch (biome) {
      case FOREST:
      case WINDSWEPT_FOREST:
      case FLOWER_FOREST:
        return Biome.FOREST;
      case BIRCH_FOREST:
      case OLD_GROWTH_BIRCH_FOREST:
        return Biome.BIRCH_FOREST;
      case SWAMP:
        return Biome.SWAMP;
      case MANGROVE_SWAMP:
    	return Biome.MANGROVE_SWAMP;
      case JUNGLE:
      case SPARSE_JUNGLE:
        return Biome.JUNGLE;
      case DARK_FOREST:
        return Biome.DARK_FOREST;
      case TAIGA:
      case SNOWY_TAIGA:
        return Biome.TAIGA;
      case SAVANNA:
      case WINDSWEPT_SAVANNA:
      case SAVANNA_PLATEAU:
        return Biome.SAVANNA;
      case BADLANDS:
      case ERODED_BADLANDS:
      case WOODED_BADLANDS:
    	return Biome.BADLANDS;
      default:
        return null;
    }
  }

  private boolean treeCanGrow(final Random random) {
    double growChance = this.plugin.getConfig().getDouble("treeGrowthPercentChance");
    if (growChance > 100.0) {
      growChance = 100.0;
      this.plugin.getLogger().warning("treeGrowthPercentChance > 100. Assuming 100.");
    }
    if (growChance < 0.0) {
      growChance = 0.0;
      this.plugin.getLogger().warning("treeGrowthPercentChance < 0. Assuming zero.");
    }
    final double randomRoll = random.nextDouble() * 100.0;
    return growChance > randomRoll;
  }
}
