package pubnub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * PubNub 3.0 Real-time Push Cloud API.
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
 * @author Stephen Blum
 * @author Paul Moore
 * @package pubnub
 */
public class Pubnub
{
	/** Default origin. */
	public static final String CLOUD_ORIGIN = "pubsub.pubnub.com";
	/** Size limit (# of characters) of a JSON message. */
	public static final int LIMIT = 1800;

	protected final String PUBLISH_KEY;
	protected final String SUBSCRIBE_KEY;
	protected final String SECRET_KEY;
	protected final byte[] CIPHER_KEY;
	protected final String ORIGIN;
	protected final boolean SSL;

	/** Secret key generated from the CIPHER_KEY. */
	private SecretKeySpec ckeySpec, skeySpec;
	/** Encryption padding, 16 bytes of 0. */
	private IvParameterSpec ivSpec;

	/**
	 * PubNub 3.0.
	 * 
	 * Prepare PubNub Class State.
	 * 
	 * @param publish_key Your Pubnub Publish key.
	 * @param subscribe_key Your Pubnub Subscribe key.
	 * @param secret_key Your Pubnub Secret key.
	 * @param cipher_key Your own 128-bit AES encryption key.
	 * @param ssl_on SSL Enabled.
	 */
	public Pubnub (String publish_key, String subscribe_key, String secret_key, byte[] cipher_key, boolean ssl_on)
	{
		PUBLISH_KEY = publish_key;
		SUBSCRIBE_KEY = subscribe_key;
		SECRET_KEY = secret_key;
		CIPHER_KEY = cipher_key;
		SSL = ssl_on;

		// Need to create the necessary objects for the digest algorithms
		if (SECRET_KEY != null)
		{
			try
			{
				skeySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "HmacSHA256");
			}
			catch (UnsupportedEncodingException e)
			{
				throw new PubnubException(e);
			}
		}

		// Create necessary objects to perform encryption/decryption if provided
		// with a cipher key.
		if (CIPHER_KEY != null)
		{
			ckeySpec = new SecretKeySpec(CIPHER_KEY, "AES");
			// Initialization Vector is 0 for simplicity.
			byte[] iv = new byte[16];
			ivSpec = new IvParameterSpec(iv);
		}

