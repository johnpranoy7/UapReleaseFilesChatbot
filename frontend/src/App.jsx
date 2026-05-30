import { useEffect, useRef, useState } from 'react';
import { getChatId, loadDocuments, resetChatId, sendChatMessage } from './api/chatApi';

function confidenceLabel(value) {
  if (value >= 0.75) return 'high';
  if (value >= 0.45) return 'medium';
  return 'low';
}

export default function App() {
  const [messages, setMessages] = useState([]);
  const [question, setQuestion] = useState('');
  const [chatId, setChatId] = useState('');
  const [loading, setLoading] = useState(false);
  const [loadingDocs, setLoadingDocs] = useState(false);
  const [status, setStatus] = useState('');
  const [error, setError] = useState('');
  const messagesEndRef = useRef(null);

  useEffect(() => {
    setChatId(getChatId());
  }, []);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  async function handleSubmit(event) {
    event.preventDefault();
    const trimmed = question.trim();
    if (!trimmed || loading) return;

    setError('');
    setQuestion('');
    setMessages((prev) => [...prev, { role: 'user', text: trimmed }]);
    setLoading(true);

    try {
      const response = await sendChatMessage(trimmed, chatId);
      setMessages((prev) => [
        ...prev,
        {
          role: 'assistant',
          text: response.message,
          confidence: response.confidence,
          source: response.source,
        },
      ]);
    } catch (err) {
      setError(err.message || 'Something went wrong.');
    } finally {
      setLoading(false);
    }
  }

  async function handleLoadDocuments() {
    setLoadingDocs(true);
    setError('');
    setStatus('');

    try {
      const result = await loadDocuments();
      setStatus(`${result.message} (${result.chunksLoaded} chunks indexed)`);
    } catch (err) {
      setError(err.message || 'Failed to load documents.');
    } finally {
      setLoadingDocs(false);
    }
  }

  function handleNewChat() {
    const newChatId = resetChatId();
    setChatId(newChatId);
    setMessages([]);
    setError('');
    setStatus('');
  }

  return (
    <div className="app-shell">
      <div className="backdrop" aria-hidden="true" />

      <header className="header">
        <div>
          <p className="eyebrow">UAP / UFO Release Files</p>
          <h1>Intelligence Chatbot</h1>
          <p className="subtitle">
            Ask questions grounded in embedded release documents via RAG.
          </p>
        </div>
        <div className="header-actions">
          <button
            type="button"
            className="secondary-button"
            onClick={handleLoadDocuments}
            disabled={loadingDocs}
          >
            {loadingDocs ? 'Indexing…' : 'Index Documents'}
          </button>
          <button type="button" className="ghost-button" onClick={handleNewChat}>
            New Chat
          </button>
        </div>
      </header>

      <main className="chat-panel">
        <div className="meta-bar">
          <span>Session: {chatId.slice(0, 8)}…</span>
          {status && <span className="status-pill">{status}</span>}
        </div>

        <div className="messages" role="log" aria-live="polite">
          {messages.length === 0 && !loading && (
            <div className="empty-state">
              <p>Try asking:</p>
              <ul>
                <li>What UAP incidents are described in the CIA release files?</li>
                <li>What intelligence sources mention experimental programs?</li>
                <li>Summarize reports related to USSR sightings.</li>
              </ul>
            </div>
          )}

          {messages.map((message, index) => (
            <article
              key={`${message.role}-${index}`}
              className={`message ${message.role}`}
            >
              <div className="message-label">
                {message.role === 'user' ? 'You' : 'Assistant'}
              </div>
              <p>{message.text}</p>
              {message.role === 'assistant' && (
                <div className="message-meta">
                  <span className={`confidence ${confidenceLabel(message.confidence)}`}>
                    Confidence: {(message.confidence * 100).toFixed(0)}%
                  </span>
                  {message.source && (
                    <span className="source">Source: {message.source}</span>
                  )}
                </div>
              )}
            </article>
          ))}

          {loading && (
            <article className="message assistant loading-message">
              <div className="message-label">Assistant</div>
              <p>Searching release files and composing an answer…</p>
            </article>
          )}
          <div ref={messagesEndRef} />
        </div>

        {error && <div className="error-banner">{error}</div>}

        <form className="composer" onSubmit={handleSubmit}>
          <textarea
            value={question}
            onChange={(event) => setQuestion(event.target.value)}
            placeholder="Ask about UAP / UFO release documents…"
            rows={3}
            disabled={loading}
          />
          <button type="submit" disabled={loading || !question.trim()}>
            Send
          </button>
        </form>
      </main>
    </div>
  );
}
