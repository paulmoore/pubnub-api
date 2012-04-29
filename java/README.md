### YOU MUST HAVE A PUBNUB ACCOUNT TO USE THE API.
### http://www.pubnub.com/account
----------------------------------------------------

## PubNub 3.0 Real-time Cloud Push API - JAVA

www.pubnub.com - PubNub Real-time Push Service in the Cloud. 
http://www.pubnub.com/tutorial/java-push-api

PubNub is a Massively Scalable Real-time Service for Web and Mobile Games.
This is a cloud-based service for broadcasting Real-time messages
to thousands of web and mobile clients simultaneously.

---
### API revamp by Paul Moore

The purpose of this refactoring is to modify the original Pubnub Java API to add features not present in the current API, or improve on them.
In addition, minor performance improvements were made.
Wherever possible, Java best practises are followed (as best to my knowledge) while at the same time trying to stick to the Pubnub API specifications and mandates.

The following features are of note:

1. The need for 3rd party libraries (other than the JSON library source) has been eliminated while maintaining the feature set.
2. HashMap arguments to API methods have been removed in favour for method overloading.
3. Subscriptions to channels are implemented as their own object, making unsubscribing, multithreading, and multiple channel management easier to control.
4. Encryption is now done on entire messages, instead of individual keys, and is better documented.
5. Whenever bytes are read, it is enforced that the character set be read as UTF-8.
6. Any Base64 encoding is done using the URL safe variant.
7. Improved error handling.
8. the uuid() method requests a UUID from the Pubnub cloud, with a fall-back to the local generation method.

## A Quick Note on Cryptography

I found that the documentation for the exact encryption specs used by the existing API (javaScript and Java) hard to follow.
So, I decided to rewrite it and document everything an implementer would need to know to interface with it at another end.

### The Algorithm

* The encryption scheme uses __128-bit AES with CBC and PKCS5 Padding__.
* When you init a Pubnub object, it now takes a byte array for the cipher key which is assumed to contain 16 bytes (your secret key).
* The Initialization Vector __(IV) is 16 bytes, all 0__.

### Encrypted Messages

When a cipher key is given to the Pubnub constructor, messages that are published with it will become encrypted.
When a message is encrypted, it will be transformed (using the above specs) from the JSONObject message into a JSONArray containing a single String value: The entire Base64 encoded value of the encrypted bytes.

For instance: say you published the message `{"test":"Hello World"}`...

1. The bytes are read from the String representation of the message.
2. Those bytes are then encrypted using the above algorithm and your secret key.
3. The output bytes are then encoded as URL safe Base64 (no padding, '-' and '_' are used for characters 62 and 63).
4. The resultant Base64 String is placed into a JSONArray, for example, it may look like this: `["eyJ0ZXN0IjoiSGVsbG8gV29ybGQifQ"]`.
5. That JSONArray is what is published.

When a subscriber to the channel receives that message...

1. The code retrieves the JSONArray (the final result of the above process).
2. The Base64 encoded String is read and decoded into bytes.
3. Those bytes are then decrypted using the above algorithm and your secret key.
4. The decrypted bytes are read into a String.
5. That String is parsed into the final result: the original message as a JSONObject.
6. That JSONObject is what is returned to the callback.

For each received message, if the message is a JSONObject then no decryption is done.  Decryption is only attempted if the message is a JSONArray and you have provided the Pubnub instance with a cipher key (since normal, unencrypted Pubnub messages are JSONObjects, not JSONArrays).

===============================================================================
## PubNub Java Client API Boiler Plate
===============================================================================

-------------------------------------------------------------------------------
### Java: (Init)
-------------------------------------------------------------------------------
```Java
    Pubnub pubnub = new Pubnub(
        "demo",  // PUBLISH_KEY
        "demo",  // SUBSCRIBE_KEY
        null,    // SECRET_KEY
        null,    // CIPHER_KEY
        false    // SSL_ON?
    );
```

-------------------------------------------------------------------------------
### Java: (Publish)
-------------------------------------------------------------------------------
```Java
    // Create JSON Message
    JSONObject message = new JSONObject();
    try { message.put( "some_key", "Hello World!" ); }
    catch (org.json.JSONException jsonError) {}

	// NOTE: You no longer need a hash map to accept arguments.
	// It is better form, faster, and has less boiler-plate code to just define the formal parameters.
	// This goes for all of the Pubnub API.
	    
    // Publish Message
    JSONArray info = pubnub.publish("hello_world", message); // Channel Name, JSON Message

    // Print Response from PubNub JSONP REST Service
    System.out.println(info);
```

-------------------------------------------------------------------------------
### Java: (Subscribe)
-------------------------------------------------------------------------------
```Java
    // Callback Interface when a Message is Received
    class Receiver implements Callback {
        public boolean execute(JSONObject message) {

            // Print Received Message
            System.out.println(message);

            // Continue Listening?
            return true;
        }
    }

    // Create a new Message Receiver
    Receiver message_receiver = new Receiver();
    
    // Listen for Messages (Subscribe)
    Subscription sub = pubnub.subscribe("hello_world", message_receiver); // Channel Name, Receiver Callback Class
    
    // A Subscription instance is returned, which does the actual heavy lifting.
    // To begin, call sub.run() (inherits Runnable).  This is blocking, and can be ran on the same thread or a different thread.
    // As an example, you could do: Thread t = new Thread(sub); t.start();
    
    // ... (in a different thread)
    sub.run(); // Messages will still be returned to the Callback instance.
    
    // Subscriptions can easily be cancelled.  Simply call sub.unsubscribe() from a non-blocked thread.
    
    // ... (in the original thread)
    sub.unsubscribe(); // Will terminate gracefully.
```

-------------------------------------------------------------------------------
### Java: (History)
-------------------------------------------------------------------------------
```Java
    // Get History
    JSONArray response = pubnub.history("hello_world", 10);

    // Print Response from PubNub JSONP REST Service
    System.out.println(response);
    System.out.println(response.optJSONObject(0).optString("some_key"));
```

-------------------------------------------------------------------------------
### Java: (Time)
-------------------------------------------------------------------------------
```Java
    // Get Time.
    double time = pubnub.time();
    
    // The response is either a double representing the Pubnub server time, or NaN if there was an error.
    System.our.println(time);
```

-------------------------------------------------------------------------------
### Java: (UUID)
-------------------------------------------------------------------------------
```Java
    // This has been changed from the existing implementation.  Now, the UUID is generated from the Pubnub servers instead of locally.
    // This is useful if you are using the API for client-side development, and want a trusted UUID source.
    // In addition, it also returns a UUID instance, instead of a String.
    
    // Get a UUID.
    UUID uuid = pubnub.uuid();
    
    // A unique id, generated from the server, or locally as a fall-back if there was an error.
    System.out.println(uuid);
```
