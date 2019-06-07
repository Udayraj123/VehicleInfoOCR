//well, FirebaseVisionText has these exacty  - store coords : map of (coords, block's text) pairs
	text.getText(), RectF rect = new RectF(text.getBoundingBox()); rect.left
//Nope, seems to crash coz of high rate?!-  First display the candidate text immediately
// show concatenated filtered text into vehicle number
POST ISSUES ONLINE-
//jsoup
//(solved) firebase

// TUNE OPENCV PARAMS
//Add drawer and clean the top boxes
//back button confirm 
	-> **make it into boilerplate along with simple permissions handler**

//>Debug bitmap reading on firebase	=> It was synchronization issue, isProcessing isn't good as it waits on main thread. But why'd it not run the task ever?!

// Nope, now can actually remove it for new url! - Improve captcha image processing

// Fix Fetching 
	// >> Get the ultimate tool : background Webview automated using simple js.
		https://github.com/daandtu/android-web-scraper/
	//	>Show layout webview in drawer until captcha is downloaded/ form is submitted

//** The new url doesn't need much IP! opencv no more needed :)
// EVEN BETTER IP -
	//> use only thresholding and a kernel to patch gray or white acc to surrounding

// Fix some crashes
	//> On clicking flash on pause
	 > Sometimes hangs on start?! : 
		//Yeah, On internet unavailable.
		//Nope, it isn't fired for JS, Listener needed to be reset
// > Add some js to crop view instead of scraping vehicle details?! <- more versatile
 	^^> This is actually better for demo - should look like google assistant

//**No need to reload captcha on pause preview now. (THINK! : the website does it!)**

//Blurkit/Blurry- Nope, Renderscript adds 2MB and isn't that good

> INJECT ALL SINGLE JS AT START. MAKE USE OF THE JSInterface
	//MutationObserver to reorder table.

Fix some bugs
	//> Proper threaded listeners for majorText update
	//> Proper threaded listeners for captcha handling
		^Is buggy at times to fill
	// > ^THREAD IT! : Convert those listeners into async tasks (As app hangs on slow internet; But webview loadUrl is already async ?!) 
	//>> FOUND IT!! // THIS WAS NOT IN A THREAD!!
                  eltCaptchaImage.callImageBitmapGetter(captchaBitmapGetter);		
	// That Syntax error was here:
		"a = (a+this[start+i])%65521; b = (b+a)%65521; "
		^^ Modulo was giving issue. replaced with i - parseInt(i/65521);

// No need now. > Update webscraper from their recent commits (and remove run2 dependency )

Work on JS to improve interface.


Guided Interface (more into memo):
	> Show numplate input all the time at bottom?!
	> Camera focus area
	> A photo showing approx optimal distance of plate
	//> Feedback on obtaining valid numplate
	> Tell thru design to open drawer AFTER capture
	> Tell to verify captcha
	Show <3 pop up on successful detection and result retrieval


*** SEEMS CAPTCHA WASN'T NEEDED AFTERALL, FILL CAPTCHA ONCE, THEN IT DOESN'T RELOAD (Only in desktop browser rn)
>>^Look into this for later version now


//  In fruit ninja menu :- Nope, Bubble overlay is better
	Swipe to select area
	search button hides the swiping gui
Turbo mode - like QR scanner (GET MORE CLARIFICATIONS)
	> Bubble overlay: Use long press/double tap to copy block
	> Min Area constraint, more constraints?!
	> Keep or remove drawer?!
	>> Captcha retryer
	> Instant try to get vehicle info whenever valid number plate found
	> Open confirmation drawer immediately if details found.
	> otherwise wait for better input

**Improve OCR on two rows numplates**

Optimizations
	// > Lower camera preview/frame size!! 
	> Modularize the MainActivity
	> Disable drawableCache in the webscraper.
	// > FOUND THIS (Add'em): bitmap.recycle();//recycle the source bitmap, this will be no longer used.

TRY THE THROTTLING BEFORE UPDATES
	^^ FIND A GUIDE ONLINE?!
	> No threading?!
	> Thread.sleep() / handler.postDelayed()
	> OCR - frame throttle with a probability
	> In app size : put some jars and resources
	> Bubble pop focus
	> Zooming

Excess:
	Load vehicle image from Vehicle model (google images)
	Timeout on numplate detected bubble

Later:
	Run Apk analyzer on OMR apk
	Firebase usage analytics (Have to see how many ppl opened the app[Privacy Policy?!])


Note numplate pattern:
https://en.wikipedia.org/wiki/Vehicle_registration_plates_of_India#Current_format
    The first two letters indicate the state or Union Territory to which the vehicle is registered.
    The next two digit numbers are the sequential number of a district. Due to heavy volume of vehicle registration, the numbers were given to the RTO offices of registration as well.
    The third part consists of one ,two or three letters. This shows the ongoing series of an RTO (Also as a counter of the number of vehicles registered) and/or vehicle classification
    The fourth part is a 4 digit number unique to each plate. A letter is prefixed when the 4 digit number runs out and then two letters and so on.
    The fifth part is an international oval "IND" and the above it a hologram having a Chakra. However, not all plates have these features.