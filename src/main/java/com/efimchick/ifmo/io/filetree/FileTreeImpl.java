package com.efimchick.ifmo.io.filetree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;


public class FileTreeImpl implements FileTree {
    private StringBuilder result = new StringBuilder();
    private Path parent;
    private Set<Integer> needLine = new HashSet<>();
    private int levelOfFile;


    @Override
    public Optional<String> tree(Path path) {
        if (path == null || Files.notExists(path)) {
            return Optional.empty();
        }
        parent = path;
        result.append(path.getFileName().toString());
        try {
            result.append(getSize(path));
            if (Files.isDirectory(path)) {
                readDir(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.of(result.toString());
    }
    private StringBuilder getSize(Path path) throws IOException {
        long size = Files.walk(path)
                .map(f -> f.toFile())
                .filter(f -> f.isFile())
                .mapToLong(f -> f.length()).sum();
        return new StringBuilder(" ").append(size).append(" bytes\n");
    }

    private void readDir (Path path) throws IOException {
        levelOfFile++;
        List<Path> listOfFiles = Files.list(path)
                .sorted((p1, p2) -> p1.getFileName().toString()
                        .compareToIgnoreCase(p2.getFileName().toString()))
                .sorted((f1, f2) -> { int file1 = Files.isDirectory(f1) ? 1 : 0;
                    int file2 = Files.isDirectory(f2) ? 1 : 0;
                    return file2 - file1; })
                .collect(Collectors.toList());
        List<IsLastInDir> isLast = getIsLastInDir(listOfFiles);
        for (IsLastInDir file : isLast) {
            Path filePath = file.getPath();
            if (Files.isDirectory(filePath)) {
                result.append(createDirLine(file));
                readDir(filePath);
            } else {
                result.append(createFileLine(file));
            }
        }
    }

    private StringBuilder createDirLine(IsLastInDir file) {
        StringBuilder fileLine = new StringBuilder();
        createIndent(fileLine);
        if (file.isLastInDir()) {
            needLine.remove(fileLine.length());
            fileLine.append("└─ ");
        } else {
            needLine.add(fileLine.length());
            fileLine.append("├─ ");
        }
        addNameAndSize(fileLine, file.getPath());
        markLine(fileLine);
        return fileLine;
    }

    private void createIndent(StringBuilder fileLine) {
        for (int i = 1; i < levelOfFile; i++) {
            fileLine.append("   ");
        }
    }

    private void addNameAndSize(StringBuilder fileLine, Path filePath) {
        fileLine.append(filePath.getFileName());
        try {
            fileLine.append(getSize(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void markLine(StringBuilder fileLine) {
        for (Integer index : needLine) {
            if (fileLine.charAt(index) == ' ') {
                fileLine.setCharAt(index, '│');
            }
        }
    }

    private List<IsLastInDir> getIsLastInDir(List<Path> list) {
        List<IsLastInDir> isLast = new ArrayList<>();
        for (Path f: list) {
            isLast.add(new IsLastInDir(f));
        }
        isLast.get(isLast.size() - 1).setLastInDir(true);
        return isLast;
    }

    private StringBuilder createFileLine(IsLastInDir file) {
        StringBuilder fileLine = new StringBuilder();
        createIndent(fileLine);
        if (file.isLastInDir()) {
            needLine.remove(fileLine.length());
            for (int i = fileLine.length() - 3; i >= 0 ; i-=3) {
                if (!needLine.contains(i)) {
                    levelOfFile--;
                } else {
                    break;
                }
            }
            fileLine.append("└─ ");
            levelOfFile--;
        } else {
            needLine.add(fileLine.length());
            fileLine.append("├─ ");
        }
        addNameAndSize(fileLine, file.getPath());
        markLine(fileLine);
        return fileLine;
    }
}
