package psycle.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import psycle.gui.components.ChipGroup;
import psycle.gui.components.GlassPanel;
import psycle.gui.components.RoundedButton;
import psycle.gui.components.RoundedTextField;
import psycle.interfaces.PageNavigator;
import psycle.model.DailyEntry;
import psycle.model.Symptom;
import psycle.storage.DataManager;
import psycle.utils.AppConstants;
import psycle.utils.DateUtils;
import psycle.utils.ThemeManager;

/**
 * Daily health log. The form is filled from the saved entry of the chosen date (if any).
 */
public class DailyLogPage extends AuthenticatedPage {

    private static final int MAX_TAG_LENGTH = 25;
    private static final int MAX_NOTES_LENGTH = 500;

    private LocalDate currentDate = DateUtils.today();

    // ---------- Date controls ----------
    private final JLabel dateLabel =
            createLabel("", ThemeManager.FONT_HEADING, ThemeManager.TEXT_DARK);
    private final RoundedTextField dateField = new RoundedTextField(9);
    private final RoundedButton previousButton = new RoundedButton("Previous day", false);
    private final RoundedButton nextButton = new RoundedButton("Next day", false);

    // ---------- Form controls ----------
    private final ChipGroup flowGroup = new ChipGroup(AppConstants.FLOW_OPTIONS, false, 4);
    private final ChipGroup moodGroup = new ChipGroup(AppConstants.MOOD_OPTIONS, false, 6);
    private final ChipGroup energyGroup = new ChipGroup(AppConstants.ENERGY_OPTIONS, false, 3);
    private final ChipGroup skinGroup = new ChipGroup(AppConstants.SKIN_OPTIONS, false, 4);
    private final ChipGroup sleepGroup = new ChipGroup(AppConstants.SLEEP_OPTIONS, false, 3);
    private final ChipGroup symptomGroup = new ChipGroup(AppConstants.SYMPTOM_OPTIONS, true, 3);

    private final JSlider painSlider = new JSlider(0, 5, 0);
    private final JLabel painLabel =
            createLabel("", ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED);
    private final RoundedTextField tagField = new RoundedTextField(14);
    private final JTextArea notesArea = new JTextArea(4, 30);
    private final RoundedButton deleteButton = new RoundedButton("Delete entry", false);

    public DailyLogPage(DataManager dataManager, PageNavigator navigator) {
        super(AppConstants.PAGE_LOG, dataManager, navigator);
        initializePage();
    }

    // =====================================================
    //  Building the page
    // =====================================================

    @Override
    protected void initializePage() {
        setPageHeader("Daily Health Log",
                "Choose the options that fit, then press Save Entry. It only takes a minute.");

        JPanel column = createVerticalPanel();
        column.add(alignLeft(buildDateCard()));
        column.add(createGap(10));
        column.add(alignLeft(buildCheckInCard()));
        column.add(createGap(10));
        column.add(alignLeft(buildSymptomCard()));
        column.add(createGap(10));
        column.add(alignLeft(buildNotesCard()));
        column.add(createGap(10));
        getContentPanel().add(createScrollPane(column), BorderLayout.CENTER);

        loadDate();
    }