		// SSL On?
		if (SSL)
		{
			ORIGIN = "https://" + CLOUD_ORIGIN;
		}
		else
		{
			ORIGIN = "http://" + CLOUD_ORIGIN;
		}
	}

	/**
	 * PubNub 2.0 Compatibility.
	 * 
	 * Prepare PubNub Class State.
	 * 
	 * @param publish_key Your Pubnub Publish key.
	 * @param subscribe_key Your Pubnub Subscribe key.
	 */
	public Pubnub (String publish_key, String subscribe_key)
	{
		this(publish_key, subscribe_key, null, null, false);
	}

	/**
	 * PubNub 3.0 without SSL.
	 * 
	 * Prepare PubNub Class State.
	 * 
	 * @param publish_key Your Pubnub Publish key.
	 * @param subscribe_key Your Pubnub Subscribe key.
	 * @param secret_key Your Pubnub Secret key.
	 */
	public Pubnub (String publish_key, String subscribe_key, String secret_key)
	{
		this(publish_key, subscribe_key, secret_key, null, false);
	}

	/**
	 * Publish.
	 * 
	 * Send a message to a channel.
	 * 
	 * @param channel The name of the channel to publish to.
	 * @param message The message to publish.
	 * @return The response array.
	 */
	public JSONArray publish (String channel, JSONObject message)
	{
		// Generate String to Sign.
		String signature = "0";

		// Encrypt the message if provided with a cipher key.
		String msgString = encrypt(message);

		if (SECRET_KEY != null)
		{
			StringBuilder string_to_sign = new StringBuilder();
			string_to_sign.append(PUBLISH_KEY).append('/').append(SUBSCRIBE_KEY).append('/').append(SECRET_KEY).append('/').append(channel).append('/').append(msgString);

			// Sign the message.
			try
			{
				Mac sha256_HMAC = Mac.getInstance("HMACSHA256");
				sha256_HMAC.init(skeySpec);
				byte[] mac_data = sha256_HMAC.doFinal(string_to_sign.toString().getBytes("UTF-8"));

				BigInteger number = new BigInteger(1, mac_data);
				signature = number.toString(16);
			}
			catch (UnsupportedEncodingException e)
			{
				throw new PubnubException(e);
			}
			catch (GeneralSecurityException e)
			{
				throw new PubnubException(e);
			}
		}

		// Build URL.
		List<String> url = new LinkedList<String>();
		url.add("publish");
		url.add(PUBLISH_KEY);
		url.add(SUBSCRIBE_KEY);
		url.add(signature);
		url.add(channel);
		url.add("0");
		url.add(msgString);

		// Return JSONArray.
		return request(url);
	}

	/**
	 * Subscribe.
	 * 
	 * Create a subscription object to a specified channel.
	 * 
	 * @param channel The channel to subscribe to.
	 * @param callback The callback object to receive the messages.
	 * @return The Subscription object.
	 * @see Subscription
	 */
	public Subscription subscribe (String channel, Callback callback)
	{
		return new Subscription(this, callback, channel);
	}

	/**
	 * History.
	 * 
	 * Load history from a channel.
	 * 
	 * @param channel The channel to get history of.
	 * @param limit The limit to the number of messages to receive.
	 * @return An array of messages.
	 */
	public JSONArray history (String channel, int limit)
	{
		List<String> url = new LinkedList<String>();

		url.add("history");
		url.add(SUBSCRIBE_KEY);
		url.add(channel);
		url.add("0");
		url.add(Integer.toString(limit));

		JSONArray response = request(url);
		try
		{
			// Go through the array of returned messages.
			for (int i = 0; i < response.length(); i++)
			{
				JSONObject message = response.optJSONObject(i);
				// Message Null? Must be encrypted.
				if (message == null)
				{
					message = new JSONObject(decrypt(response.optString(i)));
					// Put the unencrypted object back into the array.
					response.put(i, message);
				}
			}
		}
		catch (JSONException e)
		{
			throw new PubnubException(e);
		}

		return response;
	}

	/**
	 * Time.
	 * 
	 * Timestamp from PubNub Cloud.
	 * 
	 * @return A double representing the Pubnub server time, or Double.NaN if
	 *         there was an error.
	 */
	public double time ()
	{
		List<String> url = new LinkedList<String>();

		url.add("time");
		url.add("0");

		try
		{
			JSONArray response = request(url);
			return response.optDouble(0, Double.NaN);
		}
		catch (PubnubException e)
		{
		}

		return Double.NaN;
	}

	/**
	 * UUID.
	 * 
	 * Generates a UUID from the Pubnub cloud. (Server-side). If there was an
	 * error in requesting a UUID, this method will fall-back to the local
	 * creation of a random UUID object.
	 * 
	 * @return The UUID object corresponding to the response.
	 * @see UUID
	 */
	public UUID uuid ()
	{
		List<String> url = new LinkedList<String>();

		url.add("uuid");

		String s = SSL ? "s" : "";
		String origin = "http" + s + "://pubnub-prod.appspot.com";

		try
		{
			JSONArray response = request(origin, url);
			return UUID.fromString(response.optString(0));
		}
		catch (IllegalArgumentException e)
		{
		}
		catch (PubnubException e)
		{
		}

		return UUID.randomUUID();
	}

	protected URLConnection prepareConnection (Iterable<String> url_components)
	{
		return prepareConnection(ORIGIN, url_components);
	}

	protected URLConnection prepareConnection (String origin, Iterable<String> url_components)
	{
		StringBuilder o = new StringBuilder();

		o.append(origin);

		// Generate URL with UTF-8 Encoding.
		for (String url_bit : url_components)
		{
			o.append("/").append(encodeURIcomponent(url_bit));
		}

		// Fail if string too long.
		if (o.length() > LIMIT)
		{
			throw new PubnubException("Message to long: " + o.length() + " when limit is: " + LIMIT);
		}

		try
		{
			URL url = new URL(o.toString());
			URLConnection conn = url.openConnection();
			// Don't timeout, we will do this manually.
			conn.setConnectTimeout(0);
			conn.setReadTimeout(0);
			return conn;
		}
		catch (IOException e)
		{
			throw new PubnubException(e);
		}
	}

	protected JSONArray request (Iterable<String> url_components)
	{
		return request(prepareConnection(ORIGIN, url_components));
	}

	protected JSONArray request (String origin, Iterable<String> url_components)
	{
		return request(prepareConnection(origin, url_components));
	}

	protected JSONArray request (URLConnection conn)
	{
		// Needed to catch an 'expected' race condition when a Subscription is
		// using this method.
		if (conn == null)
		{
			return null;
		}

		BufferedReader reader = null;
		StringBuilder o = new StringBuilder();

		try
		{
			// Create the reader, will I/O block.
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			// Read JSON Message.
			String line;
			while ((line = reader.readLine()) != null)
			{
				o.append(line);
			}
		}
		catch (IOException e)
		{
			throw new PubnubException(e);
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException ignored)
				{
				}
			}
		}

		// Parse JSON String.
		try
		{
			return new JSONArray(o.toString());
		}
		catch (JSONException e)
		{
			throw new PubnubException(e);
		}
	}

	protected String decrypt (String raw)
	{
		if (CIPHER_KEY != null)
		{
			try
			{
				Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
				cipher.init(Cipher.DECRYPT_MODE, ckeySpec, ivSpec);

				JSONArray array = new JSONArray(raw);
				String encoded = array.optString(0);

				byte[] encrypted = Base64.decode(encoded);
				byte[] decrypted = cipher.doFinal(encrypted);

				String message = new String(decrypted, "UTF-8");
				return message;
			}
			catch (GeneralSecurityException e)
			{
				throw new PubnubException(e);
			}
			catch (UnsupportedEncodingException e)
			{
				throw new PubnubException(e);
			}
			catch (JSONException e)
			{
				throw new PubnubException(e);
			}
		}

		return raw;
	}

	protected String encrypt (JSONObject message)
	{
		String raw = message.toString();
		if (CIPHER_KEY != null)
		{
			try
			{
				Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
				cipher.init(Cipher.ENCRYPT_MODE, ckeySpec, ivSpec);

				byte[] bytes = raw.getBytes("UTF-8");
				byte[] encrypted = cipher.doFinal(bytes);

				String encoded = Base64.encode(encrypted);
				JSONArray array = new JSONArray();
				array.put(encoded);
				return array.toString();
			}
			catch (GeneralSecurityException e)
			{
				throw new PubnubException(e);
			}
			catch (UnsupportedEncodingException e)
			{
				throw new PubnubException(e);
			}
		}
		return raw;
	}

	private String encodeURIcomponent (String s)
	{
		StringBuilder o = new StringBuilder();
		for (char ch : s.toCharArray())
		{
			if (isUnsafe(ch))
			{
				o.append('%');
				o.append(toHex(ch / 16));
				o.append(toHex(ch % 16));
			}
			else
			{
				o.append(ch);
			}
		}
		return o.toString();
	}

	private char toHex (int ch)
	{
		return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
	}

	private boolean isUnsafe (char ch)
	{
		return " ~`!@#$%^&*()+=[]\\{}|;':\",./<>?".indexOf(ch) >= 0;
	}
}
