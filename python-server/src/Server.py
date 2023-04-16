from socketserver import TCPServer, BaseRequestHandler
from typing import Tuple

from .Packets import *


class RequestHandler(BaseRequestHandler):

    def handle(self) -> None:
        """
        Handles incoming connections
        """

        self.server: Server
        # Handshake
        head = self.request.recv(4)
        print(head)
        self.request.send('Py2J'.encode('ascii'))

        # Request loop
        run: bool = True
        while run:
            request = Request.read_from_socket(self.request)
            text: str = ""
            if request.type == 'K':
                run = False  # quit on kill request
            elif request.type == 'E' and self.server.expression_listener:
                text = self.server.expression_listener(request.text)
            elif request.type == 'T' and self.server.text_listener is not None:
                self.server.text_listener(request.text)

            print('Got message:')
            print(f'\ttype: {request.type}, ID: {request.id}')
            print(f'\ttext: {request.text}')

            response = Response(request.id, 0, text)
            response.write_to_socket(self.request)


class Server(TCPServer):
    """
    Server class in combination with RequestHandler
    provides basic implementation of the protocol (see: protocol.md)

    For methods documentation, see:
    https://docs.python.org/3/library/socketserver.html#socketserver.BaseServer
    """

    def __init__(self, port: int = 25565, address_or_domain: str = "localhost"):
        server_address: Tuple[str, int] = address_or_domain, port
        super().__init__(server_address, RequestHandler)

        self.text_listener = None
        self.expression_listener = None
