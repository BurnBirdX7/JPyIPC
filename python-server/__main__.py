
from .src import Server


def text_handler(text: str) -> None:
    """Simple text request handler"""
    print(f"Handling text: {text}")


def expression_handler(expression: str) -> str:
    """Simple expression request handler"""
    print(f"Handling expression: {expression}")
    return expression + expression


def example():
    server = Server(25565)
    server.expression_listener = expression_handler
    server.text_listener = text_handler

    print('Server will handle two connections request, sequentially')
    server.handle_request()
    server.handle_request()
    print('Server has handled the requests, stopping...')
    server.server_close()


if __name__ == '__main__':
    example()
