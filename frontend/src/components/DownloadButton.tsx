import React from 'react';

interface DownloadButtonProps {
  jobId: string;
  fileName: string;
  className?: string;
}

const DownloadButton: React.FC<DownloadButtonProps> = ({ jobId, fileName, className }) => {
  const handleDownload = async () => {
    try {
      const response = await fetch(`/api/v1/jobs/download/${jobId}`);
      if (!response.ok) throw new Error('Download failed');
      
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = fileName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error downloading file:', error);
    }
  };

  return (
    <button
      className={className || "px-4 py-2 bg-green-600 text-white rounded-lg font-semibold shadow hover:bg-green-700 transition"}
      onClick={handleDownload}
    >
      Download
    </button>
  );
};

export default DownloadButton; 