import React, { useState, useRef, useEffect } from "react";

const Chatbot = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const chatRef = useRef<HTMLDivElement>(null);

  const toggleChat = () => setIsOpen(!isOpen);

  const handleSend = () => {
    if (!input.trim()) return;
    setMessages([...messages, { sender: "user", text: input }]);
    setInput("");

    // Example bot reply (replace with your AI/API)
    setTimeout(() => {
      setMessages(prev => [
        ...prev,
        { sender: "bot", text: "Hello! How can I help you?" },
      ]);
    }, 500);
  };

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (chatRef.current && !chatRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };
    if (isOpen) {
      document.addEventListener("mousedown", handleClickOutside);
    } else {
      document.removeEventListener("mousedown", handleClickOutside);
    }
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [isOpen]);

  return (
    <>
      {/* Floating Chat Icon */}
      <button
        onClick={toggleChat}
        className={`fixed bottom-6 right-6 w-14 h-14 rounded-full flex items-center justify-center
                    transition-all duration-300 shadow hover:scale-110 focus:outline-none
                    ${isOpen ? "bg-mainAccent text-white" : "bg-mainAlt text-mainAccent"}`}
        aria-label="Open Chat"
      >
        💬
      </button>

      {/* Chat Window */}
      {isOpen && (
        <div
          ref={chatRef}
          className="fixed bottom-[96px] right-6 w-72 h-96 bg-white dark:bg-[#1c1c1e] shadow-lg rounded-xl flex flex-col border border-gray-200 dark:border-gray-700 overflow-hidden"
        >
          {/* Header */}
          <div className="flex items-center justify-between p-3 border-b border-gray-200 dark:border-gray-700">
            <span className="font-medium text-sm text-mainAccent dark:text-white">Chat</span>
            <button
              onClick={toggleChat}
              className="text-gray-400 hover:text-mainAccent dark:hover:text-white font-bold"
            >
              ✕
            </button>
          </div>

          {/* Messages */}
          <div className="flex-1 p-3 flex flex-col gap-2 overflow-y-auto scrollbar-thin scrollbar-thumb-gray-300 dark:scrollbar-thumb-gray-600">
            {messages.map((msg, idx) => (
              <div
                key={idx}
                className={`px-3 py-2 rounded-xl max-w-[75%] text-sm break-words
                            ${msg.sender === "user"
                    ? "self-end bg-mainAccent text-white dark:bg-[#d6336c] dark:text-white"
                    : "self-start bg-gray-100 text-gray-900 dark:bg-gray-700 dark:text-white"
                }`}
              >
                {msg.text}
              </div>
            ))}
          </div>

          {/* Input */}
          <div className="flex p-2 border-t border-gray-200 dark:border-gray-700 gap-2">
            <input
              type="text"
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={e => e.key === "Enter" && handleSend()}
              placeholder="Type a message..."
              className="flex-1 rounded-full border border-gray-300 dark:border-gray-600 px-3 py-1 text-sm bg-white dark:bg-[#2c2c2e] text-main dark:text-white focus:outline-none focus:ring-1 focus:ring-mainAccent"
            />
            <button
              onClick={handleSend}
              className="bg-mainAccent text-white px-3 py-1 rounded-full text-sm hover:bg-[#d6336c] transition-colors"
            >
              Send
            </button>
          </div>
        </div>
      )}
    </>
  );
};

export default Chatbot;