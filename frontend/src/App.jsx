import { useEffect, useRef, useState } from 'react';
import { getChatId, loadDocuments, resetChatId, sendChatMessage } from './api/chatApi';

function confidenceLabel(value) {
  if (value >= 0.75) return 'high';
  if (value >= 0.45) return 'medium';
  return 'low';
}

const SAMPLE_PROMPTS = [
  'What UAP incidents are described in the CIA release files?',
  'What intelligence sources mention experimental programs?',
  'Summarize reports related to USSR sightings.',
  "Show me NASA's Astronomy Picture of the Day",
];

export default function App() {
  const [messages, setMessages] = useState([]);
  const [question, setQuestion] = useState('');
  const [chatId, setChatId] = useState(() => getChatId());
  const [loading, setLoading] = useState(false);
  const [loadingDocs, setLoadingDocs] = useState(false);
  const [status, setStatus] = useState('');
  const [error, setError] = useState('');
  const [promptsExpanded, setPromptsExpanded] = useState(true);
  const messagesEndRef = useRef(null);
  const questionInputRef = useRef(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  useEffect(() => {
    if (messages.length > 0) {
      setPromptsExpanded(false);
    }
  }, [messages.length]);

  async function submitQuestion() {
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
          imageUrl: response.imageUrl,
        },
      ]);
    } catch (err) {
      setError(err.message || 'Something went wrong.');
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmit(event) {
    event.preventDefault();
    await submitQuestion();
  }

  function handleQuestionKeyDown(event) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      submitQuestion();
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

  function handleSamplePromptClick(prompt) {
    setQuestion(prompt);
    questionInputRef.current?.focus();
    // handleSubmit();
  }

  function handleNewChat() {
    const newChatId = resetChatId();
    setChatId(newChatId);
    setMessages([]);
    setError('');
    setStatus('');
    setPromptsExpanded(true);
  }

  return (
    <div className="app-shell">
      <div className="backdrop" aria-hidden="true" />

      <header className="header">
        <div>
          <p className="eyebrow">UFO / UAP Release Files</p>
          <h1>RAG & Toolcalling Chatbot</h1>
          <p className="subtitle">
            This RAG chatbot answers questions from the US Department of War documents released May 22 (Release 02).
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
              <p>Ask a question below, or pick a suggested prompt to get started.</p>
            </div>
          )}

          {messages.map((message, index) => (
            <article
              key={`${message.role}-${index}`}
              className={`message ${message.role}`}
            >
              <div className="message-label">
                {message.role === 'user' ? 'You ' : 'Assistant'}
              </div>
              <p>{message.text}</p>
              {message.role === 'assistant' && message.imageUrl && (
                <img
                  className="message-image"
                  src={message.imageUrl}
                  alt="NASA Astronomy Picture of the Day"
                  loading="lazy"
                />
              )}
              {message.role === 'assistant' && (
                <div className="message-meta">
                  <span className={`confidence ${confidenceLabel(message.confidence)}`}>
                    Confidence: {(message.confidence * 100).toFixed(0)}%
                  </span>
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
          <div className="composer-suggestions">
            <button
              type="button"
              className="composer-suggestions-toggle"
              onClick={() => setPromptsExpanded((expanded) => !expanded)}
              aria-expanded={promptsExpanded}
              aria-controls="suggested-prompts"
            >
              <span>Suggested prompts</span>
              <span className="composer-suggestions-icon" aria-hidden="true">
                {promptsExpanded ? '▾' : '▸'}
              </span>
            </button>
            {promptsExpanded && (
              <div id="suggested-prompts" className="prompt-chips">
                {SAMPLE_PROMPTS.map((prompt) => (
                  <button
                    key={prompt}
                    type="button"
                    className="prompt-chip"
                    onClick={() => handleSamplePromptClick(prompt)}
                    disabled={loading}
                  >
                    {prompt}
                  </button>
                ))}
              </div>
            )}
          </div>
          <div className="composer-input-row">
            <textarea
              ref={questionInputRef}
              value={question}
              onChange={(event) => setQuestion(event.target.value)}
              onKeyDown={handleQuestionKeyDown}
              placeholder="Ask about UAP / UFO release documents… (Enter to send, Shift+Enter for new line)"
              rows={3}
              disabled={loading}
            />
            <button type="submit" disabled={loading || !question.trim()}>
              Send
            </button>
          </div>
        </form>
      </main>
    </div>
  );
}
