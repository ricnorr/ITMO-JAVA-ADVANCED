package info.kgeorgiy.ja.korobejnikov.walk;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;


public class Walk {
    private static void walk(Path inputFilePath, Path outputFilePath) throws WalkException {
        if (!Files.exists(inputFilePath)) {
            throw new WalkException(inputFilePath.toString() + " doesn't exist");
        }
        try (BufferedReader reader = Files.newBufferedReader(inputFilePath)) {
            if (outputFilePath.getParent() != null && Files.notExists(outputFilePath.getParent())) {
                try {
                    Files.createDirectories(outputFilePath.getParent());
                } catch (IOException exception) {
                    throw new WalkException("Can't create directory for output file: " + exception.getMessage(), exception);
                }
            }
            try (BufferedWriter writer = Files.newBufferedWriter(outputFilePath)) {
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        processFile(line, writer);
                    }
                } catch (IOException exception) {
                    throw new WalkException("Reading input file is interrupted:  " + exception.getMessage(), exception);
                }
            } catch (IOException exception) {
                throw new WalkException("Can't open output file: " + exception.getMessage(), exception);
            }
        } catch (FileNotFoundException exception) {
            throw new WalkException("Input file not found: " + exception.getMessage(), exception);
        } catch (IOException exception) {
            throw new WalkException("Can't open input file: ", exception);
        }
    }

    private static void processFile(String stringPath, Writer writer) throws WalkException {
        try {
            writeResult(writer, PjwHash.concatHashPath(PjwHash.fileToPjw(Path.of(stringPath)), stringPath));
        } catch (InvalidPathException exception) {
            writeResult(writer, PjwHash.concatHashPath(0, stringPath));
        }
    }

    private static void writeResult(Writer writer, String string) throws WalkException {
        try {
            writer.write(string);
        } catch (IOException exception) {
            throw new WalkException("Output interrupted: " + exception.getMessage(), exception);
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
                throw new WalkException("Invalid input file path: " + exception.getMessage(), exception);
            }
            try {
                outputFilePath = Path.of(args[1]);
            } catch (InvalidPathException exception) {
                throw new WalkException("Invalid output file path: " + exception.getMessage(), exception);
            }
            walk(inputFilePath, outputFilePath);
        } catch (WalkException exception) {
            System.err.println(exception.getMessage());
            System.out.println(exception.getMessage());
        }
    }
}
