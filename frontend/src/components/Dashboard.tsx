import React, { useState, useEffect } from 'react';
import { TaskStatus } from '../types/Task';
import { taskApi } from '../services/api';
import './Dashboard.css';

interface TaskStats {
  total: number;
  todo: number;
  inProgress: number;
  inReview: number;
  done: number;
}

const Dashboard: React.FC = () => {
  const [stats, setStats] = useState<TaskStats>({
    total: 0,
    todo: 0,
    inProgress: 0,
    inReview: 0,
    done: 0,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        setLoading(true);
        const tasks = await taskApi.getAllTasks();
        
        const taskStats = {
          total: tasks.length,
          todo: tasks.filter(task => task.status === TaskStatus.TODO).length,
          inProgress: tasks.filter(task => task.status === TaskStatus.IN_PROGRESS).length,
          inReview: tasks.filter(task => task.status === TaskStatus.IN_REVIEW).length,
          done: tasks.filter(task => task.status === TaskStatus.DONE).length,
        };
        
        setStats(taskStats);
        setError(null);
      } catch (err) {
        setError('統計データの取得に失敗しました');
        console.error('Error fetching task stats:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, []);

  const getCompletionRate = () => {
    if (stats.total === 0) return 0;
    return Math.round((stats.done / stats.total) * 100);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'todo': return '#e74c3c';
      case 'inProgress': return '#f39c12';
      case 'inReview': return '#9b59b6';
      case 'done': return '#27ae60';
      default: return '#95a5a6';
    }
  };

  if (loading) {
    return <div className="dashboard-loading">統計データを読み込み中...</div>;
  }

  if (error) {
    return <div className="dashboard-error">{error}</div>;
  }

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h2>ダッシュボード</h2>
        <p>タスクの進捗状況と統計情報</p>
      </div>

      <div className="stats-grid">
        <div className="stat-card total">
          <h3>総タスク数</h3>
          <div className="stat-number">{stats.total}</div>
        </div>

        <div className="stat-card completion">
          <h3>完了率</h3>
          <div className="stat-number">{getCompletionRate()}%</div>
          <div className="progress-bar">
            <div 
              className="progress-fill" 
              style={{ width: `${getCompletionRate()}%` }}
            ></div>
          </div>
        </div>

        <div className="stat-card" style={{ borderLeftColor: getStatusColor('todo') }}>
          <h3>未着手</h3>
          <div className="stat-number">{stats.todo}</div>
        </div>

        <div className="stat-card" style={{ borderLeftColor: getStatusColor('inProgress') }}>
          <h3>進行中</h3>
          <div className="stat-number">{stats.inProgress}</div>
        </div>

        <div className="stat-card" style={{ borderLeftColor: getStatusColor('inReview') }}>
          <h3>レビュー中</h3>
          <div className="stat-number">{stats.inReview}</div>
        </div>

        <div className="stat-card" style={{ borderLeftColor: getStatusColor('done') }}>
          <h3>完了</h3>
          <div className="stat-number">{stats.done}</div>
        </div>
      </div>

      <div className="charts-section">
        <div className="chart-card">
          <h3>ステータス別分布</h3>
          <div className="status-bars">
            {Object.entries({
              'TODO': { count: stats.todo, color: getStatusColor('todo') },
              'IN_PROGRESS': { count: stats.inProgress, color: getStatusColor('inProgress') },
              'IN_REVIEW': { count: stats.inReview, color: getStatusColor('inReview') },
              'DONE': { count: stats.done, color: getStatusColor('done') },
            }).map(([status, data]) => (
              <div key={status} className="status-bar-item">
                <div className="status-label">{status}</div>
                <div className="status-bar">
                  <div 
                    className="status-bar-fill"
                    style={{ 
                      width: stats.total > 0 ? `${(data.count / stats.total) * 100}%` : '0%',
                      backgroundColor: data.color
                    }}
                  ></div>
                </div>
                <div className="status-count">{data.count}</div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;