package usr.skyswimmer.quicktoolsutils.json;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import usr.skyswimmer.quicktoolsutils.json.variables.WrappedJsonElement;

public class JsonVariablesProcessor implements Closeable  {

    private ArrayList<JsonVariablesContext> contexts = new ArrayList<JsonVariablesContext>();
    private JsonVariablesContext rootContext;

    public JsonVariablesProcessor() {
        rootContext = new JsonVariablesContext(this);
        addContext(rootContext);
    }

    /**
     * Adds variable contexts to the variable processor
     * 
     * @param ctx Context instance to add
     */
    public void addContext(JsonVariablesContext ctx) {
        if (ctx.processor != this)
            throw new IllegalArgumentException("Invalid context: context bound to a different variables processor");
        if (contexts.contains(ctx))
            return;
        contexts.add(ctx);
    }

    /**
     * Removes contexts
     * 
     * @param ctx Context instance to remove
     * @return True if successful, false otherwise
     */
    public boolean removeContext(JsonVariablesContext ctx) {
        if (contexts.contains(ctx)) {
            contexts.remove(ctx);
            return true;
        }
        return false;
    }

    /**
     * Retrieves the top-level root context
     * 
     * @return JsonVariablesContext instance
     */
    public JsonVariablesContext getRootContext() {
        return rootContext;
    }

    /**
     * Retrieves defined contexts (excluding the root)
     * 
     * @return Array of JsonVariablesContext instances
     */
    public JsonVariablesContext[] getContexts() {
        JsonVariablesContext[] cts = contexts.toArray(t -> new JsonVariablesContext[t]);
        return Arrays.copyOfRange(cts, 1, cts.length);
    }

    /**
     * Resolves variables by name
     * 
     * @param name Variable name
     * @return JsonElement value or null
     */
    public JsonElement resolveVariable(String name) {
        for (JsonVariablesContext ctx : contexts) {
            if (ctx.hasVariable(name))
                return ctx.getVariable(name);
        }
        return null;
    }

    /**
     * Wraps an json element instance in a JIT variable processor element instance
     * 
     * @param element Element to wrap
     * @return Wrapped JsonElement instance
     */
    public JsonElement wrapElement(JsonElement element) {
        // Unwrap if needed
        element = WrappedJsonElement.unwrap(element);

        // Wrap objects and arrays
        if (element.isJsonObject()) {
            // Create wrapped
            // FIXME: getAsJsonPrimitive(name) will crash
            JsonObject unwrapped = element.getAsJsonObject();
            JsonObject wrappedObj = new JsonObject();
            for (String key : unwrapped.keySet()) {
                JsonElement ele = unwrapped.get(key);
                wrappedObj.add(key, wrapElement(ele));
            }
            element = wrappedObj;
        } else if (element.isJsonArray()) {
            // Create wrapped
            JsonArray unwrapped = element.getAsJsonArray();
            JsonArray wrappedObj = new JsonArray();
            for (JsonElement ele : unwrapped) {
                wrappedObj.add(wrapElement(ele));
            }
            element = wrappedObj;
        }

        // Return wrapepd
        return new WrappedJsonElement(element, this);
    }

    @Override
    public void close() throws IOException {
        for (JsonVariablesContext ctx : getContexts()) {
            ctx.close();
            contexts.remove(ctx);
        }
        rootContext.close();
    }

}
