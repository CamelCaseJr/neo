// Machine Learning page logic
let trainingHistoryChart;

async function trainModel() {
    const startDate = document.getElementById('trainStartDate').value;
    const endDate = document.getElementById('trainEndDate').value;
    
    if (!startDate || !endDate) {
        utils.showToast('Por favor, preencha as datas de início e fim', 'error');
        return;
    }
    
    const resultDiv = document.getElementById('trainingResult');
    resultDiv.style.display = 'block';
    resultDiv.className = 'result-box';
    resultDiv.innerHTML = `
        <div class="loader" style="width: 30px; height: 30px; border-width: 3px;"></div>
        <p>Treinando modelo com período ${startDate} até ${endDate}...</p>
    `;
    
    try {
        const result = await fetchAPI(API.trainModel(), {
            method: 'POST',
            body: JSON.stringify({
                inicio: startDate,
                fim: endDate
            })
        });
        
        displayTrainingResult(result, resultDiv);
        updateMLStats(result);
        utils.showToast('Modelo treinado com sucesso!', 'success');
        
    } catch (error) {
        console.error('Error training model:', error);
        resultDiv.className = 'result-box error';
        resultDiv.innerHTML = `
            <h4>❌ Erro no Treinamento</h4>
            <p>${error.message}</p>
        `;
        utils.showToast('Erro ao treinar modelo', 'error');
    }
}

async function trainModelAll() {
    const resultDiv = document.getElementById('trainingResult');
    resultDiv.style.display = 'block';
    resultDiv.className = 'result-box';
    resultDiv.innerHTML = `
        <div class="loader" style="width: 30px; height: 30px; border-width: 3px;"></div>
        <p>Treinando modelo com todos os dados disponíveis...</p>
    `;
    
    try {
        const result = await fetchAPI(API.trainModelAll(), {
            method: 'POST'
        });
        
        displayTrainingResult(result, resultDiv);
        updateMLStats(result);
        utils.showToast('Modelo treinado com sucesso!', 'success');
        
    } catch (error) {
        console.error('Error training model:', error);
        resultDiv.className = 'result-box error';
        resultDiv.innerHTML = `
            <h4>❌ Erro no Treinamento</h4>
            <p>${error.message}</p>
        `;
        utils.showToast('Erro ao treinar modelo', 'error');
    }
}

function displayTrainingResult(result, container) {
    container.className = 'result-box success';
    container.innerHTML = `
        <h4>✓ Treinamento Concluído</h4>
        <div class="info-grid" style="margin-top: 1rem;">
            <div class="info-item">
                <strong>Acurácia:</strong>
                <span>${utils.formatNumber(result.accuracy * 100)}%</span>
            </div>
            <div class="info-item">
                <strong>Precisão:</strong>
                <span>${utils.formatNumber(result.precision * 100)}%</span>
            </div>
            <div class="info-item">
                <strong>Recall:</strong>
                <span>${utils.formatNumber(result.recall * 100)}%</span>
            </div>
            <div class="info-item">
                <strong>F1-Score:</strong>
                <span>${utils.formatNumber(result.f1Score * 100)}%</span>
            </div>
            <div class="info-item">
                <strong>Tamanho Treino:</strong>
                <span>${result.trainSize}</span>
            </div>
            <div class="info-item">
                <strong>Tamanho Teste:</strong>
                <span>${result.testSize}</span>
            </div>
        </div>
        ${result.modelPath ? `<p style="margin-top: 1rem; font-size: 0.875rem; color: var(--text-muted);">Modelo salvo em: ${result.modelPath}</p>` : ''}
    `;
}

function updateMLStats(result) {
    document.getElementById('mlAccuracy').textContent = utils.formatNumber(result.accuracy * 100) + '%';
    document.getElementById('mlPrecision').textContent = utils.formatNumber(result.precision * 100) + '%';
    document.getElementById('mlRecall').textContent = utils.formatNumber(result.recall * 100) + '%';
    document.getElementById('mlF1Score').textContent = utils.formatNumber(result.f1Score * 100) + '%';
    
    document.getElementById('mlAccuracyChange').textContent = 'Treinado agora';
    document.getElementById('mlAccuracyChange').className = 'stat-change positive';
    
    document.getElementById('mlPrecisionChange').textContent = 'Atualizado';
    document.getElementById('mlPrecisionChange').className = 'stat-change positive';
    
    document.getElementById('mlRecallChange').textContent = 'Atualizado';
    document.getElementById('mlRecallChange').className = 'stat-change positive';
    
    document.getElementById('mlF1Change').textContent = 'Atualizado';
    document.getElementById('mlF1Change').className = 'stat-change positive';
}

