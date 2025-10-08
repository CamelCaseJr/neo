// Dashboard page logic
let dangerChart, velocityChart, diameterChart, distanceChart;

async function loadDashboard() {
    try {
        await Promise.all([
            loadStats(),
            loadCharts(),
            loadRecentNeos()
        ]);
    } catch (error) {
        console.error('Error loading dashboard:', error);
    }
}

async function loadStats() {
    try {
        const neos = await fetchAPI(API.getNeos(0, 100));
        
        const totalNeos = neos.length;
        const dangerousNeos = neos.filter(n => n.ehPotencialmentePerigoso).length;
        const safeNeos = totalNeos - dangerousNeos;
        
        document.getElementById('totalNeos').textContent = utils.formatNumber(totalNeos);
        document.getElementById('dangerousNeos').textContent = utils.formatNumber(dangerousNeos);
        document.getElementById('safeNeos').textContent = utils.formatNumber(safeNeos);
        
        // Mock ML accuracy - in production, fetch from ML service
        document.getElementById('mlAccuracy').textContent = '94.7%';
        
    } catch (error) {
        console.error('Error loading stats:', error);
    }
}

async function loadCharts() {
    try {
        const neos = await fetchAPI(API.getNeos(0, 100));
        
        // Danger Distribution Chart
        const dangerousCount = neos.filter(n => n.ehPotencialmentePerigoso).length;
        const safeCount = neos.length - dangerousCount;
        
        const dangerCtx = document.getElementById('dangerChart').getContext('2d');
        dangerChart = new Chart(dangerCtx, {
            type: 'doughnut',
            data: {
                labels: ['Perigosos', 'Seguros'],
                datasets: [{
                    data: [dangerousCount, safeCount],
                    backgroundColor: [chartColors.danger, chartColors.success],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
        
        // Velocity Chart
        const velocityData = neos
            .slice(0, 10)
            .map(n => ({
                name: utils.truncate(n.nome, 15),
                velocity: parseFloat(n.velocidadeKmS || 0) * 3600
            }));
        
        const velocityCtx = document.getElementById('velocityChart').getContext('2d');
        velocityChart = new Chart(velocityCtx, {
            type: 'bar',
            data: {
                labels: velocityData.map(d => d.name),
                datasets: [{
                    label: 'Velocidade (km/h)',
                    data: velocityData.map(d => d.velocity),
                    backgroundColor: chartColors.primary,
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
        
        // Diameter Chart
        const diameterData = neos
            .slice(0, 10)
            .map(n => ({
                name: utils.truncate(n.nome, 15),
                diameter: parseFloat(n.diametroMaxM || 0)
            }));
        
        const diameterCtx = document.getElementById('diameterChart').getContext('2d');
        diameterChart = new Chart(diameterCtx, {
            type: 'bar',
            data: {
                labels: diameterData.map(d => d.name),
                datasets: [{
                    label: 'Diâmetro (m)',
                    data: diameterData.map(d => d.diameter),
                    backgroundColor: chartColors.secondary,
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
        
        // Distance Chart (mock data - não disponível no backend)
        const distanceData = neos
            .slice(0, 10)
            .map((n, i) => ({
                name: utils.truncate(n.nome, 15),
                distance: (n.diametroMaxM || 0) * 1000 * (i + 1)
            }));
        
        const distanceCtx = document.getElementById('distanceChart').getContext('2d');
        distanceChart = new Chart(distanceCtx, {
            type: 'line',
            data: {
                labels: distanceData.map(d => d.name),
                datasets: [{
                    label: 'Distância (km)',
                    data: distanceData.map(d => d.distance),
                    borderColor: chartColors.info,
                    backgroundColor: chartColors.info + '20',
                    fill: true,
                    tension: 0.4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
        
    } catch (error) {
        console.error('Error loading charts:', error);
    }
}

async function loadRecentNeos() {
    try {
        const neos = await fetchAPI(API.getNeos(0, 10));
        const tbody = document.getElementById('neosTableBody');
        
        if (neos.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="loading">Nenhum NEO encontrado</td></tr>';
            return;
        }
        
        tbody.innerHTML = neos.map(neo => {
            const diameter = neo.diametroMaxM || 0;
            const velocity = (neo.velocidadeKmS || 0) * 3600;
            const distance = diameter * 100000;
            const date = neo.dataPrimeiraAproximacao || '-';
            
            return `
                <tr>
                    <td><strong>${utils.truncate(neo.nome, 25)}</strong></td>
                    <td>${utils.formatNumber(diameter)}</td>
                    <td>${utils.formatNumber(velocity)}</td>
                    <td>${utils.formatNumber(distance)}</td>
                    <td>
                        <span class="badge ${neo.ehPotencialmentePerigoso ? 'badge-danger' : 'badge-success'}">
                            ${neo.ehPotencialmentePerigoso ? '⚠️ Sim' : '✓ Não'}
                        </span>
                    </td>
                    <td>${utils.formatDate(date)}</td>
                </tr>
            `;
        }).join('');
        
    } catch (error) {
        console.error('Error loading recent NEOs:', error);
        const tbody = document.getElementById('neosTableBody');
        tbody.innerHTML = '<tr><td colspan="6" class="loading">Erro ao carregar dados</td></tr>';
    }
}

async function refreshData() {
    utils.showToast('Atualizando dados...', 'info');
    
    // Destroy existing charts
    if (dangerChart) dangerChart.destroy();
    if (velocityChart) velocityChart.destroy();
    if (diameterChart) diameterChart.destroy();
    if (distanceChart) distanceChart.destroy();
    
    await loadDashboard();
    utils.showToast('Dados atualizados!', 'success');
}

// Initialize dashboard when page loads
document.addEventListener('DOMContentLoaded', loadDashboard);
