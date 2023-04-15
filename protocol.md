
# Protocol

Python process has to open socket and listen for incoming connections.

## J2Py - Request

Message to python is a text that contains following information

| size           | values   | type        | comment                                         |
|----------------|----------|-------------|-------------------------------------------------|
| 1 byte         | `T`/`E`  | ASCII value | type of the message, **T**ext or **E**xpression |
| 4 bytes        | [0, 2³²) | int         | Message ID (unique for the sender)              | 
| 4 bytes        | [0, 2³²) | int         | Message length                                  |
| [0, 2³²) bytes | bytes... | byte array  | _Message_, UTF-8 encoded text or expression     |

Empty message **are** allowed


## Py2J - Response

| size           | values   | type       | comment                                                              |
|----------------|----------|------------|----------------------------------------------------------------------|
| 4 bytes        | [0, 2³²) | int        | Message ID                                                           |
| 1 byte         | [0, 255] | int        | Status code, 0 - success, 1-255 - failure                            |
| 4 bytes        | [0-2³²)  | int        | Response length (always 0 for response to the **T**ext messages)     |
| [0, 2³²) bytes | bytes... | byte array | _Response_, UTF-8 encoded answer computation result or error message |

Sender (Java) derives response type from ID.