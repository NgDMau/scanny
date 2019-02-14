# bluetooth3
Retrieving information from Raspberry Pi 3 to Android
Requirements:
Android API level 27 or above. (in android studio)
However, Android version 7 is okay to run app.
Firstly, run the file "bluezServer.py" by : sudo python bluezServer.
If it does not work, you should edit the file: /lib/systemd/system/bluetooth.service and changing line:

ExecStart=/usr/lib/bluetooth/bluetoothd
to:

ExecStart=/usr/lib/bluetooth/bluetoothd -C
and then running sudo sdptool add SP .

Make sure you run the following after doing this: 1. systemctl daemon-reload 2. service bluetooth restart –

After that, reboot your Pi and run the python file above again.
If it displays: Waiting for bluetooth connection at port 1..., then run the "bluetooth3" app on your android phone and
press Conenct button. If you see: device connected successfully !!! then you just got succeeded.

Type any meesage you want to send to the Pi, the click button "Send". The content will disappear after transferred.
In the terminal of the Pi, you will see "received[message content]".


We send images or file by https://wiki.archlinux.org/index.php/ObexFTP



