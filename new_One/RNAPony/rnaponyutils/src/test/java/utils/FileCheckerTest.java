package utils;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class FileCheckerTest {

    @Test
    void isContentEqual() {
        ClassLoader classLoader = FileCheckerTest.class.getClassLoader();
        Path javaFilePath = Path.of(Objects.requireNonNull(
                classLoader.getResource(".")).toString().substring(6)
            ),
            cppFilePath = Path.of(Objects.requireNonNull(
                    classLoader.getResource(".")).toString().substring(6)
            );
        assertTrue(new FileChecker().isContentEqual(
                Path.of(cppFilePath.toString(), "cppur4_L1_0.txt").toString(),
                Path.of(javaFilePath.toString(), "javaur4_L1_0.txt").toString()));
        assertTrue(new FileChecker().isContentEqual(
                Path.of(cppFilePath.toString(), "cppur4_L1_1.txt").toString(),
                Path.of(javaFilePath.toString(), "javaur4_L1_1.txt").toString()));
        assertFalse(new FileChecker().isContentEqual(
                Path.of(cppFilePath.toString(), "cppur4_L1_1.txt").toString(),
                Path.of(javaFilePath.toString(), "wrong1javaur4_L1_1.txt").toString()));
        assertFalse(new FileChecker().isContentEqual(
                Path.of(cppFilePath.toString(), "cppur4_L1_1.txt").toString(),
                Path.of(javaFilePath.toString(), "wrong2javaur4_L1_1.txt").toString()));
    }
}