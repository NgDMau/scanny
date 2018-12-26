from bluetooth import *
from subprocess import call
from threading import Thread
import os

def make_discoverable():
    call(["bluetoothctl","discoverable","on"])
    return
def change_DPI():
    call(['./changeDPI.sh'])
    return

def changeTime():
    call(["./changeTime.sh"])
    return

def doBlue():
    server_sock=BluetoothSocket( RFCOMM )
    server_sock.bind(("",PORT_ANY))
    server_sock.listen(1)

    port = server_sock.getsockname()[1]

    uuid = "00001101-0000-1000-8000-00805F9B34FB"

    advertise_service( server_sock, "ahihi",
                    service_id = uuid,
                    service_classes = [ uuid, SERIAL_PORT_CLASS ],
                    profiles = [ SERIAL_PORT_PROFILE ], 
    #                   protocols = [ OBEX_UUID ] 
                        )
                    
    print("Waiting for connection on RFCOMM channel %d" % port)

    client_sock, client_info = server_sock.accept()
    print("Accepted connection from ", client_info)

    try:
        while True:
            data = client_sock.recv(1024)
            if len(data) == 0: break
            print("received [%s]" % data)
            print(type(data))
            if(str(data) == "browsefile"):
                print("browsefile")
                from os import listdir
                from os.path import isfile, join
                mypath = 'images/'
                onlyfiles = [f for f in listdir(mypath) if isfile(join(mypath, f))]
                stringlist = " ".join(onlyfiles)
                stringlist = "filelist " + stringlist
                print(stringlist)
                client_sock.send(stringlist) 
            elif(data=="checkinfo_123456"):        
                dataDaily=''
                myfile=open('/home/pi/test.txt', 'r') 
                dataDaily=myfile.read()
                print(str(dataDaily))
                dataDaily = "checkinfo "+str(dataDaily)
                client_sock.send(dataDaily)
            else:
                data = str(data).split(" ")
                if(data[0]=="transfer"):
                    print("transfering..."+data[1])
                    fname = 'images/'+data[1]
                    from PIL import Image
                    im = Image.open(fname)
                    im.save(fname, dpi=(100,100))
                    with open(fname,'rb') as imageFile:
                        strng = imageFile.read()
                        #strng = bytearray(strng)
                        client_sock.send(strng)
                        print(strng)
                        print(data[1]+' sent!')
                elif(data[0]=="DPI"):
                    dpifile = open("outputDPI.txt","w")
                    dpifile.write(data[1])
                    dpifile.close()
                    thread_changeDPI = Thread(target = change_DPI)
                    thread_changeDPI.start()
                    thread_changeDPI.join()
                elif(data[0] == "Time"):
                    tempList = data[1].split("-")
                    minute = tempList[len(tempList)-1]
                    hour   = tempList[len(tempList)-2]
                    time   = hour + " " + minute
                    fileTime = open('outputTime.txt','w')
                    fileTime.write(time)
                    fileTime.close()
                    thread_changeTime = Thread(target = changeTime)
                    thread_changeTime.start()
                    thread_changeTime.join()
            #process_message(str(data))
            #server_sock.send("hello again")
    except Exception as e:
        print(e)
        client_sock.close()
        server_sock.close()
        doBlue()
        pass
doBlue()

print("disconnected")

def process_message(message):
    if(message == "checkinfo_123456"):
        import os
        os.system("sudo apt-get update")
    return 


print("all done")

if __name__ == "__main__":
    thread_discoverable = Thread(target = make_discoverable)
    thread_discoverable.start()
