package psycle.gui.components;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import psycle.events.CardMouseListener;
import psycle.utils.ThemeManager;

/**
 * A group of selectable cards. Click a card to select it (MouseListener does the work).
 * multiSelect = false : only one card can be selected (like radio buttons).
 * multiSelect = true  : any number of cards can be selected (like check boxes).
 */
public class ChipGroup extends JPanel {

    private final String[] baseOptions;
    private final boolean multiSelect;
    private final LinkedHashMap<String, GlassPanel> chips = new LinkedHashMap<>();
    private final ArrayList<String> selected = new ArrayList<>();

    public ChipGroup(String[] baseOptions, boolean multiSelect, int columns) {
        this.baseOptions = baseOptions;
        this.multiSelect = multiSelect;
        setOpaque(false);
        setLayout(new GridLayout(0, columns, 0, 0)); // 0 rows = as many rows as needed
        reset();
    }

    /** Back to the original options, with nothing selected (removes custom options). */
    public void reset() {
        removeAll();
        chips.clear();
        selected.clear();
        for (String option : baseOptions) {
            createChip(option);
        }
        revalidate();
        repaint();
    }

    private void createChip(String name) {
        GlassPanel chip = new GlassPanel(18, 4);
        JLabel label = new JLabel(name, SwingConstants.CENTER);
        label.setFont(ThemeManager.FONT_BODY);
        label.setForeground(ThemeManager.TEXT_DARK);
        chip.add(label, BorderLayout.CENTER);

        // MouseListener: hover effect + click toggles the selection
        chip.addMouseListener(new CardMouseListener(chip, () -> toggle(name)));

        chips.put(name, chip);
        add(chip);
    }

    private void toggle(String name) {
        if (multiSelect) {
            setChipSelected(name, !selected.contains(name));
        } else {
            clearSelection();
            setChipSelected(name, true);
        }
    }

    private void setChipSelected(String name, boolean on) {
        GlassPanel chip = chips.get(name);
        if (chip == null) {
            return;
        }
        chip.setSelected(on);
        if (on && !selected.contains(name)) {
            selected.add(name);
        }
        if (!on) {
            selected.remove(name);
        }
    }

    /** Finds an option ignoring upper/lower case. Returns null if it does not exist. */
    private String findOption(String text) {
        Iterator<String> it = chips.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (key.equalsIgnoreCase(text)) {
                return key;
            }
        }
        return null;
    }

    // =====================================================
    //  Public methods used by the page
    // =====================================================

    public void clearSelection() {
        Iterator<Map.Entry<String, GlassPanel>> it = chips.entrySet().iterator();
        while (it.hasNext()) {
            it.next().getValue().setSelected(false);
        }
        selected.clear();
    }

    /** Single-choice groups: selects the option with this name (if it exists). */
    public void setSelected(String name) {
        clearSelection();
        String key = findOption(name);
        if (key != null) {
            setChipSelected(key, true);
        }
    }

    /** Multi-choice groups: selects these names. Unknown names become new custom chips. */
    public void setSelectedList(ArrayList<String> names) {
        reset();
        Iterator<String> it = names.iterator();
        while (it.hasNext()) {
            addOption(it.next());
        }
    }

    /** Adds a new chip (if it is not there yet) and selects it. Used for custom tags. */
    public void addOption(String name) {
        String key = findOption(name);
        if (key == null) {
            createChip(name);
            key = name;
            revalidate();
            repaint();
        }
        if (!multiSelect) {
            clearSelection();
        }
        setChipSelected(key, true);
    }

    /** Single-choice: the selected name, or "" if nothing is selected. */
    public String getSelected() {
        return selected.isEmpty() ? "" : selected.get(0);
    }

    /** All selected names, in the same order as the cards. */
    public ArrayList<String> getSelectedList() {
        ArrayList<String> result = new ArrayList<>();
        Iterator<String> it = chips.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (selected.contains(key)) {
                result.add(key);
            }
        }
        return result;
    }
}