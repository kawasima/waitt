package net.unit8.waitt.feature.devtools;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class DirectorySnapshotTest {

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Test
    public void noChangeDetectedForIdenticalSnapshots() throws Exception {
        File dir = tempDir.newFolder("classes");
        writeFile(new File(dir, "Hello.class"), "bytecode1");

        DirectorySnapshot s1 = new DirectorySnapshot(dir);
        DirectorySnapshot s2 = new DirectorySnapshot(dir);

        assertFalse(s2.hasChangedFrom(s1));
    }

    @Test
    public void changeDetectedWhenFileModified() throws Exception {
        File dir = tempDir.newFolder("classes");
        File file = new File(dir, "Hello.class");
        writeFile(file, "bytecode1");

        DirectorySnapshot before = new DirectorySnapshot(dir);

        // Different content length triggers size change detection
        writeFile(file, "bytecode2-changed");

        DirectorySnapshot after = new DirectorySnapshot(dir);

        assertTrue(after.hasChangedFrom(before));
    }

    @Test
    public void changeDetectedWhenFileAdded() throws Exception {
        File dir = tempDir.newFolder("classes");
        writeFile(new File(dir, "Hello.class"), "bytecode1");

        DirectorySnapshot before = new DirectorySnapshot(dir);

        writeFile(new File(dir, "World.class"), "bytecode2");

        DirectorySnapshot after = new DirectorySnapshot(dir);

        assertTrue(after.hasChangedFrom(before));
    }

    @Test
    public void changeDetectedWhenFileDeleted() throws Exception {
        File dir = tempDir.newFolder("classes");
        File file = new File(dir, "Hello.class");
        writeFile(file, "bytecode1");

        DirectorySnapshot before = new DirectorySnapshot(dir);

        Files.delete(file.toPath());

        DirectorySnapshot after = new DirectorySnapshot(dir);

        assertTrue(after.hasChangedFrom(before));
    }

    @Test
    public void changeDetectedInSubdirectory() throws Exception {
        File dir = tempDir.newFolder("classes");
        File subDir = new File(dir, "com/example");
        subDir.mkdirs();
        writeFile(new File(subDir, "App.class"), "bytecode1");

        DirectorySnapshot before = new DirectorySnapshot(dir);

        // Different content length triggers size change detection
        writeFile(new File(subDir, "App.class"), "bytecode2-updated");

        DirectorySnapshot after = new DirectorySnapshot(dir);

        assertTrue(after.hasChangedFrom(before));
    }

    @Test
    public void emptyDirectoryNoChange() throws Exception {
        File dir = tempDir.newFolder("classes");

        DirectorySnapshot s1 = new DirectorySnapshot(dir);
        DirectorySnapshot s2 = new DirectorySnapshot(dir);

        assertFalse(s2.hasChangedFrom(s1));
    }

    @Test
    public void nonExistentDirectoryNoChange() {
        File dir = new File(tempDir.getRoot(), "nonexistent");

        DirectorySnapshot s1 = new DirectorySnapshot(dir);
        DirectorySnapshot s2 = new DirectorySnapshot(dir);

        assertFalse(s2.hasChangedFrom(s1));
    }

    private void writeFile(File file, String content) throws Exception {
        file.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }
}
