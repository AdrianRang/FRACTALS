rm -f frames/*.png
java -cp /PATH/TO/REDHAT/BIN Video # Ask your IDE for this (copy what it ran on your terminal) or compile the classes first
ffmpeg -r 30 -i frames/frame_%d.png -vcodec libx264 output.mp4