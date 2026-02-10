package usr.skyswimmer.quicktoolsutils.json.variables;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import usr.skyswimmer.quicktoolsutils.json.JsonVariablesProcessor;

@SuppressWarnings("deprecation")
public class WrappedJsonElement extends JsonElement {

    private JsonElement delegate;
    private JsonVariablesProcessor processor;

    public WrappedJsonElement(JsonElement delegate, JsonVariablesProcessor processor) {
        this.delegate = delegate;
        this.processor = processor;
    }

    public JsonVariablesProcessor getProcessor() {
        return processor;
    }

    public JsonElement unwrap() {
        if (delegate.isJsonObject()) {
            // Unwrap object
            JsonObject obj = delegate.getAsJsonObject();
            JsonObject unwrapped = new JsonObject();
            for (String key : obj.keySet()) {
                JsonElement ele = obj.get(key);
                if (ele instanceof WrappedJsonElement)
                    ele = ((WrappedJsonElement) ele).unwrap();
                unwrapped.add(key, unwrapped);
            }
            return unwrapped;
        } else if (delegate.isJsonArray()) {
            // Unwrap array
            JsonArray arr = delegate.getAsJsonArray();
            JsonArray unwrapped = new JsonArray();
            for (JsonElement ele : arr) {
                if (ele instanceof WrappedJsonElement)
                    ele = ((WrappedJsonElement) ele).unwrap();
                unwrapped.add(ele);
            }
            return unwrapped;
        }
        return delegate;
    }

    @Override
    public JsonElement deepCopy() {
        // Check type
        if (delegate.isJsonObject()) {
            // Object
            JsonObject obj = delegate.getAsJsonObject();
            JsonObject newWrapped = new JsonObject();
            for (String key : obj.keySet()) {
                JsonElement ele = obj.get(key);
                newWrapped.add(key, ele.deepCopy());
            }
            return newWrapped;
        } else if (delegate.isJsonArray()) {
            // Array
            JsonArray arr = delegate.getAsJsonArray();
            JsonArray newWrapped = new JsonArray();
            for (JsonElement ele : arr) {
                newWrapped.add(ele.deepCopy());
            }
            return newWrapped;
        } else {
            // Default
            return new WrappedJsonElement(delegate.deepCopy(), processor);
        }
    }

    public JsonElement resolve() {
        // Resolve value
        if (!delegate.isJsonPrimitive())
            return delegate;

        // Resolve
        String pth = delegate.getAsString();
        if (pth.startsWith("{") && pth.endsWith("}")) {
            // Check
            String key = pth.substring(1);
            key = key.substring(0, key.length() - 1);
            if (!key.contains("{") && !key.contains("}")) {
                // Try to resolve
                JsonElement resolved = processor.resolveVariable(key);
                if (resolved != null)
                    return resolved;
            }
        }

        // Check
        boolean updated = false;
        String pthIndex = pth;
        String pathStart = pth;
        while (pathStart.contains("{")) {
            // Parse key and substring
            int start = pthIndex.indexOf("{");
            int start2 = pathStart.indexOf("{");
            String key = pth.substring(start);
            String prefix = pth.substring(0, start);
            if (!key.contains("}"))
                break;
            String suffix = key.substring(key.indexOf("}") + 1);
            pathStart = pathStart.substring(start2 + 1);
            pathStart = pathStart.substring(pathStart.indexOf("}") + 1);
            key = key.substring(1, key.indexOf("}"));

            // Prepare value
            String value = "";

            // Resolve
            JsonElement resolved = processor.resolveVariable(key);
            if (resolved != null) {
                // Handle result

                // Check wrapped, and if needed, re-resolve
                if (resolved instanceof WrappedJsonElement) {
                    resolved = ((WrappedJsonElement) resolved).resolve();
                }

                // Check
                if (resolved.isJsonPrimitive())
                    value = resolved.getAsString();
                else
                    value = resolved.toString();

                // Update
                pth = prefix + value + suffix;
            }

            // Fake
            String valStripped = "#" + key + "#";

            // Update index
            pthIndex = prefix + valStripped + suffix;
            updated = true;
        }

        // Return
        if (updated)
            return new JsonPrimitive(pth);
        return delegate;
    }

    @Override
    public BigDecimal getAsBigDecimal() {
        return resolve().getAsBigDecimal();
    }

    @Override
    public BigInteger getAsBigInteger() {
        return resolve().getAsBigInteger();
    }

    @Override
    public boolean getAsBoolean() {
        return resolve().getAsBoolean();
    }

    @Override
    public byte getAsByte() {
        return resolve().getAsByte();
    }

    @Override
    public char getAsCharacter() {
        return resolve().getAsCharacter();
    }

    @Override
    public double getAsDouble() {
        return resolve().getAsDouble();
    }

    @Override
    public float getAsFloat() {
        return resolve().getAsFloat();
    }

    @Override
    public int getAsInt() {
        return resolve().getAsInt();
    }

    @Override
    public JsonArray getAsJsonArray() {
        return resolve().getAsJsonArray();
    }

    @Override
    public JsonNull getAsJsonNull() {
        return resolve().getAsJsonNull();
    }

    @Override
    public JsonObject getAsJsonObject() {
        return resolve().getAsJsonObject();
    }

    @Override
    public JsonPrimitive getAsJsonPrimitive() {
        return resolve().getAsJsonPrimitive();
    }

    @Override
    public long getAsLong() {
        return resolve().getAsLong();
    }

    @Override
    public Number getAsNumber() {
        return resolve().getAsNumber();
    }

    @Override
    public short getAsShort() {
        return resolve().getAsShort();
    }

    @Override
    public String getAsString() {
        return resolve().getAsString();
    }

    @Override
    public boolean isJsonArray() {
        return resolve().isJsonArray();
    }

    @Override
    public boolean isJsonNull() {
        return resolve().isJsonNull();
    }

    @Override
    public boolean isJsonObject() {
        return resolve().isJsonObject();
    }

    @Override
    public boolean isJsonPrimitive() {
        return resolve().isJsonPrimitive();
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
