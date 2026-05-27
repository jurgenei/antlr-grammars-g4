# Samples (antlr-grammars-g4)

This directory contains Gradle build use-case examples for validating ANTLR v4 grammar samples with plugin ID `name.jurgenei.gradle.antlr`.

Note: the plugin ID is `name.jurgenei.gradle.antlr`, while the default task name remains `xmlast`.

## Files

- `use-case-01-direct/build.gradle` - direct parser/lexer/startRule configuration.
- `use-case-02-catalog/build.gradle` - catalog-driven configuration.
- `use-case-02-catalog/catalog.xml` - catalog file used by the catalog sample.
- `use-case-03-check-hook/build.gradle` - wiring `xmlast` task into `check`.

These files are designed as copy/paste templates for a grammar module where:

- parser class: `name.jurgenei.parsers.ANTLRv4Parser`
- lexer class: `name.jurgenei.parsers.ANTLRv4Lexer`
- parser entry rule: `grammarSpec`
- sample directory: `src/test/resources/antlr4`

