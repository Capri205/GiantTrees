package com.ryanmichela.trees.rendering;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Copyright 2014 Ryan Michela
 */
public class WorldChangeTracker {
  private Map<WorldChangeKey, WorldChange> changes = new HashMap<WorldChangeKey, WorldChange>(1000);

  public void addChange(final Vector location, final Material material,
                        final byte materialData, final boolean overwrite) {
    this.addChange(new WorldChange(location, material, materialData), overwrite);
  }

  public void addChange(final WorldChange worldChange, final boolean overwrite) {
    final WorldChangeKey key = new WorldChangeKey(
            worldChange.location.getBlockX(),
            worldChange.location.getBlockY(),
            worldChange.location.getBlockZ());
    if (this.changes.containsKey(key)) {
      if (overwrite) {
        this.changes.put(key, worldChange);
      }
    } else {
      this.changes.put(key, worldChange);
    }
  }

  public WorldChange getChange(final WorldChangeKey key) {
    return this.changes.get(key);
  }

  public Collection<WorldChange> getChanges() {
    return this.changes.values();
  }

  public void applyChanges(Location refPoint) {
    Set<WorldChangeKey> touchedChunks = new HashSet<WorldChangeKey>();

    for (WorldChange change : changes.values()) {
      Location changeLoc = refPoint.clone().add(change.location);
      Block block = changeLoc.getBlock();
      block.setType(change.material);
      block.setData(change.materialData, false);
      touchedChunks.add(new WorldChangeKey(block.getChunk().getX(), -1, block.getChunk().getZ()));
    }

    for (WorldChangeKey chunkKey : touchedChunks) {
      refPoint.getWorld().refreshChunk(chunkKey.x, chunkKey.z);
    }
  }
}