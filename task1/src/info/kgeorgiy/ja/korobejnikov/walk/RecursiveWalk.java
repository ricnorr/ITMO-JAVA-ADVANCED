package info.kgeorgiy.ja.korobejnikov.walk;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;


public class RecursiveWalk {

    private static class MyFileVisitor extends SimpleFileVisitor<Path> {

        private final Writer writer;

        public MyFileVisitor(Writer writer) {
            this.writer = writer;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            try {
                writer.write(PjwHash.concatHashPath(PjwHash.fileToPjw(file), file.toString()));
            } catch (IOException exception) {
                throw new WalkException("Output to " + exception.getMessage() + " interrupted by IO problem", exception);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) {
                try {
                    writer.write(PjwHash.concatHashPath(0, file.toString()));
                } catch (IOException exception) {
                    throw new WalkException("Output to " + exception.getMessage() + " interrupted by IO problem", e);
                }
            return FileVisitResult.CONTINUE;
        }
    }

    private static void walk(Path inputFilePath, Path outputFilePath) throws WalkException {
        if (!Files.exists(inputFilePath)) {
            throw new WalkException(inputFilePath.toString() + " doesn't exist");
        }
        if (outputFilePath.getParent() != null && Files.notExists(outputFilePath.getParent())) {
            try {
                Files.createDirectories(outputFilePath.getParent());
            } catch (IOException exception) {
                throw new WalkException("Creating parent directory for " + exception.getMessage() + " interrupted by IO problems", exception);
            }
        }
        try (BufferedReader reader = Files.newBufferedReader(inputFilePath)) {
            try (BufferedWriter writer = Files.newBufferedWriter(outputFilePath)) {
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        try {
                            processFile(line, writer);
                        } catch (IOException exception) {
                            throw new WalkException("Output to result file interrupted " + exception.getMessage(), exception);
                        }
                    }
                } catch (IOException exception) {
                    throw new WalkException("Input from source file interrupted " + exception.getMessage(), exception);
                }
            } catch (IOException exception) {
                throw new WalkException("Output to result file interrupted " + exception.getMessage(), exception);
            }
        } catch (FileNotFoundException exception) {
            throw new WalkException(exception.getMessage(), exception);
        } catch (IOException exception) {
            throw new WalkException("Can't open input file: ", exception);
        }
    }

    public static void processFile(String stringPath, Writer writer) throws IOException {
        try {
            Path path = Path.of(stringPath);
            Files.walkFileTree(path, new MyFileVisitor(writer));
        } catch (InvalidPathException | FileNotFoundException exception) {
            writer.write(PjwHash.concatHashPath(0, stringPath));
        }
    }

    public static void main(String[] args) {
        try {
            if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
                throw new WalkException("Invalid number of arguments or arguments are null. Should be 2 arguments: input output");
            }
            Path inputFilePath;
            Path outputFilePath;
            try {
                inputFilePath = Path.of(args[0]);
            } catch (InvalidPathException exception) {
                throw new WalkException("Invalid input file path " + exception.getMessage(), exception);
            }
            try {
                outputFilePath = Path.of(args[1]);
            } catch (InvalidPathException exception) {
                throw new WalkException("Invalid output file path " + exception.getMessage(), exception);
            }
            walk(inputFilePath, outputFilePath);
        } catch (WalkException exception) {
            System.err.println(exception.getMessage());
            System.out.println(exception.getMessage());
        }
    }
}