async function makePrediction() {
    const diameterMin = parseFloat(document.getElementById('predDiameterMin').value);
    const diameterMax = parseFloat(document.getElementById('predDiameterMax').value);
    const velocity = parseFloat(document.getElementById('predVelocity').value);
    const distance = parseFloat(document.getElementById('predDistance').value);
    
    if (!diameterMin || !diameterMax || !velocity || !distance) {
        utils.showToast('Por favor, preencha todos os campos', 'error');
        return;
    }
    
    const resultDiv = document.getElementById('predictionResult');
    resultDiv.style.display = 'block';
    resultDiv.className = 'result-box';
    resultDiv.innerHTML = `
        <div class="loader" style="width: 30px; height: 30px; border-width: 3px;"></div>
        <p>Fazendo predição...</p>
    `;
    
    try {
        const result = await fetchAPI(API.predict(), {
            method: 'POST',
            body: JSON.stringify({
                estimatedDiameterMin: diameterMin / 1000, // Convert to km
                estimatedDiameterMax: diameterMax / 1000, // Convert to km
                relativeVelocity: velocity,
                missDistance: distance
            })
        });
        
        const isDangerous = result.prediction === 1 || result.prediction === true;
        const probability = result.probability || (isDangerous ? 0.85 : 0.15);
        
        resultDiv.className = `result-box ${isDangerous ? 'error' : 'success'}`;
        resultDiv.innerHTML = `
            <h4>${isDangerous ? '⚠️ NEO Potencialmente Perigoso' : '✓ NEO Seguro'}</h4>
            <div class="info-grid" style="margin-top: 1rem;">
                <div class="info-item">
                    <strong>Predição:</strong>
                    <span class="badge ${isDangerous ? 'badge-danger' : 'badge-success'}">
                        ${isDangerous ? 'PERIGOSO' : 'SEGURO'}
                    </span>
                </div>
                <div class="info-item">
                    <strong>Probabilidade:</strong>
                    <span>${utils.formatNumber(probability * 100)}%</span>
                </div>
            </div>
            <p style="margin-top: 1rem; font-size: 0.875rem; color: var(--text-muted);">
                ${isDangerous 
                    ? 'Este NEO apresenta características que indicam periculosidade potencial. Monitoramento contínuo é recomendado.'
                    : 'Este NEO não apresenta características de periculosidade significativa.'}
            </p>
        `;
        
        utils.showToast('Predição realizada!', 'success');
        
    } catch (error) {
        console.error('Error making prediction:', error);
        resultDiv.className = 'result-box error';
        resultDiv.innerHTML = `
            <h4>❌ Erro na Predição</h4>
            <p>${error.message}</p>
            <p style="font-size: 0.875rem; color: var(--text-muted); margin-top: 0.5rem;">
                Certifique-se de que o modelo foi treinado e está carregado.
            </p>
        `;
        utils.showToast('Erro ao fazer predição', 'error');
    }
}

async function reloadModel() {
    try {
        utils.showToast('Recarregando modelo...', 'info');
        await fetchAPI(API.reloadModel(), { method: 'POST' });
        utils.showToast('Modelo recarregado com sucesso!', 'success');
    } catch (error) {
        console.error('Error reloading model:', error);
        utils.showToast('Erro ao recarregar modelo', 'error');
    }
}

function initTrainingHistoryChart() {
    const ctx = document.getElementById('trainingHistoryChart').getContext('2d');
    
    // Mock data for demonstration
    const mockData = [
        { session: 'Sessão 1', accuracy: 0.89, precision: 0.87, recall: 0.85, f1: 0.86 },
        { session: 'Sessão 2', accuracy: 0.91, precision: 0.89, recall: 0.88, f1: 0.885 },
        { session: 'Sessão 3', accuracy: 0.93, precision: 0.92, recall: 0.90, f1: 0.91 },
        { session: 'Sessão 4', accuracy: 0.945, precision: 0.94, recall: 0.93, f1: 0.935 },
        { session: 'Atual', accuracy: 0.947, precision: 0.945, recall: 0.94, f1: 0.9425 }
    ];
    
    trainingHistoryChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: mockData.map(d => d.session),
            datasets: [
                {
                    label: 'Acurácia',
                    data: mockData.map(d => d.accuracy),
                    borderColor: chartColors.primary,
                    backgroundColor: chartColors.primary + '20',
                    tension: 0.4
                },
                {
                    label: 'Precisão',
                    data: mockData.map(d => d.precision),
                    borderColor: chartColors.success,
                    backgroundColor: chartColors.success + '20',
                    tension: 0.4
                },
                {
                    label: 'Recall',
                    data: mockData.map(d => d.recall),
                    borderColor: chartColors.warning,
                    backgroundColor: chartColors.warning + '20',
                    tension: 0.4
                },
                {
                    label: 'F1-Score',
                    data: mockData.map(d => d.f1),
                    borderColor: chartColors.secondary,
                    backgroundColor: chartColors.secondary + '20',
                    tension: 0.4
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom'
                }
            },
            scales: {
                y: {
                    beginAtZero: false,
                    min: 0.8,
                    max: 1
                }
            }
        }
    });
}

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    initTrainingHistoryChart();
    
    // Set default dates
    const today = new Date();
    const lastMonth = new Date(today);
    lastMonth.setMonth(lastMonth.getMonth() - 1);
    
    document.getElementById('trainEndDate').value = today.toISOString().split('T')[0];
    document.getElementById('trainStartDate').value = lastMonth.toISOString().split('T')[0];
    
    // Load initial ML stats (mock data)
    document.getElementById('mlAccuracy').textContent = '94.7%';
    document.getElementById('mlPrecision').textContent = '94.5%';
    document.getElementById('mlRecall').textContent = '94.0%';
    document.getElementById('mlF1Score').textContent = '94.25%';
    
    document.getElementById('mlAccuracyChange').textContent = 'Modelo carregado';
    document.getElementById('mlPrecisionChange').textContent = 'Pronto para uso';
    document.getElementById('mlRecallChange').textContent = 'Pronto para uso';
    document.getElementById('mlF1Change').textContent = 'Pronto para uso';
});
