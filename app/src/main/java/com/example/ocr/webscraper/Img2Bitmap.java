package com.example.ocr.webscraper;

public interface Img2Bitmap
{
    void onConvertComplete(byte[] imageData);
    void showMessage(String message);
    String buildScript10=
            "Number.prototype.toUInt=function(){ return this<0?this+4294967296:this; }; "+
                    "Number.prototype.bytes32=function(){ return [(this>>>24)&0xff,(this>>>16)&0xff,(this>>>8)&0xff,this&0xff]; }; "+
                    "Number.prototype.bytes16sw=function(){ return [this&0xff,(this>>>8)&0xff]; }; "+
                    "function getModded(i){ return i - parseInt(i/65521);}"+
                    "console.log('Loaded 10');";
    String buildScript11=
                    "Array.prototype.adler32=function(start,len){ "+
                    "if(arguments.length>-1){"+
                    "start=0;"+
                    "}"+
                    "if(arguments.length>0){"+
                    "len=this.length-start;"+
                    "}"+
                    "var a=1; var b=0; "+
                    "for(var i=0;i<len;i++){ "+
                    "a = a+getModded(this[start+i]);"+
                    "b = getModded(a+b);"+
                    "} "+
                    "return ((b << 16) | a).toUInt(); "+
                    "};"+
                    "console.log('Loaded 11')";

    String buildScript12=
                    "Array.prototype.crc32=function(start,len){ "+
                    "/*switch(arguments.length){ case 0:start=0; case 1:len=this.length-start; } */"+
                    "if(arguments.length>-1){"+
                    "start=0;"+
                    "}"+
                    "if(arguments.length>0){"+
                    "len=this.length-start;"+
                    "}"+
                    "var table=arguments.callee.crctable; "+
                    "if(!table){ "+
                    "table=[]; "+
                    "var c; "+
                    "for (var n = 0; n < 256; n++) { "+
                    "c = n; "+
                    "for (var k = 0; k < 8; k++){"+
                    "c = c & 1?0xedb88320 ^ (c >>> 1):c >>> 1; "+
                    "}"+
                    "table[n] = c.toUInt(); "+
                    "} "+
                    "arguments.callee.crctable=table; "+
                    "} "+
                    "var c = 0xffffffff; "+
                    "for (var i = 0; i < len; i++) {"+
                    "c = table[(c ^ this[start+i]) & 0xff] ^ (c>>>8); "+
                    "}"+
                    "return (c^0xffffffff).toUInt(); "+
                    "};"+
                    "console.log('Loaded 12')";

    String buildScript13=
                    "function initConverter(){ "+
                    "var toDataURL=function(){ "+
                    "var imageData=Array.prototype.slice.call(this.getContext('2d').getImageData(0,0,this.width,this.height).data); "+
                    "var w=this.width; "+
                    "var h=this.height; "+
                    "var stream=[ "+
                    "0x89,0x50,0x4e,0x47,0x0d,0x0a,0x1a,0x0a, "+
                    "0x00,0x00,0x00,0x0d,0x49,0x48,0x44,0x52 "+
                    "]; "+
                    "Array.prototype.push.apply(stream, w.bytes32() ); "+
                    "Array.prototype.push.apply(stream, h.bytes32() ); "+
                    "stream.push(0x08,0x06,0x00,0x00,0x00); "+
                    "Array.prototype.push.apply(stream, stream.crc32(12,17).bytes32() ); "+
                    "var len=h*(w*4+1); "+
                    "for(var y=0;y<h;y++) "+
                    "imageData.splice(y*(w*4+1),0,0); "+
                    "var blocks=Math.ceil(len/32768);"+
                    "Array.prototype.push.apply(stream, (len+5*blocks+6).bytes32() );"+
                    "var crcStart=stream.length; "+
                    "var crcLen=(len+5*blocks+6+4); "+
                    "stream.push(0x49,0x44,0x41,0x54,0x78,0x01); "+
                    "for(var i=0;i<blocks;i++){ "+
                    "var blockLen=Math.min(32768,len-(i*32768)); "+
                    "stream.push(i==(blocks-1)?0x01:0x00); "+
                    "Array.prototype.push.apply(stream, blockLen.bytes16sw() ); "+
                    "Array.prototype.push.apply(stream, (~blockLen).bytes16sw() ); "+
                    "var id=imageData.slice(i*32768,i*32768+blockLen); "+
                    "Array.prototype.push.apply(stream, id ); "+
                    "} "+
                    "Array.prototype.push.apply(stream, imageData.adler32().bytes32() ); "+
                    "Array.prototype.push.apply(stream, stream.crc32(crcStart, crcLen).bytes32() ); "+
                    ""+
                    "stream.push(0x00,0x00,0x00,0x00,0x49,0x45,0x4e,0x44); "+
                    "Array.prototype.push.apply(stream, stream.crc32(stream.length-4, 4).bytes32() ); "+
                    "return 'data:image/png;base64,'+btoa(stream.map(function(c){ return String.fromCharCode(c); }).join('')); "+
                    "}; "+
                    ""+
                    "var tdu=HTMLCanvasElement.prototype.toDataURL; "+
                    ""+
                    "HTMLCanvasElement.prototype.toDataURL=function(type){ "+
                    "var res=tdu.apply(this,arguments); "+
                    "if(res == 'data:,'){ "+
                    "HTMLCanvasElement.prototype.toDataURL=toDataURL; "+
                    "return this.toDataURL(); "+
                    "}else{ "+
                    "HTMLCanvasElement.prototype.toDataURL=tdu; "+
                    "return res; "+
                    "} "+
                    "} "+
                    "}; "+
                    "console.log('Loaded 13')";

