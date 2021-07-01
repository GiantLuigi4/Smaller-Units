import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

public class LangGenerator {
	private static final String[] itemRegistryNames = new String[]{
			"smallerunits:su_grower",
			"smallerunits:su_shrinker",
	};
	private static final String[] blockRegistryNames = new String[]{
			"smallersunits:su",
	};
	
	public static void main(String[] args) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		JsonObject object = new JsonObject();
		
		for (String itemRegistryName : itemRegistryNames) {
			ResourceLocation location = new ResourceLocation(itemRegistryName);
			object.addProperty("item." + location.getNamespace() + "." + location.getPath(), toName(location.getPath()));
		}
		for (String itemRegistryName : blockRegistryNames) {
			ResourceLocation location = new ResourceLocation(itemRegistryName);
			object.addProperty("block." + location.getNamespace() + "." + location.getPath(), toName(location.getPath()));
		}
		
		System.out.println(gson.toJson(object));
	}
	
	public static String toName(String src) {
		StringBuilder out = new StringBuilder();
		for (String s : src.split("_")) {
			out.append(s.substring(0, 1).toUpperCase()).append(s.substring(1)).append(" ");
		}
		return out.substring(0, out.length() - 1);
	}
}
