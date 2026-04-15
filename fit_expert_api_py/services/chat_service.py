# services/chat_service.py

chats = []
messages = []

def create_chat(trainer_id, client_id):
    chat = {
        "id": len(chats) + 1,
        "trainer_id": trainer_id,
        "client_id": client_id
    }
    chats.append(chat)
    return chat


def send_message(chat_id, sender_id, content):
    message = {
        "id": len(messages) + 1,
        "chat_id": chat_id,
        "sender_id": sender_id,
        "content": content
    }
    messages.append(message)
    return message


def get_messages(chat_id):
    return [m for m in messages if m["chat_id"] == chat_id]