// API Configuration
const API_CONFIG = {
    NEO_CORE_URL: 'http://localhost:8080',
    ML_SERVICE_URL: 'http://localhost:8081'
};

// API Endpoints
const API = {
    // NEO Core endpoints
    getNeos: (page = 0, size = 20, dangerous = null) => {
        let url = `${API_CONFIG.NEO_CORE_URL}/api/neos?pagina=${page}&tamanho=${size}`;
        if (dangerous !== null) {
            url += `&perigoso=${dangerous}`;
        }
        return url;
    },
    getNeoById: (id) => `${API_CONFIG.NEO_CORE_URL}/api/neos/${id}`,
    
    // ML Service endpoints
    trainModel: () => `${API_CONFIG.ML_SERVICE_URL}/ml/train`,
    trainModelAll: () => `${API_CONFIG.ML_SERVICE_URL}/ml/train/all`,
    reloadModel: () => `${API_CONFIG.ML_SERVICE_URL}/ml/reload`,
    predict: () => `${API_CONFIG.ML_SERVICE_URL}/ml/predict`
};

// Utility functions
const utils = {
    formatNumber: (num) => {
        if (num === null || num === undefined) return '-';
        return new Intl.NumberFormat('pt-BR', {
            maximumFractionDigits: 2
        }).format(num);
    },
    
    formatDate: (dateString) => {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleDateString('pt-BR');
    },
    
    formatDateTime: (dateString) => {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleString('pt-BR');
    },
    
    truncate: (str, length = 30) => {
        if (!str) return '-';
        return str.length > length ? str.substring(0, length) + '...' : str;
    },
    
    showToast: (message, type = 'info') => {
        // Simple toast implementation
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.textContent = message;
        toast.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 1rem 1.5rem;
            background: ${type === 'success' ? '#10b981' : type === 'error' ? '#ef4444' : '#6366f1'};
            color: white;
            border-radius: 8px;
            z-index: 10000;
            animation: slideIn 0.3s ease-out;
        `;
        document.body.appendChild(toast);
        setTimeout(() => {
            toast.style.animation = 'slideOut 0.3s ease-out';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }
};

// Fetch helper with error handling
async function fetchAPI(url, options = {}) {
    try {
        const response = await fetch(url, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        return data;
    } catch (error) {
        console.error('API Error:', error);
        utils.showToast('Erro ao conectar com a API: ' + error.message, 'error');
        throw error;
    }
}

// Chart.js default configuration
Chart.defaults.color = '#cbd5e1';
Chart.defaults.borderColor = '#334155';
Chart.defaults.font.family = 'Inter';

const chartColors = {
    primary: '#6366f1',
    secondary: '#8b5cf6',
    success: '#10b981',
    danger: '#ef4444',
    warning: '#f59e0b',
    info: '#3b82f6'
};
