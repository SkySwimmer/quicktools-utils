package usr.skyswimmer.quicktoolsutils.json;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonUtils {

	public static JsonObject loadConfig(File configFile) throws IOException {
		try {
			return JsonParser.parseString(Files.readString(configFile.toPath())).getAsJsonObject();
		} catch (Exception e) {
			throw new IOException("Invalid config file", e);
		}
	}

	public static JsonElement getElementOrError(String scope, JsonObject object, String element) throws IOException {
		if (!object.has(element))
			throw new IOException(scope + " is missing required element '" + element + "'");
		return object.get(element);
	}

	public static Collection<String> stringArrayAsCollection(JsonArray arr) {
		ArrayList<String> lst = new ArrayList<String>();
		for (JsonElement ele : arr) {
			lst.add(ele.getAsString());
		}
		return lst;
	}

	public static HashMap<String, String> objectAsStringHashMap(JsonObject arr) {
		HashMap<String, String> lst = new LinkedHashMap<String, String>();
		for (String key : arr.keySet()) {
			lst.put(key, arr.get(key).getAsString());
		}
		return lst;
	}

	public static HashMap<String, JsonObject> objectAsJsonObjectHashMap(JsonObject arr) {
		HashMap<String, JsonObject> lst = new LinkedHashMap<String, JsonObject>();
		for (String key : arr.keySet()) {
			lst.put(key, arr.get(key).getAsJsonObject());
		}
		return lst;
	}

	public static String stringOrNull(JsonElement ele) {
		if (ele.isJsonNull())
			return null;
		return ele.getAsString();
	}

	public static boolean getBooleanOrError(String scope, JsonObject object, String element) throws IOException {
		JsonElement ele = getElementOrError(scope, object, element);
		if (!ele.isJsonPrimitive()
				|| (!ele.getAsString().equalsIgnoreCase("false") && !ele.getAsString().equalsIgnoreCase("true")))
			throw new IOException(scope + " had invalid value for " + element + " (expected boolean value)");
		return ele.getAsBoolean();
	}

	public static int getIntOrError(String scope, JsonObject object, String element) throws IOException {
		JsonElement ele = getElementOrError(scope, object, element);
		try {
			return ele.getAsInt();
		} catch (Exception e) {
			throw new IOException(scope + " had invalid value for " + element + " (expected integer value)");
		}
	}

	public static long getLongOrError(String scope, JsonObject object, String element) throws IOException {
		JsonElement ele = getElementOrError(scope, object, element);
		try {
			return ele.getAsLong();
		} catch (Exception e) {
			throw new IOException(scope + " had invalid value for " + element + " (expected integer value)");
		}
	}

	public static double getDoubleOrError(String scope, JsonObject object, String element) throws IOException {
		JsonElement ele = getElementOrError(scope, object, element);
		try {
			return ele.getAsDouble();
		} catch (Exception e) {
			throw new IOException(scope + " had invalid value for " + element + " (expected floating point value)");
		}
	}

	public static float getFloatOrError(String scope, JsonObject object, String element) throws IOException {
		JsonElement ele = getElementOrError(scope, object, element);
		try {
			return ele.getAsFloat();
		} catch (Exception e) {
			throw new IOException(scope + " had invalid value for " + element + " (expected floating point value)");
		}
	}

	public static String getStringOrError(String scope, JsonObject object, String element) throws IOException {
		JsonElement ele = getElementOrError(scope, object, element);
		if (!ele.isJsonPrimitive())
			throw new IOException(scope + " had invalid value for " + element + " (expected String value)");
		return ele.getAsString();
	}

	public static JsonObject getObjectOrError(String scope, JsonObject object, String element) throws IOException {
		JsonElement ele = getElementOrError(scope, object, element);
		if (!ele.isJsonObject())
			throw new IOException(scope + " had invalid value for " + element + " (expected json object value)");
		return ele.getAsJsonObject();
	}

	public static JsonArray getArrayOrError(String scope, JsonObject object, String element) throws IOException {
		JsonElement ele = getElementOrError(scope, object, element);
		if (!ele.isJsonArray())
			throw new IOException(scope + " had invalid value for " + element + " (expected json array value)");
		return ele.getAsJsonArray();
	}

	public static boolean getBooleanOrError(String scope, JsonElement ele) throws IOException {
		if (!ele.isJsonPrimitive()
				|| (!ele.getAsString().equalsIgnoreCase("false") && !ele.getAsString().equalsIgnoreCase("true")))
			throw new IOException(scope + " had invalid value (expected boolean value)");
		return ele.getAsBoolean();
	}

	public static int getIntOrError(String scope, JsonElement ele) throws IOException {
		try {
			return ele.getAsInt();
		} catch (Exception e) {
			throw new IOException(scope + " had invalid value (expected integer value)");
		}
	}

	public static long getLongOrError(String scope, JsonElement ele) throws IOException {
		try {
			return ele.getAsLong();
		} catch (Exception e) {
			throw new IOException(scope + " had invalid value (expected integer value)");
		}
	}

	public static double getDoubleOrError(String scope, JsonElement ele) throws IOException {
		try {
			return ele.getAsDouble();
		} catch (Exception e) {
			throw new IOException(scope + " had invalid value (expected floating point value)");
		}
	}

	public static float getFloatOrError(String scope, JsonElement ele) throws IOException {
		try {
			return ele.getAsFloat();
		} catch (Exception e) {
			throw new IOException(scope + " had invalid value (expected floating point value)");
		}
	}

	public static String getStringOrError(String scope, JsonElement ele) throws IOException {
		if (!ele.isJsonPrimitive())
			throw new IOException(scope + " had invalid value (expected String value)");
		return ele.getAsString();
	}

	public static JsonObject getObjectOrError(String scope, JsonElement ele) throws IOException {
		if (!ele.isJsonObject())
			throw new IOException(scope + " had invalid value (expected json object value)");
		return ele.getAsJsonObject();
	}

	public static JsonArray getArrayOrError(String scope, JsonElement ele) throws IOException {
		if (!ele.isJsonArray())
			throw new IOException(scope + " had invalid value (expected json array value)");
		return ele.getAsJsonArray();
	}

	public static void mergeObject(JsonObject source, JsonObject target) {
		mergeObject(source, target, false);
	}

	public static void mergeObject(JsonObject source, JsonObject target, boolean doMergeArray) {
		// Merge each entry
		for (String key : source.keySet()) {
			if (source.get(key).isJsonObject()) {
				JsonObject chT = createOrGetJsonObject(target, key);
				mergeObject(source.get(key).getAsJsonObject(), chT);
			} else if (source.get(key).isJsonArray()) {
				JsonArray chT = createOrGetJsonArray(target, key);
				if (doMergeArray)
					mergeArray(source.get(key).getAsJsonArray(), chT, doMergeArray);
				else
					target.add(key, source.get(key));
			} else
				target.add(key, source.get(key));
		}
	}

	public static void mergeArray(JsonArray source, JsonArray target, boolean doMergeArray) {
		// Merge each entry
		for (JsonElement ele : source) {
			target.add(ele);
		}
	}

	public static JsonArray createOrGetJsonArray(JsonObject obj, String name) {
		if (obj.has(name))
			return obj.get(name).getAsJsonArray();
		JsonArray res = new JsonArray();
		obj.add(name, res);
		return res;
	}

	public static JsonObject createOrGetJsonObject(JsonObject obj, String name) {
		if (obj.has(name))
			return obj.get(name).getAsJsonObject();
		JsonObject res = new JsonObject();
		obj.add(name, res);
		return res;
	}

}
