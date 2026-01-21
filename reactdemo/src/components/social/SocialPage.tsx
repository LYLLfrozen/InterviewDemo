import React, { useState, useEffect } from 'react';
import FriendList from './FriendList';
import FriendRequestComponent from './FriendRequest';
import ChatWindow from './ChatWindow';
import './SocialPage.css';

interface SocialPageProps {
  currentUserId: number;
}

const SocialPage: React.FC<SocialPageProps> = ({ currentUserId }) => {
  const [selectedFriendId, setSelectedFriendId] = useState<number | null>(null);
  const [activeView, setActiveView] = useState<'friends' | 'requests' | 'chat'>('friends');
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768);

  useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.innerWidth < 768);
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const handleSelectFriend = (friendId: number) => {
    setSelectedFriendId(friendId);
    setActiveView('chat');
  };

  const handleBackToList = () => {
    setActiveView('friends');
    setSelectedFriendId(null);
  };

  return (
    <div className="social-page">
      <div className="social-sidebar" style={{ display: isMobile && activeView === 'chat' ? 'none' : 'flex' }}>
        <div className="social-nav">
          <button
            className={`nav-btn ${activeView === 'friends' ? 'active' : ''}`}
            onClick={() => setActiveView('friends')}
          >
            好友列表
          </button>
          <button
            className={`nav-btn ${activeView === 'requests' ? 'active' : ''}`}
            onClick={() => setActiveView('requests')}
          >
            好友请求
          </button>
        </div>

        {activeView === 'friends' && (
          <FriendList
            currentUserId={currentUserId}
            onSelectFriend={handleSelectFriend}
          />
        )}

        {activeView === 'requests' && (
          <FriendRequestComponent currentUserId={currentUserId} />
        )}
      </div>

      <div className={`social-main ${isMobile && activeView === 'chat' ? 'show-chat' : ''}`}>
        {activeView === 'chat' && selectedFriendId ? (
          <ChatWindow
            currentUserId={currentUserId}
            friendId={selectedFriendId}
            onBack={isMobile ? handleBackToList : undefined}
          />
        ) : (
          <div className="social-placeholder">
            <p>选择一个好友开始聊天，或者在左侧添加新好友</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default SocialPage;
