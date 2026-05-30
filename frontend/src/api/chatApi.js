const CHAT_ID_KEY = 'uap-chatbot-chat-id';

export function getChatId() {
  let chatId = sessionStorage.getItem(CHAT_ID_KEY);
  if (!chatId) {
    chatId = crypto.randomUUID();
    sessionStorage.setItem(CHAT_ID_KEY, chatId);
  }
  return chatId;
}

export function resetChatId() {
  const chatId = crypto.randomUUID();
  sessionStorage.setItem(CHAT_ID_KEY, chatId);
  return chatId;
}

export async function sendChatMessage(question, chatId) {
  const response = await fetch('/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ chatId, question }),
  });

  if (!response.ok) {
    throw new Error(`Chat request failed (${response.status})`);
  }

  return response.json();
}

export async function loadDocuments() {
  const response = await fetch('/loadFiles');

  if (!response.ok) {
    throw new Error(`Load files request failed (${response.status})`);
  }

  return response.json();
}
