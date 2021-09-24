PL3xMapTime
================
An extension mod to Pl3xMap that shows the current ingame time in the world map.


This allows you to show the current ingame world time in your Pl3xMap, as you might be used to from other map plugins.
Just copy the jar file to your plugins folder, restart the server, and you should be done.

Configuration
-------------

Not much. The plugin adds a css/worldtime.css file to your web directory that you can adjust if you don't like the standard display.



Technical details and removing the plugin
-----------------

To integrate itself into pl3xmap, the plugin needs to 
* add some files to the web directory
* register itself by adding two lines to index.html

If you remove the plugin, those files will stay. The files themselves are quite harmless, but you should remove the addition to index.html, 
or you'll keep seeing a time widget that doesn't update anymore.

If you haven't modified the index.html file yourself, just delete it and restart the server; Pl3xMap will write a new one.

If you have modified it, the time plugin will have written two rows just in front of the body tag, linking to `css/worldtime.css` and to js/modules/WorldTime.js.
Remove those two lines.

When the plugin starts, it tries to detect if it's already installed by looking for those two references, and inserting them if it sees them missing. 
If you heavily modified the files, and changed paths around, the plugin may try to restore its references even if they're there in disguise. In this case, add 
a `<!-- notimepatch -->`  comment somewhere in front of the  `<body>` tag to prevent the plugin from messing with the file.
