//package com.tfc.smallerunits.mixins;
//
//import net.minecraftforge.fml.network.simple.IndexedMessageCodec;
//import net.minecraftforge.fml.network.simple.SimpleChannel;
//import org.apache.commons.lang3.tuple.Pair;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.gen.Accessor;
//import org.spongepowered.asm.mixin.gen.Invoker;
//
//import java.util.List;
//import java.util.function.Function;
//
//@Mixin(SimpleChannel.class)
//public interface SimpleChannelAccessor {
//	@Accessor("indexedCodec")
//	IndexedMessageCodec smaller_units_getCodec();
//
//	@Accessor("loginPackets")
//	List<Function<Boolean, ? extends List<? extends Pair<String, ?>>>> smaller_units_getLoginPackets();
//}
