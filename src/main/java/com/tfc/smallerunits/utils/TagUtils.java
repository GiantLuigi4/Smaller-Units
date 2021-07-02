package com.tfc.smallerunits.utils;

import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

public class TagUtils {
	public static ITag.INamedTag<Block> getBlockTag(ResourceLocation name) {
		for (ITag.INamedTag<Block> allTag : BlockTags.getAllTags()) {
			if (allTag.getName().equals(name)) {
				return allTag;
			}
		}
		return BlockTags.createOptional(name);
	}
}
