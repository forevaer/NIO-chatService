package com.godme.codec;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class MessageCodec {
    private static Charset charset;
    static{
        charset = Charset.forName("utf-8");
    }
    public static ByteBuffer encode(String string) {
        return charset.encode(string);

    }
    public static String decode(ByteBuffer buffer) {
        return charset.decode(buffer).toString();
    }

    public static void main(String[] args) {
        System.out.println(MessageCodec.decode(MessageCodec.encode("我爱你")));
    }
}
