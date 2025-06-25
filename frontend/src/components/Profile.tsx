import React, { useState } from 'react';
import { User } from '../types/Task';
import './Profile.css';

interface ProfileProps {
  user: User | null;
}

const Profile: React.FC<ProfileProps> = ({ user }) => {
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    name: user?.name || '',
    email: user?.email || '',
  });

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // TODO: ユーザー情報更新API呼び出し
    // console.log('Update user:', formData);
    setIsEditing(false);
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
                  <button type="button" onClick={handleCancel} className="btn-cancel">
                    キャンセル
                  </button>
                  <button type="submit" className="btn-save">
                    保存
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