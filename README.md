# antlr-grammars-g4

ANTLR v4 self-grammar module extracted from `gradle-antlr-xml-plugin`.

This repository packages the ANTLR v4 grammar (`ANTLRv4Lexer.g4` / `ANTLRv4Parser.g4`) and validates it against real `.g4` samples in test resources.

## What this repo contains

- grammar sources: `src/main/antlr/name/jurgenei/parsers`
- lexer support class: `src/main/java/name/jurgenei/parsers/LexerAdaptor.java`
- sample inputs: `src/test/resources/antlr4`
- dynamic-loading parser test: `src/test/java/name/jurgenei/parsers/G4LexerParserTest.java`

## Build model

This project uses:

- `antlr` plugin for source generation
- local composite plugin include for `xmlast` via `../gradle-antlr-plugin`
- custom tasks to generate and compile ANTLR sources into `build/classes/java/antlr`

The generated parser/lexer classes are loaded dynamically in tests (reflection), so tests do not require direct compile-time references to generated classes.

## Requirements

- Java 21+
- Gradle 8+

## Quick start

```bash
./gradlew clean test
```

## Important tasks

- `generateLexerSources` - generates lexer sources from `ANTLRv4Lexer.g4`
- `generateParserSources` - generates parser sources from `ANTLRv4Parser.g4`
- `compileAntlrSources` - compiles generated sources + `LexerAdaptor`
- `test` - runs dynamic-loading parser tests over sample `.g4` files
- `xmlast` - optional conversion of sample `.g4` files to XML AST

## XML AST task

The `xmlast` task is configured with:

- `parserClassName = name.jurgenei.parsers.ANTLRv4Parser`
- `lexerClassName = name.jurgenei.parsers.ANTLRv4Lexer`
- `startRule = grammarSpec`
- source directory: `src/test/resources/antlr4`
- output directory: `build/xmlast-samples`

Run manually:

```bash
./gradlew xmlast
```

## Notes

- `check` currently focuses on source presence verification (`verifyGrammarSources`) and tests.
- `xmlast` is available as an explicit validation step when needed.

## Project status

This module is actively wired for dynamic parser loading and sample-based grammar verification aligned with the local `gradle-antlr-plugin` integration.
