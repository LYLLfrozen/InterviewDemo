import React, { useState } from 'react';
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

  const handleSelectFriend = (friendId: number) => {
    setSelectedFriendId(friendId);
    setActiveView('chat');
  };

  return (
    <div className="social-page">
      <div className="social-sidebar">
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

      <div className="social-main">
        {activeView === 'chat' && selectedFriendId ? (
          <ChatWindow
            currentUserId={currentUserId}
            friendId={selectedFriendId}
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
