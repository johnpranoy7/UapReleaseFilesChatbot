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
  "Show me NASA's picture of the day for February 14, two years before the current year",
  "Show me NASA's Astronomy Picture of the Day",
];

const SIDE_NAV_TABS = {
  RECRUITERS: 'recruiters',
  DEVELOPER: 'developer',
};

const PORTFOLIO = {
  name: 'John Pranoy Yalla',
  role: 'Software Engineer / Full Stack Developer · 6+ years experience',
  email: 'johnpranoy7@gmail.com',
  linkedIn: 'https://www.linkedin.com/in/johnpranoy7/',
  github: 'https://github.com/johnpranoy7',
  resume: '/resume.pdf',
};

function resolvedApodExampleDate() {
  const year = new Date().getFullYear() - 2;
  return `${year}-02-14`;
}

export default function App() {
  const [messages, setMessages] = useState([]);
  const [question, setQuestion] = useState('');
  const [chatId, setChatId] = useState(() => getChatId());
  const [loading, setLoading] = useState(false);
  const [loadingDocs, setLoadingDocs] = useState(false);
  const [status, setStatus] = useState('');
  const [error, setError] = useState('');
  const [promptsExpanded, setPromptsExpanded] = useState(false);
  const [sideNavTab, setSideNavTab] = useState(SIDE_NAV_TABS.RECRUITERS);
  const messagesContainerRef = useRef(null);
  const questionInputRef = useRef(null);

  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  useEffect(() => {
    if (messages.length === 0 && !loading) {
      return;
    }
    const container = messagesContainerRef.current;
    if (!container) {
      return;
    }
    container.scrollTop = container.scrollHeight;
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
      if (result.skipped) {
        setStatus(`${result.message} (${result.chunksLoaded} chunks already indexed)`);
      } else {
        setStatus(`${result.message} (${result.chunksLoaded} chunks indexed)`);
      }
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
        <div className="header-main">
          <div className="header-project">
            <p className="eyebrow">Portfolio demo · UFO / UAP Release Files</p>
            <h1>RAG & Toolcalling Chatbot</h1>
            <p className="subtitle">
              Full-stack Spring AI app — ask questions about U.S. UAP declassified release documents
              (Release 02, May 2025) or request NASA&apos;s Astronomy Picture of the Day.
            </p>
          </div>

          <div className="header-portfolio">
            <p className="header-portfolio-eyebrow">Open to software developer roles</p>
            <p className="header-name">{PORTFOLIO.name}</p>
            <p className="header-role">{PORTFOLIO.role}</p>
            <div className="header-links">
              <a
                href={PORTFOLIO.linkedIn}
                className="header-link"
                target="_blank"
                rel="noopener noreferrer"
              >
                LinkedIn
              </a>
              <a
                href={PORTFOLIO.github}
                className="header-link"
                target="_blank"
                rel="noopener noreferrer"
              >
                GitHub
              </a>
              <a href={`mailto:${PORTFOLIO.email}`} className="header-link">
                Email
              </a>
              {PORTFOLIO.resume && (
                <a
                  href={PORTFOLIO.resume}
                  className="header-link"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  Resume
                </a>
              )}
            </div>
          </div>
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

      <div className="app-layout">
        <aside className="side-nav" aria-label="Chatbot tools and usage">
          <div className="side-nav-tabs" role="tablist" aria-label="Sidebar audience">
            <button
              type="button"
              role="tab"
              id="side-nav-tab-recruiters"
              className={`side-nav-tab${sideNavTab === SIDE_NAV_TABS.RECRUITERS ? ' active' : ''}`}
              aria-selected={sideNavTab === SIDE_NAV_TABS.RECRUITERS}
              aria-controls="side-nav-panel-recruiters"
              onClick={() => setSideNavTab(SIDE_NAV_TABS.RECRUITERS)}
            >
              Recruiters
            </button>
            <button
              type="button"
              role="tab"
              id="side-nav-tab-developer"
              className={`side-nav-tab${sideNavTab === SIDE_NAV_TABS.DEVELOPER ? ' active' : ''}`}
              aria-selected={sideNavTab === SIDE_NAV_TABS.DEVELOPER}
              aria-controls="side-nav-panel-developer"
              onClick={() => setSideNavTab(SIDE_NAV_TABS.DEVELOPER)}
            >
              Developer
            </button>
          </div>

          {sideNavTab === SIDE_NAV_TABS.RECRUITERS && (
            <section
              id="side-nav-panel-recruiters"
              role="tabpanel"
              aria-labelledby="side-nav-tab-recruiters"
              className="side-nav-panel"
            >
              <p className="side-nav-intro">
                Pick a suggested prompt below or type your own question. The assistant routes to document
                search or NASA APOD automatically.
              </p>

              <div className="tool-card">
                <h4>Document search <span className="tool-tag">RAG</span></h4>
                <p>Answers from embedded UAP release PDFs — sightings, reports, and release content.</p>
                <p className="tool-example">
                  &ldquo;What UAP incidents are described in the release files?&rdquo;
                </p>
              </div>

              <div className="tool-card">
                <h4>NASA APOD <span className="tool-tag">Tool call</span></h4>
                <p>Astronomy picture of the day for today or any date you describe in plain English.</p>
                <p className="tool-example">
                  See suggested prompts in the chat panel for a relative-date example.
                </p>
              </div>

              <p className="side-nav-note">
                <strong>Confidence</strong> reflects answer strength (0–100%). NASA requests may take a little longer.
              </p>
            </section>
          )}

          {sideNavTab === SIDE_NAV_TABS.DEVELOPER && (
            <section
              id="side-nav-panel-developer"
              role="tabpanel"
              aria-labelledby="side-nav-tab-developer"
              className="side-nav-panel"
            >
              <p className="side-nav-intro">
                No keyword-based intent routing. Spring AI uses{' '}
                <strong>tool calling</strong> — the model picks{' '}
                <code>searchUapReleaseDocuments</code> or <code>getNasaApod</code> and fills arguments from the
                user&apos;s question.
              </p>

              <ul className="side-nav-list">
                <li>PDFs chunked and embedded into <strong>pgvector</strong> (OpenAI embeddings, top-K retrieval)</li>
                <li><strong>ToolCallAdvisor</strong> routes UAP vs NASA requests at runtime</li>
                <li><strong>JDBC chat memory</strong> keeps the last 8 messages per session</li>
                <li>React UI served from the same Spring Boot app (single Docker deploy)</li>
              </ul>

              <div className="side-nav-dev-note">
                <p className="side-nav-dev-note-label">Developer note</p>
                <p>
                  <strong>Relative date → tool args:</strong> Ask for &ldquo;NASA&apos;s picture of the day for
                  February 14, two years before the current year.&rdquo; Spring AI injects today&apos;s date into the
                  system prompt, picks <code>getNasaApod</code>, and passes a resolved{' '}
                  <code>{resolvedApodExampleDate()}</code> — no custom date-parsing code in the backend.
                </p>
              </div>

              <p className="side-nav-stack">
                <span className="side-nav-stack-label">Built with</span>
                Spring Boot · Spring AI · OpenAI · pgvector · React · Docker · Render · Supabase
              </p>
            </section>
          )}
        </aside>

        <main className="chat-panel">
        <div className="meta-bar">
          <span>Session: {chatId.slice(0, 8)}…</span>
          {status && <span className="status-pill">{status}</span>}
        </div>

        <div className="messages" role="log" aria-live="polite" ref={messagesContainerRef}>
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
    </div>
  );
}
