import socket
client = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
client.connect(('47.105.33.143',9200))

client.send("put:2022-03-11@18438@0,0000000".encode('utf-8'))

client.send("put:2022-03-11@18438@0,0000003".encode('utf-8'))
client.send("put:2022-03-11@18438@0,0000004".encode('utf-8'))
client.send("put:2022-03-11@18438@0,0000005".encode('utf-8'))
client.send("put:2022-03-11@18438@0,0000006".encode('utf-8'))
client.send("get:2022-03-11@18438@0".encode('utf-8'))
msg = client.recv(10000)
print(msg)
client.send("put:2022-03-11@18438@0,0000007".encode('utf-8'))
client.send("put:2022-03-11@18438@0,0000008".encode('utf-8'))
client.send("put:2022-03-11@18438@0,0000009".encode('utf-8'))

client.send("get:2022-03-11@18438@0".encode('utf-8'))
msg = client.recv(10000)
client.send("exit".encode('utf-8'))
# client.send("std".encode('utf-8'))
print(msg)
