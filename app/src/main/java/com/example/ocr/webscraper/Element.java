package com.example.ocr.webscraper;

import android.graphics.Bitmap;
import android.util.Log;

public class Element {

    private String elementLocator;
    private WebScraper web;

    public Element (WebScraper web, String elementLocator){
        this.web = web;
        this.elementLocator = elementLocator;

    }

    public void setText(String text){
        String t = "javascript:" + elementLocator + ".value='" + text + "';void(0);";
        Log.i("Logmsg",t);
        web.run(t);
    }
    public void setAttribute(String attribute,String text){
        String t = "javascript:" + elementLocator + "."+attribute+"='" + text + "';void(0);";
        Log.i("Logmsg",t);
        web.run(t);
    }

    public void click(){
        web.run("javascript:" + elementLocator + ".click();void(0);");
    }

    public String getText(){
        return web.run2("javascript:window.HtmlViewer.processContent(" + elementLocator + ".innerText);");
    }
    public String getAttribute(String attribute){
        return web.run2("javascript:window.HtmlViewer.processContent(" + elementLocator + "."+attribute+");");
    }

    public String getValue(){
        return web.run2("javascript:window.HtmlViewer.processContent(" + elementLocator + ".value);");
    }

    public String getName(){
        return web.run2("javascript:window.HtmlViewer.processContent(" + elementLocator + ".name);");
    }

    public String getTitle(){
        return web.run2("javascript:window.HtmlViewer.processContent(" + elementLocator + ".title);");
    }
}
