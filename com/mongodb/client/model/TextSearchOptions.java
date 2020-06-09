/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.model;

import com.mongodb.lang.Nullable;

public final class TextSearchOptions {
    private String language;
    private Boolean caseSensitive;
    private Boolean diacriticSensitive;

    @Nullable
    public String getLanguage() {
        return this.language;
    }

    public TextSearchOptions language(@Nullable String language) {
        this.language = language;
        return this;
    }

    @Nullable
    public Boolean getCaseSensitive() {
        return this.caseSensitive;
    }

    public TextSearchOptions caseSensitive(@Nullable Boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }

    @Nullable
    public Boolean getDiacriticSensitive() {
        return this.diacriticSensitive;
    }

    public TextSearchOptions diacriticSensitive(@Nullable Boolean diacriticSensitive) {
        this.diacriticSensitive = diacriticSensitive;
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        TextSearchOptions that = (TextSearchOptions)o;
        if (this.language != null ? !this.language.equals(that.language) : that.language != null) {
            return false;
        }
        if (this.caseSensitive != null ? !this.caseSensitive.equals(that.caseSensitive) : that.caseSensitive != null) {
            return false;
        }
        return this.diacriticSensitive != null ? this.diacriticSensitive.equals(that.diacriticSensitive) : that.diacriticSensitive == null;
    }

    public int hashCode() {
        int result = this.language != null ? this.language.hashCode() : 0;
        result = 31 * result + (this.caseSensitive != null ? this.caseSensitive.hashCode() : 0);
        result = 31 * result + (this.diacriticSensitive != null ? this.diacriticSensitive.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "Text Search Options{language='" + this.language + '\'' + ", caseSensitive=" + this.caseSensitive + ", diacriticSensitive=" + this.diacriticSensitive + '}';
    }
}

