import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

public class FilenameUtils {

    private static final char EXTENSION_SEPARATOR = '.';
    private static final char UNIX_SEPARATOR = '/';
    private static final char WINDOWS_SEPARATOR = '\\';
    private static final char SYSTEM_SEPARATOR = File.separatorChar;
    private static final char OTHER_SEPARATOR;


    static {
        if (SYSTEM_SEPARATOR == WINDOWS_SEPARATOR) {
            OTHER_SEPARATOR = UNIX_SEPARATOR;
        } else {
            OTHER_SEPARATOR = WINDOWS_SEPARATOR;
        }
    }


    /**
     * Instances should NOT be constructed in standard programming.
     */
    public FilenameUtils() {
        super();
    }


    private static boolean isSeparator(char ch) {
        return (ch == UNIX_SEPARATOR) || (ch == WINDOWS_SEPARATOR);
    }


    public static String normalize(String filename) {
        return doNormalize(filename, true);
    }


    public static String normalizeNoEndSeparator(String filename) {
        return doNormalize(filename, false);
    }


    private static String doNormalize(String filename, boolean keepSeparator) {
        if (filename == null) {
            return null;
        }
        int size = filename.length();
        if (size == 0) {
            return filename;
        }
        int prefix = getPrefixLength(filename);
        if (prefix < 0) {
            return null;
        }

        char[] array = new char[size + 2];  // +1 for possible extra slash, +2 for arraycopy
        filename.getChars(0, filename.length(), array, 0);

        // fix separators throughout
        for (int i = 0; i < array.length; i++) {
            if (array[i] == OTHER_SEPARATOR) {
                array[i] = SYSTEM_SEPARATOR;
            }
        }

        // add extra separator on the end to simplify code below
        boolean lastIsDirectory = true;
        if (array[size - 1] != SYSTEM_SEPARATOR) {
            array[size++] = SYSTEM_SEPARATOR;
            lastIsDirectory = false;
        }

        // adjoining slashes
        for (int i = prefix + 1; i < size; i++) {
            if (array[i] == SYSTEM_SEPARATOR && array[i - 1] == SYSTEM_SEPARATOR)
            {
                System.arraycopy(array, i, array, i - 1, size - i);
                size--;
                i--;
            }
        }

        // dot slash
        for (int i = prefix + 1; i < size; i++) {
            if (array[i] == SYSTEM_SEPARATOR && array[i - 1] == '.' &&
                    (i == prefix + 1 || array[i - 2] == SYSTEM_SEPARATOR)) {
                if (i == size - 1) {
                    lastIsDirectory = true;
                }
                System.arraycopy(array, i + 1, array, i - 1, size - i);
                size -= 2;
                i--;
            }
        }

        // double dot slash
        outer:
        for (int i = prefix + 2; i < size; i++) {
            if (array[i] == SYSTEM_SEPARATOR && array[i - 1] == '.' && array[i - 2] == '.' &&
                    (i == prefix + 2 || array[i - 3] == SYSTEM_SEPARATOR)) {
                if (i == prefix + 2) {
                    return null;
                }
                if (i == size - 1) {
                    lastIsDirectory = true;
                }
                int j;
                for (j = i - 4; j >= prefix; j--) {
                    if (array[j] == SYSTEM_SEPARATOR) {
                        // remove b/../ from a/b/../c
                        System.arraycopy(array, i + 1, array, j + 1, size - i);
                        size -= (i - j);
                        i = j + 1;
                        continue outer;
                    }
                }
                // remove a/../ from a/../c
                System.arraycopy(array, i + 1, array, prefix, size - i);
                size -= (i + 1 - prefix);
                i = prefix + 1;
            }
        }

        if (size <= 0) {  // should never be less than 0
            return "";
        }
        if (size <= prefix) {  // should never be less than prefix
            return new String(array, 0, size);
        }
        if (lastIsDirectory && keepSeparator) {
            return new String(array, 0, size);  // keep trailing separator
        }
        return new String(array, 0, size - 1);  // lose trailing separator
    }


