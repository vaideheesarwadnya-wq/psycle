package psycle.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.GridBagLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import psycle.gui.components.GlassPanel;
import psycle.gui.components.RoundedButton;
import psycle.gui.components.RoundedPasswordField;
import psycle.gui.components.RoundedTextField;
import psycle.interfaces.PageNavigator;
import psycle.model.User;
import psycle.storage.DataManager;
import psycle.utils.AppConstants;
import psycle.utils.ThemeManager;

/**
 * Welcome / login page. No email or online account: only a local PIN.
 */
public class WelcomePage extends BasePage {

    private static final int CARD_WIDTH = 440;
    private static final int MAX_NAME_LENGTH = 30;

    private final RoundedTextField nameField = new RoundedTextField(16);
    private final RoundedPasswordField createPinField = new RoundedPasswordField(16);
    private final RoundedPasswordField confirmPinField = new RoundedPasswordField(16);
    private final RoundedPasswordField loginPinField = new RoundedPasswordField(16);
    private final JLabel loginHeading =
            createLabel("Welcome back", ThemeManager.FONT_HEADING, ThemeManager.TEXT_DARK);
    private final JPanel formHolder = createTransparentPanel(new BorderLayout());

    private JPanel createForm;
    private JPanel loginForm;

    public WelcomePage(DataManager dataManager, PageNavigator navigator) {
        super(AppConstants.PAGE_WELCOME, dataManager, navigator);
        initializePage();
    }

    // =====================================================
    //  Building the page
    // =====================================================

    @Override
    protected void initializePage() {
        setLayout(new GridBagLayout()); // centres everything on the page

        createForm = buildCreateForm();
        loginForm = buildLoginForm();

        JPanel column = createVerticalPanel();
        column.add(centered(createBrandRow()));
        column.add(Box.createVerticalStrut(2));
        column.add(centered(createLabel(AppConstants.TAGLINE,
                ThemeManager.FONT_HEADING, ThemeManager.TEXT_MUTED)));
        column.add(Box.createVerticalStrut(20));
        column.add(buildCard());
        add(column);
    }

