package org.example.whzbot.data.variable;

/**
 * StaticType are built-in types that exist with memory.
 */
public class StaticType extends AbstractType {
    public static StaticType type_type = new StaticType("type");
    public static StaticType integer_type = new StaticType("int");
    public static StaticType string_type = new StaticType("str");
    public static StaticType func_type = new StaticType("func");

    public StaticType(String name) {
        super(name);
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public boolean canCast(IVariable var) {
        return false;
    }
}
