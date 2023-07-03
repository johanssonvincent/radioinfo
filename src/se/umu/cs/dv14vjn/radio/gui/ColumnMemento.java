package se.umu.cs.dv14vjn.radio.gui;

import javax.swing.table.TableColumn;

/**
 * ColumnMemento is a class that stores the state of a TableColumn object
 * @author Vincent Johansson
 */
public class ColumnMemento {

    private final TableColumn state;

    /**
     * Initializes a new se.umu.cs.dv14vjn.radio.gui.ColumnMemento
     * @param columnModel TableColumn of which the state is to be saved
     */
    public ColumnMemento(TableColumn columnModel) {
        state = new TableColumn();
        state.setMaxWidth(columnModel.getMaxWidth());
        state.setMinWidth(columnModel.getMinWidth());
        state.setPreferredWidth(columnModel.getPreferredWidth());
        state.setWidth(columnModel.getWidth());
    }

    /**
     * Returns the saved TableColumn state
     * @return state TableColumn
     */
    public TableColumn getState() {
        return state;
    }
}
