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
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import com.ryanmichela.trees.cost.ItemCost;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.ryanmichela.trees.rendering.TreeRenderer;

public class PlantTreeEventHandler implements Listener {

  private int boneMealConsumed;
  private Material coreblock;
  private boolean enabled;
  private final Plugin plugin;
  private final PopupHandler popup;
  private List<PhysicalCraftingRecipe> recipes = new ArrayList<>();
  private final TreeRenderer renderer;
  private static final HashMap<Character, Material> patternMaterialMap;
  static {
	  patternMaterialMap = new HashMap<Character, Material>() {
		  private static final long serialVersionUID = 1L; {
			  put('A', Material.ACACIA_SAPLING); 
			  put('O', Material.OAK_SAPLING);
			  put('D', Material.DARK_OAK_SAPLING);
			  put('B', Material.BIRCH_SAPLING);
			  put('M', Material.MANGROVE_PROPAGULE);
			  put('S', Material.SPRUCE_SAPLING);
			  put('J', Material.JUNGLE_SAPLING);
			  put('C', Material.CHERRY_SAPLING);
		  }
	  };
  }

	public void drawPattern(Material[][] p) {
		for (int i = 0; i < 5; i++) {
			String row = "";
			for (int j = 0; j < 5; j++) {
				if (p[i][j].equals(coreblock)) {
					row += "X";
				} else if (p[i][j].equals(Material.AIR)) {
					row += "-";
				} else {
					row += p[i][j].toString().charAt(0);
				}
			}
		}
	}
  
  	/*
  	 * Load up tree recipes and validate
  	 */
	public PlantTreeEventHandler(final Plugin plugin) {
		this.plugin = plugin;
		this.renderer = new TreeRenderer(plugin);
		this.popup = new PopupHandler(plugin);

		try {
			// Read config and create tree recipes
			if (!plugin.getConfig().contains("bone-meal-consumed")) {
				plugin.getLogger().warning("Warning: Please include a bone-meal-consumed config setting");
				boneMealConsumed = 64;
			} else {
				boneMealConsumed = plugin.getConfig().getInt("bone-meal-consumed");
			}

			if (!plugin.getConfig().contains("core")) {
				plugin.getLogger().warning("Warning: Please include a core block config setting");
				coreblock = Material.EMERALD_BLOCK;
			} else {
				try {
					coreblock = Material.valueOf(plugin.getConfig().getString("core"));
				} catch (Exception e) {
					plugin.getLogger().warning("Warning: Invalid core block provided. Default to EMERALD_BLOCK");
					coreblock = Material.EMERALD_BLOCK;
				}
			}
			patternMaterialMap.put('X', coreblock);

			final ConfigurationSection patternSection = plugin.getConfig().getConfigurationSection("planting-patterns");
			for (final String pattern : patternSection.getKeys(false)) {

				boolean validpattern = true;

				List<String> patternrows = patternSection.getStringList(pattern);
				Material[][] patternmaterials = new Material[patternrows.size()][patternrows.size()];

				int row = 0;
				int rowlength = 0;
				int prevrowlength = -1;
				if (patternrows.size() != 5) {
					plugin.getLogger().severe("Error: Invalid dimensions for pattern " + pattern + ". Needs to be a 5x5 matrix.");
					validpattern = false;
				} else {
					for (String patternrow : patternrows) {

						int col = 0;

						// check our row is consistent with previous rows and the number of rows
						rowlength = patternrow.length();
						if (patternrows.size() != rowlength && rowlength != 5) {
							plugin.getLogger().severe("Error: Dimensions for pattern " + pattern + " do not match. Needs to be a 5x5 matrix.");
							validpattern = false;
							break;
						}
						if (rowlength != prevrowlength && prevrowlength != -1) {
							plugin.getLogger().severe("Error: row lengths do not match for pattern " + pattern);
							validpattern = false;
							break;
						}
						prevrowlength = rowlength;

						CharacterIterator rcit = new StringCharacterIterator(patternrow);
						while (rcit.current() != CharacterIterator.DONE && validpattern) {
							if (!patternMaterialMap.keySet().contains(rcit.current()) && rcit.current() != 'X') {
								plugin.getLogger().severe("Error: " + rcit.current() + " is not a valid sapling code in pattern " + pattern);
								validpattern = false;
							}
							patternmaterials[row][col] = patternMaterialMap.get(rcit.current());
							rcit.next();
							col++;
						}
						if (!validpattern) {
							break;
						}
						row++;
					}
				}
				if (validpattern) {
					PhysicalCraftingRecipe recipe = new PhysicalCraftingRecipe(pattern, coreblock, patternmaterials);
					drawPattern(patternmaterials);
					recipes.add(recipe);
				}
			}
			enabled = true;
		} catch (final Exception e) {
			plugin.getLogger().severe(
					"The planting-pattern config section is invalid! Disabling survival planting of giant trees. " + e.toString());
			enabled = false;
		}
	}

