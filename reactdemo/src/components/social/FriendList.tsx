import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './FriendList.css';

interface Friend {
  id: number;
  userId: number;
  friendId: number;
  createdAt: string;
}

interface User {
  id: number;
  name: string;
  username: string;
  status: number;
}

interface FriendListProps {
  currentUserId: number;
  onSelectFriend: (friendId: number) => void;
}

const FriendList: React.FC<FriendListProps> = ({ currentUserId, onSelectFriend }) => {
  const [friends, setFriends] = useState<Friend[]>([]);
  const [friendDetails, setFriendDetails] = useState<Map<number, User>>(new Map());
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 获取好友列表
  const fetchFriends = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get('http://localhost:8080/api/social/friends', {
        headers: {
          'User-Id': currentUserId.toString()
        }
      });
      if (response.data.code === 200) {
        const friendsData = response.data.data || [];
        setFriends(friendsData);
        // 获取每个好友的详细信息
        if (friendsData.length > 0) {
          fetchFriendDetails(friendsData);
        }
      } else {
        setError(response.data.msg || '获取好友列表失败');
      }
    } catch (err: any) {
      setError(err.message || '网络错误');
    } finally {
      setLoading(false);
    }
  };

  // 获取好友详细信息
  const fetchFriendDetails = async (friendList: Friend[]) => {
    const detailsMap = new Map<number, User>();
    for (const friend of friendList) {
      try {
        // 假设有获取用户信息的接口
        const response = await axios.get(`http://localhost:8080/user/${friend.friendId}`);
        if (response.data.code === 200) {
          detailsMap.set(friend.friendId, response.data.data);
        }
      } catch (err) {
        // 如果接口不存在或失败，使用默认用户信息
        console.warn(`获取好友 ${friend.friendId} 信息失败，使用默认信息`, err);
        detailsMap.set(friend.friendId, {
          id: friend.friendId,
          name: `用户${friend.friendId}`,
          username: `user${friend.friendId}`,
          status: 0
        } as User);
      }
    }
    setFriendDetails(detailsMap);
  };

  useEffect(() => {
    fetchFriends();
    // 定期刷新好友列表
    const interval = setInterval(fetchFriends, 30000); // 每30秒刷新一次
    return () => clearInterval(interval);
  }, [currentUserId]);

  return (
    <div className="friend-list-container">
      <h2>好友列表</h2>
      {loading && <p>加载中...</p>}
      {error && <p className="error">{error}</p>}
      {!loading && friends.length === 0 && <p>暂无好友</p>}
      <ul className="friend-list">
        {friends.map((friend) => {
          const userInfo = friendDetails.get(friend.friendId);
          return (
            <li 
              key={friend.id} 
              className="friend-item"
              onClick={() => onSelectFriend(friend.friendId)}
            >
              <div className="friend-avatar">
                {userInfo?.name?.charAt(0) || '?'}
              </div>
              <div className="friend-info">
                <div className="friend-name">
                  {userInfo?.name || `用户${friend.friendId}`}
                </div>
                <div className="friend-username">
                  @{userInfo?.username || '未知'}
                </div>
              </div>
              <div className={`friend-status ${userInfo?.status === 0 ? 'online' : 'offline'}`}>
                {userInfo?.status === 0 ? '在线' : '离线'}
              </div>
            </li>
          );
        })}
      </ul>
    </div>
  );
};

export default FriendList;
