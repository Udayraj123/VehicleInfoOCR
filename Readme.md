## Vehicle Info Camera 
An app made using Firebase MLKit Text Recognizer, JSoup and a little bit of opencv.
Useful for general purpose OCR as well.

## WIP
#### TODO (Details in Rough.md)
	[X] Use Firebase OCR
	[X] Add Permission handler
	[X] Add OpenCv morph
	[X] now fix jsoup to download captcha and show in image preview 
	[X] Find CV parameters on sample from pc
	[X] getting majorText from the bounding boxes
	[X] Putting numberplate data into text on preview btn click
	[X] Flash btn from OMR
	[X] Add Bottom Drawer 
	[X] Floating number plates
	[ ] Instruction visuals
	[ ] Blurring effect
	[ ] Focus guider
	[ ] Improve Webview display using js
	[ ] Celebrate Konfetti popper on succesful result!

#### Excess

 	Textbox style just like numplate

 	Test Imgproc preprocess onto cameraSource bytebuffer frames

#### Much Excess

	[X] Remove opencv, process only bitmap

	Recent searches into cards! - make use of miniRTO here too

## Size Analysis(TODO : Add screenshot)
jar libs : 7MB
resources : 5MB
android-web-scraper : 2 MB
FirebaseMLKit : 2 MB

## Credits
Vahan portals

	https://parivahan.gov.in/rcdlstatus/

	https://vahan.nic.in/nrservices/faces/user/searchstatus.xhtml

App workflow inspired from miniRTO app: 
https://github.com/chandruscm/miniRTO 

Firebase MLKit, and This blog : https://medium.com/digital-curry/
firebase-mlkit-textdetection-in-android-using-firebase-ml-vision-apis-with-live-camera-72ef47ad4ebd

Android web scraping wrapper: 
https://github.com/daandtu/android-web-scraper

<!-- Trail : https://github.com/Orange-OpenSource/android-trail-drawing -->
<!-- Owl sheet : link?! -->
