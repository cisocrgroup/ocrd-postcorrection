rem ========================
rem build script for windows
rem ========================

copy index.html ..\webapp\index.html 
copy assets\js\vendor\tether.min.js ..\webapp\assets\js\tether.min.js

copy assets\js\vendor\jquery.js ..\webapp\assets\js\jquery.js

rd /s /q ..\webapp\assets\images
robocopy assets\images ..\webapp\assets\images /s /e 

rd /s /q ..\webapp\assets\css
robocopy assets\css ..\webapp\assets\css /s /e

cd assets\js
node r.js -o build.js

cd ..\..
