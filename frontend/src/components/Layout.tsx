import React, { useState, useEffect, useRef } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { User } from '../types/Task';
import './Layout.css';

interface LayoutProps {
  user: User | null;
  onLogout: () => void;
  children: React.ReactNode;
}

const Layout: React.FC<LayoutProps> = ({ user, onLogout, children }) => {
  const location = useLocation();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  const isActive = (path: string) => location.pathname === path;

  const toggleMenu = () => {
    setIsMenuOpen(!isMenuOpen);
  };

  const closeMenu = () => {
    setIsMenuOpen(false);
  };

  // 外側をクリックした時にメニューを閉じる
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setIsMenuOpen(false);
      }
    };

    if (isMenuOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isMenuOpen]);

  return (
    <div className='app-layout'>
      <header className='app-header'>
        <div className='header-left'>
          <Link
            to='/board'
          >
            <h1>Kanban Board</h1>
          </Link>
        </div>

        <div className='header-right'>
          <div className='user-menu' ref={menuRef}>
            <button
              onClick={toggleMenu}
              className='user-menu-button'
              aria-expanded={isMenuOpen}
            >
              <span className='user-name'>{user?.name}さん</span>
              <span className='hamburger-icon'>
                <span></span>
                <span></span>
                <span></span>
              </span>
            </button>

            {isMenuOpen && (
              <div className='dropdown-menu'>
                <Link
                  to='/board'
                  className={`dropdown-link ${
                    isActive('/board') ? 'active' : ''
                  }`}
                  onClick={closeMenu}
                >
                  <span className='menu-icon'>📋</span>
                  ボード
                </Link>
                <Link
                  to='/dashboard'
                  className={`dropdown-link ${
                    isActive('/dashboard') ? 'active' : ''
                  }`}
                  onClick={closeMenu}
                >
                  <span className='menu-icon'>📊</span>
                  ダッシュボード
                </Link>
                <Link
                  to='/profile'
                  className={`dropdown-link ${
                    isActive('/profile') ? 'active' : ''
                  }`}
                  onClick={closeMenu}
                >
                  <span className='menu-icon'>👤</span>
                  プロフィール
                </Link>
                <div className='dropdown-divider'></div>
                <button
                  onClick={() => {
                    onLogout();
                    closeMenu();
                  }}
                  className='logout-dropdown-btn'
                >
                  ログアウト
                </button>
              </div>
            )}
          </div>
        </div>
      </header>

      <main className='main-content'>{children}</main>
    </div>
  );
};

export default Layout;