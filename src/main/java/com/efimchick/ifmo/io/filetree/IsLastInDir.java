package com.efimchick.ifmo.io.filetree;

import java.nio.file.Path;

public class IsLastInDir {
    private Path path;
    private boolean isLastInDir;

    public IsLastInDir(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public boolean isLastInDir() {
        return isLastInDir;
    }

    public void setLastInDir(boolean lastInDir) {
        isLastInDir = lastInDir;
    }
}
