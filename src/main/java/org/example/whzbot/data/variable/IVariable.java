package org.example.whzbot.data.variable;

import org.example.whzbot.storage.json.JsonLongNode;
import org.example.whzbot.storage.json.JsonStringNode;

/**
 * Interface for variables in memory.
 */
public interface IVariable {
    AbstractType getType();
    String getName();
    void setName(String n);
    boolean isLoaded();

    /**
     * Trace size of the memory use, limit total memory use.
     * @return memory size being "allocated", counted by some memory unit.
     */
    int getMemorySize();

    String toString();

    static IVariable fromJson(JsonStringNode node, JsonLongNode ref, Memory memory){
        String type_name = node.getContent();
        //memory.get(type_name, memory.get(""));
        return null;
    }
}
