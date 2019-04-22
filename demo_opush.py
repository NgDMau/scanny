import os
address = "9C:2E:A1:9A:54:96"
part1 = "sdptool search --bdaddr "
part2 = " OPUSH | grep Channel > opush_channel.txt"
command = part1 + str(address) + part2
os.system(command)
with open("opush_channel.txt","r") as file:
    string = file.read()
    #print(string)
lst = [int(s) for s in string.split() if s.isdigit()]
print( lst[0] )