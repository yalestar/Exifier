import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class Exifier {

    private static String inputDir;
    private static String outputDir;
    private static int count;
    private static int noExifCount;
    private static int errors;


    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("USAGE: java -jar Exifier.jar input-dir output-dir");
            System.exit(-1);
        } else {
            inputDir = args[0];
            outputDir = args[1];
        }

        if (checkOrMakeDirs(inputDir) && checkOrMakeDirs(outputDir)) {

            Exifier exifier = new Exifier();
            File[] ls = exifier.getFileList(inputDir);
            exifier.processFiles(ls);
        }

        System.out.println("\n======================================");
        System.out.println("\tDONE");

        System.out.println("\t" + count + " images processed");
        if (noExifCount > 0) {
            System.out.println("\t" + noExifCount + " had no available EXIF data");
        }
        if (errors > 0) {
            System.out.println("\t" + errors + " had JPEG errors");
        }
        System.out.println("======================================");
    }


    private File[] getFileList(String dir) {

        FileFilter filter = new FileFilter() {

            public boolean accept(File pathname) {
                String l = pathname.getAbsolutePath();
                return (l.endsWith(".jpg") || l.endsWith(".JPG"));
            }
        };

        File p = new File(dir);
        return p.listFiles(filter);
    }


    private static boolean checkOrMakeDirs(String path) {
        File f = new File(path);

        if (f.exists() && f.isDirectory()) {
            return true;
        } else {
            return f.mkdirs();
        }
    }


    private void processFiles(File[] ls) {
        Metadata md = null;

        for (int i = 0; i < ls.length; i++) {
            File l = ls[i];
            try {
                md = JpegMetadataReader.readMetadata(l);
                readExif(l, md);
            } catch (JpegProcessingException e) {
                System.out.println("Unable to read JPEG headers for " + l.getName());
                errors++;
            }

        }
    }


    public void readExif(File l, Metadata md) {

        Tag dateTag = getDateTag(md);

        String content = null;
        String newName = null;
        String oldName = l.getAbsolutePath();
        if (dateTag != null) {
            try {
                content = dateTag.getDescription().trim();
                if (content.equals("")) {
                    System.out.println("No EXIF data found for " + oldName);
                    noExifCount++;
                    newName = oldName;
                } else {
                    newName = getNewName(dateTag);
                }

            } catch (MetadataException e) {
                e.printStackTrace();
            }

            String outFull = FilenameUtils.concat(outputDir, newName);

            try {
                copy(oldName, outFull);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private Tag getDateTag(Metadata md) {
        Iterator it = md.getDirectoryIterator();
        while (it.hasNext()) {
            Directory o = (Directory) it.next();
            Iterator tags = o.getTagIterator();
            while (tags.hasNext()) {
                Tag tag = (Tag) tags.next();
                try {
                    String tn = tag.getTagName();
                    String tv = tag.getDescription();
                    if (tn.equals("Date/Time Original")) {
                        return tag;
                    }
                } catch (MetadataException e) {
                }
            }
        }
        return null;
    }


    private LinkedHashMap getDateComponents(Tag tag) throws MetadataException {

        // 2006:07:09 13:33:16
        String orig = tag.getDescription();
        LinkedHashMap pod = new LinkedHashMap();

        String [] dtArray = orig.split(" ");
        String d = dtArray[0];
        String t = dtArray[1];
        String[] dateParts = d.split(":");
        String[] timeParts = t.split(":");
        pod.put("year", dateParts[0]);
        pod.put("month", dateParts[1]);
        pod.put("day", dateParts[2]);
        pod.put("hour", timeParts[0]);
        pod.put("min", timeParts[1]);
        pod.put("sec", timeParts[2]);

        return pod;

    }


    private String getNewName(Tag dateTag) {
        // 20060506_122332.jpg
        try {
            LinkedHashMap lhm = getDateComponents(dateTag);

            StringBuffer sb = new StringBuffer();
            sb.append(lhm.get("year"));
            sb.append(lhm.get("month"));
            sb.append(lhm.get("day"));

            sb.append("_");
            sb.append(lhm.get("hour"));
            sb.append(lhm.get("min"));
            sb.append(lhm.get("sec"));
            sb.append(".jpg");

            return sb.toString();
        } catch (MetadataException e) {
            e.printStackTrace();
        }

        return null;
    }


    private void copy(String src, String dst) throws IOException {

        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        System.out.println("Copied " + src + " to " + dst);
        count++;
        in.close();
        out.close();
    }
}
