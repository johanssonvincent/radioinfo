package se.umu.cs.dv14vjn.radio.gui;

import java.util.HashMap;

/**
 * Manager for saved ColumnMemento objects
 * @author Vincent Johansson
 */
public class MementoManager {

    private final HashMap<String,ColumnMemento> map;

    /**
     * Initializes a new se.umu.cs.dv14vjn.radio.gui.MementoManager
     */
    public MementoManager() {
        map = new HashMap<>();
    }

    /**
     * Save a ColumnMemento object
     * @param key String key for the ColumnMemento object
     * @param memento ColumnMemento object to be saved
     */
    public void save(String key, ColumnMemento memento) {
        map.put(key,memento);
    }

    /**
     * Returns the ColumnMemento object saved with the given key
     * @param key String key for the ColumnMemento object
     * @return ColumnMemento object
     */
    public ColumnMemento restore(String key) {
        return map.get(key);
    }

    /**
     * Checks if there are any ColumnMemento objects saved with the given key
     * @param key String key for the ColumnMemento object
     * @return false if there are no ColumnMemento objects saved with the given key, true if there are
     */
    public boolean hasMemento(String key) {
        return map.containsKey(key);
    }
}
