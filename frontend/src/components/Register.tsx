import React, { useState } from 'react';
import { User } from '../types/Task';
import './Register.css';

interface RegisterProps {
  onRegister: (token: string, user: User) => void;
  onBackToLogin: () => void;
}

const Register: React.FC<RegisterProps> = ({ onRegister, onBackToLogin }) => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const validateForm = () => {
    if (!name.trim()) {
      setError('名前を入力してください。');
      return false;
    }
    if (!email.trim()) {
      setError('メールアドレスを入力してください。');
      return false;
    }
    if (password.length < 6) {
      setError('パスワードは6文字以上で入力してください。');
      return false;
    }
    if (!/(?=.*[a-zA-Z])(?=.*\d)/.test(password)) {
      setError('パスワードは英字と数字の両方を含む必要があります。');
      return false;
    }
    if (password !== confirmPassword) {
      setError('パスワードが一致しません。');
      return false;
    }
    return true;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!validateForm()) {
      return;
    }

    setIsLoading(true);

    try {
      const response = await fetch(
        `${
          process.env.API_BASE_URL || 'http://localhost:8080'
        }/api/auth/register`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            name: name.trim(),
            email: email.trim(),
            password,
          }),
        }
      );

      if (response.ok) {
        const data = await response.json();
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify(data.user));
        onRegister(data.token, data.user);
      } else {
        const errorData = await response.json().catch(() => null);
        if (response.status === 409) {
          setError('このメールアドレスは既に登録されています。');
        } else if (errorData && errorData.message) {
          setError(errorData.message);
        } else {
          setError('登録に失敗しました。入力内容を確認してください。');
        }
      }
    } catch (error) {
      setError('ネットワークエラーが発生しました。');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="register-container">
      <div className="register-card">
        <h2>新規ユーザー登録</h2>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="name">名前</label>
            <input
              type="text"
              id="name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              placeholder="山田太郎"
            />
          </div>
          <div className="form-group">
            <label htmlFor="email">メールアドレス</label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              placeholder="example@example.com"
            />
          </div>
          <div className="form-group">
            <label htmlFor="password">パスワード</label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              placeholder="英字と数字を含む6文字以上"
            />
          </div>
          <div className="form-group">
            <label htmlFor="confirmPassword">パスワード（確認）</label>
            <input
              type="password"
              id="confirmPassword"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              placeholder="パスワードを再入力"
            />
          </div>
          {error && <div className="error-message">{error}</div>}
          <button type="submit" disabled={isLoading} className="register-button">
            {isLoading ? '登録中...' : '登録'}
          </button>
        </form>
        <div className="login-link">
          <span>既にアカウントをお持ちですか？</span>
          <button type="button" onClick={onBackToLogin} className="link-button">
            ログインはこちら
          </button>
        </div>
      </div>
    </div>
  );
};

export default Register;