    @EventHandler
	public void onPlayerInteract(final PlayerInteractEvent event) {
		if (!enabled) {
			return;
		}

		Block clickedBlock = event.getClickedBlock();
		ItemStack itemInMainHand = event.getPlayer().getInventory().getItemInMainHand();

		boolean goodtogo = event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
				&& clickedBlock.getType().equals(this.coreblock) && itemInMainHand.getType().equals(Material.BONE_MEAL)
				&& itemInMainHand.getAmount() == 64;
		if (!goodtogo) {
			return;
		}

		if (!event.getPlayer().hasPermission("gianttrees.grow")) {
			return;
		}

		final ItemStack itemInHand = event.getItem();

		final ItemCost cost = new ItemCost(Material.BONE_MEAL, (short) 15, boneMealConsumed);

		// build matrix of blocks 5x5 that are around the core
		Material[][] worldblocks = new Material[5][5];
		for (int x = 0; x < 5; x++) {
			for (int z = 0; z < 5; z++) {
				Block block = clickedBlock.getRelative(z - 2, 0, x - 2);
				worldblocks[x][z] = block.getType();
			}
		}

		// pass block matrix to each recipe to see if it matches
		boolean matched = false;
		ListIterator<PhysicalCraftingRecipe> rit = recipes.listIterator();
		while (rit.hasNext() && !matched) {
			PhysicalCraftingRecipe recipe = rit.next();
			if (recipe.matches(worldblocks)) {
				if (cost.isAffordable(event.getPlayer())) {
//					if (stackIsCorrect(itemInHand)) {
						final String treeType = recipe.getTreeName().toUpperCase();
						final Random seed = new Random(clickedBlock.getWorld().getSeed());
						final File treeFile = new File(plugin.getDataFolder(), "tree." + treeType + ".xml");
						final File rootFile = new File(plugin.getDataFolder(), "tree." + treeType + ".root.xml");
						event.setCancelled(true);
						cost.apply(event.getPlayer());
						popup.sendPopup(event.getPlayer(), "Stand back! Generating a giant " + treeType + " tree!");
						clearBlocks(clickedBlock);
						renderer.renderTree(clickedBlock.getLocation(), treeFile, rootFile, seed.nextInt(), true);
						matched = true;
//					}
				}
			}
		}
		if (!matched) {
			popup.sendPopup(event.getPlayer(), "Unable to match this pattern");
		}
	}

    /*
     * Clear the core blocka and surrounding saplings
     * Need to do this as some trees do not overwrite/eliminate these
     * 
     * @param - location of core block
     */
    private void clearBlocks(Block coreblock) {
    	for (int x = 0; x < 5; x++) {
			for (int z = 0; z < 5; z++) {
				Block block = coreblock.getRelative(z - 2, 0, x - 2);
				block.setType(Material.AIR);
			}
		}
    }

  private boolean stackIsCorrect(final ItemStack inHand) {
    return (inHand != null) && (inHand.getType() == Material.BONE_MEAL);
  }
}