    /** The glass card that holds the form, the guest button and the privacy message. */
    private GlassPanel buildCard() {
        // Anonymous subclass: fixed width, height depends on the content
        GlassPanel card = new GlassPanel(30, 24) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(CARD_WIDTH, super.getPreferredSize().height);
            }
        };

        JPanel inner = createVerticalPanel();
        formHolder.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(formHolder);
        inner.add(Box.createVerticalStrut(12));

        RoundedButton guestButton = new RoundedButton("Continue as guest", false);
        guestButton.addActionListener(e -> handleGuest());
        stretch(guestButton);
        inner.add(guestButton);

        inner.add(Box.createVerticalStrut(12));
        JLabel privacy = createLabel(
                "<html><div style='text-align:center;width:330px'>"
                + "Your data stays on this device. No account, no internet, no tracking."
                + "</div></html>", ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED);
        inner.add(centered(privacy));

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JPanel createBrandRow() {
        JPanel row = createTransparentPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        JComponent logo = createLogo();
        logo.setPreferredSize(new Dimension(56, 56));
        row.add(logo);
        row.add(createLabel(AppConstants.APP_NAME,
                ThemeManager.FONT_TITLE.deriveFont(46f), ThemeManager.ACCENT));
        return row;
    }

    /** Simple painted logo: a pink circle with a white ring (a cycle) and a small dot. */
    private JComponent createLogo() {
        return new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ThemeManager.enableAntialiasing(g2);
                int s = Math.min(getWidth(), getHeight());

                g2.setPaint(new GradientPaint(0, 0, ThemeManager.PRIMARY,
                        s, s, ThemeManager.ACCENT));
                g2.fillOval(0, 0, s, s);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(4f));
                g2.drawOval(s / 4, s / 4, s / 2, s / 2);

                // small dot on the ring (top right)
                double dotX = s / 2.0 + (s / 4.0) * 0.707;
                double dotY = s / 2.0 - (s / 4.0) * 0.707;
                g2.fillOval((int) dotX - 5, (int) dotY - 5, 10, 10);
                g2.dispose();
            }
        };
    }

    private JPanel buildCreateForm() {
        JPanel form = createVerticalPanel();
        form.add(centered(createLabel("Create your PIN",
                ThemeManager.FONT_HEADING, ThemeManager.TEXT_DARK)));
        form.add(Box.createVerticalStrut(4));
        form.add(centered(createLabel("Choose a 4-6 digit PIN to keep your space private.",
                ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED)));
        form.add(Box.createVerticalStrut(14));

        form.add(createFieldRow("Your name (optional)", nameField));
        form.add(Box.createVerticalStrut(8));
        form.add(createFieldRow("PIN", createPinField));
        form.add(Box.createVerticalStrut(8));
        form.add(createFieldRow("Confirm PIN", confirmPinField));
        form.add(Box.createVerticalStrut(16));

        RoundedButton createButton = new RoundedButton("Create PIN");
        createButton.addActionListener(e -> handleCreatePin());
        confirmPinField.addActionListener(e -> handleCreatePin()); // Enter key
        stretch(createButton);
        form.add(createButton);
        return form;
    }

    private JPanel buildLoginForm() {
        JPanel form = createVerticalPanel();
        form.add(centered(loginHeading));
        form.add(Box.createVerticalStrut(4));
        form.add(centered(createLabel("Enter your PIN to open your space.",
                ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED)));
        form.add(Box.createVerticalStrut(14));

        form.add(createFieldRow("PIN", loginPinField));
        form.add(Box.createVerticalStrut(16));

        RoundedButton loginButton = new RoundedButton("Log in");
        loginButton.addActionListener(e -> handleLogin());
        loginPinField.addActionListener(e -> handleLogin()); // Enter key
        stretch(loginButton);
        form.add(loginButton);
        form.add(Box.createVerticalStrut(6));

        JButton forgotButton = createLinkButton("Forgot PIN?");
        forgotButton.addActionListener(e -> handleForgotPin());
        form.add(forgotButton);
        return form;
    }

    // ---------- Small layout helpers ----------

    /** A small label above an input field. */
    private JPanel createFieldRow(String labelText, JComponent field) {
        JPanel row = createVerticalPanel();
        JLabel label = createLabel(labelText, ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED);
        label.setBorder(new EmptyBorder(0, 6, 3, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));

        row.add(label);
        row.add(field);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        return row;
    }

    private <T extends JComponent> T centered(T component) {
        component.setAlignmentX(Component.CENTER_ALIGNMENT);
        return component;
    }

    /** Makes a component as wide as its parent. */
    private void stretch(JComponent component) {
        component.setAlignmentX(Component.CENTER_ALIGNMENT);
        component.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
    }

    /** A text-only button that looks like a link. */
    private JButton createLinkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(ThemeManager.FONT_SMALL);
        button.setForeground(ThemeManager.ACCENT);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    private void showForm(JPanel form) {
        formHolder.removeAll();
        formHolder.add(form, BorderLayout.CENTER);
        formHolder.revalidate();
        formHolder.repaint();
        revalidate();
    }

    private void clearFields() {
        nameField.setText("");
        createPinField.setText("");
        confirmPinField.setText("");
        loginPinField.setText("");
    }

    // =====================================================
    //  Showing the page
    // =====================================================

    /** Shows the login form if a PIN exists, otherwise the create-PIN form. */
    @Override
    public void onShow() {
        clearFields();
        User user = getDataManager().getUser();
        if (user.hasPin()) {
            loginHeading.setText(user.getName().isEmpty()
                    ? "Welcome back" : "Welcome back, " + user.getName());
            showForm(loginForm);
            SwingUtilities.invokeLater(() -> loginPinField.requestFocusInWindow());
        } else {
            showForm(createForm);
            SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
        }
    }

    // =====================================================
    //  Button actions
    // =====================================================

    private void handleCreatePin() {
        String name = nameField.getText().trim();
        String pin = new String(createPinField.getPassword());
        String confirm = new String(confirmPinField.getPassword());

        try {
            if (name.length() > MAX_NAME_LENGTH) {
                throw new IllegalArgumentException(
                        "Your name can have at most " + MAX_NAME_LENGTH + " characters.");
            }
            if (!pin.equals(confirm)) {
                throw new IllegalArgumentException("The two PINs do not match.");
            }

            User user = getDataManager().getUser();
            user.setName(name);
            user.setPin(pin); // throws IllegalArgumentException if the PIN is not 4-6 digits
            user.setLockEnabled(true);
            getDataManager().saveUser();

            clearFields();
            navigateTo(AppConstants.PAGE_DASHBOARD);
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            createPinField.setText("");
            confirmPinField.setText("");
        } catch (IOException e) {
            showError("Your PIN could not be saved.\n" + e.getMessage());
        }
    }

    private void handleLogin() {
        String pin = new String(loginPinField.getPassword());

        if (pin.isEmpty()) {
            showError("Please enter your PIN.");
            return;
        }
        if (!User.isValidPin(pin)) {
            showError("A PIN contains only digits (" + AppConstants.MIN_PIN_LENGTH + " to "
                    + AppConstants.MAX_PIN_LENGTH + " of them).");
            loginPinField.setText("");
            return;
        }
        if (getDataManager().getUser().checkPin(pin)) {
            clearFields();
            navigateTo(AppConstants.PAGE_DASHBOARD);
        } else {
            showError("Incorrect PIN. Please try again.");
            loginPinField.setText("");
        }
    }

    private void handleGuest() {
        boolean ok = confirm("Guest mode keeps your entries only until you close Psycle.\n"
                + "Nothing is saved on this computer.\n\nContinue as guest?", "Guest mode");
        if (ok) {
            getDataManager().startGuestSession();
            navigateTo(AppConstants.PAGE_DASHBOARD);
        }
    }

    private void handleForgotPin() {
        boolean ok = confirm("Psycle cannot recover a forgotten PIN.\n"
                + "Resetting will permanently delete ALL saved Psycle data on this computer.\n\n"
                + "Delete everything and start again?", "Reset PIN");
        if (!ok) {
            return;
        }
        try {
            getDataManager().deleteAllData();
            clearFields();
            showForm(createForm);
            SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
        } catch (IOException e) {
            showError("The data could not be deleted.\n" + e.getMessage());
        }
    }
}
