package pubnub;

import java.util.LinkedList;
import java.util.List;

/**
 * A simple, bulk-free Base64 encoder/decoder URL variant.
 * 
 * This implementation differs from standard Base64 in that it is <i>URL
 * safe</i>. That is, it uses '-' and '_' for characters 62 and 63 respectively.
 * In addition, it <b>does not pad</b> input or output.
 * 
 * PubNub Real-time Cloud-Hosted Push API and Push Notification Client
 * Frameworks Copyright (c) 2011 TopMambo Inc. http://www.pubnub.com/
 * http://www.pubnub.com/terms
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * PubNub Real-time Cloud-Hosted Push API and Push Notification Client
 * Frameworks Copyright (c) 2011 TopMambo Inc. http://www.pubnub.com/
 * http://www.pubnub.com/terms
 * 
 * @author Paul Moore
 */
public class Base64
{
	/** Encoding table. Used for <b>octet -> char</b> encoding. */
	private static final char[] ENCODABET = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_' };

	/** Decoding table. Used for <b>char -> octet</b> decoding. Offset to 45. */
	private static final byte[] DECODABET = { 62, 0, 0, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 0, 0, 0, 0, 63, 0, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51 };

	/**
	 * Encode a sequence of bytes as a Base64 String.
	 * 
	 * @param bytes The raw bytes to encode.
	 * @return A Base64 String representing the byte array.
	 */
	public static String encode (byte[] bytes)
	{
		// Estimation of size of output String.
		StringBuilder output = new StringBuilder(4 * (bytes.length / 3 + 1));

		// Go through each triplet of 3 bytes, which produce 4 octets.
		int i;
		for (i = 0; i < bytes.length - 2; i += 3)
		{
			int buffer = 0;
			
			// Fill the buffer with the bytes, producing a 24-bit integer.
			buffer |= (bytes[i] << 16) & 0xff0000;
			buffer |= (bytes[i + 1] << 8) & 0xff00;
			buffer |= bytes[i + 2] & 0xff;

			// Read out the 4 octets into the output buffer.
			output.append(ENCODABET[(buffer >>> 18) & 0x3f]);
			output.append(ENCODABET[(buffer >>> 12) & 0x3f]);
			output.append(ENCODABET[(buffer >>> 6) & 0x3f]);
			output.append(ENCODABET[buffer & 0x3f]);
		}

		// Special case 1: One byte extra, will produce 2 octets.
		if (bytes.length % 3 == 1)
		{
			int buffer = (bytes[i] << 16) & 0xff0000;

			output.append(ENCODABET[(buffer >>> 18) & 0x3f]);
			output.append(ENCODABET[(buffer >>> 12) & 0x3f]);
		}
		// Special case 2: Two bytes extra, will produce 3 octets.
		else if (bytes.length % 3 == 2)
		{
			int buffer = 0;

			buffer |= (bytes[i] << 16) & 0xff0000;
			buffer |= (bytes[i + 1] << 8) & 0xff00;

			output.append(ENCODABET[(buffer >>> 18) & 0x3f]);
			output.append(ENCODABET[(buffer >>> 12) & 0x3f]);
			output.append(ENCODABET[(buffer >>> 6) & 0x3f]);
		}

		return output.toString();
	}

	/**
	 * Decode a Base64 String into a byte array.
	 * 
	 * @param raw The raw Base64 String to decode.
	 * @return The decoded byte array.
	 */
	public static byte[] decode (String raw)
	{
		List<Byte> output = new LinkedList<Byte>();

		// Go through each group of 4 octets to obtain 3 bytes.
		int i;
		for (i = 0; i < raw.length() - 3; i += 4)
		{
			int buffer = 0;

			// Read the 4 octets into the buffer, producing a 24-bit integer.
			buffer |= DECODABET[raw.charAt(i) - 45] << 18;
			buffer |= DECODABET[raw.charAt(i + 1) - 45] << 12;
			buffer |= DECODABET[raw.charAt(i + 2) - 45] << 6;
			buffer |= DECODABET[raw.charAt(i + 3) - 45];

			// Append the 3 re-constructed bytes into the output buffer.
			output.add((byte) ((buffer >>> 16) & 0xff));
			output.add((byte) ((buffer >>> 8) & 0xff));
			output.add((byte) (buffer & 0xff));
		}

		// Special case 1: Only 2 octets remain, producing 1 byte.
		if (raw.length() % 4 == 2)
		{
			int buffer = 0;

			buffer |= DECODABET[raw.charAt(i) - 45] << 18;
			buffer |= DECODABET[raw.charAt(i + 1) - 45] << 12;

			output.add((byte) ((buffer >>> 16) & 0xff));
		}
		// Special case 2: Only 3 octets remain, producing 2 bytes.
		else if (raw.length() % 4 == 3)
		{
			int buffer = 0;

			buffer |= DECODABET[raw.charAt(i) - 45] << 18;
			buffer |= DECODABET[raw.charAt(i + 1) - 45] << 12;
			buffer |= DECODABET[raw.charAt(i + 2) - 45] << 6;

			output.add((byte) ((buffer >>> 16) & 0xff));
			output.add((byte) ((buffer >>> 8) & 0xff));
		}

		// Construct the byte array from the output buffer.
		byte[] bytes = new byte[output.size()];
		i = 0;
		for (byte b : output)
		{
			bytes[i++] = b;
		}

		return bytes;
	}
}
