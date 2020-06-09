/*
 * Decompiled with CFR 0.145.
 */
package org.bson.json;

import org.bson.assertions.Assertions;

public final class StrictCharacterStreamJsonWriterSettings {
    private final boolean indent;
    private final String newLineCharacters;
    private final String indentCharacters;
    private final int maxLength;

    public static Builder builder() {
        return new Builder();
    }

    private StrictCharacterStreamJsonWriterSettings(Builder builder) {
        this.indent = builder.indent;
        this.newLineCharacters = builder.newLineCharacters != null ? builder.newLineCharacters : System.getProperty("line.separator");
        this.indentCharacters = builder.indentCharacters;
        this.maxLength = builder.maxLength;
    }

    public boolean isIndent() {
        return this.indent;
    }

    public String getNewLineCharacters() {
        return this.newLineCharacters;
    }

    public String getIndentCharacters() {
        return this.indentCharacters;
    }

    public int getMaxLength() {
        return this.maxLength;
    }

    public static final class Builder {
        private boolean indent;
        private String newLineCharacters = System.getProperty("line.separator");
        private String indentCharacters = "  ";
        private int maxLength;

        public StrictCharacterStreamJsonWriterSettings build() {
            return new StrictCharacterStreamJsonWriterSettings(this);
        }

        public Builder indent(boolean indent) {
            this.indent = indent;
            return this;
        }

        public Builder newLineCharacters(String newLineCharacters) {
            Assertions.notNull("newLineCharacters", newLineCharacters);
            this.newLineCharacters = newLineCharacters;
            return this;
        }

        public Builder indentCharacters(String indentCharacters) {
            Assertions.notNull("indentCharacters", indentCharacters);
            this.indentCharacters = indentCharacters;
            return this;
        }

        public Builder maxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        private Builder() {
        }
    }

}

