package com.android.bluetoothmessenger.Content;

import java.util.ArrayList;
import java.util.List;

public class MessagesLab {
    private static MessagesLab sMessagesLab;
    private List<String[]> mMessages = new ArrayList<>();

    public static MessagesLab get() {
        if (sMessagesLab == null) {
            sMessagesLab = new MessagesLab();
        }
        return sMessagesLab;
    }

    public void addMessage(String name, String message) {
        mMessages.add(new String[] {name, message});
    }

    public List<String[]> getMessages() {
        return mMessages;
    }
}
