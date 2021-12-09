package tfc.smallerunits.utils.rendering;

import com.jozufozu.flywheel.backend.model.IndexedModel;
import net.minecraft.client.renderer.RenderType;

import java.util.Comparator;

public class ModelStorage implements Comparable<ModelStorage>, Comparator<ModelStorage> {
	public RenderType renderType;
	public IndexedModel model;
	public boolean isPresent = false;
	
	public ModelStorage() {
	}
	
	@Override
	public int compareTo(ModelStorage o) {
		RenderType otherType = o.renderType;
		if (RenderTypeHelper.getType(otherType) == RenderTypeHelper.getType(renderType)) return 0;
		if (RenderTypeHelper.getType(renderType) == RenderTypeHelper.getType(RenderType.getTranslucent())) return 1;
		if (RenderTypeHelper.getType(renderType) == RenderTypeHelper.getType(RenderType.getCutoutMipped())) return 1;
		if (RenderTypeHelper.getType(renderType) == RenderTypeHelper.getType(RenderType.getCutout())) return 1;
		if (RenderTypeHelper.getType(otherType) == RenderTypeHelper.getType(RenderType.getTranslucent())) return -1;
		if (RenderTypeHelper.getType(otherType) == RenderTypeHelper.getType(RenderType.getCutoutMipped())) return 1;
		if (RenderTypeHelper.getType(otherType) == RenderTypeHelper.getType(RenderType.getCutout())) return 1;
//		if (renderType == RenderTypeHelper.getType(RenderType.getCutout())) {
//			if (otherType == RenderType.getTranslucent()) return 1;
//			if (otherType != RenderType.getSolid()) return 1;
//			else return -1;
//		}
//		if (otherType == RenderTypeHelper.getType(RenderType.getCutoutMipped())) return 1;
//		if (otherType == RenderTypeHelper.getType(RenderType.getCutout())) return 1;
//		if (renderType == RenderTypeHelper.getType(RenderType.getTranslucent())) return 1;
//		if (renderType == RenderTypeHelper.getType(RenderType.getSolid())) return -1;
//		if (otherType == RenderTypeHelper.getType(RenderType.getTranslucent())) return -1;
//		if (otherType == RenderTypeHelper.getType(RenderType.getCutout())) return -1;
//		if (otherType == RenderTypeHelper.getType(RenderType.getCutoutMipped())) return -1;
//		if (RenderTypeHelper.getType(otherType) == RenderTypeHelper.getType(RenderType.getTranslucent())) {
//			if (RenderTypeHelper.getType(renderType) == RenderTypeHelper.getType(RenderType.getTranslucent())) {
//				return 0;
//			}
//			return 1;
//		}
		return 0;
	}
	
	@Override
	public int compare(ModelStorage o1, ModelStorage o2) {
		return o1.compareTo(o2);
	}
}
