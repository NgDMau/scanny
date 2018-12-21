from bluetooth import *

server_sock=BluetoothSocket( RFCOMM )
server_sock.bind(("",PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()[1]

uuid = "0000XXXX-0000-1000-8000-00805F9B34FB"

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
        data = str(data)
        if (data == "browsefile"):  # Browse images
            from os import listdir
            from os.path import isfile, join
            mypath = 'images/'
            onlyfiles = [f for f in listdir(mypath) if isfile(join(mypath, f))]
            stringlist = " ".join(onlyfiles)
            stringlist = "filelist " + stringlist
            print(stringlist)
            server_sock.send(stringlist)
        elif (data == "checkinfo_123456"): # Check system info
            with open('test.txt','r') as file:
            systeminfo = file.readlines()
            server_sock.send(systeminfo)
        elif(data.split(" ")[0] == "transfer"):
            filename = data.split(" ")[1]

        server_sock.send("hello again")
except Exception as e:
    print(e)
    pass

print("disconnected")

def process_message(message):
    if(message == "checkinfo_123456"):
        import os
        os.system("sudo apt-get update")
    return 

client_sock.close()
server_sock.close()
print("all done")