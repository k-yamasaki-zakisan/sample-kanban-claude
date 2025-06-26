import React, { useState } from 'react';
import { User, UserUpdateDto } from '../types/Task';
import { userApi } from '../services/api';
import { useNotification } from '../contexts/NotificationContext';
import './Profile.css';

interface ProfileProps {
  user: User | null;
  onUserUpdate: (user: User) => void;
}

const Profile: React.FC<ProfileProps> = ({ user, onUserUpdate }) => {
  const [isEditing, setIsEditing] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [formData, setFormData] = useState({
    name: user?.name || '',
    email: user?.email || '',
  });
  const { addNotification } = useNotification();

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      // 変更があった場合のみ更新データを作成
      const updateData: UserUpdateDto = {};
      if (formData.name !== user?.name) {
        updateData.name = formData.name;
      }
      if (formData.email !== user?.email) {
        updateData.email = formData.email;
      }

      // 変更がない場合は何もしない
      if (Object.keys(updateData).length === 0) {
        addNotification('変更がありませんでした。', 'info');
        setIsEditing(false);
        return;
      }

      // API呼び出し
      const updatedUser = await userApi.updateUser(updateData);
      
      // ローカルストレージのユーザー情報も更新
      localStorage.setItem('user', JSON.stringify(updatedUser));
      
      // 親コンポーネントに更新されたユーザー情報を通知
      onUserUpdate(updatedUser);
      
      addNotification('プロフィールが正常に更新されました。', 'success');
      setIsEditing(false);
    } catch (error) {
      console.error('Failed to update user profile:', error);
      addNotification('プロフィールの更新に失敗しました。', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleCancel = () => {
    setFormData({
      name: user?.name || '',
      email: user?.email || '',
    });
    setIsEditing(false);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ja-JP', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (!user) {
    return <div className="profile-error">ユーザー情報が見つかりません</div>;
  }

  return (
    <div className="profile">
      <div className="profile-header">
        <h2>プロフィール</h2>
        <p>あなたのアカウント情報</p>
      </div>

      <div className="profile-content">
        <div className="profile-card">
          <div className="profile-avatar">
            <div className="avatar-circle">
              {user.name.charAt(0).toUpperCase()}
            </div>
          </div>

          <div className="profile-info">
            {isEditing ? (
              <form onSubmit={handleSubmit} className="profile-form">
                <div className="form-group">
                  <label htmlFor="name">名前</label>
                  <input
                    type="text"
                    id="name"
                    name="name"
                    value={formData.name}
                    onChange={handleInputChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="email">メールアドレス</label>
                  <input
                    type="email"
                    id="email"
                    name="email"
                    value={formData.email}
                    onChange={handleInputChange}
                    required
                  />
                </div>

                <div className="form-actions">
                  <button type="button" onClick={handleCancel} className="btn-cancel" disabled={isLoading}>
                    キャンセル
                  </button>
                  <button type="submit" className="btn-save" disabled={isLoading}>
                    {isLoading ? '保存中...' : '保存'}
                  </button>
                </div>
              </form>
            ) : (
              <div className="profile-display">
                <div className="info-item">
                  <label>名前</label>
                  <div className="info-value">{user.name}</div>
                </div>

                <div className="info-item">
                  <label>メールアドレス</label>
                  <div className="info-value">{user.email}</div>
                </div>

                <div className="info-item">
                  <label>最終ログイン</label>
                  <div className="info-value">
                    {user.lastLogin ? formatDate(user.lastLogin) : '記録なし'}
                  </div>
                </div>

                <div className="info-item">
                  <label>登録日</label>
                  <div className="info-value">
                    {formatDate(user.createdAt)}
                  </div>
                </div>

                <button 
                  onClick={() => setIsEditing(true)} 
                  className="btn-edit-profile"
                >
                  プロフィール編集
                </button>
              </div>
            )}
          </div>
        </div>

        <div className="profile-stats">
          <h3>アカウント統計</h3>
          <div className="stats-grid">
            <div className="stat-item">
              <div className="stat-value">-</div>
              <div className="stat-label">作成したタスク</div>
            </div>
            <div className="stat-item">
              <div className="stat-value">-</div>
              <div className="stat-label">完了したタスク</div>
            </div>
            <div className="stat-item">
              <div className="stat-value">
                {Math.floor((Date.now() - new Date(user.createdAt).getTime()) / (1000 * 60 * 60 * 24))}
              </div>
              <div className="stat-label">利用日数</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;