package tfc.smallerunits.utils.config.annoconfg.fabric.json;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import tfc.smallerunits.utils.config.annoconfg.builder.CategoryBuilder;

import java.util.function.Supplier;

public class JsonCategoryBuilder extends CategoryBuilder {
	JsonObject wrapped;
	String root;
	
	public JsonCategoryBuilder(String root, JsonObject wrapped) {
		this.root = root;
		this.wrapped = wrapped;
	}
	
	@Override
	public CategoryBuilder subcategory(String translationName) {
		JsonObject obj = new JsonObject();
		return new JsonCategoryBuilder(translationName, obj);
	}
	
	@Override
	public void finish(CategoryBuilder builder2) {
		if (builder2 instanceof JsonCategoryBuilder subCategoryBuilder) {
			wrapped.add(subCategoryBuilder.root.substring(root.length() + 1), subCategoryBuilder.wrapped);
		} else {
			throw new RuntimeException("Can only add json categories to a category");
		}
	}
	
	@Override
	public Supplier<Integer> intRange(String translationName, int min, int defaultV, int max) {
		String name = translationName.substring(root.length() + 1);
		wrapped.addProperty(name, defaultV);
		
		return () -> {
			if (!wrapped.has(name)) {
				wrapped.addProperty(name, defaultV);
				return defaultV;
			}
			
			JsonPrimitive primitive = wrapped.getAsJsonPrimitive(name);
			if (!primitive.isNumber()) {
				wrapped.remove(name);
				wrapped.addProperty(name, defaultV);
				return defaultV;
			}
			
			int i = primitive.getAsInt();
			if (i < min) {
				wrapped.remove(name);
				wrapped.addProperty(name, min);
				return min;
			}
			if (i > max) {
				wrapped.remove(name);
				wrapped.addProperty(name, max);
				return max;
			}
			
			return i;
		};
	}
	
	@Override
	public Supplier<Integer> intValue(String translationName, int defaultV) {
		String name = translationName.substring(root.length() + 1);
		wrapped.addProperty(name, defaultV);
		
		return () -> {
			if (!wrapped.has(name)) {
				wrapped.addProperty(name, defaultV);
				return defaultV;
			}
			
			JsonPrimitive primitive = wrapped.getAsJsonPrimitive(name);
			if (!primitive.isNumber()) {
				wrapped.remove(name);
				wrapped.addProperty(name, defaultV);
				return defaultV;
			}
			
			return primitive.getAsInt();
		};
	}
	
	@Override
	public Supplier<Long> longRange(String translationName, long min, long defaultV, long max) {
		String name = translationName.substring(root.length() + 1);
		wrapped.addProperty(name, defaultV);
		
		return () -> {
			if (!wrapped.has(name)) {
				wrapped.addProperty(name, defaultV);
				return defaultV;
			}
			
			JsonPrimitive primitive = wrapped.getAsJsonPrimitive(name);
			if (!primitive.isNumber()) {
				wrapped.remove(name);
				wrapped.addProperty(name, defaultV);
				return defaultV;
			}
			
			long i = primitive.getAsLong();
			if (i < min) {
				wrapped.remove(name);
				wrapped.addProperty(name, min);
				return min;
			}
			if (i > max) {
				wrapped.remove(name);
				wrapped.addProperty(name, max);
				return max;
			}
			
			return i;
		};
	}
	
	@Override
	public Supplier<Long> longValue(String translationName, long defaultV) {
		String name = translationName.substring(root.length() + 1);
		wrapped.addProperty(name, defaultV);
		
		return () -> {
			if (!wrapped.has(name)) {
				wrapped.addProperty(name, defaultV);
				return defaultV;
			}
			
			JsonPrimitive primitive = wrapped.getAsJsonPrimitive(name);
			if (!primitive.isNumber()) {
				wrapped.remove(name);
				wrapped.addProperty(name, defaultV);
				return defaultV;
			}
			
			return primitive.getAsLong();
		};
	}
	
	@Override
	public Supplier<Double> doubleRange(String translationName, double min, double defaultV, double max) {
		String name = translationName.substring(root.length() + 1);
		wrapped.addProperty(name, defaultV);
		
		return () -> {
			if (!wrapped.has(name)) {
				wrapped.addProperty(name, defaultV);
				return defaultV;
			}
			
			JsonPrimitive primitive = wrapped.getAsJsonPrimitive(name);
			if (!primitive.isNumber()) {
				wrapped.remove(name);
				wrapped.addProperty(name, defaultV);
				return defaultV;
			}
			
			double i = primitive.getAsDouble();
			if (i < min) {
				wrapped.remove(name);
				wrapped.addProperty(name, min);
				return min;
			}
			if (i > max) {
				wrapped.remove(name);
				wrapped.addProperty(name, max);
				return max;
			}
			
			return i;
		};
	}
	
	@Override
	public Supplier<Double> doubleValue(String translationName, double defaultV) {
		String name = translationName.substring(root.length() + 1);
		wrapped.addProperty(name, defaultV);
		
		return () -> {
			if (!wrapped.has(name)) {
				wrapped.addProperty(name, defaultV);
				return defaultV;
			}
			
			JsonPrimitive primitive = wrapped.getAsJsonPrimitive(name);
			if (!primitive.isNumber()) {
				wrapped.remove(name);
				wrapped.addProperty(name, defaultV);
				return defaultV;
			}
			
			return primitive.getAsDouble();
		};
	}
	
	@Override
	public Supplier<Boolean> boolValue(String translationName, boolean defaultV) {
		String name = translationName.substring(root.length() + 1);
		wrapped.addProperty(name, defaultV);
		
		return () -> {
			if (!wrapped.has(name)) {
				wrapped.addProperty(name, defaultV);
				return defaultV;
			}
			
			JsonPrimitive primitive = wrapped.getAsJsonPrimitive(name);
			if (!primitive.isBoolean()) {
				wrapped.remove(name);
				wrapped.addProperty(name, defaultV);
				return defaultV;
			}
			
			return primitive.getAsBoolean();
		};
	}
}
