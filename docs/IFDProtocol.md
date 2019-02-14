# IFD Protocol

This page is extracted from "艾富迪通讯协议v1.3(IS014443A+B+15693)"

- USB HID Specification
<br><https://www.usb.org/hid>
- 艾富迪通讯协议v1.3(IS014443A+B+15693)
<br><https://wenku.baidu.com/view/405cfb08f78a6529647d53af.html> ([PDF](IFD510.pdf))

## USB-HID RFID Reader

Initialize the device
```
control transfer
    requestType:   ENDPOINT_IN | REQUEST_TYPE_STANDARD | RECIPIENT_INTERFACE
    request:       REQUEST_GET_DESCRIPTOR
    value:         DT_REPORT >> 8
    index:         0
    buffer:        byte array (fill in zero)
    buffer-length: 0x100
    timeout:       1000
```

Send packet
```
control transfer
    requestType:   ENDPOINT_OUT | REQUEST_TYPE_CLASS | RECIPIENT_INTERFACE
    request:       HID_SET_REPORT
    value:         (HID_REPORT_TYPE_FEATURE >> 8) | 0x00
    index:         0
    buffer:        Packet with HID-Header
    buffer-length: 0x100
    timeout:       1000
```

Receive packet
```
control transfer
    requestType:   ENDPOINT_IN | REQUEST_TYPE_CLASS | RECIPIENT_INTERFACE
    request:       HID_GET_REPORT
    value:         (HID_REPORT_TYPE_FEATURE >> 8) | 0x00
    index:         0
    buffer:        byte array (fill in zero)
    buffer-length: 0x100
    timeout:       1000
```

HID Header
```
without MF_WRITE
[0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x00]

with MF_WRITE
[0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1F, 0x00]
```


## Packet layout

```
Host to Slave
+-----+------------+-------------+---------+-------------+-----+-----+
| STX | STATION-ID | DATA-LENGTH | COMMAND | DATA[0...n] | BCC | ETX |
+-----+------------+-------------+---------+-------------+-----+-----+

Slave to Host
+-----+------------+-------------+---------+-------------+-----+-----+
| STX | STATION-ID | DATA-LENGTH | STATUS  | DATA[0...n] | BCC | ETX |
+-----+------------+-------------+---------+-------------+-----+-----+

Definition
+-------------+--------------------------+--------------------------+
|    Field    |        Definition        |           Note           |
+-------------+--------------------------+--------------------------+
| STX         | 0x02                     | Start TX                 |
| ETX         | 0x03                     | End TX                   |
| STATION-ID  | Device ID                | 0x00 in Standalone mode  |
| DATA-LENGTH | DATA with COMMAND/STATUS |                          |
| DATA        | Payload                  | Max payload is 255 bytes |
| COMMAND     | Sent command word        |                          |
| STATUS      | Returns status word      |                          |
| BCC         | CRC8 without STX and ETX |                          |
+-------------+--------------------------+--------------------------+
STATION-ID:
    The device address must be in the multi-machine communication.
    After receiving the data packet, the reader determines whether
    the address in the packet matches the preset address of the packet,
    and the match will respond.
```

## Command

```
+---------+----------------+
| Command |      Name      |
+---------+----------------+
| 0x21    | MF_WRITE       |
| 0x25    | MF_GET_SNR     |
| 0x89    | CONTROL_BUZZER |
|         | LOCK_CARD      |
|         | RESET_READER   |
+---------+----------------+
```

### MF_WRITE (0x21)

```
Send data
+------------+------+-----------------------+
| DATA[0]    | 0x00 | MF_WRITE mode control |
| DATA[1]    | 0x01 | Length                |
| DATA[2]    | 0x01 | Start address         |
| DATA[3]    |      | Card type             |
| DATA[4..9] |      | Card payload          |
| DATA[10]   | 0x80 | Unknown               |
+------------+------+-----------------------+
Card type:
    T5577:  0x00
    EM4305: 0x02
Card payload:
    See MF_GET_SNR returns DATA

Receive data (successfully)
+-----------+------+---------------+
| STATUS    | 0x00 | OK            |
| DATA[0-3] |      | Serial number |
+-----------+------+---------------+

Receive data (failed)
+---------+------+-----------------+
| STATUS  | 0x01 | Failed          |
| DATA[0] |      | See Error Codes |
+---------+------+-----------------+
```

### MF_GET_SNR (0x25)

```
Send data
+---------+------+---------+
| DATA[0] | 0x00 | Unknown |
| DATA[1] | 0x00 | Unknown |
+---------+------+---------+

Receive data (successfully)
+-----------+------+-------------+
| STATUS    | 0x00 | OK          |
| DATA[0]   |      | Customer ID |
| DATA[1-4] |      | Wiegand 34  |
+-----------+------+-------------+

Receive data (failed)
+---------+------+-----------------+
| STATUS  | 0x01 | Failed          |
| DATA[0] |      | See Error Codes |
+---------+------+-----------------+
```


### CONTROL_BUZZER (0x89)

```
Send data
+---------+--------------+----------------------------+
| DATA[0] | Buzzer Cycle | 20 ms/cycle, max value: 50 |
| DATA[1] | Buzzer Count | 1 sec/cycle                |
+---------+--------------+----------------------------+

Receive data (successfully)
+---------+------+--------------+
| STATUS  | 0x00 | OK           |
| DATA[0] | 0x80 | Successfully |
+---------+------+--------------+

Receive data (failed)
+---------+------+-----------------+
| STATUS  | 0x01 | Failed          |
| DATA[0] |      | See Error Codes |
+---------+------+-----------------+
```

### LOCK_CARD based CONTROL_BUZZER

```
Send data
+---------+--------------+
| DATA[0] | Buzzer Cycle |
| DATA[1] | Buzzer Count |
+---------+--------------+
Buzzer Cycle:
    T5577  Unlock: 0x04
    EM4305 Unlock: 0x05
    T5577  Lock:   0x06
    EM4305 Lock:   0x07
Buzzer Count:
    Required is 0x01
```

### RESET_READER based CONTROL_BUZZER

```
Send data
+---------+--------------+
| DATA[0] | Buzzer Cycle |
| DATA[1] | Buzzer Count |
+---------+--------------+
Buzzer Cycle:
    Required is 0x01
Buzzer Count:
    Required is 0x01
```

## Error Codes

```
+------+-----------------------+
| Code |      Description      |
+------+-----------------------+
| 0x81 | Set failed            |
| 0x82 | Communication timeout |
| 0x83 | Card not found        |
| 0x84 | Card data error       |
| 0x87 | Unknown error         |
| 0x85 | Command format error  |
| 0x8F | Command not found     |
+------+-----------------------+
```

## Control Logic

### Read Card
```
Send MF_GET_SNR
```

### Write Card
```
Send LOCK_CARD (Unlock)
Send MF_WRITE
Send LOCK_CARD for lock
Send RESET_READER

# Wait 200ms to read the card
```
