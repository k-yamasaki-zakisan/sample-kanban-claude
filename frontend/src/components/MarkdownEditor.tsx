import React, { useState, useRef, useCallback } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeHighlight from 'rehype-highlight';
import { imageApi } from '../services/api';
import { useNotification } from '../contexts/NotificationContext';
import './MarkdownEditor.css';

interface MarkdownEditorProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  rows?: number;
}

const MarkdownEditor: React.FC<MarkdownEditorProps> = ({
  value,
  onChange,
  placeholder = 'Enter description...',
  rows = 4
}) => {
  const [dragOver, setDragOver] = useState(false);
  const [uploading, setUploading] = useState(false);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const { addNotification } = useNotification();

  const insertTextAtCursor = useCallback((text: string) => {
    const textarea = textareaRef.current;
    if (!textarea) return;

    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const newValue = value.substring(0, start) + text + value.substring(end);
    
    onChange(newValue);

    // カーソル位置を調整
    setTimeout(() => {
      const newCursorPos = start + text.length;
      textarea.setSelectionRange(newCursorPos, newCursorPos);
      textarea.focus();
    }, 0);
  }, [value, onChange]);

  const handleImageUpload = useCallback(async (files: FileList) => {
    if (files.length === 0) return;

    const validFiles = Array.from(files).filter(file => {
      const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
      const maxSize = 10 * 1024 * 1024; // 10MB

      if (!validTypes.includes(file.type)) {
        addNotification(`${file.name}: サポートされていないファイル形式です`, 'error');
        return false;
      }

      if (file.size > maxSize) {
        addNotification(`${file.name}: ファイルサイズが大きすぎます (最大10MB)`, 'error');
        return false;
      }

      return true;
    });

    if (validFiles.length === 0) return;

    setUploading(true);
    try {
      for (const file of validFiles) {
        const uploadedImage = await imageApi.uploadImage(file);
        const markdownImage = `![${uploadedImage.originalFilename}](${uploadedImage.imageUrl})`;
        insertTextAtCursor(`${markdownImage}\n`);
      }

      addNotification(
        `${validFiles.length}個の画像をアップロードしました`,
        'success'
      );
    } catch (error) {
      console.error('Error uploading images:', error);
      addNotification('画像のアップロードに失敗しました', 'error');
    } finally {
      setUploading(false);
    }
  }, [insertTextAtCursor, addNotification]);

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(true);
  }, []);

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(false);
  }, []);

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(false);
    
    const files = e.dataTransfer.files;
    const imageFiles = Array.from(files).filter(file => 
      file.type.startsWith('image/')
    );
    
    if (imageFiles.length > 0) {
      handleImageUpload(imageFiles as any);
    }
  }, [handleImageUpload]);

  const handlePaste = useCallback((e: React.ClipboardEvent) => {
    const items = e.clipboardData.items;
    const imageFiles: File[] = [];

    for (let i = 0; i < items.length; i++) {
      const item = items[i];
      if (item.type.indexOf('image') !== -1) {
        const file = item.getAsFile();
        if (file) {
          imageFiles.push(file);
        }
      }
    }

    if (imageFiles.length > 0) {
      e.preventDefault();
      handleImageUpload(imageFiles as any);
    }
  }, [handleImageUpload]);

  return (
    <div className="markdown-editor-container">
      <div className="markdown-editor-tabs">
        <div className="tab active">Edit</div>
        <div className="tab">Preview</div>
      </div>
      
      <div className="markdown-editor-content">
        <div className={`editor-section ${dragOver ? 'drag-over' : ''}`}>
          <textarea
            ref={textareaRef}
            value={value}
            onChange={(e) => onChange(e.target.value)}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            onPaste={handlePaste}
            placeholder={`${placeholder}\n\n画像をドラッグ&ドロップまたは貼り付けでアップロード\n\nMarkdown記法をサポート:\n- **太字**\n- *斜体*\n- \`コード\`\n- [リンク](http://example.com)\n- リストなど...`}
            rows={rows}
            className="markdown-textarea"
            disabled={uploading}
          />
          {dragOver && (
            <div className="drag-overlay">
              <div className="drag-message">
                📁 画像をドロップしてアップロード
              </div>
            </div>
          )}
          {uploading && (
            <div className="upload-overlay">
              <div className="upload-message">
                ⏳ アップロード中...
              </div>
            </div>
          )}
        </div>

        <div className="preview-section">
          {value.trim() ? (
            <div className="markdown-content">
              <ReactMarkdown
                remarkPlugins={[remarkGfm]}
                rehypePlugins={[rehypeHighlight]}
              >
                {value}
              </ReactMarkdown>
            </div>
          ) : (
            <div className="preview-placeholder">プレビューがここに表示されます</div>
          )}
        </div>
      </div>
    </div>
  );
};

export default MarkdownEditor;