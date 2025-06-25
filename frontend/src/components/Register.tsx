import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { User } from '../types/Task';
import './Register.css';

interface RegisterProps {
  onRegister: (user: User) => void;
}

const Register: React.FC<RegisterProps> = ({ onRegister }) => {
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
    server: ''
  });

  const validateForm = () => {
    const errors = {
      name: '',
      email: '',
      password: '',
      confirmPassword: '',
      server: ''
    };

    if (!name.trim()) {
      errors.name = '名前を入力してください。';
    }
    if (!email.trim()) {
      errors.email = 'メールアドレスを入力してください。';
    }
    if (password.length < 6) {
      errors.password = 'パスワードは6文字以上で入力してください。';
    } else if (!/(?=.*[a-zA-Z])(?=.*\d)/.test(password)) {
      errors.password = 'パスワードは英字と数字の両方を含む必要があります。';
    }
    if (password !== confirmPassword) {
      errors.confirmPassword = 'パスワードが一致しません。';
    }

    setFieldErrors(errors);
    return !errors.name && !errors.email && !errors.password && !errors.confirmPassword;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFieldErrors({
      name: '',
      email: '',
      password: '',
      confirmPassword: '',
      server: ''
    });

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
        onRegister(data.user);
        navigate('/');
      } else {
        const errorData = await response.json().catch(() => null);
        if (response.status === 409) {
          setFieldErrors(prev => ({ ...prev, email: 'このメールアドレスは既に登録されています。' }));
        } else if (errorData && errorData.message) {
          setFieldErrors(prev => ({ ...prev, server: errorData.message }));
        } else {
          setFieldErrors(prev => ({ ...prev, server: '登録に失敗しました。入力内容を確認してください。' }));
        }
      }
    } catch (error) {
      setFieldErrors(prev => ({ ...prev, server: 'ネットワークエラーが発生しました。' }));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="register-container">
      <div className="register-card">
        <h2>新規ユーザー登録</h2>
        <form onSubmit={handleSubmit} noValidate>
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
            {fieldErrors.name && <div className="field-error-message">{fieldErrors.name}</div>}
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
            {fieldErrors.email && <div className="field-error-message">{fieldErrors.email}</div>}
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
            {fieldErrors.password && <div className="field-error-message">{fieldErrors.password}</div>}
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
            {fieldErrors.confirmPassword && <div className="field-error-message">{fieldErrors.confirmPassword}</div>}
          </div>
          {fieldErrors.server && (
            <div className="server-error-message">
              {fieldErrors.server}
            </div>
          )}
          <button type="submit" disabled={isLoading} className="register-button">
            {isLoading ? '登録中...' : '登録'}
          </button>
        </form>
        <div className="login-link">
          <span>既にアカウントをお持ちですか？</span>
          <Link to="/login" className="link-button">
            ログインはこちら
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Register;