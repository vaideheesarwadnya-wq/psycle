package psycle.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import psycle.gui.components.GlassPanel;
import psycle.gui.components.RoundedButton;
import psycle.gui.components.RoundedPasswordField;
import psycle.interfaces.PageNavigator;
import psycle.model.User;
import psycle.service.ReminderService;
import psycle.storage.DataExporter;
import psycle.storage.DataManager;
import psycle.utils.AppConstants;
import psycle.utils.ThemeManager;

/**
 * Settings and privacy page.
 */
public class SettingsPage extends AuthenticatedPage {

    private static final int TEXT_WIDTH = 540;

    // ---------- Change PIN ----------
    private final RoundedPasswordField currentPinField = new RoundedPasswordField(10);
    private final RoundedPasswordField newPinField = new RoundedPasswordField(10);
    private final RoundedPasswordField confirmPinField = new RoundedPasswordField(10);
    private final JLabel pinHintLabel =
            createLabel("", ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED);
    private RoundedButton changePinButton;

    // ---------- Application lock ----------
    private final JCheckBox lockBox = createCheckBox("Ask for my PIN every time Psycle opens");
    private final JLabel lockHintLabel =
            createLabel("", ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED);

    // ---------- Reminders ----------
    private final JCheckBox dailyBox = createCheckBox("Remind me to log my day if I have not yet");
    private final JCheckBox periodBox = createCheckBox("Remind me before my predicted period");
    private final JCheckBox discreetBox = createCheckBox("Use discreet wording (no health terms)");
    private final JSpinner daysSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 7, 1));
    private final JLabel reminderPreviewLabel =
            createLabel("", ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED);

    // ---------- Storage info ----------
    private final JLabel storageLabel =
            createLabel("", ThemeManager.FONT_SMALL, ThemeManager.TEXT_DARK);

    public SettingsPage(DataManager dataManager, PageNavigator navigator) {
        super(AppConstants.PAGE_SETTINGS, dataManager, navigator);
        initializePage();
    }

    // =====================================================
    //  Building the page
    // =====================================================

    @Override
    protected void initializePage() {
        setPageHeader("Settings & Privacy",
                "Everything stays on this computer. You are in control of your data.");

        JPanel column = createVerticalPanel();
        column.add(buildPinCard());
        column.add(createGap(8));
        column.add(buildLockCard());
        column.add(createGap(8));
        column.add(buildReminderCard());
        column.add(createGap(8));
        column.add(buildExportCard());
        column.add(createGap(8));
        column.add(buildDeleteCard());
        column.add(createGap(8));
        column.add(buildPrivacyCard());
        column.add(createGap(6));

        getContentPanel().add(createScrollPane(column), BorderLayout.CENTER);
    }

    private GlassPanel buildPinCard() {
        changePinButton = new RoundedButton("Change PIN");
        changePinButton.addActionListener(e -> handleChangePin());

        JPanel body = createVerticalPanel();
        body.add(alignLeft(labeledRow("Current PIN", currentPinField)));
        body.add(alignLeft(labeledRow("New PIN", newPinField)));
        body.add(alignLeft(labeledRow("Confirm new PIN", confirmPinField)));
        body.add(createGap(6));
        body.add(alignLeft(changePinButton));
        body.add(createGap(6));
        body.add(alignLeft(pinHintLabel));
        return createCard("Change PIN",
                "Your PIN is " + AppConstants.MIN_PIN_LENGTH + " to " + AppConstants.MAX_PIN_LENGTH
                        + " digits.", body);
    }

    private GlassPanel buildLockCard() {
        lockBox.addActionListener(e -> handleLockToggle());

        JPanel body = createVerticalPanel();
        body.add(alignLeft(lockBox));
        body.add(createGap(4));
        body.add(alignLeft(lockHintLabel));
        return createCard("Application lock",
                "Decide whether Psycle asks for your PIN when it starts.", body);
    }

    private GlassPanel buildReminderCard() {
        JPanel daysRow = createTransparentPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        daysRow.add(createLabel("Start the period reminder",
                ThemeManager.FONT_BODY, ThemeManager.TEXT_DARK));
        daysRow.add(daysSpinner);
        daysRow.add(createLabel("day(s) before", ThemeManager.FONT_BODY, ThemeManager.TEXT_DARK));

        RoundedButton saveButton = new RoundedButton("Save reminder settings");
        saveButton.addActionListener(e -> handleSaveReminders());

        JPanel body = createVerticalPanel();
        body.add(alignLeft(dailyBox));
        body.add(alignLeft(periodBox));
        body.add(alignLeft(daysRow));
        body.add(alignLeft(discreetBox));
        body.add(createGap(8));
        body.add(alignLeft(saveButton));
        body.add(createGap(8));
        body.add(alignLeft(reminderPreviewLabel));
        return createCard("Discreet reminders",
                "Reminders appear quietly in the sidebar while Psycle is open. "
                        + "No system notifications are used.", body);
    }

    private GlassPanel buildExportCard() {
        RoundedButton exportButton = new RoundedButton("Export my data (CSV)");
        exportButton.addActionListener(e -> handleExport());

        JPanel body = createVerticalPanel();
        body.add(alignLeft(exportButton));
        return createCard("Export data",
                "Saves your periods and daily entries as two CSV files you can open in Excel. "
                        + "For a printable report, use the PDF Report page.", body);
    }

    private GlassPanel buildDeleteCard() {
        RoundedButton deleteButton = new RoundedButton("Delete all my data");
        deleteButton.addActionListener(e -> handleDeleteAll());

        JPanel body = createVerticalPanel();
        body.add(alignLeft(deleteButton));
        return createCard("Delete all data",
                "Permanently removes your PIN, periods, daily entries and settings "
                        + "from this computer. This cannot be undone.", body);
    }

    private GlassPanel buildPrivacyCard() {
        String privacyText = "- Your data is stored only on this computer, in the data folder.\n"
                + "- No online account, no internet connection, no advertising or analytics.\n"
                + "- Your health data is never sent anywhere.\n"
                + "- Your PIN is saved only as a SHA-256 hash, never as plain text.\n"
                + "- Important: the PIN and the app lock protect the Psycle window. The data files "
                + "themselves are plain text and not encrypted, so also protect your computer "
                + "with a Windows password or disk encryption.\n"
                + "- Predictions are estimates, not medical advice.";

        JPanel body = createVerticalPanel();
        body.add(alignLeft(createLabel(wrapHtml(privacyText, TEXT_WIDTH),
                ThemeManager.FONT_SMALL, ThemeManager.TEXT_DARK)));
        body.add(createGap(10));
        body.add(alignLeft(storageLabel));
        return createCard("Privacy & local storage", "How Psycle treats your information.", body);
    }

    // ---------- Layout helpers ----------

    private GlassPanel createCard(String title, String subtitle, JPanel body) {
        GlassPanel card = new GlassPanel(28, 16);
        JPanel inner = createVerticalPanel();
        inner.add(alignLeft(createLabel(title, ThemeManager.FONT_HEADING, ThemeManager.TEXT_DARK)));
        inner.add(alignLeft(createLabel(wrapHtml(subtitle, TEXT_WIDTH),
                ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED)));
        inner.add(createGap(8));
        inner.add(alignLeft(body));
        card.add(inner, BorderLayout.CENTER);
        return alignLeft(card);
    }

    private JPanel labeledRow(String text, JComponent field) {
        JPanel row = createTransparentPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        JLabel label = createLabel(text, ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED);
        label.setPreferredSize(new Dimension(120, 24));
        row.add(label);
        row.add(field);
        return row;
    }

    private JCheckBox createCheckBox(String text) {
        JCheckBox box = new JCheckBox(text);
        box.setOpaque(false);
        box.setFont(ThemeManager.FONT_BODY);
        box.setForeground(ThemeManager.TEXT_DARK);
        box.setFocusPainted(false);
        return box;
    }

    // =====================================================
    //  Loading the current values (every time the page is shown)
    // =====================================================

    @Override
    protected void refreshContent() {
        DataManager dm = getDataManager();
        User user = dm.getUser();
        boolean hasPin = user.hasPin();

        // PIN
        currentPinField.setText("");
        newPinField.setText("");
        confirmPinField.setText("");
        currentPinField.setEnabled(hasPin);
        newPinField.setEnabled(hasPin);
        confirmPinField.setEnabled(hasPin);
        changePinButton.setEnabled(hasPin);
        if (hasPin) {
            pinHintLabel.setText("Your PIN is stored as a SHA-256 hash, never as plain text.");
        } else if (dm.isGuestMode()) {
            pinHintLabel.setText("PINs are not available in guest mode.");
        } else {
            pinHintLabel.setText("You have no PIN yet. Create one on the Welcome page.");
        }

        // Lock
        lockBox.setSelected(user.isLockEnabled());
        lockBox.setEnabled(hasPin);
        updateLockHint();

        // Reminders
        dailyBox.setSelected(ReminderService.isEnabled(dm, ReminderService.KEY_DAILY, false));
        periodBox.setSelected(ReminderService.isEnabled(dm, ReminderService.KEY_PERIOD, false));
        discreetBox.setSelected(ReminderService.isEnabled(dm, ReminderService.KEY_DISCREET, true));
        daysSpinner.setValue(ReminderService.getDaysBefore(dm));
        updateReminderPreview();

        updateStorageInfo();
    }

    private void updateLockHint() {
        if (!getDataManager().getUser().hasPin()) {
            lockHintLabel.setText("Create a PIN on the Welcome page to use the app lock.");
        } else if (lockBox.isSelected()) {
            lockHintLabel.setText("Psycle will ask for your PIN each time it opens.");
        } else {
            lockHintLabel.setText(wrapHtml("Psycle will open straight to the dashboard. "
                    + "Anyone using this computer could see your data.", TEXT_WIDTH));
        }
    }

    private void updateReminderPreview() {
        ArrayList<String> due = ReminderService.getReminders(getDataManager());
        String text = due.isEmpty() ? "No reminders are due right now."
                : "Due now: " + String.join(" ", due);
        reminderPreviewLabel.setText(wrapHtml(text, TEXT_WIDTH));
    }

    private void updateStorageInfo() {
        DataManager dm = getDataManager();
        String text;
        if (dm.isGuestMode()) {
            text = "Guest mode: nothing is saved to disk. Everything you enter is forgotten "
                    + "when you close Psycle.";
        } else {
            text = "Data folder: " + new File(AppConstants.DATA_FOLDER).getAbsolutePath() + "\n"
                    + "Files: " + AppConstants.USER_FILE + ", " + AppConstants.CYCLES_FILE + ", "
                    + AppConstants.ENTRIES_FILE + ", " + AppConstants.SETTINGS_FILE + "\n"
                    + "Currently stored: " + dm.getCycles().size() + " periods and "
                    + dm.getEntries().size() + " daily entries.";
        }
        storageLabel.setText(wrapHtml(text, TEXT_WIDTH));
    }

    // =====================================================
    //  Button actions
    // =====================================================

    private void handleChangePin() {
        User user = getDataManager().getUser();
        String current = new String(currentPinField.getPassword());
        String newPin = new String(newPinField.getPassword());
        String confirm = new String(confirmPinField.getPassword());

        try {
            if (!user.hasPin()) {
                throw new IllegalArgumentException("You have no PIN yet. Create one on the Welcome page.");
            }
            if (!user.checkPin(current)) {
                throw new IllegalArgumentException("Your current PIN is not correct.");
            }
            if (!User.isValidPin(newPin)) {
                throw new IllegalArgumentException("The new PIN must be " + AppConstants.MIN_PIN_LENGTH
                        + " to " + AppConstants.MAX_PIN_LENGTH + " digits.");
            }
            if (!newPin.equals(confirm)) {
                throw new IllegalArgumentException("The new PIN and its confirmation do not match.");
            }
            if (newPin.equals(current)) {
                throw new IllegalArgumentException("Please choose a PIN that is different from the old one.");
            }

            user.setPin(newPin); // stored as a hash
            getDataManager().saveUser();

            currentPinField.setText("");
            newPinField.setText("");
            confirmPinField.setText("");
            showInfo("Your PIN was changed.");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (IOException e) {
            showError("The new PIN could not be saved to disk.\n" + e.getMessage());
        }
    }

    private void handleLockToggle() {
        User user = getDataManager().getUser();
        boolean wanted = lockBox.isSelected();
        try {
            user.setLockEnabled(wanted);
            getDataManager().saveUser();
            updateLockHint();
        } catch (IOException e) {
            user.setLockEnabled(!wanted); // undo, because it was not saved
            lockBox.setSelected(!wanted);
            showError("The setting could not be saved.\n" + e.getMessage());
        }
    }

    private void handleSaveReminders() {
        DataManager dm = getDataManager();
        try {
            dm.setSetting(ReminderService.KEY_DAILY, String.valueOf(dailyBox.isSelected()));
            dm.setSetting(ReminderService.KEY_PERIOD, String.valueOf(periodBox.isSelected()));
            dm.setSetting(ReminderService.KEY_DISCREET, String.valueOf(discreetBox.isSelected()));
            dm.setSetting(ReminderService.KEY_DAYS, String.valueOf(daysSpinner.getValue()));
            updateReminderPreview();
            refreshSidebar(); // show or hide the reminder in the sidebar right away
            showInfo("Your reminder settings were saved.");
        } catch (IOException e) {
            showError("The reminder settings could not be saved.\n" + e.getMessage());
        }
    }

    private void handleExport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a folder for your exported data");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showDialog(this, "Export here") != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File folder = chooser.getSelectedFile();
        boolean exists = new File(folder, DataExporter.PERIODS_FILE).exists()
                || new File(folder, DataExporter.ENTRIES_FILE).exists();
        if (exists && !confirm("Files with the same names already exist in this folder.\n"
                + "Replace them?", "Replace files")) {
            return;
        }

        try {
            DataExporter.exportAll(getDataManager(), folder);
            showInfo("Your data was exported to:\n" + folder.getAbsolutePath() + "\n\n"
                    + DataExporter.PERIODS_FILE + "\n" + DataExporter.ENTRIES_FILE);
        } catch (IOException e) {
            showError("Your data could not be exported.\n" + e.getMessage());
        }
    }

    private void handleDeleteAll() {
        DataManager dm = getDataManager();
        boolean guest = dm.isGuestMode();

        String question = guest
                ? "Clear everything you entered in this guest session?\n"
                  + "(Your saved data on this computer is not touched.)"
                : "This will permanently delete ALL Psycle data on this computer:\n"
                  + "your PIN, periods, daily entries and settings.\n\n"
                  + "This cannot be undone. Delete everything?";
        if (!confirm(question, "Delete all data")) {
            return;
        }

        try {
            if (guest) {
                dm.startGuestSession(); // clears only the in-memory guest data
                showInfo("The guest data was cleared.");
                refreshContent();
            } else {
                dm.deleteAllData();
                showInfo("All of your data was deleted.");
                navigateTo(AppConstants.PAGE_WELCOME); // no PIN exists now, so you can start fresh
            }
        } catch (IOException e) {
            showError("The data could not be deleted.\n" + e.getMessage());
        }
    }
}