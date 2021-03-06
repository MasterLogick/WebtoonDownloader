package net.macu.core;

import net.macu.UI.IconManager;
import net.macu.UI.MainView;
import net.macu.UI.ViewManager;
import net.macu.cutter.AsIsCutter;
import net.macu.cutter.ConstantHeightCutter;
import net.macu.cutter.Cutter;
import net.macu.cutter.SingleScanCutter;
import net.macu.cutter.manual.ManualCutter;
import net.macu.cutter.pasta.PastaCutter;
import net.macu.imgWriter.ImgWriter;
import net.macu.imgWriter.PsbWriter;
import net.macu.imgWriter.StandardWriter;
import net.macu.service.*;
import net.macu.settings.L;
import net.macu.settings.Settings;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {
    private static List<Cutter> cutters;
    private static List<Service> services;
    private static List<ImgWriter> imgWriters;

    public static void main(String[] args) {
        prepareContext();
        new Thread(() -> IOManager.checkUpdates(false)).start();
        new MainView();
    }

    private static void prepareContext() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            JOptionPane.showMessageDialog(null, "Error: " + sw);
        });
        loadNativeFromJar();
        Settings.loadSettings();
        L.loadLanguageData();
        IconManager.loadIcons();
        ViewManager.setLookAndFeel();
        cutters = Collections.unmodifiableList(Arrays.asList(new ManualCutter(), new PastaCutter(), new AsIsCutter(),
                new ConstantHeightCutter(), new SingleScanCutter()));
        services = Collections.unmodifiableList(Arrays.asList(new AcQq(), new Daum(), new LocalFiles(), new ManhuaGui(),
                new Naver(), new Rawdevart()));
        imgWriters = Collections.unmodifiableList(Arrays.asList(StandardWriter.createImageIOWriter("PNG"),
                StandardWriter.createImageIOWriter("JPEG"), StandardWriter.createImageIOWriter("WEBP"),
                StandardWriter.createImageIOWriter("GIF"), new PsbWriter()));
        ViewManager.initFileChoosers();
        IOManager.initClient();
    }

    public static List<Cutter> getCutters() {
        return cutters;
    }

    public static List<Service> getServices() {
        return services;
    }

    public static List<ImgWriter> getImgWriters() {
        return imgWriters;
    }

    public static String getVersion() {
        return "v6.0.0";
    }

    //from com.luciad.imageio.webp.NativeLibraryUrls
    public static void loadNativeFromJar() {
        String os = System.getProperty("os.name").toLowerCase();
        String bits = System.getProperty("os.arch").contains("64") ? "64" : "32";
        String libFilename = "libwebp-imageio.so";
        String platform = "linux";
        if (os.contains("win")) {
            platform = "win";
            libFilename = "webp-imageio.dll";
        } else if (os.contains("mac")) {
            platform = "mac";
            libFilename = "libwebp-imageio.dylib";
        }

        try {
            InputStream in = Main.class.getResourceAsStream(String.format("/native/%s/%s/%s", platform, bits, libFilename));
            Throwable var5 = null;

            try {
                if (in == null) {
                    throw new RuntimeException(String.format("Could not find WebP native library for %s %s in the jar", platform, bits));
                }

                File tmpLibraryFile = Files.createTempFile("", libFilename).toFile();
                tmpLibraryFile.deleteOnExit();
                FileOutputStream out = new FileOutputStream(tmpLibraryFile);
                Throwable var8 = null;

                try {
                    byte[] buffer = new byte[8192];

                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                } catch (Throwable var34) {
                    var8 = var34;
                    throw var34;
                } finally {
                    if (out != null) {
                        if (var8 != null) {
                            try {
                                out.close();
                            } catch (Throwable var33) {
                                var8.addSuppressed(var33);
                            }
                        } else {
                            out.close();
                        }
                    }

                }

                System.load(tmpLibraryFile.getAbsolutePath());
            } catch (Throwable var36) {
                var5 = var36;
                throw var36;
            } finally {
                if (in != null) {
                    if (var5 != null) {
                        try {
                            in.close();
                        } catch (Throwable var32) {
                            var5.addSuppressed(var32);
                        }
                    } else {
                        in.close();
                    }
                }
            }
        } catch (IOException var38) {
            throw new RuntimeException("Could not load native WebP library", var38);
        }
    }
}
