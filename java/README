## ---------------------------------------------------
##
## YOU MUST HAVE A PUBNUB ACCOUNT TO USE THE API.
## http://www.pubnub.com/account
##
## ----------------------------------------------------

## ------------------------------------------
## PubNub 3.0 Real-time Cloud Push API - JAVA
## ------------------------------------------
##
## www.pubnub.com - PubNub Real-time Push Service in the Cloud. 
## http://www.pubnub.com/tutorial/java-push-api
##
## PubNub is a Massively Scalable Real-time Service for Web and Mobile Games.
## This is a cloud-based service for broadcasting Real-time messages
## to thousands of web and mobile clients simultaneously.

---
## API revamp by Paul Moore

The purpose of this refactoring is to modify the original Pubnub Java API to add features not present in the current API, or improve on them.
In addition, minor performance improvements were made.
Wherever possible, Java best practises are followed (as best to my knowledge) while at the same time trying to stick to the Pubnub API specifications and mandates.

===============================================================================
###PubNub Java Client API Boiler Plate
===============================================================================

-------------------------------------------------------------------------------
###JavaScript: (Subscribe)
-------------------------------------------------------------------------------
```JavaScript
    PUBNUB.subscribe( { channel : "hello_world" } , function(message) {
        console.log(JSON.stringify(message));
        alert(JSON.stringify(message));
    } );
```

-------------------------------------------------------------------------------
###Java: (Init)
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
###Java: (Publish)
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
###Java: (Subscribe)
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
    
    // ... (in a different thread)
    sub.run(); // Messages will still be returned to the Callback instance.
    
    // Subscriptions can easily be cancelled.  Simply call sub.unsubscribe() from a non-blocked thread.
    
    // ... (in the original thread)
    sub.unsubscribe(); // Will terminate gracefully.
```

-------------------------------------------------------------------------------
###Java: (History)
-------------------------------------------------------------------------------
```Java
    // Get History
    JSONArray response = pubnub.history("hello_world", 10);

    // Print Response from PubNub JSONP REST Service
    System.out.println(response);
    System.out.println(response.optJSONObject(0).optString("some_key"));
```

-------------------------------------------------------------------------------
###Java: (Time)
-------------------------------------------------------------------------------
```Java
    // Get Time.
    double time = pubnub.time();
    
    // The response is either a double representing the Pubnub server time, or NaN if there was an error.
    System.our.println(time);
```

-------------------------------------------------------------------------------
###Java: (UUID)
-------------------------------------------------------------------------------
```Java
    // This has been changed from the existing implementation.  Now, the UUID is generated from the Pubnub servers instead of locally.
    // This is useful if you are using the API for client-side development, and want a trusted UUID source.
    // In addition, it also returns a UUID instance, instead of a String.
    
    // Get a UUID.
    UUID uuid = pubnub.uuid();
    
    // A unique id, or null if there was an error.
    System.out.println(uuid);
```

