package usr.skyswimmer.quicktoolsutils.json;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import usr.skyswimmer.quicktoolsutils.json.variables.WrappedJsonElement;

public class JsonVariablesContext {

    private static class VariableContainerPointer {
        public String localName;
        public String keyPath;

        public VariableContainer container; // Variable

        public VariableContainerPointer parent; // Parent pointer
        public JsonElement parentValue; // The object that holds this pointer's content
    }

    private static class VariableContainer {
        public String name;

        public JsonElement baseValue;
        public HashMap<String, VariableContainer> children = new LinkedHashMap<String, VariableContainer>();

        public HashMap<JsonVariablesContext, VariableContainerPointer> holdingContexts = new LinkedHashMap<JsonVariablesContext, VariableContainerPointer>();

        public void assign(JsonElement element) {
            // Assign
            baseValue = element;

            // Update context parent element to match new value
            for (JsonVariablesContext ctx : holdingContexts.keySet()) {
                VariableContainerPointer ptr = holdingContexts.get(ctx);
                if (ptr.parentValue != null && ptr.parentValue.isJsonObject()) {
                    ptr.parentValue.getAsJsonObject().add(name, element);
                }
            }
        }
    }

    JsonVariablesProcessor processor;

    private HashMap<String, VariableContainerPointer> allContainers = new LinkedHashMap<String, VariableContainerPointer>();
    private HashMap<String, VariableContainerPointer> rootContainers = new LinkedHashMap<String, VariableContainerPointer>();

    JsonVariablesContext(JsonVariablesProcessor processor) {
        this.processor = processor;
    }

    // FIXME: current concept doesnt deal with cases where eg. a new root element is
    // made, maybe allow specific contexts/pointers to be added to a base context
    // which are called whenever new root elements are made

    private void attachVariable(VariableContainerPointer container) {
        // Add container
        allContainers.put(container.keyPath.toLowerCase(), container);
        if (!container.keyPath.contains("."))
            rootContainers.put(container.keyPath.toLowerCase(), container);

        // Add context to container
        container.container.holdingContexts.put(this, container);

        // Add to local parent object if needed
        if (container.parentValue != null && container.parentValue.isJsonObject())
            container.parentValue.getAsJsonObject().add(container.localName, container.container.baseValue);

        // Go through children and add each child variable to local context
        for (String childKey : container.container.children.keySet()) {
            VariableContainer child = container.container.children.get(childKey);

            // Check
            if (!child.holdingContexts.containsKey(this)) {
                // Create pointer
                VariableContainerPointer pointer = new VariableContainerPointer();
                pointer.container = child;
                pointer.parent = container;
                pointer.localName = child.name;
                pointer.keyPath = container.keyPath + "." + child.name;
                pointer.parentValue = container.container.baseValue; // Set to the parent of the current child object

                // Attach
                attachVariable(pointer);
            }
        }
    }

    private void detachVariable(VariableContainerPointer container) {
        // Remove container
        allContainers.remove(container.keyPath.toLowerCase());
        if (!container.keyPath.contains("."))
            rootContainers.remove(container.keyPath.toLowerCase());

        // Remove from parent element
        if (container.parentValue != null && container.parentValue.isJsonObject()
                && container.parentValue.getAsJsonObject().has(container.localName))
            container.parentValue.getAsJsonObject().remove(container.localName);

        // Go through children and remove each child variable from local context
        for (String childKey : container.container.children.keySet()) {
            VariableContainer child = container.container.children.get(childKey);

            // Check
            if (child.holdingContexts.containsKey(this)) {
                // Get pointer
                VariableContainerPointer pointer = child.holdingContexts.get(this);

                // Remove child variable
                detachVariable(pointer);
            }
        }

        // Remove context from container
        container.container.holdingContexts.remove(this);
    }

    // FIXME: rework class

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
        VariableContainerPointer cont = allContainers.get(key.toLowerCase());

        // Remove variable
        detachVariable(cont);

        // Return
        return cont.container.baseValue;
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
        VariableContainerPointer container = allContainers.get(key.toLowerCase());
        if (container == null)
            return null;
        return container.container.baseValue;
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
        // return allContainers.get(key.toLowerCase()).children.values().stream().map(t
        // -> t.key).toArray(t -> new String[t]);

        // FIXME: reimplement
        return null;
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
        // return allContainers.get(key.toLowerCase()).children.values().stream().map(t
        // -> t.name).toArray(t -> new String[t]);

        // FIXME: reimplement
        return null;
    }

    /**
     * Retrieves all variables
     * 
     * @return Array of variable strings
     */
    public String[] getVariables() {
        // return allContainers.values().stream().map(t -> t.key).toArray(t -> new
        // String[t]);

        // FIXME: reimplement
        return null;
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

        // Check existing
        if (allContainers.containsKey(key.toLowerCase())) {
            // Found existing
            VariableContainerPointer ptr = allContainers.get(key.toLowerCase());

            // Update
            ptr.container.assign(value);
        } else {
            // Create containers
            String path = "";
            VariableContainerPointer parent = null;
            for (String part : key.split("\\.")) {
                // Get path
                String pth = path;
                if (!pth.isEmpty())
                    pth += ".";
                pth += part;
                path = pth;

                // Create or get container
                VariableContainerPointer container;
                if (!allContainers.containsKey(pth.toLowerCase())) {
                    // Create
                    container = new VariableContainerPointer();

                    // Create variable
                    VariableContainer var = new VariableContainer();
                    var.baseValue = new JsonObject();
                    var.name = part;

                    // Assign
                    container.container = var;
                    container.localName = part;
                    container.keyPath = pth;
                    container.parent = parent;
                    if (parent != null)
                        container.parentValue = parent.container.baseValue;

                    // Add to parent
                    if (parent != null)
                        parent.container.children.put(part, var);

                    // Attach
                    attachVariable(container);
                } else {
                    // Get
                    container = allContainers.get(pth.toLowerCase());
                    pth = container.keyPath;
                    path = pth;
                }

                // Update parent
                parent = container;
            }

            // Assign value
            if (parent != null)
                parent.container.assign(value);
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
     * @param context Context object to import
     */
    public void importContext(String baseKey, JsonVariablesContext context) {
        // Prepare key
        // if (baseKey.endsWith("."))
        // baseKey = baseKey.substring(0, baseKey.length() - 1);

        // // Go through vars
        // for (String var : context.getVariables()) {
        // String key = baseKey;
        // if (!key.isEmpty())
        // key += ".";
        // key += var;
        // JsonElement ele = context.getVariable(var);
        // if (ele instanceof WrappedJsonElement && ((WrappedJsonElement)
        // ele).getProcessor() != processor) {
        // ele = ((WrappedJsonElement) ele).unwrap();
        // ele = new WrappedJsonElement(ele, processor);
        // }
        // assignVariable(key, ele, false);
        // }

        // FIXME: reimplement
        return;
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
            if (!ele.isJsonObject())
                assignVariable(key, object.get(keyPart));

            // Import if needed
            if (ele.isJsonObject()) {
                // Import child object
                JsonObject obj = ele.getAsJsonObject();
                importObject(key, obj);
            }
        }
    }
}
