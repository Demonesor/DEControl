
toket ="5OH:PT:0106"
toket = toket.split(":")[2]
ip = "192.168." + toket.strip("")[0] + "." + toket.strip("")[1:] 

print(ip)