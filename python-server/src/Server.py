
from socketserver import TCPServer, BaseRequestHandler
from typing import Tuple

from .Packets import *


class RequestHandler(BaseRequestHandler):
    def handle(self) -> None:
        # Handshake
        head = self.request.recv(4)
        print(head)
        self.request.send('Py2J'.encode('ascii'))

        run: bool = True
        while run:
            request = Request.read_from_socket(self.request)
            if request.text == 'die':
                run = False

            print('Got message:')
            print(f'\ttype: {request.type}, ID: {request.id}')
            print(f'\ttext: {request.text}')

            response = Response(request.id, 0, 'garbage')
            response.write_to_socket(self.request)


class Server(TCPServer):
    def __init__(self, port: int = 25565, address_or_domain: str = "localhost"):
        server_address: Tuple[str, int] = address_or_domain, port
        super().__init__(server_address, RequestHandler)
