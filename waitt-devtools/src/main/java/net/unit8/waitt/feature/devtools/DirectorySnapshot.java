package net.unit8.waitt.feature.devtools;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Captures the state of files in a directory tree for change detection.
 *
 * @author kawasima
 */
class DirectorySnapshot {
    private final Map<String, FileInfo> files;

    DirectorySnapshot(File directory) {
        files = new HashMap<String, FileInfo>();
        if (directory.exists()) {
            scan(directory, directory);
        }
    }

    private void scan(File root, File dir) {
        File[] entries = dir.listFiles();
        if (entries == null) return;
        for (File file : entries) {
            if (file.isDirectory()) {
                scan(root, file);
            } else {
                String relativePath = root.toURI().relativize(file.toURI()).getPath();
                files.put(relativePath, new FileInfo(file.lastModified(), file.length()));
            }
        }
    }

    boolean hasChangedFrom(DirectorySnapshot previous) {
        if (previous == null) return !files.isEmpty();
        return !files.equals(previous.files);
    }

    private static class FileInfo {
        final long lastModified;
        final long size;

        FileInfo(long lastModified, long size) {
            this.lastModified = lastModified;
            this.size = size;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FileInfo)) return false;
            FileInfo that = (FileInfo) o;
            return lastModified == that.lastModified && size == that.size;
        }

        @Override
        public int hashCode() {
            return (int) (31 * lastModified + size);
        }
    }
}
