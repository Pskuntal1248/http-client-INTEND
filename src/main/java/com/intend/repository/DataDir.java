package com.intend.repository;

import java.io.File;

/**
 * Centralises the data-directory convention used by all repositories.
 * <p>
 * On every OS the directory resolves to:
 * <pre>
 *   macOS   → /Users/&lt;user&gt;/.intend/
 *   Linux   → /home/&lt;user&gt;/.intend/
 *   Windows → C:\Users\&lt;user&gt;\.intend\
 * </pre>
 * The directory is created lazily on first access if it does not exist.
 */
public final class DataDir {

    /** Folder name inside the user's home directory. */
    private static final String DIR_NAME = ".intend";

    private DataDir() { /* utility class */ }

    /**
     * Returns the shared data directory ({@code ~/.intend/}), creating it if
     * necessary.
     */
    public static File root() {
        File dir = new File(System.getProperty("user.home"), DIR_NAME);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * Returns a {@link File} handle for the given file name inside the data
     * directory.
     *
     * @param fileName  simple file name (e.g. {@code "history.json"})
     * @return absolute {@code File} reference
     */
    public static File resolve(String fileName) {
        return new File(root(), fileName);
    }
}