    public static String concat(String basePath, String fullFilenameToAdd) {
        int prefix = getPrefixLength(fullFilenameToAdd);
        if (prefix < 0) {
            return null;
        }
        if (prefix > 0) {
            return normalize(fullFilenameToAdd);
        }
        if (basePath == null) {
            return null;
        }
        int len = basePath.length();
        if (len == 0) {
            return normalize(fullFilenameToAdd);
        }
        char ch = basePath.charAt(len - 1);
        if (isSeparator(ch)) {
            return normalize(basePath + fullFilenameToAdd);
        } else {
            return normalize(basePath + '/' + fullFilenameToAdd);
        }
    }


    public static String separatorsToUnix(String path) {
        if (path == null || path.indexOf(WINDOWS_SEPARATOR) == -1) {
            return path;
        }
        return path.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
    }


    public static String separatorsToWindows(String path) {
        if (path == null || path.indexOf(UNIX_SEPARATOR) == -1) {
            return path;
        }
        return path.replace(UNIX_SEPARATOR, WINDOWS_SEPARATOR);
    }


    public static String separatorsToSystem(String path) {
        if (path == null) {
            return null;
        }
        if (SYSTEM_SEPARATOR == WINDOWS_SEPARATOR) {
            return separatorsToWindows(path);
        } else {
            return separatorsToUnix(path);
        }
    }


    public static int getPrefixLength(String filename) {
        if (filename == null) {
            return -1;
        }
        int len = filename.length();
        if (len == 0) {
            return 0;
        }
        char ch0 = filename.charAt(0);
        if (ch0 == ':') {
            return -1;
        }
        if (len == 1) {
            if (ch0 == '~') {
                return 2;  // return a length greater than the input
            }
            return (isSeparator(ch0) ? 1 : 0);
        } else {
            if (ch0 == '~') {
                int posUnix = filename.indexOf(UNIX_SEPARATOR, 1);
                int posWin = filename.indexOf(WINDOWS_SEPARATOR, 1);
                if (posUnix == -1 && posWin == -1) {
                    return len + 1;  // return a length greater than the input
                }
                posUnix = (posUnix == -1 ? posWin : posUnix);
                posWin = (posWin == -1 ? posUnix : posWin);
                return Math.min(posUnix, posWin) + 1;
            }
            char ch1 = filename.charAt(1);
            if (ch1 == ':') {
                ch0 = Character.toUpperCase(ch0);
                if (ch0 >= 'A' && ch0 <= 'Z') {
                    if (len == 2 || isSeparator(filename.charAt(2)) == false) {
                        return 2;
                    }
                    return 3;
                }
                return -1;

            } else if (isSeparator(ch0) && isSeparator(ch1)) {
                int posUnix = filename.indexOf(UNIX_SEPARATOR, 2);
                int posWin = filename.indexOf(WINDOWS_SEPARATOR, 2);
                if ((posUnix == -1 && posWin == -1) || posUnix == 2 || posWin == 2)
                {
                    return -1;
                }
                posUnix = (posUnix == -1 ? posWin : posUnix);
                posWin = (posWin == -1 ? posUnix : posWin);
                return Math.min(posUnix, posWin) + 1;
            } else {
                return (isSeparator(ch0) ? 1 : 0);
            }
        }
    }


