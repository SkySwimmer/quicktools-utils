package usr.skyswimmer.quicktoolsutils.json;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import usr.skyswimmer.quicktoolsutils.json.variables.WrappedJsonElement;

public class JsonVariablesContext implements Closeable {

    private static class VariableContainerPointer {
        public String localName;
        public String keyPath;

        public VariableContainer container; // Variable
        public JsonElement parentValue; // The object that holds this pointer's content
    }

    private static class VariableContainer {
        public String name;
        public VariableContainer parentContainer;

        public boolean wrap;
        public JsonElement baseValueElement;
        public HashMap<String, VariableContainer> children = new LinkedHashMap<String, VariableContainer>();

        public HashMap<JsonVariablesContext, VariableContainerPointer> holdingContexts = new LinkedHashMap<JsonVariablesContext, VariableContainerPointer>();

        public JsonElement resolve(JsonVariablesProcessor proc) {
            if (!wrap)
                return baseValueElement;
            return proc.wrapElement(baseValueElement);
        }

        public void assign(JsonElement element, boolean process) {
            // Assign
            wrap = process;
            if (wrap) {
                // Unwrap
                element = WrappedJsonElement.unwrap(element);
            }
            baseValueElement = element;

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

    private HashMap<VariableContainerPointer, JsonVariablesContext> targetPointers = new HashMap<VariableContainerPointer, JsonVariablesContext>();
    private ArrayList<JsonVariablesContext> targetContexts = new ArrayList<JsonVariablesContext>();

    private boolean retain;

    public void retain() {
        retain = true;
    }

    public JsonVariablesContext(JsonVariablesProcessor processor) {
        this.processor = processor;
    }

    private void attachVariable(VariableContainerPointer container) {
        // Add container
        allContainers.put(container.keyPath.toLowerCase(), container);
        if (!container.keyPath.contains(".")) {
            // Add root container
            rootContainers.put(container.keyPath.toLowerCase(), container);

            // Add to attached contexts
            for (JsonVariablesContext ctx : targetContexts) {
                VariableContainerPointer ptr = new VariableContainerPointer();
                ptr.container = container.container;
                ptr.keyPath = container.keyPath;
                ptr.localName = container.localName;
                ctx.attachVariable(ptr);
            }

            // Add to attached variables
            for (VariableContainerPointer ptr : targetPointers.keySet()) {
                JsonVariablesContext ctx = targetPointers.get(ptr);
                VariableContainerPointer ptr2 = new VariableContainerPointer();
                ptr2.container = container.container;
                ptr2.keyPath = ptr.keyPath + "." + container.keyPath;
                ptr2.localName = container.localName;
                ptr2.parentValue = ptr.container.baseValueElement;
                ctx.attachVariable(ptr2);
            }
        }

        // Add context to container
        container.container.holdingContexts.put(this, container);

        // Add to local parent object if needed
        if (container.parentValue != null && container.parentValue.isJsonObject())
            container.parentValue.getAsJsonObject().add(container.localName, container.container.resolve(processor));

        // Go through children and add each child variable to local context
        for (String childKey : container.container.children.keySet()) {
            VariableContainer child = container.container.children.get(childKey);

            // Check
            if (!child.holdingContexts.containsKey(this)) {
                // Create pointer
                VariableContainerPointer pointer = new VariableContainerPointer();
                pointer.container = child;
                pointer.localName = child.name;
                pointer.keyPath = container.keyPath + "." + child.name;
                pointer.parentValue = container.container.baseValueElement; // Set to the parent of the current child
                                                                            // object

                // Attach
                attachVariable(pointer);
            }
        }
    }

    private void detachVariable(VariableContainerPointer container) {
        // Remove container
        allContainers.remove(container.keyPath.toLowerCase());
        if (!container.keyPath.contains(".")) {
            // Remove root container
            rootContainers.remove(container.keyPath.toLowerCase());

            // Remove from attached contexts
            for (JsonVariablesContext ctx : targetContexts) {
                // Get variable and remove
                VariableContainerPointer ptr = ctx.allContainers.get(container.keyPath.toLowerCase());
                if (ptr != null)
                    ctx.detachVariable(ptr);
            }

            // Remove from attached variables
            for (VariableContainerPointer ptr : targetPointers.keySet()) {
                JsonVariablesContext ctx = targetPointers.get(ptr);
                VariableContainerPointer tar = ctx.allContainers
                        .get((ptr.keyPath + "." + container.localName).toLowerCase());
                if (tar != null)
                    ctx.detachVariable(tar);
            }
        }

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

        // Check parent
        if (cont.container.parentContainer != null) {
            // Remove from parent
            VariableContainer parentVar = cont.container.parentContainer;
            if (parentVar.children.containsKey(cont.localName)) {
                // Remove child entry
                parentVar.children.remove(cont.localName);

                // Detach from all attached contexts
                for (JsonVariablesContext ctx : cont.container.holdingContexts.keySet()
                        .toArray(t -> new JsonVariablesContext[t])) {
                    VariableContainerPointer ptr = cont.container.holdingContexts.get(ctx);

                    // Detach
                    ctx.detachVariable(ptr);
                }
            }
        }

        // Return
        return cont.container.resolve(processor);
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
        return container.container.resolve(processor);
    }

    /**
     * Retrieves child variable keys of a specific variable
     * 
     * @param key Variable key
     * @return Array of fully-qualified child variable keys or an empty array
     */
    public String[] getChildVariables(String key) {
        return getChildVariables(key, false);
    }

    /**
     * Retrieves child variable keys of a specific variable
     * 
     * @param key       Variable key
     * @param recursive True to recursively include all children of the given key
     *                  and their children, false to only retrieve top-level child
     *                  keys
     * @return Array of fully-qualified child variable keys or an empty array
     */
    public String[] getChildVariables(String key, boolean recursive) {
        if (!hasVariable(key))
            return new String[0];
        VariableContainerPointer ptr = allContainers.get(key.toLowerCase());
        ArrayList<String> childKeys = new ArrayList<String>();
        for (VariableContainer child : ptr.container.children.values()) {
            childKeys.add(ptr.keyPath + "." + child.name);
            if (recursive) {
                // Add children
                for (String ch : getChildVariables(ptr.keyPath + "." + child.name, true))
                    childKeys.add(ch);
            }
        }
        return childKeys.toArray(t -> new String[t]);
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
        VariableContainerPointer ptr = allContainers.get(key.toLowerCase());
        ArrayList<String> childKeys = new ArrayList<String>();
        for (VariableContainer child : ptr.container.children.values()) {
            childKeys.add(child.name);
        }
        return childKeys.toArray(t -> new String[t]);
    }

    /**
     * Retrieves all variables
     * 
     * @return Array of variable strings
     */
    public String[] getVariables() {
        return allContainers.values().stream().map(t -> t.keyPath).toArray(t -> new String[t]);
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
        String base = "";
        if (key.contains(".")) {
            base = key.substring(0, key.lastIndexOf("."));
            key = key.substring(key.lastIndexOf(".") + 1);
        }
        assignVariable(base, key, value, process);
    }

    /**
     * Assigns JSON variables (defaults to process = true)
     * 
     * @param baseKey Variable parent key
     * @param name    Variable name
     * @param value   Variable value
     */
    public void assignVariable(String baseKey, String name, JsonElement value) {
        assignVariable(baseKey, name, value, true);
    }

    /**
     * Assigns JSON variables
     * 
     * @param baseKey Variable parent key
     * @param name    Variable name
     * @param value   Variable value
     * @param process True if the variable should be processed (scanned in and
     *                expanded), false to leave it raw
     */
    public void assignVariable(String baseKey, String name, JsonElement value, boolean process) {
        // Check existing
        String fullKey = baseKey;
        if (!fullKey.isEmpty())
            fullKey += ".";
        fullKey += name;
        if (allContainers.containsKey(fullKey.toLowerCase())) {
            // Found existing
            VariableContainerPointer ptr = allContainers.get(fullKey.toLowerCase());

            // Update
            ptr.container.assign(value, process);
        } else {
            // Create containers
            VariableContainerPointer container = null;
            VariableContainerPointer parent = null;
            if (!baseKey.isEmpty()) {
                // Get key
                parent = allContainers.get(baseKey.toLowerCase());
                if (parent == null) {
                    // Find any parent that exists
                    String keyName = "";
                    String keyRemainer = "";
                    String keyParent = baseKey;
                    VariableContainerPointer ptrParent = allContainers.get(keyParent);
                    while (ptrParent == null) {
                        if (!keyParent.contains(".")) {
                            // End
                            if (!keyName.isEmpty())
                                keyRemainer = keyName + (!keyRemainer.isEmpty() ? "." + keyRemainer : "");
                            keyName = keyParent;
                            keyParent = "";
                            ptrParent = allContainers.get(keyParent);
                            break;
                        }
                        if (!keyName.isEmpty())
                            keyRemainer = keyName + (!keyRemainer.isEmpty() ? "." + keyRemainer : "");
                        keyName = keyParent.substring(keyParent.lastIndexOf(".") + 1);
                        keyParent = keyParent.substring(0, keyParent.lastIndexOf("."));
                        ptrParent = allContainers.get(keyParent);
                    }

                    // Create key in parent
                    String pthL = keyParent;
                    if (!pthL.isEmpty())
                        pthL += ".";
                    pthL += keyName;
                    if (!allContainers.containsKey(pthL.toLowerCase())) {
                        // Create
                        container = new VariableContainerPointer();

                        // Create variable
                        VariableContainer var = new VariableContainer();
                        if (ptrParent != null)
                            var.parentContainer = ptrParent.container;
                        var.baseValueElement = new JsonObject();
                        var.name = keyName;

                        // Assign
                        container.container = var;
                        container.localName = keyName;
                        container.keyPath = pthL;
                        if (ptrParent != null)
                            container.parentValue = ptrParent.container.baseValueElement;

                        // Add to parent
                        if (ptrParent != null)
                            ptrParent.container.children.put(keyName, var);

                        // Attach
                        attachVariable(container);
                    } else {
                        // Get
                        container = allContainers.get(pthL.toLowerCase());
                        pthL = container.keyPath;
                    }

                    // Update parent
                    parent = container;

                    // Check remaining
                    if (!keyRemainer.isEmpty()) {
                        // Additional keys need to be made

                        // Go through keys
                        String path = parent.keyPath;
                        for (String part : keyRemainer.split("\\.")) {
                            // Get path
                            String pth = path;
                            if (!pth.isEmpty())
                                pth += ".";
                            pth += part;
                            path = pth;

                            // Create or get container
                            container = null;
                            if (!allContainers.containsKey(pth.toLowerCase())) {
                                // Create
                                container = new VariableContainerPointer();

                                // Create variable
                                VariableContainer var = new VariableContainer();
                                if (parent != null)
                                    var.parentContainer = parent.container;
                                var.baseValueElement = new JsonObject();
                                var.name = part;

                                // Assign
                                container.container = var;
                                container.localName = part;
                                container.keyPath = pth;
                                if (parent != null)
                                    container.parentValue = parent.container.baseValueElement;

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
                    }
                }
            }

            // Create or get container
            container = null;
            if (!allContainers.containsKey(fullKey.toLowerCase())) {
                // Create
                container = new VariableContainerPointer();

                // Create variable
                VariableContainer var = new VariableContainer();
                if (parent != null)
                    var.parentContainer = parent.container;
                var.baseValueElement = new JsonObject();
                var.name = name;

                // Assign
                container.container = var;
                container.localName = name;
                container.keyPath = fullKey;
                if (parent != null)
                    container.parentValue = parent.container.baseValueElement;

                // Add to parent
                if (parent != null)
                    parent.container.children.put(name, var);

                // Attach
                attachVariable(container);
            } else {
                // Get
                container = allContainers.get(fullKey.toLowerCase());
                fullKey = container.keyPath;
            }

            // Assign value
            container.container.assign(value, process);
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
        if (baseKey.endsWith("."))
            baseKey = baseKey.substring(0, baseKey.length() - 1);
        if (baseKey.isEmpty()) {
            // Attach to root
            if (context.targetContexts.contains(this))
                return;

            // Add to target
            context.targetContexts.add(this);

            // Copy root pointers
            for (VariableContainerPointer root : context.rootContainers.values()) {
                VariableContainerPointer ptr = new VariableContainerPointer();
                ptr.container = root.container;
                ptr.keyPath = root.keyPath;
                ptr.localName = root.localName;
                attachVariable(ptr);
            }
        } else {
            // Attach to sub-context

            // Check existing
            VariableContainerPointer cont = null;
            if (allContainers.containsKey(baseKey.toLowerCase())) {
                // Found existing
                VariableContainerPointer ptr = allContainers.get(baseKey.toLowerCase());
                cont = ptr;
            } else {
                // Create containers
                VariableContainerPointer container = null;
                VariableContainerPointer parent = null;
                if (!baseKey.isEmpty()) {
                    // Get key
                    parent = allContainers.get(baseKey.toLowerCase());
                    if (parent == null) {
                        // Find any parent that exists
                        String keyName = "";
                        String keyRemainer = "";
                        String keyParent = baseKey;
                        VariableContainerPointer ptrParent = allContainers.get(keyParent);
                        while (ptrParent == null) {
                            if (!keyParent.contains(".")) {
                                // End
                                if (!keyName.isEmpty())
                                    keyRemainer = keyName + (!keyRemainer.isEmpty() ? "." + keyRemainer : "");
                                keyName = keyParent;
                                keyParent = "";
                                ptrParent = allContainers.get(keyParent);
                                break;
                            }
                            if (!keyName.isEmpty())
                                keyRemainer = keyName + (!keyRemainer.isEmpty() ? "." + keyRemainer : "");
                            keyName = keyParent.substring(keyParent.lastIndexOf(".") + 1);
                            keyParent = keyParent.substring(0, keyParent.lastIndexOf("."));
                            ptrParent = allContainers.get(keyParent);
                        }

                        // Create key in parent
                        String pthL = keyParent;
                        if (!pthL.isEmpty())
                            pthL += ".";
                        pthL += keyName;
                        if (!allContainers.containsKey(pthL.toLowerCase())) {
                            // Create
                            container = new VariableContainerPointer();

                            // Create variable
                            VariableContainer var = new VariableContainer();
                            if (ptrParent != null)
                                var.parentContainer = ptrParent.container;
                            var.baseValueElement = new JsonObject();
                            var.name = keyName;

                            // Assign
                            container.container = var;
                            container.localName = keyName;
                            container.keyPath = pthL;
                            if (ptrParent != null)
                                container.parentValue = ptrParent.container.baseValueElement;

                            // Add to parent
                            if (ptrParent != null)
                                ptrParent.container.children.put(keyName, var);

                            // Attach
                            attachVariable(container);
                        } else {
                            // Get
                            container = allContainers.get(pthL.toLowerCase());
                            pthL = container.keyPath;
                        }

                        // Update parent
                        parent = container;

                        // Check remaining
                        if (!keyRemainer.isEmpty()) {
                            // Additional keys need to be made

                            // Go through keys
                            String path = parent.keyPath;
                            for (String part : keyRemainer.split("\\.")) {
                                // Get path
                                String pth = path;
                                if (!pth.isEmpty())
                                    pth += ".";
                                pth += part;
                                path = pth;

                                // Create or get container
                                container = null;
                                if (!allContainers.containsKey(pth.toLowerCase())) {
                                    // Create
                                    container = new VariableContainerPointer();

                                    // Create variable
                                    VariableContainer var = new VariableContainer();
                                    if (parent != null)
                                        var.parentContainer = parent.container;
                                    var.baseValueElement = new JsonObject();
                                    var.name = part;

                                    // Assign
                                    container.container = var;
                                    container.localName = part;
                                    container.keyPath = pth;
                                    if (parent != null)
                                        container.parentValue = parent.container.baseValueElement;

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
                        }
                    }
                }

                // Assign value
                if (parent != null)
                    cont = parent;
            }
            if (cont != null) {
                // Add to target
                context.targetPointers.put(cont, this);

                // Copy root pointers
                for (VariableContainerPointer root : context.rootContainers.values()) {
                    // Create pointer
                    VariableContainerPointer ptr = new VariableContainerPointer();
                    ptr.container = root.container;
                    ptr.keyPath = baseKey + "." + root.keyPath;
                    ptr.localName = root.localName;
                    ptr.parentValue = cont.container.baseValueElement;

                    // Add to parent
                    if (cont != null)
                        cont.container.children.put(root.keyPath, ptr.container);

                    // Attach
                    attachVariable(ptr);
                }
            }
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
        importObject(baseKey, object, true);
    }

    /**
     * Imports json objects as variable values
     * 
     * @param baseKey Base element key
     * @param object  Json object to import
     * @param process True to wrap wrap/unwrap the object being imported in variable
     *                resolution, false to leave raw
     */
    public void importObject(String baseKey, JsonObject object, boolean process) {
        // Handle keys
        if (baseKey.endsWith("."))
            baseKey = baseKey.substring(0, baseKey.length() - 1);

        // Import object
        String[] keys = object.keySet().toArray(t -> new String[t]);
        for (String keyPart : keys) {
            // Get element
            JsonElement ele = object.get(keyPart);

            // Assign
            if (!ele.isJsonObject())
                assignVariable(baseKey, keyPart, ele, process);

            // Import if needed
            if (ele.isJsonObject()) {
                // Import child object
                JsonObject obj = ele.getAsJsonObject();
                importObject((baseKey.isEmpty() ? "" : baseKey + ".") + keyPart, obj);
            }
        }
    }

    /**
     * Duplicates the current variable context
     * 
     * @param proc Variable processor to use
     * @return Duplicated JsonVariablesContext instance
     */
    public JsonVariablesContext duplicate(JsonVariablesProcessor proc) {
        JsonVariablesContext ctx = new JsonVariablesContext(proc);
        ctx.importContext(this);
        return ctx;
    }

    @Override
    public void close() throws IOException {
        if (retain)
            return;
        
        // Detach all
        for (VariableContainerPointer ptr : rootContainers.values().toArray(t -> new VariableContainerPointer[t])) {
            detachVariable(ptr);
        }
    }
}
