import React, { useState, useRef } from 'react';
import { TaskImage } from '../types/Task';
import { imageApi } from '../services/api';
import { useNotification } from '../contexts/NotificationContext';
import './ImageUpload.css';

interface ImageUploadProps {
  taskId: number;
  images: TaskImage[];
  onImagesChange: (images: TaskImage[]) => void;
  disabled?: boolean;
}

const ImageUpload: React.FC<ImageUploadProps> = ({
  taskId,
  images,
  onImagesChange,
  disabled = false
}) => {
  const [uploading, setUploading] = useState(false);
  const [dragOver, setDragOver] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const { addNotification } = useNotification();

  const handleFileSelect = async (files: FileList | null) => {
    if (!files || files.length === 0 || disabled) return;

    const fileArray = Array.from(files);
    const validFiles = fileArray.filter(file => {
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
      const uploadPromises = validFiles.map((file, index) => 
        imageApi.uploadTaskImage(taskId, file, images.length + index)
      );

      const newImages = await Promise.all(uploadPromises);
      const updatedImages = [...images, ...newImages];
      onImagesChange(updatedImages);

      addNotification(
        `${newImages.length}個の画像をアップロードしました`,
        'success'
      );
    } catch (error) {
      console.error('Error uploading images:', error);
      addNotification('画像のアップロードに失敗しました', 'error');
    } finally {
      setUploading(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  const handleDeleteImage = async (imageId: number) => {
    if (disabled) return;
    
    try {
      await imageApi.deleteImage(imageId);
      const updatedImages = images.filter(img => img.id !== imageId);
      onImagesChange(updatedImages);
      addNotification('画像を削除しました', 'success');
    } catch (error) {
      console.error('Error deleting image:', error);
      addNotification('画像の削除に失敗しました', 'error');
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    if (!disabled) {
      setDragOver(true);
    }
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(false);
    if (!disabled) {
      handleFileSelect(e.dataTransfer.files);
    }
  };

  const handleFileInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    handleFileSelect(e.target.files);
  };

  const handleUploadClick = () => {
    if (!disabled && fileInputRef.current) {
      fileInputRef.current.click();
    }
  };

  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
  };

  return (
    <div className="image-upload-container">
      <div className="image-upload-header">
        <label>Images ({images.length})</label>
        {!disabled && (
          <button
            type="button"
            onClick={handleUploadClick}
            disabled={uploading}
            className="btn-upload-images"
          >
            {uploading ? 'アップロード中...' : '画像を追加'}
          </button>
        )}
      </div>

      <input
        ref={fileInputRef}
        type="file"
        multiple
        accept="image/*"
        onChange={handleFileInputChange}
        style={{ display: 'none' }}
        disabled={disabled}
      />

      {!disabled && (
        <div
          className={`upload-drop-zone ${dragOver ? 'drag-over' : ''} ${uploading ? 'uploading' : ''}`}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
          onClick={handleUploadClick}
        >
          <div className="drop-zone-content">
            <span className="upload-icon">📁</span>
            <p>クリックまたはドラッグ&ドロップで画像をアップロード</p>
            <small>JPEG, PNG, GIF, WebP (最大10MB)</small>
          </div>
        </div>
      )}

      {images.length > 0 && (
        <div className="uploaded-images">
          {images.map((image) => (
            <div key={image.id} className="image-item">
              <div className="image-preview">
                <img
                  src={image.imageUrl}
                  alt={image.originalFilename}
                  onError={(e) => {
                    const target = e.target as HTMLImageElement;
                    target.src = '/placeholder-image.png'; // Fallback image
                  }}
                />
                {!disabled && (
                  <button
                    type="button"
                    onClick={() => handleDeleteImage(image.id)}
                    className="btn-delete-image"
                    title="画像を削除"
                  >
                    ×
                  </button>
                )}
              </div>
              <div className="image-info">
                <div className="image-filename" title={image.originalFilename}>
                  {image.originalFilename}
                </div>
                <div className="image-size">
                  {formatFileSize(image.fileSize)}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ImageUpload;