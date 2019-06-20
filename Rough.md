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
	// 8 June 3 AM : (major)Bug free app ready!

// No need now. > Update webscraper from their recent commits (and remove run2 dependency )

MiniDOs 
	// > Pause toast
	// > Camera higher res
	// > Remove plate number on back press/drawer hide
	/Check:/ > Icons resizing bug fix
	//> Splash screen
		> Simple Button Guide on splashscreen
		> A guiding photo showing how to hold at optimal distance of plate
	// > Titles into drawer..: "Confirm Details", "RTO Website: ",
	// Starting popup "Made with <3 by <link>Udayraj and <link>Tanesh "
		"Feedback Credits <insta>Shashank, Tanvesh, VCD..."
	Show Confetti <3 pop up on successful detection and result retrieval
	//"<git>See Source", 
	//	"Share App"
	> GPLv3, Developer signatures in code
	//> App Icon, 
	// Not that good -  Cam wheel icon
	// > Install Name Different
	> [On Throttle] Rebase and push to https://github.com/Udayraj123/VehicleInfoOCR
		> Keep sharmatanesh to latest..
	// Done, but would it lag? >Icon resizing: all converted to linearlayouts

[ ] Load captcha when splash screen starts

Fix: 
2019-06-09 00:01:13.542 3728-3748/com.example.ocr I/Vision: Loading library libocr.so
2019-06-09 00:01:13.543 3728-3748/com.example.ocr I/Vision: libocr.so library load status: false
2019-06-09 00:01:13.625 3728-3747/com.example.ocr W/GooglePlayServicesUtil: Google Play services out of date.  Requires 11925000 but found 11743470
app is not indexable error in manifest one Activity


> NOW THROTTLE THE APP TO MINIMAL. THEN ADD MASS
	^^ FIND A GUIDE ONLINE?! IF NOT, MAKE ONE LATER?!
	// Nope, that's crash-prone. > No threading?!
	// > OCR - frame throttle with a probability
	// > In app size : put some jars and resources : Gallery easter egg	
	// In captcha solver: > Thread.sleep() / handler.postDelayed()


Guided Interface (more into memo):
	> Show numplate input all the time at bottom (Like google lens!)
	> Camera focus area(same as QR)
	//> Feedback on obtaining valid numplate
	//:Guide in splash > Tell thru design to open drawer AFTER capture
	// textview > Tell to verify captcha

Work on JS to improve interface.


*** SEEMS CAPTCHA WASN'T NEEDED AFTERALL, FILL CAPTCHA ONCE, THEN IT DOESN'T RELOAD (Only in desktop browser rn)
>>^Look into this for later version now

//  In fruit ninja menu :- Nope, Bubble overlay is better
	Swipe to select area
	search button hides the swiping gui

// Turbo mode - like QR scanner (GET MORE CLARIFICATIONS)
	// Found better one. > Bubble overlay: Use long press/double tap to copy block
	// Nope, zoom levels! > Min Area constraint, more constraints?!
	// Keep as overlay(no draw animation). > Keep or remove drawer?!
	>> Captcha retryer
	//> Instant try to get vehicle info whenever valid number plate found
	// Nope, pop a button rather> Open confirmation drawer immediately if details found. otherwise wait for better input

Further
	< Bubble pop focus
	< Zooming : https://github.com/googlesamples/android-vision/blob/master/visionSamples/ocr-reader/app/src/main/java/com/google/android/gms/samples/vision/ocrreader/ui/camera/CameraSource.java
	< Static loading : https://stackoverflow.com/questions/11085271/loading-static-html-to-webview/11088345#11088345
	<**Improve OCR on two rows numplates**
	< Load vehicle image from Vehicle model (google images)
	< Threaded loading on numplate detected bubble. Later put into AR

Making of your android starter template
> incl Rotation handler : https://android.jlelse.eu/handling-orientation-changes-in-android-7072958c442a

Optimizations
	> Lower camera preview/frame size!! 
	> Modularize the MainActivity
		> More app events
	> Disable drawableCache in the webscraper.
	// > FOUND THIS (Add'em): bitmap.recycle();//recycle the source bitmap, this will be no longer used.

	Fix this for better startup time: 
	starting instant run server: is main process
	2019-06-09 11:53:29.333 11759-11759/com.example.ocr I/zygote: Rejecting re-init on previously-failed class java.lang.Class<android.support.v4.view.ViewCompat$OnUnhandledKeyEventListenerWrapper>: java.lang.NoClassDefFoundError: Failed resolution of: Landroid/view/View$OnUnhandledKeyEventListener;
	2019-06-09 11:53:29.333 11759-11759/com.example.ocr I/zygote:     at void android.support.v4.view.ViewCompat.setOnApplyWindowInsetsListener(android.view.View, android.support.v4.view.OnApplyWindowInsetsListener) (ViewCompat.java:2203)
	2019-06-09 11:53:29.333 11759-11759/com.example.ocr I/zygote:     

Later:
	Run Apk analyzer on OMR apk
	Make Starter Android template.
	Make minimal size android apk (note size and startup time, compare with studio's hello world size)
	Firebase usage analytics (Have to see how many ppl opened the app[Privacy Policy?!])

Note numplate pattern:
https://en.wikipedia.org/wiki/Vehicle_registration_plates_of_India#Current_format
    The first two letters indicate the state or Union Territory to which the vehicle is registered.
    The next two digit numbers are the sequential number of a district. Due to heavy volume of vehicle registration, the numbers were given to the RTO offices of registration as well.
    The third part consists of one ,two or three letters. This shows the ongoing series of an RTO (Also as a counter of the number of vehicles registered) and/or vehicle classification
    The fourth part is a 4 digit number unique to each plate. A letter is prefixed when the 4 digit number runs out and then two letters and so on.
    The fifth part is an international oval "IND" and the above it a hologram having a Chakra. However, not all plates have these features.

    <!-- FOUND OUT : main thread, also known as the UI thread, -->

