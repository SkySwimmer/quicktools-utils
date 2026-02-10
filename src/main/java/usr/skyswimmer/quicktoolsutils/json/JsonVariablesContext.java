package usr.skyswimmer.quicktoolsutils.json;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import usr.skyswimmer.quicktoolsutils.json.variables.WrappedJsonElement;

public class JsonVariablesContext {

    private class KeyContainer {
        public KeyContainer parent;

        public String key;
        public String name;

        public JsonElement baseValue;
        public HashMap<String, KeyContainer> children = new LinkedHashMap<String, KeyContainer>();
    }

    JsonVariablesProcessor processor;

    private HashMap<String, KeyContainer> allContainers = new LinkedHashMap<String, KeyContainer>();
    private HashMap<String, KeyContainer> rootContainers = new LinkedHashMap<String, KeyContainer>();

    JsonVariablesContext(JsonVariablesProcessor processor) {
        this.processor = processor;
    }

    /**
     * Removes variables
     * 
     * @param key Variable to remove
     * @return JsonElement instance of the removed variable or null
     */
    public JsonElement removeVariable(String key) {
        // Check
        if (!hasVariable(key))
            return null;

        // Find element
        KeyContainer cont = allContainers.get(key.toLowerCase());

        // Remove children
        for (String name : cont.children.keySet())
            removeVariable(key + "." + name);

        // Remove element
        allContainers.remove(key.toLowerCase());
        if (!key.contains("."))
            rootContainers.remove(key.toLowerCase());

        // Return
        return cont.baseValue;
    }

    /**
     * Checks if a variable key is recognized
     * 
     * @param key Variable key
     * @return True if present, false otherwise
     */
    public boolean hasVariable(String key) {
        return allContainers.containsKey(key.toLowerCase());
    }

    /**
     * Retrieves variable values by key
     * 
     * @param key Variable key
     * @return JsonElement instance or null
     */
    public JsonElement getVariable(String key) {
        KeyContainer container = allContainers.get(key.toLowerCase());
        if (container == null)
            return null;
        return container.baseValue;
    }

    /**
     * Retrieves child variable keys of a specific variable
     * 
     * @param key Variable key
     * @return Array of fully-qualified child variable keys or an empty array
     */
    public String[] getChildVariables(String key) {
        if (!hasVariable(key))
            return new String[0];
        return allContainers.get(key.toLowerCase()).children.values().stream().map(t -> t.key).toArray(t -> new String[t]);
    }

    /**
     * Retrieves child variable keys of a specific variable
     * 
     * @param key Variable key
     * @return Array of child variable names or an empty array
     */
    public String[] getChildVariableNames(String key) {
        if (!hasVariable(key))
            return new String[0];
        return allContainers.get(key.toLowerCase()).children.values().stream().map(t -> t.name).toArray(t -> new String[t]);
    }

    /**
     * Retrieves all variables
     * 
     * @return Array of variable strings
     */
    public String[] getVariables() {
        return allContainers.values().stream().map(t -> t.key).toArray(t -> new String[t]);
    }

    /**
     * Assigns JSON variables (defaults to process = true)
     * 
     * @param key   Variable key
     * @param value Variable value
     */
    public void assignVariable(String key, JsonElement value) {
        assignVariable(key, value, true);
    }

    /**
     * Assigns JSON variables
     * 
     * @param key     Variable key
     * @param value   Variable value
     * @param process True if the variable should be processed (scanned in and
     *                expanded), false to leave it raw
     */
    public void assignVariable(String key, JsonElement value, boolean process) {
        // Process variable value
        if (process) {
            // Process variable, expand if needed
            value = processor.wrapElement(value);
        }

        // Create containers
        String path = "";
        KeyContainer parent = null;
        for (String part : key.split("\\.")) {
            // Get path
            String pth = path;
            if (!pth.isEmpty())
                pth += ".";
            pth += part;
            path = pth;

            // Create or get container
            KeyContainer container;
            if (!allContainers.containsKey(pth)) {
                // Create
                container = new KeyContainer();
                container.parent = parent;
                container.key = pth;
                container.name = part;
                container.baseValue = new JsonObject();
                allContainers.put(pth.toLowerCase(), container);

                // Add to parent
                if (parent != null) {
                    // Add
                    parent.children.put(part.toLowerCase(), container);

                    // Add object
                    if (parent.baseValue.isJsonObject())
                        parent.baseValue.getAsJsonObject().add(part, container.baseValue);
                }
            } else {
                // Get
                container = allContainers.get(pth.toLowerCase());
            }

            // Check root
            if (parent == null && !rootContainers.containsKey(pth.toLowerCase()))
                rootContainers.put(pth.toLowerCase(), container);

            // Update parent
            parent = container;
        }

        // Update value
        if (parent != null) {
            parent.baseValue = value;
            if (parent.parent != null && parent.parent.baseValue.isJsonObject()
                    && parent.parent.baseValue.getAsJsonObject().has(parent.name))
                parent.parent.baseValue.getAsJsonObject().add(parent.name, value);
        }
    }

    /**
     * Imports context objects as variable values
     * 
     * @param context Context object to import
     */
    public void importContext(JsonVariablesContext context) {
        importContext("", context);
    }

    /**
     * Imports context objects as variable values
     * 
     * @param baseKey Base element key
     * @param context  Context object to import
     */
    public void importContext(String baseKey, JsonVariablesContext context) {
        // Prepare key
        if (baseKey.endsWith("."))
            baseKey = baseKey.substring(0, baseKey.length() - 1);

        // Go through vars
        for (String var : context.getVariables()) {
            String key = baseKey;
            if (!key.isEmpty())
                key += ".";
            key += var;
            JsonElement ele = context.getVariable(var);
            if (ele instanceof WrappedJsonElement) {
                ele = ((WrappedJsonElement) ele).unwrap();
                ele = new WrappedJsonElement(ele, processor);
            }
            assignVariable(key, ele, false);
        }
    }

    /**
     * Imports json objects as variable values
     * 
     * @param object Json object to import
     */
    public void importObject(JsonObject object) {
        importObject("", object);
    }

    /**
     * Imports json objects as variable values
     * 
     * @param baseKey Base element key
     * @param object  Json object to import
     */
    public void importObject(String baseKey, JsonObject object) {
        // Handle keys
        if (baseKey.endsWith("."))
            baseKey = baseKey.substring(0, baseKey.length() - 1);

        // Import object
        for (String keyPart : object.keySet()) {
            String key = baseKey;
            if (!key.isEmpty())
                key += ".";
            key += keyPart;

            // Get element
            JsonElement ele = object.get(keyPart);

            // Assign
            if (!ele.isJsonObject() || !hasVariable(key))
                assignVariable(key, object.get(keyPart));

            // Import if needed
            if (ele.isJsonObject()) {
                // Import child object
                JsonObject obj = ele.getAsJsonObject();
                importObject(key, obj);
            }
        }
    }

    /**
     * Duplicates the variable context
     * 
     * @param processor Variable processor to use
     * @return Copy of the current JsonVariablesContext instance configured for
     *         another procesor
     */
    public JsonVariablesContext duplicate(JsonVariablesProcessor processor) {
        JsonVariablesContext copy = new JsonVariablesContext(processor);
        for (String key : getVariables()) {
            JsonElement ele = getVariable(key);
            if (ele instanceof WrappedJsonElement) {
                ele = ((WrappedJsonElement) ele).unwrap();
                ele = new WrappedJsonElement(ele, processor);
            }
            copy.assignVariable(key, ele, false);
        }
        return copy;
    }

}