    private GlassPanel buildDateCard() {
        GlassPanel card = new GlassPanel(28, 14);
        JPanel inner = createVerticalPanel();

        previousButton.addActionListener(e -> changeDay(-1));
        nextButton.addActionListener(e -> changeDay(1));
        JPanel dayRow = createTransparentPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        dayRow.add(previousButton);
        dayRow.add(dateLabel);
        dayRow.add(nextButton);

        RoundedButton goButton = new RoundedButton("Go", false);
        goButton.addActionListener(e -> handleGoToDate());
        dateField.addActionListener(e -> handleGoToDate()); // Enter key
        JPanel jumpRow = createTransparentPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        jumpRow.add(createLabel("Go to date (yyyy-MM-dd):",
                ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED));
        jumpRow.add(dateField);
        jumpRow.add(goButton);

        inner.add(alignLeft(dayRow));
        inner.add(createGap(8));
        inner.add(alignLeft(jumpRow));
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private GlassPanel buildCheckInCard() {
        GlassPanel card = new GlassPanel(28, 14);
        JPanel inner = createVerticalPanel();

        inner.add(alignLeft(createLabel("How are you today?",
                ThemeManager.FONT_HEADING, ThemeManager.TEXT_DARK)));
        inner.add(createGap(6));
        inner.add(section("Menstrual flow", flowGroup));
        inner.add(createGap(6));
        inner.add(section("Pain level", buildPainPanel()));
        inner.add(createGap(6));
        inner.add(section("Mood", moodGroup));
        inner.add(createGap(6));
        inner.add(section("Energy level", energyGroup));
        inner.add(createGap(6));
        inner.add(section("Skin condition", skinGroup));
        inner.add(createGap(6));
        inner.add(section("Sleep quality", sleepGroup));

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildPainPanel() {
        painSlider.setMajorTickSpacing(1);
        painSlider.setPaintTicks(true);
        painSlider.setPaintLabels(true);
        painSlider.setSnapToTicks(true);
        painSlider.setOpaque(false);
        painSlider.setFont(ThemeManager.FONT_SMALL);
        painSlider.setForeground(ThemeManager.TEXT_MUTED);
        painSlider.setMaximumSize(new Dimension(420, painSlider.getPreferredSize().height));
        painSlider.setBorder(new EmptyBorder(0, 10, 0, 0));
        painSlider.addChangeListener(e -> updatePainLabel());

        painLabel.setBorder(new EmptyBorder(0, 10, 0, 0));

        JPanel panel = createVerticalPanel();
        panel.add(alignLeft(painLabel));
        panel.add(alignLeft(painSlider));
        updatePainLabel();
        return panel;
    }

    private void updatePainLabel() {
        painLabel.setText("Selected: " + painSlider.getValue() + " / 5   (0 = no pain, 5 = severe)");
    }

    private GlassPanel buildSymptomCard() {
        GlassPanel card = new GlassPanel(28, 14);
        JPanel inner = createVerticalPanel();

        inner.add(alignLeft(createLabel("Physical symptoms",
                ThemeManager.FONT_HEADING, ThemeManager.TEXT_DARK)));
        inner.add(alignLeft(createLabel("Select all that apply.",
                ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED)));
        inner.add(createGap(6));
        inner.add(alignLeft(symptomGroup));
        inner.add(createGap(8));

        RoundedButton addTagButton = new RoundedButton("Add tag", false);
        addTagButton.addActionListener(e -> handleAddTag());
        tagField.addActionListener(e -> handleAddTag()); // Enter key
        JPanel tagRow = createTransparentPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tagRow.add(createLabel("Add your own tag:",
                ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED));
        tagRow.add(tagField);
        tagRow.add(addTagButton);
        inner.add(alignLeft(tagRow));

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private GlassPanel buildNotesCard() {
        GlassPanel card = new GlassPanel(28, 14);
        JPanel inner = createVerticalPanel();

        inner.add(alignLeft(createLabel("Notes",
                ThemeManager.FONT_HEADING, ThemeManager.TEXT_DARK)));
        inner.add(alignLeft(createLabel("Anything you noticed today (max "
                + MAX_NOTES_LENGTH + " characters).",
                ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED)));
        inner.add(createGap(6));

        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setFont(ThemeManager.FONT_BODY);
        notesArea.setForeground(ThemeManager.TEXT_DARK);
        notesArea.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setBorder(new LineBorder(ThemeManager.PRIMARY, 2, true));
        notesScroll.setPreferredSize(new Dimension(300, 100));
        notesScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        inner.add(alignLeft(notesScroll));
        inner.add(createGap(14));

        RoundedButton saveButton = new RoundedButton("Save Entry");
        saveButton.addActionListener(e -> handleSave());
        RoundedButton clearButton = new RoundedButton("Clear form", false);
        clearButton.addActionListener(e -> resetForm());
        deleteButton.addActionListener(e -> handleDelete());

        JPanel buttons = createTransparentPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.add(saveButton);
        buttons.add(clearButton);
        buttons.add(deleteButton);
        inner.add(alignLeft(buttons));

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    /** A small muted title above a control. */
    private JPanel section(String title, java.awt.Component content) {
        JPanel panel = createVerticalPanel();
        JLabel label = createLabel(title,
                ThemeManager.FONT_SMALL.deriveFont(Font.BOLD), ThemeManager.TEXT_MUTED);
        label.setBorder(new EmptyBorder(0, 10, 0, 0));
        panel.add(alignLeft(label));
        if (content instanceof javax.swing.JComponent) {
            panel.add(alignLeft((javax.swing.JComponent) content));
        } else {
            panel.add(content);
        }
        return alignLeft(panel);
    }

    // =====================================================
    //  Showing and loading a date
    // =====================================================

    /** Opens on the requested date (from Calendar or Dashboard), otherwise on today. */
    @Override
    protected void refreshContent() {
        LocalDate requested = getDataManager().takeRequestedLogDate();
        if (requested != null && !DateUtils.isFuture(requested)) {
            currentDate = requested;
        } else {
            currentDate = DateUtils.today();
        }
        loadDate();
    }

    /** Shows the chosen date and fills the form from the saved entry (or defaults). */
    private void loadDate() {
        dateLabel.setText(longDate(currentDate));
        dateField.setText(DateUtils.format(currentDate));
        nextButton.setEnabled(currentDate.isBefore(DateUtils.today()));

        DailyEntry entry = getDataManager().getEntryForDate(DateUtils.format(currentDate));
        if (entry == null) {
            resetForm();
            deleteButton.setEnabled(false);
        } else {
            fillForm(entry);
            deleteButton.setEnabled(true);
        }
    }

    private void fillForm(DailyEntry entry) {
        flowGroup.setSelected(entry.getFlow());
        moodGroup.setSelected(entry.getMood());
        energyGroup.setSelected(entry.getEnergy());
        skinGroup.setSelected(entry.getSkin());
        sleepGroup.setSelected(entry.getSleep());
        painSlider.setValue(entry.getPainLevel());

        ArrayList<String> names = new ArrayList<>();
        Iterator<Symptom> it = entry.getSymptoms().iterator();
        while (it.hasNext()) {
            names.add(it.next().getName());
        }
        symptomGroup.setSelectedList(names);

        notesArea.setText(entry.getNotes());
        notesArea.setCaretPosition(0);
        tagField.setText("");
    }

    /** Puts the form back to its starting values (does not delete anything saved). */
    private void resetForm() {
        flowGroup.setSelected("None");
        moodGroup.clearSelection();
        energyGroup.setSelected("Medium");
        skinGroup.setSelected("Clear");
        sleepGroup.setSelected("Average");
        painSlider.setValue(0);
        symptomGroup.reset();
        notesArea.setText("");
        tagField.setText("");
    }

    private void changeDay(int delta) {
        LocalDate target = currentDate.plusDays(delta);
        if (DateUtils.isFuture(target)) {
            return; // the Next button is disabled on today, this is just a safety check
        }
        currentDate = target;
        loadDate();
    }

    private void handleGoToDate() {
        try {
            LocalDate target = DateUtils.parse(dateField.getText());
            if (DateUtils.isFuture(target)) {
                throw new IllegalArgumentException("You cannot log a day in the future.");
            }
            currentDate = target;
            loadDate();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            dateField.setText(DateUtils.format(currentDate));
        }
    }

    // =====================================================
    //  Button actions
    // =====================================================

    private void handleAddTag() {
        String tag = tagField.getText().trim();
        try {
            if (tag.isEmpty()) {
                throw new IllegalArgumentException("Type a tag first, for example: Dizzy");
            }
            if (tag.length() > MAX_TAG_LENGTH) {
                throw new IllegalArgumentException(
                        "A tag can have at most " + MAX_TAG_LENGTH + " characters.");
            }
            if (tag.contains(",") || tag.contains(";")) {
                throw new IllegalArgumentException("A tag cannot contain commas or semicolons.");
            }
            symptomGroup.addOption(tag); // new card, already selected
            tagField.setText("");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void handleSave() {
        try {
            String mood = moodGroup.getSelected();
            if (mood.isEmpty()) {
                throw new IllegalArgumentException("Please choose your mood before saving.");
            }
            String notes = notesArea.getText().trim();
            if (notes.length() > MAX_NOTES_LENGTH) {
                throw new IllegalArgumentException("Your notes are too long ("
                        + notes.length() + " characters). The limit is " + MAX_NOTES_LENGTH + ".");
            }

            // Collect everything from the form into one DailyEntry object
            DailyEntry entry = new DailyEntry(DateUtils.format(currentDate), mood,
                    orDefault(flowGroup.getSelected(), "None"), painSlider.getValue());
            entry.setEnergy(orDefault(energyGroup.getSelected(), "Medium"));
            entry.setSkin(orDefault(skinGroup.getSelected(), "Clear"));
            entry.setSleep(orDefault(sleepGroup.getSelected(), "Average"));
            for (String name : symptomGroup.getSelectedList()) {
                entry.addSymptom(new Symptom(name, !isBuiltInSymptom(name)));
            }
            entry.setNotes(notes);

            getDataManager().saveEntry(entry);
            deleteButton.setEnabled(true);

            String message = "Your entry for " + DateUtils.formatNice(currentDate) + " was saved.";
            if (getDataManager().isGuestMode()) {
                message += "\nGuest mode: it will be forgotten when you close Psycle.";
            }
            showInfo(message);
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (IOException e) {
            showError("Your entry could not be saved.\n" + e.getMessage());
        }
    }

    private void handleDelete() {
        if (!confirm("Delete the entry for " + DateUtils.formatNice(currentDate) + "?",
                "Delete entry")) {
            return;
        }
        try {
            if (getDataManager().removeEntry(DateUtils.format(currentDate))) {
                showInfo("The entry was deleted.");
            }
            loadDate();
        } catch (IOException e) {
            showError("The entry could not be deleted.\n" + e.getMessage());
        }
    }

    // ---------- Small helpers ----------

    private String orDefault(String value, String fallback) {
        return value.isEmpty() ? fallback : value;
    }

    private boolean isBuiltInSymptom(String name) {
        for (String option : AppConstants.SYMPTOM_OPTIONS) {
            if (option.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}