/*
 * Decompiled with CFR 0.145.
 */
package org.bson.json;

import java.io.Writer;
import org.bson.AbstractBsonWriter;
import org.bson.BsonBinary;
import org.bson.BsonContextType;
import org.bson.BsonDbPointer;
import org.bson.BsonMaxKey;
import org.bson.BsonMinKey;
import org.bson.BsonNull;
import org.bson.BsonRegularExpression;
import org.bson.BsonTimestamp;
import org.bson.BsonUndefined;
import org.bson.BsonWriterSettings;
import org.bson.json.Converter;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.bson.json.StrictCharacterStreamJsonWriter;
import org.bson.json.StrictCharacterStreamJsonWriterSettings;
import org.bson.json.StrictJsonWriter;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

public class JsonWriter
extends AbstractBsonWriter {
    private final JsonWriterSettings settings;
    private final StrictCharacterStreamJsonWriter strictJsonWriter;

    public JsonWriter(Writer writer) {
        this(writer, new JsonWriterSettings());
    }

    public JsonWriter(Writer writer, JsonWriterSettings settings) {
        super(settings);
        this.settings = settings;
        this.setContext(new Context(null, BsonContextType.TOP_LEVEL));
        this.strictJsonWriter = new StrictCharacterStreamJsonWriter(writer, StrictCharacterStreamJsonWriterSettings.builder().indent(settings.isIndent()).newLineCharacters(settings.getNewLineCharacters()).indentCharacters(settings.getIndentCharacters()).maxLength(settings.getMaxLength()).build());
    }

    public Writer getWriter() {
        return this.strictJsonWriter.getWriter();
    }

    @Override
    protected Context getContext() {
        return (Context)super.getContext();
    }

    @Override
    protected void doWriteName(String name) {
        this.strictJsonWriter.writeName(name);
    }

    @Override
    protected void doWriteStartDocument() {
        this.strictJsonWriter.writeStartObject();
        BsonContextType contextType = this.getState() == AbstractBsonWriter.State.SCOPE_DOCUMENT ? BsonContextType.SCOPE_DOCUMENT : BsonContextType.DOCUMENT;
        this.setContext(new Context(this.getContext(), contextType));
    }

    @Override
    protected void doWriteEndDocument() {
        this.strictJsonWriter.writeEndObject();
        if (this.getContext().getContextType() == BsonContextType.SCOPE_DOCUMENT) {
            this.setContext(this.getContext().getParentContext());
            this.writeEndDocument();
        } else {
            this.setContext(this.getContext().getParentContext());
        }
    }

    @Override
    protected void doWriteStartArray() {
        this.strictJsonWriter.writeStartArray();
        this.setContext(new Context(this.getContext(), BsonContextType.ARRAY));
    }

    @Override
    protected void doWriteEndArray() {
        this.strictJsonWriter.writeEndArray();
        this.setContext(this.getContext().getParentContext());
    }

    @Override
    protected void doWriteBinaryData(BsonBinary binary) {
        this.settings.getBinaryConverter().convert(binary, this.strictJsonWriter);
    }

    @Override
    public void doWriteBoolean(boolean value) {
        this.settings.getBooleanConverter().convert(value, this.strictJsonWriter);
    }

    @Override
    protected void doWriteDateTime(long value) {
        this.settings.getDateTimeConverter().convert(value, this.strictJsonWriter);
    }

    @Override
    protected void doWriteDBPointer(BsonDbPointer value) {
        if (this.settings.getOutputMode() == JsonMode.EXTENDED) {
            new Converter<BsonDbPointer>(){

                @Override
                public void convert(BsonDbPointer value1, StrictJsonWriter writer) {
                    writer.writeStartObject();
                    writer.writeStartObject("$dbPointer");
                    writer.writeString("$ref", value1.getNamespace());
                    writer.writeName("$id");
                    JsonWriter.this.doWriteObjectId(value1.getId());
                    writer.writeEndObject();
                    writer.writeEndObject();
                }
            }.convert(value, (StrictJsonWriter)this.strictJsonWriter);
        } else {
            new Converter<BsonDbPointer>(){

                @Override
                public void convert(BsonDbPointer value1, StrictJsonWriter writer) {
                    writer.writeStartObject();
                    writer.writeString("$ref", value1.getNamespace());
                    writer.writeName("$id");
                    JsonWriter.this.doWriteObjectId(value1.getId());
                    writer.writeEndObject();
                }
            }.convert(value, (StrictJsonWriter)this.strictJsonWriter);
        }
    }

    @Override
    protected void doWriteDouble(double value) {
        this.settings.getDoubleConverter().convert(value, this.strictJsonWriter);
    }

    @Override
    protected void doWriteInt32(int value) {
        this.settings.getInt32Converter().convert(value, this.strictJsonWriter);
    }

    @Override
    protected void doWriteInt64(long value) {
        this.settings.getInt64Converter().convert(value, this.strictJsonWriter);
    }

    @Override
    protected void doWriteDecimal128(Decimal128 value) {
        this.settings.getDecimal128Converter().convert(value, this.strictJsonWriter);
    }

    @Override
    protected void doWriteJavaScript(String code) {
        this.settings.getJavaScriptConverter().convert(code, this.strictJsonWriter);
    }

    @Override
    protected void doWriteJavaScriptWithScope(String code) {
        this.writeStartDocument();
        this.writeString("$code", code);
        this.writeName("$scope");
    }

    @Override
    protected void doWriteMaxKey() {
        this.settings.getMaxKeyConverter().convert(null, this.strictJsonWriter);
    }

    @Override
    protected void doWriteMinKey() {
        this.settings.getMinKeyConverter().convert(null, this.strictJsonWriter);
    }

    @Override
    public void doWriteNull() {
        this.settings.getNullConverter().convert(null, this.strictJsonWriter);
    }

    @Override
    public void doWriteObjectId(ObjectId objectId) {
        this.settings.getObjectIdConverter().convert(objectId, this.strictJsonWriter);
    }

    @Override
    public void doWriteRegularExpression(BsonRegularExpression regularExpression) {
        this.settings.getRegularExpressionConverter().convert(regularExpression, this.strictJsonWriter);
    }

    @Override
    public void doWriteString(String value) {
        this.settings.getStringConverter().convert(value, this.strictJsonWriter);
    }

    @Override
    public void doWriteSymbol(String value) {
        this.settings.getSymbolConverter().convert(value, this.strictJsonWriter);
    }

    @Override
    public void doWriteTimestamp(BsonTimestamp value) {
        this.settings.getTimestampConverter().convert(value, this.strictJsonWriter);
    }

    @Override
    public void doWriteUndefined() {
        this.settings.getUndefinedConverter().convert(null, this.strictJsonWriter);
    }

    @Override
    public void flush() {
        this.strictJsonWriter.flush();
    }

    public boolean isTruncated() {
        return this.strictJsonWriter.isTruncated();
    }

    @Override
    protected boolean abortPipe() {
        return this.strictJsonWriter.isTruncated();
    }

    public class Context
    extends AbstractBsonWriter.Context {
        @Deprecated
        public Context(Context parentContext, BsonContextType contextType, String indentChars) {
            this(parentContext, contextType);
        }

        public Context(Context parentContext, BsonContextType contextType) {
            super(JsonWriter.this, parentContext, contextType);
        }

        @Override
        public Context getParentContext() {
            return (Context)super.getParentContext();
        }
    }

}

