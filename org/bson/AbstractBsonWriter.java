/*
 * Decompiled with CFR 0.145.
 */
package org.bson;

import java.io.Closeable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.bson.BsonArray;
import org.bson.BsonBinary;
import org.bson.BsonBoolean;
import org.bson.BsonContextType;
import org.bson.BsonDateTime;
import org.bson.BsonDbPointer;
import org.bson.BsonDecimal128;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonElement;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonInvalidOperationException;
import org.bson.BsonJavaScript;
import org.bson.BsonJavaScriptWithScope;
import org.bson.BsonObjectId;
import org.bson.BsonReader;
import org.bson.BsonRegularExpression;
import org.bson.BsonSerializationException;
import org.bson.BsonString;
import org.bson.BsonSymbol;
import org.bson.BsonTimestamp;
import org.bson.BsonType;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.BsonWriterSettings;
import org.bson.FieldNameValidator;
import org.bson.NoOpFieldNameValidator;
import org.bson.StringUtils;
import org.bson.assertions.Assertions;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

public abstract class AbstractBsonWriter
implements BsonWriter,
Closeable {
    private final BsonWriterSettings settings;
    private final Stack<FieldNameValidator> fieldNameValidatorStack = new Stack();
    private State state;
    private Context context;
    private int serializationDepth;
    private boolean closed;

    protected AbstractBsonWriter(BsonWriterSettings settings) {
        this(settings, new NoOpFieldNameValidator());
    }

    protected AbstractBsonWriter(BsonWriterSettings settings, FieldNameValidator validator) {
        if (validator == null) {
            throw new IllegalArgumentException("Validator can not be null");
        }
        this.settings = settings;
        this.fieldNameValidatorStack.push(validator);
        this.state = State.INITIAL;
    }

    protected String getName() {
        return this.context.name;
    }

    protected boolean isClosed() {
        return this.closed;
    }

    protected void setState(State state) {
        this.state = state;
    }

    protected State getState() {
        return this.state;
    }

    protected Context getContext() {
        return this.context;
    }

    protected void setContext(Context context) {
        this.context = context;
    }

    protected abstract void doWriteStartDocument();

    protected abstract void doWriteEndDocument();

    protected abstract void doWriteStartArray();

    protected abstract void doWriteEndArray();

    protected abstract void doWriteBinaryData(BsonBinary var1);

    protected abstract void doWriteBoolean(boolean var1);

    protected abstract void doWriteDateTime(long var1);

    protected abstract void doWriteDBPointer(BsonDbPointer var1);

    protected abstract void doWriteDouble(double var1);

    protected abstract void doWriteInt32(int var1);

    protected abstract void doWriteInt64(long var1);

    protected abstract void doWriteDecimal128(Decimal128 var1);

    protected abstract void doWriteJavaScript(String var1);

    protected abstract void doWriteJavaScriptWithScope(String var1);

    protected abstract void doWriteMaxKey();

    protected abstract void doWriteMinKey();

    protected abstract void doWriteNull();

    protected abstract void doWriteObjectId(ObjectId var1);

    protected abstract void doWriteRegularExpression(BsonRegularExpression var1);

    protected abstract void doWriteString(String var1);

    protected abstract void doWriteSymbol(String var1);

    protected abstract void doWriteTimestamp(BsonTimestamp var1);

    protected abstract void doWriteUndefined();

    @Override
    public void writeStartDocument(String name) {
        this.writeName(name);
        this.writeStartDocument();
    }

    @Override
    public void writeStartDocument() {
        this.checkPreconditions("writeStartDocument", State.INITIAL, State.VALUE, State.SCOPE_DOCUMENT, State.DONE);
        if (this.context != null && this.context.name != null) {
            this.fieldNameValidatorStack.push(this.fieldNameValidatorStack.peek().getValidatorForField(this.getName()));
        }
        ++this.serializationDepth;
        if (this.serializationDepth > this.settings.getMaxSerializationDepth()) {
            throw new BsonSerializationException("Maximum serialization depth exceeded (does the object being serialized have a circular reference?).");
        }
        this.doWriteStartDocument();
        this.setState(State.NAME);
    }

    @Override
    public void writeEndDocument() {
        this.checkPreconditions("writeEndDocument", State.NAME);
        BsonContextType contextType = this.getContext().getContextType();
        if (contextType != BsonContextType.DOCUMENT && contextType != BsonContextType.SCOPE_DOCUMENT) {
            this.throwInvalidContextType("WriteEndDocument", contextType, BsonContextType.DOCUMENT, BsonContextType.SCOPE_DOCUMENT);
        }
        if (this.context.getParentContext() != null && this.context.getParentContext().name != null) {
            this.fieldNameValidatorStack.pop();
        }
        --this.serializationDepth;
        this.doWriteEndDocument();
        if (this.getContext() == null || this.getContext().getContextType() == BsonContextType.TOP_LEVEL) {
            this.setState(State.DONE);
        } else {
            this.setState(this.getNextState());
        }
    }

    @Override
    public void writeStartArray(String name) {
        this.writeName(name);
        this.writeStartArray();
    }

    @Override
    public void writeStartArray() {
        this.checkPreconditions("writeStartArray", State.VALUE);
        if (this.context != null && this.context.name != null) {
            this.fieldNameValidatorStack.push(this.fieldNameValidatorStack.peek().getValidatorForField(this.getName()));
        }
        ++this.serializationDepth;
        if (this.serializationDepth > this.settings.getMaxSerializationDepth()) {
            throw new BsonSerializationException("Maximum serialization depth exceeded (does the object being serialized have a circular reference?).");
        }
        this.doWriteStartArray();
        this.setState(State.VALUE);
    }

    @Override
    public void writeEndArray() {
        this.checkPreconditions("writeEndArray", State.VALUE);
        if (this.getContext().getContextType() != BsonContextType.ARRAY) {
            this.throwInvalidContextType("WriteEndArray", this.getContext().getContextType(), BsonContextType.ARRAY);
        }
        if (this.context.getParentContext() != null && this.context.getParentContext().name != null) {
            this.fieldNameValidatorStack.pop();
        }
        --this.serializationDepth;
        this.doWriteEndArray();
        this.setState(this.getNextState());
    }

    @Override
    public void writeBinaryData(String name, BsonBinary binary) {
        Assertions.notNull("name", name);
        Assertions.notNull("value", binary);
        this.writeName(name);
        this.writeBinaryData(binary);
    }

    @Override
    public void writeBinaryData(BsonBinary binary) {
        Assertions.notNull("value", binary);
        this.checkPreconditions("writeBinaryData", State.VALUE, State.INITIAL);
        this.doWriteBinaryData(binary);
        this.setState(this.getNextState());
    }

    @Override
    public void writeBoolean(String name, boolean value) {
        this.writeName(name);
        this.writeBoolean(value);
    }

    @Override
    public void writeBoolean(boolean value) {
        this.checkPreconditions("writeBoolean", State.VALUE, State.INITIAL);
        this.doWriteBoolean(value);
        this.setState(this.getNextState());
    }

    @Override
    public void writeDateTime(String name, long value) {
        this.writeName(name);
        this.writeDateTime(value);
    }

    @Override
    public void writeDateTime(long value) {
        this.checkPreconditions("writeDateTime", State.VALUE, State.INITIAL);
        this.doWriteDateTime(value);
        this.setState(this.getNextState());
    }

    @Override
    public void writeDBPointer(String name, BsonDbPointer value) {
        Assertions.notNull("name", name);
        Assertions.notNull("value", value);
        this.writeName(name);
        this.writeDBPointer(value);
    }

    @Override
    public void writeDBPointer(BsonDbPointer value) {
        Assertions.notNull("value", value);
        this.checkPreconditions("writeDBPointer", State.VALUE, State.INITIAL);
        this.doWriteDBPointer(value);
        this.setState(this.getNextState());
    }

    @Override
    public void writeDouble(String name, double value) {
        this.writeName(name);
        this.writeDouble(value);
    }

    @Override
    public void writeDouble(double value) {
        this.checkPreconditions("writeDBPointer", State.VALUE, State.INITIAL);
        this.doWriteDouble(value);
        this.setState(this.getNextState());
    }

    @Override
    public void writeInt32(String name, int value) {
        this.writeName(name);
        this.writeInt32(value);
    }

    @Override
    public void writeInt32(int value) {
        this.checkPreconditions("writeInt32", State.VALUE);
        this.doWriteInt32(value);
        this.setState(this.getNextState());
    }

    @Override
    public void writeInt64(String name, long value) {
        this.writeName(name);
        this.writeInt64(value);
    }

    @Override
    public void writeInt64(long value) {
        this.checkPreconditions("writeInt64", State.VALUE);
        this.doWriteInt64(value);
        this.setState(this.getNextState());
    }

    @Override
    public void writeDecimal128(Decimal128 value) {
        Assertions.notNull("value", value);
        this.checkPreconditions("writeInt64", State.VALUE);
        this.doWriteDecimal128(value);
        this.setState(this.getNextState());
    }

    @Override
    public void writeDecimal128(String name, Decimal128 value) {
        Assertions.notNull("name", name);
        Assertions.notNull("value", value);
        this.writeName(name);
        this.writeDecimal128(value);
    }

    @Override
    public void writeJavaScript(String name, String code) {
        Assertions.notNull("name", name);
        Assertions.notNull("value", code);
        this.writeName(name);
        this.writeJavaScript(code);
    }

    @Override
    public void writeJavaScript(String code) {
        Assertions.notNull("value", code);
        this.checkPreconditions("writeJavaScript", State.VALUE);
        this.doWriteJavaScript(code);
        this.setState(this.getNextState());
    }

    @Override
    public void writeJavaScriptWithScope(String name, String code) {
        Assertions.notNull("name", name);
        Assertions.notNull("value", code);
        this.writeName(name);
        this.writeJavaScriptWithScope(code);
    }

    @Override
    public void writeJavaScriptWithScope(String code) {
        Assertions.notNull("value", code);
        this.checkPreconditions("writeJavaScriptWithScope", State.VALUE);
        this.doWriteJavaScriptWithScope(code);
        this.setState(State.SCOPE_DOCUMENT);
    }

    @Override
    public void writeMaxKey(String name) {
        this.writeName(name);
        this.writeMaxKey();
    }

    @Override
    public void writeMaxKey() {
        this.checkPreconditions("writeMaxKey", State.VALUE);
        this.doWriteMaxKey();
        this.setState(this.getNextState());
    }

    @Override
    public void writeMinKey(String name) {
        this.writeName(name);
        this.writeMinKey();
    }

    @Override
    public void writeMinKey() {
        this.checkPreconditions("writeMinKey", State.VALUE);
        this.doWriteMinKey();
        this.setState(this.getNextState());
    }

    @Override
    public void writeName(String name) {
        Assertions.notNull("name", name);
        if (this.state != State.NAME) {
            this.throwInvalidState("WriteName", State.NAME);
        }
        if (!this.fieldNameValidatorStack.peek().validate(name)) {
            throw new IllegalArgumentException(String.format("Invalid BSON field name %s", name));
        }
        this.doWriteName(name);
        this.context.name = name;
        this.state = State.VALUE;
    }

    protected void doWriteName(String name) {
    }

    @Override
    public void writeNull(String name) {
        this.writeName(name);
        this.writeNull();
    }

    @Override
    public void writeNull() {
        this.checkPreconditions("writeNull", State.VALUE);
        this.doWriteNull();
        this.setState(this.getNextState());
    }

    @Override
    public void writeObjectId(String name, ObjectId objectId) {
        Assertions.notNull("name", name);
        Assertions.notNull("value", objectId);
        this.writeName(name);
        this.writeObjectId(objectId);
    }

    @Override
    public void writeObjectId(ObjectId objectId) {
        Assertions.notNull("value", objectId);
        this.checkPreconditions("writeObjectId", State.VALUE);
        this.doWriteObjectId(objectId);
        this.setState(this.getNextState());
    }

    @Override
    public void writeRegularExpression(String name, BsonRegularExpression regularExpression) {
        Assertions.notNull("name", name);
        Assertions.notNull("value", regularExpression);
        this.writeName(name);
        this.writeRegularExpression(regularExpression);
    }

    @Override
    public void writeRegularExpression(BsonRegularExpression regularExpression) {
        Assertions.notNull("value", regularExpression);
        this.checkPreconditions("writeRegularExpression", State.VALUE);
        this.doWriteRegularExpression(regularExpression);
        this.setState(this.getNextState());
    }

    @Override
    public void writeString(String name, String value) {
        Assertions.notNull("name", name);
        Assertions.notNull("value", value);
        this.writeName(name);
        this.writeString(value);
    }

    @Override
    public void writeString(String value) {
        Assertions.notNull("value", value);
        this.checkPreconditions("writeString", State.VALUE);
        this.doWriteString(value);
        this.setState(this.getNextState());
    }

    @Override
    public void writeSymbol(String name, String value) {
        Assertions.notNull("name", name);
        Assertions.notNull("value", value);
        this.writeName(name);
        this.writeSymbol(value);
    }

    @Override
    public void writeSymbol(String value) {
        Assertions.notNull("value", value);
        this.checkPreconditions("writeSymbol", State.VALUE);
        this.doWriteSymbol(value);
        this.setState(this.getNextState());
    }

    @Override
    public void writeTimestamp(String name, BsonTimestamp value) {
        Assertions.notNull("name", name);
        Assertions.notNull("value", value);
        this.writeName(name);
        this.writeTimestamp(value);
    }

    @Override
    public void writeTimestamp(BsonTimestamp value) {
        Assertions.notNull("value", value);
        this.checkPreconditions("writeTimestamp", State.VALUE);
        this.doWriteTimestamp(value);
        this.setState(this.getNextState());
    }

    @Override
    public void writeUndefined(String name) {
        this.writeName(name);
        this.writeUndefined();
    }

    @Override
    public void writeUndefined() {
        this.checkPreconditions("writeUndefined", State.VALUE);
        this.doWriteUndefined();
        this.setState(this.getNextState());
    }

    protected State getNextState() {
        if (this.getContext().getContextType() == BsonContextType.ARRAY) {
            return State.VALUE;
        }
        return State.NAME;
    }

    protected boolean checkState(State[] validStates) {
        for (State cur : validStates) {
            if (cur != this.getState()) continue;
            return true;
        }
        return false;
    }

    protected void checkPreconditions(String methodName, State ... validStates) {
        if (this.isClosed()) {
            throw new IllegalStateException("BsonWriter is closed");
        }
        if (!this.checkState(validStates)) {
            this.throwInvalidState(methodName, validStates);
        }
    }

    protected void throwInvalidContextType(String methodName, BsonContextType actualContextType, BsonContextType ... validContextTypes) {
        String validContextTypesString = StringUtils.join(" or ", Arrays.asList(validContextTypes));
        throw new BsonInvalidOperationException(String.format("%s can only be called when ContextType is %s, not when ContextType is %s.", new Object[]{methodName, validContextTypesString, actualContextType}));
    }

    protected void throwInvalidState(String methodName, State ... validStates) {
        if (!(this.state != State.INITIAL && this.state != State.SCOPE_DOCUMENT && this.state != State.DONE || methodName.startsWith("end") || methodName.equals("writeName"))) {
            String typeName = methodName.substring(5);
            if (typeName.startsWith("start")) {
                typeName = typeName.substring(5);
            }
            String article = "A";
            if (Arrays.asList(Character.valueOf('A'), Character.valueOf('E'), Character.valueOf('I'), Character.valueOf('O'), Character.valueOf('U')).contains(Character.valueOf(typeName.charAt(0)))) {
                article = "An";
            }
            throw new BsonInvalidOperationException(String.format("%s %s value cannot be written to the root level of a BSON document.", article, typeName));
        }
        String validStatesString = StringUtils.join(" or ", Arrays.asList(validStates));
        throw new BsonInvalidOperationException(String.format("%s can only be called when State is %s, not when State is %s", new Object[]{methodName, validStatesString, this.state}));
    }

    @Override
    public void close() {
        this.closed = true;
    }

    @Override
    public void pipe(BsonReader reader) {
        Assertions.notNull("reader", reader);
        this.pipeDocument(reader, null);
    }

    public void pipe(BsonReader reader, List<BsonElement> extraElements) {
        Assertions.notNull("reader", reader);
        Assertions.notNull("extraElements", extraElements);
        this.pipeDocument(reader, extraElements);
    }

    protected void pipeExtraElements(List<BsonElement> extraElements) {
        Assertions.notNull("extraElements", extraElements);
        for (BsonElement cur : extraElements) {
            this.writeName(cur.getName());
            this.pipeValue(cur.getValue());
        }
    }

    protected boolean abortPipe() {
        return false;
    }

    private void pipeDocument(BsonReader reader, List<BsonElement> extraElements) {
        reader.readStartDocument();
        this.writeStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            this.writeName(reader.readName());
            this.pipeValue(reader);
            if (!this.abortPipe()) continue;
            return;
        }
        reader.readEndDocument();
        if (extraElements != null) {
            this.pipeExtraElements(extraElements);
        }
        this.writeEndDocument();
    }

    private void pipeJavascriptWithScope(BsonReader reader) {
        this.writeJavaScriptWithScope(reader.readJavaScriptWithScope());
        this.pipeDocument(reader, null);
    }

    private void pipeValue(BsonReader reader) {
        switch (reader.getCurrentBsonType()) {
            case DOCUMENT: {
                this.pipeDocument(reader, null);
                break;
            }
            case ARRAY: {
                this.pipeArray(reader);
                break;
            }
            case DOUBLE: {
                this.writeDouble(reader.readDouble());
                break;
            }
            case STRING: {
                this.writeString(reader.readString());
                break;
            }
            case BINARY: {
                this.writeBinaryData(reader.readBinaryData());
                break;
            }
            case UNDEFINED: {
                reader.readUndefined();
                this.writeUndefined();
                break;
            }
            case OBJECT_ID: {
                this.writeObjectId(reader.readObjectId());
                break;
            }
            case BOOLEAN: {
                this.writeBoolean(reader.readBoolean());
                break;
            }
            case DATE_TIME: {
                this.writeDateTime(reader.readDateTime());
                break;
            }
            case NULL: {
                reader.readNull();
                this.writeNull();
                break;
            }
            case REGULAR_EXPRESSION: {
                this.writeRegularExpression(reader.readRegularExpression());
                break;
            }
            case JAVASCRIPT: {
                this.writeJavaScript(reader.readJavaScript());
                break;
            }
            case SYMBOL: {
                this.writeSymbol(reader.readSymbol());
                break;
            }
            case JAVASCRIPT_WITH_SCOPE: {
                this.pipeJavascriptWithScope(reader);
                break;
            }
            case INT32: {
                this.writeInt32(reader.readInt32());
                break;
            }
            case TIMESTAMP: {
                this.writeTimestamp(reader.readTimestamp());
                break;
            }
            case INT64: {
                this.writeInt64(reader.readInt64());
                break;
            }
            case DECIMAL128: {
                this.writeDecimal128(reader.readDecimal128());
                break;
            }
            case MIN_KEY: {
                reader.readMinKey();
                this.writeMinKey();
                break;
            }
            case DB_POINTER: {
                this.writeDBPointer(reader.readDBPointer());
                break;
            }
            case MAX_KEY: {
                reader.readMaxKey();
                this.writeMaxKey();
                break;
            }
            default: {
                throw new IllegalArgumentException("unhandled BSON type: " + (Object)((Object)reader.getCurrentBsonType()));
            }
        }
    }

    private void pipeDocument(BsonDocument value) {
        this.writeStartDocument();
        for (Map.Entry<String, BsonValue> cur : value.entrySet()) {
            this.writeName(cur.getKey());
            this.pipeValue(cur.getValue());
        }
        this.writeEndDocument();
    }

    private void pipeArray(BsonReader reader) {
        reader.readStartArray();
        this.writeStartArray();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            this.pipeValue(reader);
            if (!this.abortPipe()) continue;
            return;
        }
        reader.readEndArray();
        this.writeEndArray();
    }

    private void pipeArray(BsonArray array) {
        this.writeStartArray();
        for (BsonValue cur : array) {
            this.pipeValue(cur);
        }
        this.writeEndArray();
    }

    private void pipeJavascriptWithScope(BsonJavaScriptWithScope javaScriptWithScope) {
        this.writeJavaScriptWithScope(javaScriptWithScope.getCode());
        this.pipeDocument(javaScriptWithScope.getScope());
    }

    private void pipeValue(BsonValue value) {
        switch (value.getBsonType()) {
            case DOCUMENT: {
                this.pipeDocument(value.asDocument());
                break;
            }
            case ARRAY: {
                this.pipeArray(value.asArray());
                break;
            }
            case DOUBLE: {
                this.writeDouble(value.asDouble().getValue());
                break;
            }
            case STRING: {
                this.writeString(value.asString().getValue());
                break;
            }
            case BINARY: {
                this.writeBinaryData(value.asBinary());
                break;
            }
            case UNDEFINED: {
                this.writeUndefined();
                break;
            }
            case OBJECT_ID: {
                this.writeObjectId(value.asObjectId().getValue());
                break;
            }
            case BOOLEAN: {
                this.writeBoolean(value.asBoolean().getValue());
                break;
            }
            case DATE_TIME: {
                this.writeDateTime(value.asDateTime().getValue());
                break;
            }
            case NULL: {
                this.writeNull();
                break;
            }
            case REGULAR_EXPRESSION: {
                this.writeRegularExpression(value.asRegularExpression());
                break;
            }
            case JAVASCRIPT: {
                this.writeJavaScript(value.asJavaScript().getCode());
                break;
            }
            case SYMBOL: {
                this.writeSymbol(value.asSymbol().getSymbol());
                break;
            }
            case JAVASCRIPT_WITH_SCOPE: {
                this.pipeJavascriptWithScope(value.asJavaScriptWithScope());
                break;
            }
            case INT32: {
                this.writeInt32(value.asInt32().getValue());
                break;
            }
            case TIMESTAMP: {
                this.writeTimestamp(value.asTimestamp());
                break;
            }
            case INT64: {
                this.writeInt64(value.asInt64().getValue());
                break;
            }
            case DECIMAL128: {
                this.writeDecimal128(value.asDecimal128().getValue());
                break;
            }
            case MIN_KEY: {
                this.writeMinKey();
                break;
            }
            case DB_POINTER: {
                this.writeDBPointer(value.asDBPointer());
                break;
            }
            case MAX_KEY: {
                this.writeMaxKey();
                break;
            }
            default: {
                throw new IllegalArgumentException("unhandled BSON type: " + (Object)((Object)value.getBsonType()));
            }
        }
    }

    protected class Mark {
        private final Context markedContext;
        private final State markedState;
        private final String currentName;
        private final int serializationDepth;

        protected Mark() {
            this.markedContext = AbstractBsonWriter.this.context.copy();
            this.markedState = AbstractBsonWriter.this.state;
            this.currentName = AbstractBsonWriter.this.context.name;
            this.serializationDepth = AbstractBsonWriter.this.serializationDepth;
        }

        protected void reset() {
            AbstractBsonWriter.this.setContext(this.markedContext);
            AbstractBsonWriter.this.setState(this.markedState);
            AbstractBsonWriter.this.context.name = this.currentName;
            AbstractBsonWriter.this.serializationDepth = this.serializationDepth;
        }
    }

    public class Context {
        private final Context parentContext;
        private final BsonContextType contextType;
        private String name;

        public Context(Context from) {
            this.parentContext = from.parentContext;
            this.contextType = from.contextType;
        }

        public Context(Context parentContext, BsonContextType contextType) {
            this.parentContext = parentContext;
            this.contextType = contextType;
        }

        public Context getParentContext() {
            return this.parentContext;
        }

        public BsonContextType getContextType() {
            return this.contextType;
        }

        public Context copy() {
            return new Context(this);
        }
    }

    public static enum State {
        INITIAL,
        NAME,
        VALUE,
        SCOPE_DOCUMENT,
        DONE,
        CLOSED;
        
    }

}

