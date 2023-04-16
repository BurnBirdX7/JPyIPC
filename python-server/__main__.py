
from .src import Server


def example():
    server = Server()
    print('Server will handle two connections request, sequentially')
    server.handle_request()
    server.handle_request()
    print('Server has handled the requests, stopping...')


if __name__ == '__main__':
    example()
