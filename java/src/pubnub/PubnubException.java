package pubnub;

/**
 * A PubnubException is a simple wrapper around a RuntimeException. This can be
 * thrown by most operations when using a Pubnub object. It extends
 * RuntimeException to enable optional error handling and reduce boiler-plate
 * code.
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
public class PubnubException extends RuntimeException
{
	private static final long serialVersionUID = 4250132581198592948L;

	protected PubnubException (Exception e)
	{
		super(e);
	}

	protected PubnubException (String reason)
	{
		super(reason);
	}
}
