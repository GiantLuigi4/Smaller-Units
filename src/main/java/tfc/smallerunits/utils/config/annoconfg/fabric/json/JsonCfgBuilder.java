package tfc.smallerunits.utils.config.annoconfg.fabric.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.screens.Screen;
import tfc.smallerunits.utils.config.annoconfg.Config;
import tfc.smallerunits.utils.config.annoconfg.builder.CategoryBuilder;
import tfc.smallerunits.utils.config.annoconfg.builder.CfgBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class JsonCfgBuilder extends CfgBuilder {
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
	
	final JsonObject wrapped;
	Runnable onConfigChange;
	String root;
	
	public JsonCfgBuilder(String root) {
		super();
		wrapped = new JsonObject();
		this.root = root;
	}
	
	@Override
	public CategoryBuilder rootCategory() {
		return new JsonCategoryBuilder(root, wrapped);
	}
	
	@Override
	public void setSaveFunction(Runnable onConfigChange) {
		this.onConfigChange = onConfigChange;
	}
	
	@Override
	public Function<Screen, Screen> createScreen() {
		return null;
	}
	
	@Override
	public Config build() {
		return new Config() {
			@Override
			public void write(File fl) {
				try {
					FileOutputStream outputStream = new FileOutputStream(fl);
					outputStream.write(gson.toJson(wrapped).getBytes(StandardCharsets.UTF_8));
					outputStream.close();
					outputStream.flush();
				} catch (Throwable ignored) {
				}
			}
			
			void merge(JsonObject into, JsonObject from) {
				for (String s : into.keySet().toArray(new String[0])) {
					JsonElement intoEl = into.get(s);
					JsonElement fromEl = from.get(s);
					
					if (intoEl.isJsonObject()) {
						merge((JsonObject) intoEl, (JsonObject) fromEl);
					} else {
						into.remove(s);
						into.add(s, fromEl);
					}
				}
			}
			
			@Override
			public void read(File fl) {
				try {
					FileInputStream inputStream = new FileInputStream(fl);
					JsonObject object = gson.fromJson(new String(inputStream.readAllBytes()), JsonObject.class);
					inputStream.close();
					
					synchronized (wrapped) {
						merge(wrapped, object);
					}
				} catch (Throwable err) {
					err.printStackTrace();
				}
			}
		};
	}
}
