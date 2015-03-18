package models.util;

/**
 * Fast encoder/decoder of long integers to/from Base64 (11 chars) with some shuffling
 * to hide the actual value.
 */
public class IdentifierCodec {

    private static final String BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ-abcdefghijklmnopqrstuvwxyz_0123456789";
    private static final int[] REVERSE_BASE64 = new int[0x7F];
    static {
        // Build a fast reverse lookup table
        for (int i = 0; i < REVERSE_BASE64.length; i++) {
            REVERSE_BASE64[i] = BASE64.indexOf(i);
        }
    }

    // Random long value to obfuscate a bit
    private static final long SALT = 0L;

    /**
     * Encode a <code>long</code> value to an URL-safe 11 character string.
     */
    public static String encode (long id) {
        // Shuffle a bit
        id ^= SALT;

        // XOR every byte with the preceding one, from LSB to MSB
        for (int i = 1; i < 8; i++) {
            id ^= (id << 8) & (0xFFL << (i*8));
        }

        // Base-64 encode. 64 bits --> 11 chars
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < 11; i++) {
            buf.append(BASE64.charAt((int)(id & 63))); // 63 == 6 bits
            id >>= 6;
        }

        return buf.toString();
    }

    /**
     * Decode an obfuscated string to a <code>long</code> value.
     */
    public static long decode (String text) {
        // Base-64 decode
        long id = 0;
        for (int i = text.length(); --i >= 0;) {
            id <<= 6;
            id |= REVERSE_BASE64[text.charAt(i)];
        }

        // XOR every byte with the preceding one, from MSB to LSB
        for (int i = 8; --i > 0;) {
            id ^= (id << 8) & (0xFFL << (i*8));
        }

        // De-shuffle
        return id ^ SALT;
    }

//    public static void main(String[] args) {
//        long start = 0xFF00000000000000L;
//        for (long i = start; i < start+1024; i++) {
//            String b64 = encode(i);
//            System.out.println("0x" + Long.toHexString(i) + " " + b64 + " " + (decode(b64) == i));
//        }
//    }
}