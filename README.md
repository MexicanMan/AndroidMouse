# AndroidMouse (BlueM)
Making an android-based bluetooth mouse for Ubuntu.

Current status: First version ready.

Linux kernel version: 4.18

## Launch note

Android app (BlueM): 
*  Open, build, install and launch BlueM android app.
 
Kernel module (BlueMMouseDriver):
*  `make`;
*  `sudo insmod bluem.ko`.
  
 To unload module use `sudo rmmod bluem`.
