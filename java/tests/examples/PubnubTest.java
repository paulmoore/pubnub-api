package examples;

import java.util.Iterator;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import pubnub.Base64;
import pubnub.Callback;
import pubnub.Pubnub;
import pubnub.PubnubException;
import pubnub.Subscription;

/**
 * Pubnub Tests.
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
 */
class PubnubTest
{
	/** AES cipher key. Replace with your own 128-bit secret key. */
	private static final byte[] crypto_key = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

	private static String channel;

	public static void main (String args[])
	{
		// Get a random channel for testing.
		channel = "hello_world" + UUID.randomUUID();

		String s = Base64.encode("{\"test\":\"Hello World\"}".getBytes());
		System.out.println(s);
		System.out.println(new String(Base64.decode(s)));
		/*
		PubnubTest.test_uuid();
		PubnubTest.test_time();
		PubnubTest.test_publish();
		PubnubTest.test_subscribe();
		PubnubTest.test_history();*/
	}

	public static void test_uuid ()
	{
		// UUID Test
		System.out.println("\nTESTING UUID:");
		Pubnub pubnub = new Pubnub("demo", "demo", "demo", crypto_key, true);
		System.out.println(pubnub.uuid());
	}

	public static void test_time ()
	{
		// Time Test
		System.out.println("\nTESTING TIME:");

		// Create Pubnub Object
		Pubnub pubnub = new Pubnub("demo", "demo", "demo", crypto_key, true);

		System.out.println(pubnub.time());
	}

	public static void test_publish ()
	{
		// Publish Test
		System.out.println("\nTESTING PUBLISH:");

		// Create Pubnub Object
		Pubnub pubnub = new Pubnub("demo", "demo", "demo", crypto_key, true);

		// Create JSON Message
		JSONObject message = new JSONObject();
		try
		{
			message.put("some_val", "Hello World! --> ɂ顶@#$%^&*()!");
		}
		catch (org.json.JSONException jsonError)
		{
			jsonError.printStackTrace();
		}

		// Publish Message
		JSONArray info = pubnub.publish(channel, message);

		// Print Response from PubNub JSONP REST Service
		System.out.println(info);
	}

	public static void test_subscribe ()
	{
		// Subscribe Test
		System.out.println("\nTESTING SUBSCRIBE:");

		Pubnub pubnub = new Pubnub("demo", "demo", "demo", crypto_key, true);

		// Callback Interface when a Message is Received
		class Receiver implements Callback
		{
			public boolean execute (JSONObject message)
			{

				// Print Received Message
				System.out.println("Received message:");
				System.out.println(message);
				try
				{
					Iterator<?> keys = message.keys();
					while (keys.hasNext())
					{
						System.out.print(message.get(keys.next().toString()) + " ");
					}
					System.out.println();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				// Continue Listening?
				return false;
			}
		}

		// Create a new Message Receiver
		Receiver message_receiver = new Receiver();

		// Listen for Messages (Subscribe)
		Subscription sub = pubnub.subscribe(channel, message_receiver);

		Thread t = new Thread(sub);
		t.start();

		try
		{
			// Create JSON Message
			JSONObject message = new JSONObject();
			try
			{
				message.put("some_val", "Hello World! --> ɂ顶@#$%^&*()!");
			}
			catch (org.json.JSONException jsonError)
			{
				jsonError.printStackTrace();
			}
			System.out.println("Published message:");
			System.out.println(message);

			pubnub.publish(channel, message);

			// Just in case the publish did not go through, make sure we
			// unsubscribe.
			Thread.sleep(5000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		catch (PubnubException e)
		{
			e.printStackTrace();
		}
		finally
		{
			sub.unsubscribe();
		}
	}

	public static void test_history ()
	{
		// History Test
		System.out.println("\nTESTING HISTORY:");

		// Create Pubnub Object
		Pubnub pubnub = new Pubnub("demo", "demo", "demo", crypto_key, true);

		// Get History
		JSONArray response = pubnub.history(channel, 1);

		// Print Response from PubNub JSONP REST Service
		System.out.println(response);
	}
}