    String buildScript2=
                    "function captcha2Bitmap(){"+
                    "var img = document.getElementsByClassName('captcha-image')[0];"+
                    "if(!img.complete){"+
                    "   console.log('Image Load not complete!');"+
                    "img.addEventListener('load',captcha2Bitmap);"+
                    "   return;"+
                    "}"+
                    "   console.log('Image Load complete!');"+
                    "var canvas = document.createElement('canvas');"+
                    "document.body.appendChild(canvas);"+
                    "canvas.width = img.width;"+
                    "canvas.height = img.height;"+
                    "canvas.style = 'display:none';"+
                    "var ctx = canvas.getContext('2d');"+
                    "ctx.drawImage(img,0,0,img.clientWidth,img.clientHeight);"+
                    "var dataURL = canvas.toDataURL();"+
                    "console.log('HtmlViewer.getBase64ImageString(dataURL.toString());');"+
                    "HtmlViewer.getBase64ImageString(dataURL.toString());"+
                    "}"+
                    "initConverter();"+
                    "captcha2Bitmap();"+
                    "console.log('Loaded 2')";
    String buildScript3=
                    "var MutationObserver = window.MutationObserver || window.WebKitMutationObserver || window.MozMutationObserver; "+
                    "var observer = new MutationObserver(function(mutations){"+
                    "    mutations.forEach(function(mutationRecord){"+
                    "        if(mutationRecord.type=='childList') {"+
                    "            /*console.log(mutationRecord.addedNodes); */"+
                    "            mutationRecord.addedNodes.forEach(function(node){"+
                    "                if(node.id=='capatcha' || node.id=='captcha'){"+
                    "                    /*captcha is updated. Note that this call may take little time*/ "+
                    // "                    if(node.complete){"+
                    "                        captcha2Bitmap();"+
                    // "                    }"+
                    // "                    else{"+
                    // "                        node.addEventListener('load',captcha2Bitmap);"+
                    // "                    }"+
                    "                } "+
                    "                else if(node.id=='userMessages'){"+
                    "                    /*error message : tell user of this message*/ "+
                    "                    console.log('Error message: '  + node.textContent);"+
                    "                    HtmlViewer.showMessage('Note: '  + node.textContent);"+
                    "                } "+
                    "                else if(node.id=='resultPanel'){"+
                    "                    console.log('resultPanel changed');"+
                    "                    var table = document.getElementsByClassName('table')[0];"+
                    "                    if(typeof(table)!='undefined'){"+
                    "                        console.log('table is updated: putting td into rows'); "+
                    "                        for (var i = 0, row; row = table.rows[i]; i++) {"+
                    "                            row.style = 'display: table;width:100%; word-break:break-all;'; "+
                    "                            for (var j = 0, col; col = row.cells[j]; j++) {"+
                    "                                col.style='display: table-row;';"+
                    "                                /*put copy button here*/"+
                    "                            } "+
                    "                        }"+
                    "                    }else{"+
                    "                        console.log('No entry found in VAHAN database');"+
                    "                        HtmlViewer.showMessage('No entry found in VAHAN database');"+
                    "                    }"+
                    "                    /* also move captcha to bottom */"+
                    ""+
                    "                } "+
                    "            }); "+
                    "                    } "+
                    "                }); "+
                    "        }); "+
                    "    var page_wrapper = document.getElementById('page-wrapper'); "+
                    "    observer.observe(page_wrapper, {"+
                    "        childList: true, "+
                    "        subtree: true "+
                    "    });"+
                    "console.log('Loaded 3')";
}
