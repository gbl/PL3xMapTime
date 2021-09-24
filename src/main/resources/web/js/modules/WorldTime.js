import { P } from './Pl3xMap.js';

class WorldTime {
    constructor(json) {

	// Do not use a Leaflet control; we only want to output, and leaflet
	// doesn't define a "topcenter" position anyway.

	const row = this.element('worldtimerow');
	const container = this.element('worldtimecontainer');
	const img = this.element('worldtimeimg', 'img');
	img.src="images/grass.png";
	const text = this.element('worldtimetext');

	row.appendChild(container);
	container.appendChild(img);
	container.appendChild(text);

	this._img = img;
	this._text = text;

	document.body.appendChild(row);

	const url = document.URL;
	const slashPos = url.lastIndexOf('/');
	this._jsonUrl = url.substring(0, slashPos) + "/worldtimes.json";
	this._advancer = null;
	this._curWorld = 'none';
	this.getWorldTime();
    }

    getWorldTime() {
	this._lastslice = null;
	fetch(this._jsonUrl, {cache: "no-store"})
	.then(response => response.json())
	.then(data => {
	    // console.log(data);
	    const worldname = this.getMapWorldName();
	    this._curWorld = worldname;
	    if (!data.times[worldname]) {
		console.log("worldname is "+worldname+", data.times entry missing");
	    	this._advancer = setTimeout(() => {this.checkWorldChange()}, 1000);
		return;
	    }
	    this.updateOutput(data.times[worldname].time);
	    if (data.times[worldname].advancing) {
	        this._originalJsonTime = data.times[worldname].time;
		this._localTimeDelta = 0;
	    	this._advancer = setTimeout(() => {this.advance()}, 1000);
	    } else {
	    	this._advancer = setTimeout(() => {this.checkWorldChange()}, 1000);
	    }
	});
    }

    checkWorldChange() {
	if (this.getMapWorldName() !== this._curWorld) {
		this.getWorldTime();
		return;
	}
	this._advancer = setTimeout(() => { this.checkWorldChange()}, 1000);
    }

    advance() {
	if (this.getMapWorldName() !== this._curWorld) {
		this.getWorldTime();
		return;
	}

    	// The time isn't 100% correct anyway, because the java part has to write
	// it, we download the file a bit later. But to keep the delta constant,
	// make sure the timeout always ticks at the full second.
    	const intoCurrentSecond = Date.now() % 1000;
	// and unfortunately, sometimes we get the timeout at xx.998 even when
	// we set a 1000 seconds timeout at .001 :/
	var sleepTime = 1000 - intoCurrentSecond;
	if (sleepTime < 100) {
		sleepTime = 1100;
	}
	// console.log(Date.now(), sleepTime);
	this._localTimeDelta += 20;
	this.updateOutput(this._originalJsonTime + this._localTimeDelta);
	if (this._localTimeDelta >= 1200) {	// once per minute
		this._advancer = setTimeout(() => { this.getWorldTime()}, sleepTime);
	} else {
		this._advancer = setTimeout(() => { this.advance()}, sleepTime);
	}
    }

    updateOutput(time) {
	// TODO this could profit from a real strftime library, but
	// not too much, as "date" doesn't make any sense.
        time += 6000;		// 0 is early morning, not midnight!
    	const hours = ("0" + Math.trunc((time / 1000) % 24)).slice(-2);
	const minutes = ("0" + Math.trunc(((time % 1000)*60)/1000)).slice(-2);
    	this._text.innerHTML = hours+":"+minutes;

	time += 12000;		// clock pngs start at noon, not midnight ...
	const dayslice = ("0"+Math.trunc((time % 24000)*64/24000)).slice(-2);
	if (this._lastslice != dayslice) {
	    this._img.src = "images/clocks/clock_"+dayslice+".png";
	    this._lastslice = dayslice;
	}
    }

    element(className, tag) {
    	const elem = document.createElement(tag ? tag : 'div');
	elem.className = className;
	return elem;
    }

    getMapWorldName() {
    	if (!P)	return "none";
	if (!P.worldList) return "none";
	if (!P.worldList.curWorld) return "none";
	if (!P.worldList.curWorld.name) return "none";
	return P.worldList.curWorld.name;
    }
}

new WorldTime();
