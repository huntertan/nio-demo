package nio.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * Created by hanqing.tan on 2016/3/25.
 */
public class CharsetUtil {
    private static final String UTF_8 = "UTF-8";
    private static CharsetEncoder encoder = Charset.forName(UTF_8).newEncoder();
    private static CharsetDecoder decoder = Charset.forName(UTF_8).newDecoder();

    public static ByteBuffer encode(String text) throws CharacterCodingException {
        return encoder.encode(CharBuffer.wrap(text));
    }

    public static CharBuffer decode(ByteBuffer byteBuffer) throws CharacterCodingException {
        return decoder.decode(byteBuffer);
    }
}
