/*
 * Copyright 2019 ukuz90
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.ukuz.piccolo.common.message;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.PooledByteBufAllocator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * @author ukuz90
 */
public abstract class BaseMqMessage {

    private CodedOutputStream outputStream;
    private CodedInputStream inputStream;

    public byte[] encode() {
        ByteBuf output = PooledByteBufAllocator.DEFAULT.buffer();
        this.outputStream = CodedOutputStream.newInstance(new ByteBufOutputStream(output));
        doEncode();
        try {
            //when byte size >= 4KB would auto flush
            this.outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] bytes = new byte[this.outputStream.getTotalBytesWritten()];

        output.readBytes(bytes);
        return bytes;
    }

    public void decode(byte[] in) {
        this.inputStream = CodedInputStream.newInstance(in);
        doDecode();
    }

    protected final void writeByte(byte b) {
        writeAndIgnoreException(os -> os.write(b));
    }

    protected final void writeShort(short s) {
        writeAndIgnoreException(os -> os.writeInt32NoTag(s));
    }

    protected final void writeInt(int i) {
        writeAndIgnoreException(os -> os.writeInt32NoTag(i));
    }

    protected final void writeLong(long l) {
        writeAndIgnoreException(os -> os.writeInt64NoTag(l));
    }

    protected final void writeFloat(float f) {
        writeAndIgnoreException(os -> os.writeFloatNoTag(f));
    }

    protected final void writeDouble(double d) {
        writeAndIgnoreException(os -> os.writeDoubleNoTag(d));
    }

    protected final void writeBoolean(boolean b) {
        writeAndIgnoreException(os -> os.writeBoolNoTag(b));
    }

    protected final void writeString(String content) {
        writeBytes(content == null ? null : content.getBytes(StandardCharsets.UTF_8));
    }

    protected final void writeBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            writeAndIgnoreException(os -> os.writeInt32NoTag(0));
        } else {
            writeAndIgnoreException(os -> os.writeInt32NoTag(bytes.length));
            writeAndIgnoreException(os -> os.write(bytes, 0, bytes.length));
        }
    }

    protected final byte readByte() {
        return readAndIgnoreException(is -> is.readRawByte(), (byte)0);
    }

    protected final short readShort() {
        int ret = readAndIgnoreException(is -> is.readInt32(), 0);
        return (short) ret;
    }

    protected final int readInt() {
        return readAndIgnoreException(is -> is.readInt32(), 0);
    }

    protected final long readLong() {
        return readAndIgnoreException(is -> is.readInt64(), 0L);
    }

    protected final float readFloat() {
        return readAndIgnoreException(is -> is.readFloat(), 0f);
    }

    protected final double readDouble() {
        return readAndIgnoreException(is -> is.readDouble(), 0d);
    }

    protected final boolean readBoolean() {
        return readAndIgnoreException(is -> is.readBool(), false);
    }

    protected final String readString() {
        return new String(readBytes(), StandardCharsets.UTF_8);
    }

    public byte[] readBytes() {
        int len = readAndIgnoreException(is -> is.readInt32(), 0);
        if (len <= 0) {
            return new byte[0];
        }
        return readAndIgnoreException(is -> is.readRawBytes(len), new byte[0]);
    }



    private final <R> R readAndIgnoreException(IgnoreExceptionFunction<CodedInputStream, R> function, R defaultRet) {
        if (this.inputStream != null) {
            return function.apply(this.inputStream, defaultRet);
        }
        throw new RuntimeException("should call decode");
    }

    private final void writeAndIgnoreException(IgnoreExceptionConsumer<CodedOutputStream> consumer) {
        Optional.ofNullable(this.outputStream).ifPresent(consumer::accept);
    }

    @FunctionalInterface
    private interface IgnoreExceptionFunction<T, R> extends BiFunction<T, R, R> {

        @Override
        default R apply(T t, R defaultRet) {
            try {
               return applyWithException(t);
            } catch (Exception e) {
            }
            return defaultRet;
        }

        R applyWithException(T t) throws Exception;
    }

    @FunctionalInterface
    private interface IgnoreExceptionConsumer<T> extends Consumer<T> {

        @Override
        default void accept(T t) {
            try {
                acceptWithException(t);
            } catch (Exception e) {
            }
        }

        void acceptWithException(T t) throws Exception;
    }

    protected abstract void doEncode();

    protected abstract void doDecode();
}
