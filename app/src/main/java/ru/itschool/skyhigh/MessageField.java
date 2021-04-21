package ru.itschool.skyhigh;

import java.util.HashMap;
public class MessageField extends HashMap<String, String>{
    public static final String NAME = "name";
    public static final String HINT_NAME = "hint";

    // Конструктор
    public MessageField(String name, String hint) {
        super.put(NAME, name);
        super.put(HINT_NAME, hint);
    }
}
