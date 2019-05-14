package com.ibasco.sourcebuddy.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Component
@Scope(SCOPE_PROTOTYPE)
public class CommandHistory {

    private static final Logger log = LoggerFactory.getLogger(CommandHistory.class);

    //private static final int DEFAULT_MAX_ENTRIES = 100;

    private LinkedList<String> entries;

    public CommandHistory(@Value("${app.history-max-entries}") Integer maxEntries) {
        //int maxEntries1 = maxEntries != null ? maxEntries : DEFAULT_MAX_ENTRIES;
        this.entries = new LinkedList<>();
    }

    public String next() {
        if (this.entries.isEmpty()) {
            log.debug("Queue is empty");
            return null;
        }
        if (this.entries.size() == 1) {
            return current();
        }
        String next = this.entries.removeLast();
        this.entries.addFirst(next);
        return next;
    }

    public String previous() {
        if (this.entries.isEmpty()) {
            log.debug("Queue is empty");
            return null;
        }
        if (this.entries.size() == 1) {
            return current();
        }
        String previous = this.entries.removeFirst();
        this.entries.addLast(previous);
        return previous;
    }

    private String current() {
        return entries.peekFirst();
    }

    public void clear() {
        this.entries.clear();
    }

    public void add(String command) {
        this.entries.addFirst(command);
    }

    public List<String> getEntries() {
        if (entries.isEmpty())
            return new ArrayList<>();
        return Arrays.asList(this.entries.toArray(new String[0]));
    }

    public int size() {
        return this.entries.size();
    }
}
