#!/bin/bash
# build script for linux/Unix systems

cp index.html ../webapp/index.html 
cp assets/js/vendor/tether.min.js ../webapp/assets/js

cp assets/js/vendor/jquery.js ../webapp/assets/js/jquery.js

rm -rf ../webapp/assets/images
cp -r assets/images ../webapp/assets
rm -rf ../webapp/assets/css
cp -r assets/css ../webapp/assets

cd assets/js
node r.js -o build.js
