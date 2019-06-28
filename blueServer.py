from bluetooth import *
from subprocess import call
from threading import Thread
import os
import subprocess

info_file_path = '/home/pi/scanny/dailyLog.txt'
image_folder_path = ""


def make_discoverable():
    call(["bluetoothctl","discoverable","on"])
    print("DISCOVERABLE IS ONNNNNNNN.....")
    return
def change_DPI():
    call(['./changeDPI.sh'])
    return

def changeTime():
    call(["./changeTime.sh"])
    return

def checkcode(givenstring, code):
    for i in range(len(givenstring)-len(code)):
        start_element = givenstring[i]
        check_string  = start_element
        print(start_element)
        for j in range(len(code)-1):
            check_string += givenstring[i+j+1]
        print(check_string)
        if check_string == code:
            return code
    return False

def delRedundant(data):
    data = str(data)
    if(checkcode(data, "b'") != False) :
        data = data.split("'")[1]
        return data
    else:
        return data

def opush_channel(address):
    part1 = "sdptool search --bdaddr "
    part2 = " OPUSH | grep Channel > opush_channel.txt"
    command = part1 + str(address) + part2
    os.system(command)
    with open("opush_channel.txt","r") as file:
        string = file.read()
    lst = [int(s) for s in string.split() if s.isdigit()]
    if lst[0] is not None:
        return lst[0]
    else:
        return 6

code_list = ["browsefile", "checkinfo_123456", "transfer", "DPI", "Time"]

def filetrans_obexftp(file_path, address):
    channel = opush_channel(address)
    part1 = "obexftp --nopath --noconn --uuid none --bluetooth "
    part2 = " --channel " + str(channel) + " --put " + str(file_path)
    os.system(part1+part2)

    

def doBlue():
    server_sock=BluetoothSocket( RFCOMM )
    server_sock.bind(("",PORT_ANY))
    server_sock.listen(1)

    port = server_sock.getsockname()[1]

    uuid = "00001101-0000-1000-8000-00805F9B34FB"

    advertise_service( server_sock, "NguyenDinhMau",
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
            print(str(data))
            print(type(data))
            #for code in code_list:
            #    result = checkcode(str(data), code)
            data = delRedundant(data)

            if(data == "browsefile"):
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
                myfile=open(info_file_path, 'r') 
                dataDaily=myfile.read()
                print(str(dataDaily))
                dataDaily = "checkinfo "+str(dataDaily)
                client_sock.send(dataDaily)
                #obexftp --nopath --noconn --uuid none --bluetooth 9C:2E:A1:9A:54:96 --channel 5 --put /images/8b8.jpg
            else:
                data = str(data).split(" ")
                if(data[0]=="transfer"):
                    print("transfering..."+data[1])
                    fname = 'images/'+data[1]
                    import os
                    part1 = "obexftp --nopath --noconn --uuid none --bluetooth "
                    address = str(client_info[0])
                    part2 = " --channel 5 --put "
                    filename = data[1]
                    filetrans_obexftp(fname, address)
                    recur = 0
                    #os.system(command)
                    #os.system(command)
                    """try:
                        os.system(command)
                    except Exception as e:
                        error = "error "+e
                        client_sock.send(error)
                        recur += 1"""                     

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
                else:
                    print("Cannot recognize command: "+str(data))
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


if __name__ == "__main__":
    thread_discoverable = Thread(target = make_discoverable)
    thread_discoverable.start()
