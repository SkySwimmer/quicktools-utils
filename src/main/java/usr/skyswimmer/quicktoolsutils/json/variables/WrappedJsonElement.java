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

    // FIXME: implement

    @Override
    public BigDecimal getAsBigDecimal() {
        // TODO Auto-generated method stub
        return delegate.getAsBigDecimal();
    }

    @Override
    public BigInteger getAsBigInteger() {
        // TODO Auto-generated method stub
        return delegate.getAsBigInteger();
    }

    @Override
    public boolean getAsBoolean() {
        // TODO Auto-generated method stub
        return delegate.getAsBoolean();
    }

    @Override
    public byte getAsByte() {
        // TODO Auto-generated method stub
        return delegate.getAsByte();
    }

    @Override
    public char getAsCharacter() {
        // TODO Auto-generated method stub
        return delegate.getAsCharacter();
    }

    @Override
    public double getAsDouble() {
        // TODO Auto-generated method stub
        return delegate.getAsDouble();
    }

    @Override
    public float getAsFloat() {
        // TODO Auto-generated method stub
        return delegate.getAsFloat();
    }

    @Override
    public int getAsInt() {
        // TODO Auto-generated method stub
        return delegate.getAsInt();
    }

    @Override
    public JsonArray getAsJsonArray() {
        // TODO Auto-generated method stub
        return delegate.getAsJsonArray();
    }

    @Override
    public JsonNull getAsJsonNull() {
        // TODO Auto-generated method stub
        return delegate.getAsJsonNull();
    }

    @Override
    public JsonObject getAsJsonObject() {
        // TODO Auto-generated method stub
        return delegate.getAsJsonObject();
    }

    @Override
    public JsonPrimitive getAsJsonPrimitive() {
        // TODO Auto-generated method stub
        return delegate.getAsJsonPrimitive();
    }

    @Override
    public long getAsLong() {
        // TODO Auto-generated method stub
        return delegate.getAsLong();
    }

    @Override
    public Number getAsNumber() {
        // TODO Auto-generated method stub
        return delegate.getAsNumber();
    }

    @Override
    public short getAsShort() {
        // TODO Auto-generated method stub
        return delegate.getAsShort();
    }

    @Override
    public String getAsString() {
        // TODO Auto-generated method stub
        return delegate.getAsString();
    }

    @Override
    public boolean isJsonArray() {
        return delegate.isJsonArray();
    }

    @Override
    public boolean isJsonNull() {
        return delegate.isJsonNull();
    }

    @Override
    public boolean isJsonObject() {
        return delegate.isJsonObject();
    }

    @Override
    public boolean isJsonPrimitive() {
        return delegate.isJsonPrimitive();
    }

    @Override
    public String toString() {
        return super.toString();
    }
    
}
