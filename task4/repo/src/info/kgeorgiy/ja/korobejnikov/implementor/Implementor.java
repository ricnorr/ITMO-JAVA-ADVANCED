package info.kgeorgiy.ja.korobejnikov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * Implements java interfaces and packs in {@code .jar format}
 */
public class Implementor implements JarImpler {

    /**
     * Creates a new {@code Implememntor}
     */
    public Implementor() {

    }

    /**
     * Compile implementation of {@code token} located in {@code path}.
     * Implementation's name must be equal to implementation made by {@link Implementor#implement(Class, Path)}
     *
     * @param token a type token
     * @param path  a root directory
     * @throws ImplerException impossible to get system compiler or error occurred while compiling
     */

    private void compile(final Class<?> token, final Path path) throws ImplerException {
        final String file = getPathToImpl(token, path, "Impl.java", File.separator).toString();
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new NullPointerException("Can't get compiler");
        }
        final List<String> args = new ArrayList<>();
        args.add(file);
        args.add("-cp");
        args.add(path + File.pathSeparator + getClassPath(token));

        final int exitCode = compiler.run(null, null, null, args.toArray(String[]::new));
        if (exitCode != 0) {
            throw new ImplerException("Can't compile the file");
        }
    }

    /**
     * Validates if {@code token} is implementable.
     * {@code token} is implementable if not a public interface.
     *
     * @param token a type token to validate
     * @throws ImplerException if token is not implementable
     */

    private void validateToken(final Class<?> token) throws ImplerException {
        if (token == null || !token.isInterface() || Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Token " + token + " is not a public interface");
        }
    }


    /**
     * Implements public interface and write result in file.
     * Implementation's name is target's name + suffix "Impl.java".
     * Implementation of each method returns default type value.
     *
     * @param token a type token
     * @param root  a root directory.
     * @throws ImplerException if implementation is failed. To get cause, use {@link ImplerException#getCause()}
     */
    @Override
    public void implement(final Class<?> token, final Path root) throws ImplerException {
        validateToken(token);
        if (root == null) {
            throw new ImplerException("Path can't be null");
        }
        final Path fullPath = getPathToImpl(token, root, "Impl.java", File.separator);
        createParentDirectories(fullPath);
        try (final BufferedWriter writer = Files.newBufferedWriter(fullPath)) {
            writer.write(
                    String.join(
                            System.lineSeparator(),
                            generatePackage(token),
                            generateClassHeader(token),
                            generateMethodsBlocks(token.getMethods()),
                            "}"));
        } catch (final IOException exception) {
            throw new ImplerException("Can't open " + fullPath + " for writing: " + exception.getMessage(), exception);
        }
    }


    /**
     * Returns default value for {@code token}.
     * If {@code token} is not primitive, returns {@code null}.
     * If {@code token} is {@code boolean}, returns {@code false}.
     * If (@code token) is {@code void}, returns nothing.
     * Else returns {@code 0}
     *
     * @param token a type token
     * @return a default value for type token
     */
    private String getDefaultValue(final Class<?> token) {
        if (!token.isPrimitive()) {
            return "null";
        }
        if (token == Boolean.TYPE) {
            return "false";
        }
        if (token == Void.TYPE) {
            return "";
        }
        return "0";
    }

    /**
     * Creates parent directories for {@code path}.
     *
     * @param path target path
     * @throws ImplerException if unable to create directories
     * @return a path of created parent directory
     */
    private Path createParentDirectories(final Path path) throws ImplerException {
        final Path parentPath = path.getParent();
        if (parentPath == null) {
            return Path.of(".");
        }
        try {
            return Files.createDirectories(parentPath);
        } catch (final IOException exception) {
            throw new ImplerException("Can't create parent directory for" + path);
        }
    }

    /**
     * Checks if {@code method} is implementable.
     * {@code method} is implementable if not static, not default, not private
     *
     * @param method a target method
     * @return true if method is implementable, otherwise false
     */
    private boolean isImplementable(final Method method) {
        return !Modifier.isStatic(method.getModifiers()) && !method.isDefault() && !Modifier.isPrivate(method.getModifiers());
    }

    /**
     * Generates package statement for implementation of {@code token}.
     * If {@code token} hasn't package - returns empty string
     *
     * @param token a type token
     * @return package statement for implementation
     */
    private String generatePackage(final Class<?> token) {
        if (token.getPackage() != null && !token.getPackageName().equals("")) {
            return String.join(" ", "package", token.getPackageName() + ";");
        }
        return "";
    }

    /**
     * Generates class definition for implementation of {@code token}.
     *
     * @param token a type token
     * @return class header for type token
     */
    private String generateClassHeader(final Class<?> token) {
        return String.join(" ", "class", token.getSimpleName() + "Impl ", "implements", token.getCanonicalName(), "{");
    }

    /**
     * Generates code for implementations of {@code methods}.
     * Invoke {@link Implementor#generateMethodBlock(Method)} for each {@code method} in {@code methods}
     *
     * @param methods target methods
     * @return code of implementation
     */
    private String generateMethodsBlocks(final Method[] methods) {
        return Arrays.stream(methods)
                .map(this::generateMethodBlock)
                .collect(Collectors.joining(System.lineSeparator(), System.lineSeparator(), ""));
    }

    /**
     * Generates code for implementation of {@code method}.
     *
     * @param method a target method
     * @return code of implementation
     */
    private String generateMethodBlock(final Method method) {
        if (!isImplementable(method)) {
            return "";
        }
        return String.join(System.lineSeparator(),
                String.join("", "\t", String.join(
                        " ",
                        "public",
                        method.getReturnType().getCanonicalName(),
                        method.getName(),
                        generateMethodParameters(method),
                        "{")),
                String.join("", "\t\t", generateMethodCode(method)),
                String.join("", "\t", "}")
        );
    }

    /**
     * Generates signature for implementation of {@code method}.
     *
     * @param method a target method
     * @return method signature for implementation
     */
    private String generateMethodParameters(final Method method) {
        return Arrays.stream(method.getParameters())
                .map(parameter -> parameter.getType().getCanonicalName() + " " + parameter.getName())
                .collect(Collectors.joining("," + " ", "(", ")"));
    }

    /**
     * Generates code for implementation of {@code method}.
     * Implementation returns a default value, defined by {@link Implementor#getDefaultValue(Class)}
     *
     * @param method a target method
     * @return a body for method implementation
     */
    private String generateMethodCode(final Method method) {
        return String.join("", "return", " ", getDefaultValue(method.getReturnType()), ";");
    }

    /**
     * Returns classpath of {@code token}
     *
     * @param token a type token
     * @return class-path for type token
     * @throws ImplerException if impossible to get classpath
     */
    private String getClassPath(final Class<?> token) throws ImplerException {
        try {
            return Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (final URISyntaxException e) {
            throw new ImplerException("Can't get class-path of " + token);
        }
    }

    /**
     * Implements {@code token} and packs implementation in {@code jar-file}.
     * Resulting jar-file has name {@code jarFile}.
     *
     * @param token   a type token to be implemented
     * @param jarFile a target {@code jar} file.
     * @throws ImplerException if implementation or packing is failed by some reasons
     */
    @Override
    public void implementJar(final Class<?> token, final Path jarFile) throws ImplerException {
        validateToken(token);
        if (jarFile == null) {
            throw new ImplerException("Path can't be null");
        }
        final Path parentDirectory = createParentDirectories(jarFile);
        final Implementor implementor = new Implementor();
        final Path tempDirectory = createTempDirectories(parentDirectory);
        implementor.implement(token, tempDirectory);
        compile(token, tempDirectory);
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (final JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            jarOutputStream.putNextEntry(new ZipEntry(getPathToImpl(token, Path.of(""), "Impl.class", "/").toString()));
            Files.copy(getPathToImpl(token, tempDirectory, "Impl.class", File.separator), jarOutputStream);
        } catch (final IOException e) {
            throw new ImplerException("Error while writing in jar: " + e.getMessage());
        } finally {
            try {
                Files.walkFileTree(tempDirectory, new CleanDirectoryVisitor());
            } catch (final IOException exception) {
                System.err.println("Can't delete temporary directory: " + exception.getMessage());
            }
        }
    }

    /**
     * Creates temp directory for {@code path} with prefix {@code temp}
     *
     * @param path a target path
     * @return a name of created directory
     * @throws ImplerException if can't create temp directory
     */
    private static Path createTempDirectories(final Path path) throws ImplerException {
        try {
            return Files.createTempDirectory(path, "temp");
        } catch (final IOException exception) {
            throw new ImplerException("Can't create temp directory");
        }
    }

    /**
     * Wraps JarImplementor for console working.
     * when 1 argument {@code class-name}, implements {@code class-name}, using {@link Implementor#implement(Class, Path)}
     * when 2 arguments {@code class-name [-jar] jar-name}, implements {@code class-name}
     * and packs result in {@code jar-name}, using {@link Implementor#implementJar(Class, Path)}
     *
     * @param args console arguments
     */
    public static void main(final String[] args) {
        if (args == null || args[0] == null || !(validateArgsForJar(args) || validateArgsForSimpleMode(args))) {
            System.err.println("Illegal arguments format. Should be <classname> [-jar JarName]");
            return;
        }
        try {
            if (validateArgsForJar(args)) {
                new Implementor().implementJar(Class.forName(args[0]), Path.of(args[2]));
            } else {
                new Implementor().implement(Class.forName(args[0]), Path.of(""));
            }
        } catch (final InvalidPathException exception) {
            System.err.println("JarName: " + args[2] + " is an illegal path");
        } catch (final ImplerException exception) {
            System.err.println("Can't implement interface " + args[0] + " " + exception.getMessage());
        } catch (final ClassNotFoundException exception) {
            System.err.println("Class " + args[0] + " can't be loaded");
        }
    }

    /**
     * Validates arguments for {@link Implementor#main(String[])} to be invoked in jar mode.
     * Jar mode is implementing class and packing implementation in jar
     *
     * @param args console arguments
     * @return true if app could be executed in jar mode
     */

    public static boolean validateArgsForJar(final String[] args) {
        return args.length == 3 && args[1] != null && args[2] != null && args[1].equals("-jar") && args[2].endsWith(".jar");
    }

    /**
     * Validates arguments for {@link Implementor#main(String[])} to be invoked in simple mode.
     * Simple mode is implementing class
     *
     * @param args console arguments
     * @return true if app could be executed in simple mode
     */
    public static boolean validateArgsForSimpleMode(final String[] args) {
        return args.length == 1;
    }


    /**
     * Returns path to the file with name {@code "token-name + suffix"}.
     * Separators in path are defined by {@code separator}.
     *
     * @param token     a type token
     * @param root      a root directory
     * @param separator a separator in a path
     * @param suffix    a suffix to be added to {@code token name}
     * @return path to the implementation of {@code token}
     */
    private Path getPathToImpl(final Class<?> token, final Path root, final String suffix, final String separator) {
        return root.resolve(token.getPackageName()
                .replace(".", separator))
                .resolve(token.getSimpleName() + suffix);
    }

    /**
     * Deletes directory and files in it.
     */
    private static class CleanDirectoryVisitor extends SimpleFileVisitor<Path> {

        /**
         * Generates {@code CleanDirectoryVisitor}
         */
        CleanDirectoryVisitor() {
            super();
        }

        /**
         * Deletes file
         *
         * @param file       a target file
         * @param attributes a target file basic attributes
         * @return {@link FileVisitResult#CONTINUE}
         * @throws IOException if an IO occurs
         */
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        /**
         * Deletes directory
         *
         * @param file      a target directory
         * @param exception if exception occurred while deleting files
         * @return {@link FileVisitResult#CONTINUE}
         * @throws IOException if an IO occurs
         */
        @Override
        public FileVisitResult postVisitDirectory(final Path file, final IOException exception) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }
    }
}