    /**
     * Returns the index of the last directory separator character.
     * <p/>
     * This method will handle a file in either Unix or Windows format.
     * The position of the last forward or backslash is returned.
     * <p/>
     * The output will be the same irrespective of the machine that the code is running on.
     *
     * @param filename the filename to find the last path separator in, null returns -1
     * @return the index of the last separator character, or -1 if there
     *         is no such character
     */
    public static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        }
        int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }


    /**
     * Returns the index of the last extension separator character, which is a dot.
     * <p/>
     * This method also checks that there is no directory separator after the last dot.
     * To do this it uses {@link #indexOfLastSeparator(String)} which will
     * handle a file in either Unix or Windows format.
     * <p/>
     * The output will be the same irrespective of the machine that the code is running on.
     *
     * @param filename the filename to find the last path separator in, null returns -1
     * @return the index of the last separator character, or -1 if there
     *         is no such character
     */
    public static int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        }
        int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
        int lastSeparator = indexOfLastSeparator(filename);
        return (lastSeparator > extensionPos ? -1 : extensionPos);
    }


    /**
     * Gets the prefix from a full filename, such as <code>C:/</code>
     * or <code>~/</code>.
     * <p/>
     * This method will handle a file in either Unix or Windows format.
     * The prefix includes the first slash in the full filename where applicable.
     * <pre>
     * Windows:
     * a\b\c.txt           --> ""          --> relative
     * \a\b\c.txt          --> "\"         --> current drive absolute
     * C:a\b\c.txt         --> "C:"        --> drive relative
     * C:\a\b\c.txt        --> "C:\"       --> absolute
     * \\server\a\b\c.txt  --> "\\server\" --> UNC
     * <p/>
     * Unix:
     * a/b/c.txt           --> ""          --> relative
     * /a/b/c.txt          --> "/"         --> absolute
     * ~/a/b/c.txt         --> "~/"        --> current user
     * ~                   --> "~/"        --> current user (slash added)
     * ~user/a/b/c.txt     --> "~user/"    --> named user
     * ~user               --> "~user/"    --> named user (slash added)
     * </pre>
     * <p/>
     * The output will be the same irrespective of the machine that the code is running on.
     * ie. both Unix and Windows prefixes are matched regardless.
     *
     * @param filename the filename to query, null returns null
     * @return the prefix of the file, null if invalid
     */
    public static String getPrefix(String filename) {
        if (filename == null) {
            return null;
        }
        int len = getPrefixLength(filename);
        if (len < 0) {
            return null;
        }
        if (len > filename.length()) {
            return filename + UNIX_SEPARATOR;  // we know this only happens for unix
        }
        return filename.substring(0, len);
    }


    /**
     * Gets the path from a full filename, which excludes the prefix.
     * <p/>
     * This method will handle a file in either Unix or Windows format.
     * The method is entirely text based, and returns the text before and
     * including the last forward or backslash.
     * <pre>
     * C:\a\b\c.txt --> a\b\
     * ~/a/b/c.txt  --> a/b/
     * a.txt        --> ""
     * a/b/c        --> a/b/
     * a/b/c/       --> a/b/c/
     * </pre>
     * <p/>
     * The output will be the same irrespective of the machine that the code is running on.
     * <p/>
     * This method drops the prefix from the result.
     * See {@link #getFullPath(String)} for the method that retains the prefix.
     *
     * @param filename the filename to query, null returns null
     * @return the path of the file, an empty string if none exists, null if invalid
     */
    public static String getPath(String filename) {
        return doGetPath(filename, 1);
    }


    /**
     * Gets the path from a full filename, which excludes the prefix, and
     * also excluding the final directory separator.
     * <p/>
     * This method will handle a file in either Unix or Windows format.
     * The method is entirely text based, and returns the text before the
     * last forward or backslash.
     * <pre>
     * C:\a\b\c.txt --> a\b
     * ~/a/b/c.txt  --> a/b
     * a.txt        --> ""
     * a/b/c        --> a/b
     * a/b/c/       --> a/b/c
     * </pre>
     * <p/>
     * The output will be the same irrespective of the machine that the code is running on.
     * <p/>
     * This method drops the prefix from the result.
     * See {@link #getFullPathNoEndSeparator(String)} for the method that retains the prefix.
     *
     * @param filename the filename to query, null returns null
     * @return the path of the file, an empty string if none exists, null if invalid
     */
    public static String getPathNoEndSeparator(String filename) {
        return doGetPath(filename, 0);
    }


    /**
     * Does the work of getting the path.
     *
     * @param filename     the filename
     * @param separatorAdd 0 to omit the end separator, 1 to return it
     * @return the path
     */
    private static String doGetPath(String filename, int separatorAdd) {
        if (filename == null) {
            return null;
        }
        int prefix = getPrefixLength(filename);
        if (prefix < 0) {
            return null;
        }
        int index = indexOfLastSeparator(filename);
        if (prefix >= filename.length() || index < 0) {
            return "";
        }
        return filename.substring(prefix, index + separatorAdd);
    }


    /**
     * Gets the full path from a full filename, which is the prefix + path.
     * <p/>
     * This method will handle a file in either Unix or Windows format.
     * The method is entirely text based, and returns the text before and
     * including the last forward or backslash.
     * <pre>
     * C:\a\b\c.txt --> C:\a\b\
     * ~/a/b/c.txt  --> ~/a/b/
     * a.txt        --> ""
     * a/b/c        --> a/b/
     * a/b/c/       --> a/b/c/
     * C:           --> C:
     * C:\          --> C:\
     * ~            --> ~/
     * ~/           --> ~/
     * ~user        --> ~user/
     * ~user/       --> ~user/
     * </pre>
     * <p/>
     * The output will be the same irrespective of the machine that the code is running on.
     *
     * @param filename the filename to query, null returns null
     * @return the path of the file, an empty string if none exists, null if invalid
     */
    public static String getFullPath(String filename) {
        return doGetFullPath(filename, true);
    }


    /**
     * Gets the full path from a full filename, which is the prefix + path,
     * and also excluding the final directory separator.
     * <p/>
     * This method will handle a file in either Unix or Windows format.
     * The method is entirely text based, and returns the text before the
     * last forward or backslash.
     * <pre>
     * C:\a\b\c.txt --> C:\a\b
     * ~/a/b/c.txt  --> ~/a/b
     * a.txt        --> ""
     * a/b/c        --> a/b
     * a/b/c/       --> a/b/c
     * C:           --> C:
     * C:\          --> C:\
     * ~            --> ~
     * ~/           --> ~
     * ~user        --> ~user
     * ~user/       --> ~user
     * </pre>
     * <p/>
     * The output will be the same irrespective of the machine that the code is running on.
     *
     * @param filename the filename to query, null returns null
     * @return the path of the file, an empty string if none exists, null if invalid
     */
    public static String getFullPathNoEndSeparator(String filename) {
        return doGetFullPath(filename, false);
    }


    /**
     * Does the work of getting the path.
     *
     * @param filename         the filename
     * @param includeSeparator true to include the end separator
     * @return the path
     */
    private static String doGetFullPath(String filename, boolean includeSeparator) {
        if (filename == null) {
            return null;
        }
        int prefix = getPrefixLength(filename);
        if (prefix < 0) {
            return null;
        }
        if (prefix >= filename.length()) {
            if (includeSeparator) {
                return getPrefix(filename);  // add end slash if necessary
            } else {
                return filename;
            }
        }
        int index = indexOfLastSeparator(filename);
        if (index < 0) {
            return filename.substring(0, prefix);
        }
        int end = index + (includeSeparator ? 1 : 0);
        return filename.substring(0, end);
    }


    /**
     * Gets the name minus the path from a full filename.
     * <p/>
     * This method will handle a file in either Unix or Windows format.
     * The text after the last forward or backslash is returned.
     * <pre>
     * a/b/c.txt --> c.txt
     * a.txt     --> a.txt
     * a/b/c     --> c
     * a/b/c/    --> ""
     * </pre>
     * <p/>
     * The output will be the same irrespective of the machine that the code is running on.
     *
     * @param filename the filename to query, null returns null
     * @return the name of the file without the path, or an empty string if none exists
     */
    public static String getName(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfLastSeparator(filename);
        return filename.substring(index + 1);
    }


    /**
     * Gets the base name, minus the full path and extension, from a full filename.
     * <p/>
     * This method will handle a file in either Unix or Windows format.
     * The text after the last forward or backslash and before the last dot is returned.
     * <pre>
     * a/b/c.txt --> c
     * a.txt     --> a
     * a/b/c     --> c
     * a/b/c/    --> ""
     * </pre>
     * <p/>
     * The output will be the same irrespective of the machine that the code is running on.
     *
     * @param filename the filename to query, null returns null
     * @return the name of the file without the path, or an empty string if none exists
     */
    public static String getBaseName(String filename) {
        return removeExtension(getName(filename));
    }


    /**
     * Gets the extension of a filename.
     * <p/>
     * This method returns the textual part of the filename after the last dot.
     * There must be no directory separator after the dot.
     * <pre>
     * foo.txt      --> "txt"
     * a/b/c.jpg    --> "jpg"
     * a/b.txt/c    --> ""
     * a/b/c        --> ""
     * </pre>
     * <p/>
     * The output will be the same irrespective of the machine that the code is running on.
     *
     * @param filename the filename to retrieve the extension of.
     * @return the extension of the file or an empty string if none exists.
     */
    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfExtension(filename);
        if (index == -1) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }
    

    /**
     * Removes the extension from a filename.
     * <p/>
     * This method returns the textual part of the filename before the last dot.
     * There must be no directory separator after the dot.
     * <pre>
     * foo.txt    --> foo
     * a\b\c.jpg  --> a\b\c
     * a\b\c      --> a\b\c
     * a.b\c      --> a.b\c
     * </pre>
     * <p/>
     * The output will be the same irrespective of the machine that the code is running on.
     *
     * @param filename the filename to query, null returns null
     * @return the filename minus the extension
     */
    public static String removeExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfExtension(filename);
        if (index == -1) {
            return filename;
        } else {
            return filename.substring(0, index);
        }
    }



    /**
     * Checks whether the extension of the filename is that specified.
     * <p/>
     * This method obtains the extension as the textual part of the filename
     * after the last dot. There must be no directory separator after the dot.
     * The extension check is case-sensitive on all platforms.
     *
     * @param filename  the filename to query, null returns false
     * @param extension the extension to check for, null or empty checks for no extension
     * @return true if the filename has the specified extension
     */
    public static boolean isExtension(String filename, String extension) {
        if (filename == null) {
            return false;
        }
        if (extension == null || extension.length() == 0) {
            return (indexOfExtension(filename) == -1);
        }
        String fileExt = getExtension(filename);
        return fileExt.equals(extension);
    }


    /**
     * Checks whether the extension of the filename is one of those specified.
     * <p/>
     * This method obtains the extension as the textual part of the filename
     * after the last dot. There must be no directory separator after the dot.
     * The extension check is case-sensitive on all platforms.
     *
     * @param filename   the filename to query, null returns false
     * @param extensions the extensions to check for, null checks for no extension
     * @return true if the filename is one of the extensions
     */
    public static boolean isExtension(String filename, String[] extensions) {
        if (filename == null) {
            return false;
        }
        if (extensions == null || extensions.length == 0) {
            return (indexOfExtension(filename) == -1);
        }
        String fileExt = getExtension(filename);
        for (int i = 0; i < extensions.length; i++) {
            if (fileExt.equals(extensions[i])) {
                return true;
            }
        }
        return false;
    }


    /**
     * Checks whether the extension of the filename is one of those specified.
     * <p/>
     * This method obtains the extension as the textual part of the filename
     * after the last dot. There must be no directory separator after the dot.
     * The extension check is case-sensitive on all platforms.
     *
     * @param filename   the filename to query, null returns false
     * @param extensions the extensions to check for, null checks for no extension
     * @return true if the filename is one of the extensions
     */
    public static boolean isExtension(String filename, Collection extensions) {
        if (filename == null) {
            return false;
        }
        if (extensions == null || extensions.isEmpty()) {
            return (indexOfExtension(filename) == -1);
        }
        String fileExt = getExtension(filename);
        for (Iterator it = extensions.iterator(); it.hasNext();) {
            if (fileExt.equals(it.next())) {
                return true;
            }
        }
        return false;
    }


}
