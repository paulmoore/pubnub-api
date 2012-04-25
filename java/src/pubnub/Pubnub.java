package pubnub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * PubNub 3.0 Real-time Push Cloud API.
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
	protected final String CIPHER_KEY;
	protected final String ORIGIN;
	protected final boolean SSL;
	
	private PubnubCrypto crypto;
	
	/**
	 * PubNub 3.0.
	 * 
	 * Prepare PubNub Class State.
	 * 
	 * @param String Publish Key.
	 * @param String Subscribe Key.
	 * @param String Secret Key.
	 * @param boolean SSL Enabled.
	 */
	public Pubnub (String publish_key, String subscribe_key, String secret_key, String cipher_key, boolean ssl_on)
	{
		PUBLISH_KEY = publish_key;
		SUBSCRIBE_KEY = subscribe_key;
		SECRET_KEY = secret_key;
		CIPHER_KEY = cipher_key;
		SSL = ssl_on;
		
		if (CIPHER_KEY != null)
		{
			crypto = new PubnubCrypto(CIPHER_KEY);
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
	 * @param String Publish Key.
	 * @param String Subscribe Key.
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
	 * @param String Publish Key.
	 * @param String Subscribe Key.
	 * @param String Secret Key.
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
	 * @param String channel name.
	 * @param JSONObject message.
	 * @return boolean false on fail.
	 */
	public JSONArray publish (String channel, JSONObject message)
	{
		// Generate String to Sign.
		String signature = "0";
		
		// Encrypt the message if provided with a cipher key.
		message = encrypt(message);
		
		if (SECRET_KEY != null)
		{
			StringBuilder string_to_sign = new StringBuilder();
			string_to_sign.append(PUBLISH_KEY).append('/').append(SUBSCRIBE_KEY).append('/').append(SECRET_KEY).append('/').append(channel).append('/').append(message.toString());

			// Sign Message.
			signature = PubnubCrypto.getHMacSHA256(SECRET_KEY, string_to_sign.toString());
		}

		// Build URL.
		List<String> url = new LinkedList<String>();
		url.add("publish");
		url.add(PUBLISH_KEY);
		url.add(SUBSCRIBE_KEY);
		url.add(signature);
		url.add(channel);
		url.add("0");
		url.add(message.toString());

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
	 * @return The subscription object.
	 * @see pubnub.Subscription
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
	 * @param String channel name.
	 * @param int limit history count response.
	 * @return JSONArray of history.
	 */
	public JSONArray history (String channel, int limit)
	{
		List<String> url = new LinkedList<String>();

		url.add("history");
		url.add(SUBSCRIBE_KEY);
		url.add(channel);
		url.add("0");
		url.add(Integer.toString(limit));

		return decrypt(request(url));
	}

	/**
	 * Time.
	 * 
	 * Timestamp from PubNub Cloud.
	 * 
	 * @return double timestamp.
	 */
	public double time ()
	{
		List<String> url = new LinkedList<String>();

		url.add("time");
		url.add("0");

		JSONArray response = request(url);
		if (response == null)
		{
			return Double.NaN;
		}
		return response.optDouble(0);
	}

	/**
	 * UUID.
	 * 
	 * Generates a UUID from the Pubnub cloud.
	 * (Server-side)
	 * 
	 * @return The UUID object corresponding to the response.
	 */
	public UUID uuid ()
	{
		List<String> url = new LinkedList<String>();

		url.add("uuid");

		String s = SSL ? "s" : "";
		String origin = "http" + s + "://pubnub-prod.appspot.com";

		JSONArray response = request(origin, url);
		if (response == null)
		{
			return null;
		}
		try
		{
			return UUID.fromString(response.optString(0));
		}
		catch (IllegalArgumentException e)
		{
		}
		return null;
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
			return null;
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
		}
		
		return null;
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
			// Failed JSONP HTTP Request.
			return null;
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
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
		}
		
		// Failed JSON Parsing.
		return null;
	}

	protected JSONObject decrypt (JSONObject object)
	{
		if (CIPHER_KEY != null)
		{
			object = crypto.decrypt(object);
		}
		return object;
	}
	
	protected JSONArray decrypt (JSONArray array)
	{
		if (CIPHER_KEY != null)
		{
			array = crypto.decryptJSONArray(array);
		}
		return array;
	}
	
	protected JSONObject encrypt (JSONObject object)
	{
		if (CIPHER_KEY != null)
		{
			object = crypto.encrypt(object);
		}
		return object;
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
