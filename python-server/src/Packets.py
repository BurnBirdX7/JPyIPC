from socket import socket
import struct

from dataclasses import dataclass


@dataclass
class Request:
    type: str
    id: int
    text: str

    @staticmethod
    def read_from_socket(s: socket) -> "Request":
        received: bytes = bytes()

        header_size: int = 9
        while len(received) < header_size:
            read = s.recv(2048)
            received += read
            if len(read) == 0:
                raise ConnectionError('Cannot read header')

        type_bytes, msg_id, length = struct.unpack('!cII', received[:9])
        type_str = type_bytes.decode('ascii')

        while len(received) < length + header_size:
            read = s.recv(2048)
            received += read
            if len(read) == 0:
                raise ConnectionError('Cannot read text')

        text: str = received[9:].decode('utf-8')
        return Request(type_str, msg_id, text)


@dataclass
class Response:
    id: int
    status: int
    text: str

    def write_to_socket(self, s: socket) -> None:
        text_encoded = self.text.encode('utf-8')
        header: bytes = struct.pack('!IBI', self.id, self.status, len(text_encoded))
        s.sendall(header + text_encoded)
