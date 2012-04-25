package pubnub;

import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A Subscription object is a concrete realization of a connection
 * to a Pubnub channel.  This class implements <code>Runnable</code>
 * for easy integration into the threading model.
 * 
 * The main purpose of this class is to better support multiple,
 * Interruptible subscriptions by treating each connection (subscription)
 * as a separate object.  Also, no additional external libraries are required.
 * 
 * It should be noted that the creation of a subscription object does not
 * start listening for messages, <code>run()</code> must first be invoked.
 * However, this method is blocking, and so it is up to the user to handle any
 * multithreading if necessary.
 * 
 * At any time, <code>unsubscribe()</code> can be called to cancel the subscription
 * (if it is active).
 * 
 * @author Paul Moore
 */
public class Subscription implements Runnable
{
	private final Pubnub pubnub;
	private final Callback callback;
	private final String channel;

	private URLConnection conn;
	private boolean shouldStop = false;

	/**
	 * Creates a subscription object to a given channel and Pubnub account.
	 * 
	 * @param pubnub The Pubnub object this subscription belongs to.
	 * @param callback The callback to receive this subscription's messages.
	 * @param channel The Pubnub channel.
	 */
	protected Subscription (Pubnub pubnub, Callback callback, String channel)
	{
		this.pubnub = pubnub;
		this.callback = callback;
		this.channel = channel;
	}

	/**
	 * If this subscription is active, the subscription will terminate immediately,
	 * returning from the call to <code>run</code> in the thread it was invoked.  Or,
	 * it will terminate after the currently received messages are being processed.
	 * 
	 * If the subscription is not active, this method will prevent the subscription
	 * from occurring.
	 */
	public synchronized void unsubscribe ()
	{
		shouldStop = true;
		if (conn != null)
		{
			((HttpURLConnection) conn).disconnect();
			conn = null;
		}
	}
	
	/**
	 * Begins the subscription process.  This method is blocking.
	 * To terminate, <code>unsubscribe()</code> must be called in
	 * a separate thread, or the <code>callback</code> must return
	 * <code>false</code> the next time a message is received.
	 * 
	 * This method can and should only be invoked once.  If it is called
	 * more than once, nothing will happen, the subscription can
	 * only be used a single time.  To resubscribe, use <code>Pubnub.subscribe()</code>
	 * to obtain a new Subscription object.
	 */
	@Override
	public void run ()
	{
		String timetoken = "0";
		
		// Loop while the subscription has not been stopped.
		while (true)
		{
			synchronized (this)
			{
				// Caused by a call to unsubscribe().
				if (shouldStop)
				{
					return;
				}
				// Prepare the connection.
				conn = pubnub.prepareConnection(Arrays.asList("subscribe", pubnub.SUBSCRIBE_KEY, channel, "0", timetoken));
				// Problem opening connection.
				if (conn == null)
				{
					return;
				}
			}
			
			// Wait for Message.
			JSONArray response = pubnub.request(conn);
			// Connection failed or user called unsubscribe().
			if (response == null)
			{
				// Wait 1 second then try again.
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException ignored)
				{
				}

				continue;
			}
			JSONArray messages = response.optJSONArray(0);

			// Update TimeToken.
			if (!response.optString(1).isEmpty())
			{
				timetoken = response.optString(1);
			}

			// Run user Callback and Reconnect if user permits.
			for (int i = 0; messages.length() > i; i++)
			{
				JSONObject message = pubnub.decrypt(messages.optJSONObject(i));
				// The subscription can also be cancelled by the callback.
				if (!callback.execute(message))
				{
					return;
				}
			}
		}
	}
	
	@Override
	protected void finalize () throws Throwable
	{
		unsubscribe();
		super.finalize();
	}
}
