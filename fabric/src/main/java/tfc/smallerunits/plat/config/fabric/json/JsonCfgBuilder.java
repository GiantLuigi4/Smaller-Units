package tfc.smallerunits.plat.config.fabric.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.screens.Screen;
import tfc.smallerunits.plat.config.fabric.CategoryBuilder;
import tfc.smallerunits.plat.config.fabric.CfgBuilder;
import tfc.smallerunits.plat.config.fabric.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class JsonCfgBuilder extends CfgBuilder {
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
	
	JsonObject wrapped;
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
				JsonObject dst = new JsonObject();
				for (String s : into.keySet()) {
					JsonElement intoEl = into.get(s);
					JsonElement fromEl = from.get(s);
					
					if (intoEl.isJsonObject()) {
						merge((JsonObject) intoEl, (JsonObject) fromEl);
					} else {
						dst.add(s, fromEl);
					}
				}
				for (String s : dst.keySet()) {
					into.remove(s);
					into.add(s, dst.get(s));
				}
			}
			
			@Override
			public void read(File fl) {
				try {
					FileInputStream inputStream = new FileInputStream(fl);
					JsonObject object = gson.fromJson(new String(inputStream.readAllBytes()), JsonObject.class);
					inputStream.close();
					
					merge(wrapped, object);
				} catch (Throwable err) {
					err.printStackTrace();
				}
			}
		};
	}
}
