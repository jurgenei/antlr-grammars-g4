package name.jurgenei.parsers;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.TokenStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Unit tests for ANTLR v4 (G4) grammar lexer and parser using dynamic class loading.
 *
 * <p>Parser/lexer classes are loaded reflectively so this test does not have a compile-time
 * dependency on generated ANTLR classes.</p>
 */
public class G4LexerParserTest {

    private List<File> testG4Files;
    private Constructor<? extends Lexer> lexerConstructor;
    private Constructor<? extends Parser> parserConstructor;
    private Method grammarFileMethod;

    @Before
    public void setupTestFiles() throws Exception {
        testG4Files = getG4FilesInDirectory(new File("src/test/resources/antlr4"));
        Assert.assertTrue("No G4 test files found in src/test/resources/antlr4", !testG4Files.isEmpty());
        loadParserClasses();
    }

    private void loadParserClasses() throws Exception {
        try {
            final Class<?> lexerRaw = Class.forName("name.jurgenei.parsers.ANTLRv4Lexer");
            final Class<?> parserRaw = Class.forName("name.jurgenei.parsers.ANTLRv4Parser");

            if (!Lexer.class.isAssignableFrom(lexerRaw)) {
                throw new IllegalStateException("ANTLRv4Lexer is not a Lexer");
            }
            if (!Parser.class.isAssignableFrom(parserRaw)) {
                throw new IllegalStateException("ANTLRv4Parser is not a Parser");
            }

            @SuppressWarnings("unchecked") final Class<? extends Lexer> lexerClass = (Class<? extends Lexer>) lexerRaw;
            @SuppressWarnings("unchecked") final Class<? extends Parser> parserClass = (Class<? extends Parser>) parserRaw;

            lexerConstructor = lexerClass.getConstructor(CharStream.class);
            parserConstructor = parserClass.getConstructor(TokenStream.class);
            try {
                grammarFileMethod = parserClass.getMethod("grammarSpec");
            } catch (NoSuchMethodException ignored) {
                grammarFileMethod = parserClass.getMethod("grammarFile");
            }
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(
                    "Could not load ANTLR-generated parser classes. Ensure grammar generation/compilation ran first.",
                    ex);
        }
    }

    @Test
    public void canParseG4TestFiles() throws Exception {
        for (File g4File : testG4Files) {
            try (InputStream is = Files.newInputStream(g4File.toPath())) {
                final CharStream charStream = CharStreams.fromStream(is, StandardCharsets.UTF_8);
                final Lexer lexer = lexerConstructor.newInstance(charStream);
                final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
                final Parser parser = parserConstructor.newInstance(tokenStream);

                parser.removeErrorListeners();
                parser.addErrorListener(new BaseErrorListener() {
                    @Override
                    public void syntaxError(
                            final Recognizer<?, ?> recognizer,
                            final Object offendingSymbol,
                            final int line,
                            final int charPositionInLine,
                            final String msg,
                            final RecognitionException e) {
                        Assert.fail("Parse error in " + g4File.getName() + " at line " + line + ": " + msg);
                    }
                });

                final Object tree = grammarFileMethod.invoke(parser);
                Assert.assertNotNull("Parse tree should not be null for " + g4File.getName(), tree);
            }
        }
    }

    @Test
    public void lexerTokenizesG4Grammar() throws Exception {
        if (testG4Files.isEmpty()) {
            return;
        }

        final File testFile = testG4Files.get(0);
        try (InputStream is = Files.newInputStream(testFile.toPath())) {
            final CharStream charStream = CharStreams.fromStream(is, StandardCharsets.UTF_8);
            final Lexer lexer = lexerConstructor.newInstance(charStream);
            final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            tokenStream.fill();

            Assert.assertTrue("Lexer should produce at least one token", tokenStream.getNumberOfOnChannelTokens() > 0);
        }
    }

    private List<File> getG4FilesInDirectory(final File directory) {
        try {
            return Files.walk(directory.toPath())
                    .filter(p -> p.toString().endsWith(".g4"))
                    .map(Path::toFile)
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error reading directory: " + directory);
            return java.util.Collections.emptyList();
        }
    }
}
