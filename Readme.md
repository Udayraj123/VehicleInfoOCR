## Vehicle Info Camera 
An app made using Firebase MLKit Text Recognizer, Android Web Scraper and a little image processing.
Will be useful for general purpose OCR as well.

## App Release
Get the latest apk from github releases: https://github.com/Udayraj123/VehicleInfoOCR/releases

Drive link for stable apk: https://drive.google.com/file/d/1-AmrFMz0lzGSRFyFWr4NVEHDmdONEyLj/view?usp=sharing

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
	[X] Instruction visuals
	[ ] Load captcha right when splash screen shown
	[ ] Focus guider/live cropper
	[ ] Image picker form gallery 
	[ ] Improve Webview display using js
	[ ] Celebrate Konfetti popper on succesful result!

#### Excess
 	[ ] Preprocess the cameraSource bytebuffer frames
	[X] Remove opencv, process only bitmap
	[ ] Recent searches into cards! - make use of miniRTO here too
	[ ] Other side-interest features

<!-- ## Size Analysis(TODO : Add screenshot)
jar libs : 7MB
resources : 5MB
android-web-scraper : 2 MB
FirebaseMLKit : 2 MB
 -->
## Credits

App workflow inspired from miniRTO app: 
https://github.com/chandruscm/miniRTO 

Firebase MLKit, and This blog : https://medium.com/digital-curry/
firebase-mlkit-textdetection-in-android-using-firebase-ml-vision-apis-with-live-camera-72ef47ad4ebd

Android Web Scraper: 
https://github.com/daandtu/android-web-scraper

Splashy: 
https://github.com/rahuldange09/Splashy

EditTextPicker: 
https://github.com/AliAzaz/Edittext-Library
<!-- Trail : https://github.com/Orange-OpenSource/android-trail-drawing -->
<!-- Owl sheet : link?! -->
<!-- More blogs: https://medium.com/linedevth/build-your-android-app-faster-and-smaller-than-ever-25f53fdd3cdc -->


Vahan portals:

	https://parivahan.gov.in/rcdlstatus/

	https://vahan.nic.in/nrservices/faces/user/searchstatus.xhtml
