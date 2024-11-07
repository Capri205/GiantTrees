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
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
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

  // complete list of biomes for reference
  /*
  Biome OCEAN = getBiome("ocean");
  Biome PLAINS = getBiome("plains");
  Biome DESERT = getBiome("desert");
  Biome WINDSWEPT_HILLS = getBiome("windswept_hills");
  Biome FOREST = getBiome("forest");
  Biome TAIGA = getBiome("taiga");
  Biome SWAMP = getBiome("swamp");
  Biome MANGROVE_SWAMP = getBiome("mangrove_swamp");
  Biome RIVER = getBiome("river");
  Biome NETHER_WASTES = getBiome("nether_wastes");
  Biome THE_END = getBiome("the_end");
  Biome FROZEN_OCEAN = getBiome("frozen_ocean");
  Biome FROZEN_RIVER = getBiome("frozen_river");
  Biome SNOWY_PLAINS = getBiome("snowy_plains");
  Biome MUSHROOM_FIELDS = getBiome("mushroom_fields");
  Biome BEACH = getBiome("beach");
  Biome JUNGLE = getBiome("jungle");
  Biome SPARSE_JUNGLE = getBiome("sparse_jungle");
  Biome DEEP_OCEAN = getBiome("deep_ocean");
  Biome STONY_SHORE = getBiome("stony_shore");
  Biome SNOWY_BEACH = getBiome("snowy_beach");
  Biome BIRCH_FOREST = getBiome("birch_forest");
  Biome DARK_FOREST = getBiome("dark_forest");
  Biome SNOWY_TAIGA = getBiome("snowy_taiga");
  Biome OLD_GROWTH_PINE_TAIGA = getBiome("old_growth_pine_taiga");
  Biome WINDSWEPT_FOREST = getBiome("windswept_forest");
  Biome SAVANNA = getBiome("savanna");
  Biome SAVANNA_PLATEAU = getBiome("savanna_plateau");
  Biome BADLANDS = getBiome("badlands");
  Biome WOODED_BADLANDS = getBiome("wooded_badlands");
  Biome SMALL_END_ISLANDS = getBiome("small_end_islands");
  Biome END_MIDLANDS = getBiome("end_midlands");
  Biome END_HIGHLANDS = getBiome("end_highlands");
  Biome END_BARRENS = getBiome("end_barrens");
  Biome WARM_OCEAN = getBiome("warm_ocean");
  Biome LUKEWARM_OCEAN = getBiome("lukewarm_ocean");
  Biome COLD_OCEAN = getBiome("cold_ocean");
  Biome DEEP_LUKEWARM_OCEAN = getBiome("deep_lukewarm_ocean");
  Biome DEEP_COLD_OCEAN = getBiome("deep_cold_ocean");
  Biome DEEP_FROZEN_OCEAN = getBiome("deep_frozen_ocean");
  Biome THE_VOID = getBiome("the_void");
  Biome SUNFLOWER_PLAINS = getBiome("sunflower_plains");
  Biome WINDSWEPT_GRAVELLY_HILLS = getBiome("windswept_gravelly_hills");
  Biome FLOWER_FOREST = getBiome("flower_forest");
  Biome ICE_SPIKES = getBiome("ice_spikes");
  Biome OLD_GROWTH_BIRCH_FOREST = getBiome("old_growth_birch_forest");
  Biome OLD_GROWTH_SPRUCE_TAIGA = getBiome("old_growth_spruce_taiga");
  Biome WINDSWEPT_SAVANNA = getBiome("windswept_savanna");
  Biome ERODED_BADLANDS = getBiome("eroded_badlands");
  Biome BAMBOO_JUNGLE = getBiome("bamboo_jungle");
  Biome SOUL_SAND_VALLEY = getBiome("soul_sand_valley");
  Biome CRIMSON_FOREST = getBiome("crimson_forest");
  Biome WARPED_FOREST = getBiome("warped_forest");
  Biome BASALT_DELTAS = getBiome("basalt_deltas");
  Biome DRIPSTONE_CAVES = getBiome("dripstone_caves");
  Biome LUSH_CAVES = getBiome("lush_caves");
  Biome DEEP_DARK = getBiome("deep_dark");
  Biome MEADOW = getBiome("meadow");
  Biome GROVE = getBiome("grove");
  Biome SNOWY_SLOPES = getBiome("snowy_slopes");
  Biome FROZEN_PEAKS = getBiome("frozen_peaks");
  Biome JAGGED_PEAKS = getBiome("jagged_peaks");
  Biome STONY_PEAKS = getBiome("stony_peaks");
  Biome CHERRY_GROVE = getBiome("cherry_grove");
  */

  private boolean isAcceptableBiome(final Biome biome) {
	    return (biome == Biome.FOREST)
	        || (biome == Biome.BIRCH_FOREST)
	        || (biome == Biome.CHERRY_GROVE)
	        || (biome == Biome.SWAMP)
	        || (biome == Biome.MANGROVE_SWAMP)
	        || (biome == Biome.JUNGLE)
	        || (biome == Biome.BADLANDS)
	        || (biome == Biome.DARK_FOREST)
	        || (biome == Biome.TAIGA)
	        || (biome == Biome.SAVANNA);
  }

  private Biome simplifyBiome(final Biome biome) {

	  switch (biome.getKey().value().toUpperCase()) {

      case "FOREST":
      case "WINDSWEPT_FOREST":
      case "FLOWER_FOREST":
        return Biome.FOREST;
      case "BIRCH_FOREST":
      case "OLD_GROWTH_BIRCH_FOREST":
        return Biome.BIRCH_FOREST;
      case "CHERRY_GROVE":
        return Biome.CHERRY_GROVE;
      case "SWAMP":
        return Biome.SWAMP;
      case "MANGROVE_SWAMP":
    	return Biome.MANGROVE_SWAMP;
      case "JUNGLE":
      case "SPARSE_JUNGLE":
        return Biome.JUNGLE;
      case "DARK_FOREST":
        return Biome.DARK_FOREST;
      case "TAIGA":
      case "SNOWY_TAIGA":
        return Biome.TAIGA;
      case "SAVANNA":
      case "WINDSWEPT_SAVANNA":
      case "SAVANNA_PLATEAU":
        return Biome.SAVANNA;
      case "BADLANDS":
      case "ERODED_BADLANDS":
      case "WOODED_BADLANDS":
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
