package net.macu.UI;

import com.bulenkov.darcula.DarculaLaf;
import net.macu.browser.plugin.BrowserPlugin;
import net.macu.browser.proxy.cert.CertificateAuthority;
import net.macu.core.FileFilterImpl;
import net.macu.core.JobManager;
import net.macu.core.Main;
import net.macu.service.ServiceManager;
import net.macu.settings.L;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;

public class ViewManager {
    private static JFrame frame = null;
    private static MainView view;
    private static JFileChooser singleFileChooser;
    private static JFileChooser dirChooser;
    private static SettingsFrame settingsFrame;

    public static void startProgress(int max, String message) {
        view.startProgress(max, message);
    }

    public static void incrementProgress(String message) {
        view.incrementProgress(message);
    }

    public static void resetProgress() {
        view.resetProgress();
    }

    public static void showMessageDialog(String s) {
        if (!s.startsWith("<html>")) {
            s = "<html>" + s.replaceAll("\n", "<br>") + "</html>";
        }
        JEditorPane ep = new JEditorPane("text/html", s);
        ep.addHyperlinkListener(e -> {
            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (IOException | URISyntaxException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        ep.setEditable(false);
        ep.setBackground(new JLabel().getBackground());
        JOptionPane.showMessageDialog(frame, ep);
    }

    public static boolean showConfirmDialog(String s) {
        return JOptionPane.showConfirmDialog(frame, s, L.get("UI.ViewManager.confirm_dialog_title"), JOptionPane.YES_NO_CANCEL_OPTION) == JOptionPane.OK_OPTION;
    }

    public static void createView() {
        constructFileChoosers();
        constructFrame();
        settingsFrame = new SettingsFrame();
        frame.setJMenuBar(constructMenu());
        view = new MainView();
        frame.setContentPane(view.$$$getRootComponent$$$());
        frame.pack();
        frame.setVisible(true);
    }

    public static String requestSelectSingleFile(String extension) {
        String path = null;
        singleFileChooser.resetChoosableFileFilters();
        singleFileChooser.addChoosableFileFilter(new FileFilterImpl(extension));
        if (singleFileChooser.showDialog(frame, L.get("UI.ViewManager.single_file_approve_button")) == JFileChooser.APPROVE_OPTION) {
            path = singleFileChooser.getSelectedFile().getAbsolutePath();
            if (!path.toLowerCase().endsWith("." + extension)) {
                path += "." + extension;
            }
        }
        return path;
    }

    public static String requestSelectDir() {
        if (dirChooser.showDialog(frame, L.get("UI.ViewManager.dir_select_button")) == JFileChooser.APPROVE_OPTION) {
            return dirChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    public static void packFrame() {
        frame.pack();
    }

    public static JFrame getFrame() {
        return frame;
    }

    public static void showPreviewFrame(BufferedImage image, Component parent) {
        if (image != null) {
            JFrame f = new JFrame(L.get("UI.ViewManager.preview_frame_title"));
            JScrollPane pane = new JScrollPane(new JLabel(new ImageIcon(image)));
            pane.getVerticalScrollBar().setUnitIncrement(14);
            pane.getHorizontalScrollBar().setUnitIncrement(14);
            f.add(pane);
            f.setIconImage(IconManager.getBrandIcon());
            f.setLocationRelativeTo(parent);
            f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            f.setSize(600, 600);
            f.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    f.dispose();
                }
            });
            f.setVisible(true);
        }
    }

    public static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new DarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    private static void constructFileChoosers() {
        singleFileChooser = new JFileChooser();
        singleFileChooser.setDialogTitle(L.get("UI.ViewManager.file_chooser_title"));
        singleFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        singleFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        singleFileChooser.setAcceptAllFileFilterUsed(false);
        singleFileChooser.setMultiSelectionEnabled(false);
        dirChooser = new JFileChooser();
        dirChooser.setDialogTitle(L.get("UI.ViewManager.file_chooser_title"));
        dirChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        dirChooser.setAcceptAllFileFilterUsed(false);
        dirChooser.setMultiSelectionEnabled(false);
    }

    private static void constructFrame() {
        if (frame == null) {
            frame = new JFrame(L.get("UI.ViewManager.main_frame_title"));
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setIconImage(IconManager.getBrandIcon());
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    JobManager.cancel();
                    frame.dispose();
                }
            });
        }
    }

    private static JMenuBar constructMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu fileMenu = new JMenu(L.get("UI.ViewManager.help_menu"));
        JMenuItem settingsMenu = new JMenuItem(L.get("UI.ViewManager.settings_menu"));
        settingsMenu.addActionListener(e -> settingsFrame.setVisible(true));
        fileMenu.add(settingsMenu);
        JMenuItem supportedServicesItem = new JMenuItem(L.get("UI.ViewManager.supported_services_menu"));
        supportedServicesItem.addActionListener(actionEvent -> ViewManager.showMessageDialog(L.get("UI.ViewManager.supported_services_list", ServiceManager.getSupportedServicesList())));
        fileMenu.add(supportedServicesItem);
        JMenuItem aboutItem = new JMenuItem(L.get("UI.ViewManager.about_menu"));
        aboutItem.addActionListener(actionEvent -> ViewManager.showMessageDialog(L.get("UI.ViewManager.about_text", Main.getVersion())));
        fileMenu.add(aboutItem);
        bar.add(fileMenu);
        JMenu pluginMenu = new JMenu(L.get("UI.ViewManager.plugin_menu"));
        JMenuItem generateCertificateItem = new JMenuItem(L.get("UI.ViewManager.generate_certificate_menu"));
        generateCertificateItem.addActionListener(e -> CertificateAuthority.openGenerateCertFrame());
        pluginMenu.add(generateCertificateItem);
        JMenuItem pluginConnectionItem = new JMenuItem(L.get("UI.ViewManager.plugin_connection_menu"));
        pluginConnectionItem.addActionListener(e -> {
            ViewManager.showMessageDialog(L.get("UI.ViewManager.plugin_connection", BrowserPlugin.getPlugin().isConnected() ? L.get("UI.ViewManager.plugin_connection_true") : L.get("UI.ViewManager.plugin_connection_false")));
        });
        pluginMenu.add(pluginConnectionItem);
        bar.add(pluginMenu);
        return bar;
    }
}
