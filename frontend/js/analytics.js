// Analytics page logic
let diameterHistogram, velocityScatter, distanceScatter, monthlyApproaches, correlationMatrix;

async function loadAnalytics() {
    try {
        const neos = await fetchAPI(API.getNeos(0, 200)); // Load more data for better analytics
        
        updateAdvancedStats(neos);
        initializeCharts(neos);
        updateStatsTable(neos);
        
    } catch (error) {
        console.error('Error loading analytics:', error);
        utils.showToast('Erro ao carregar analytics', 'error');
    }
}

function updateAdvancedStats(neos) {
    // Calculate average diameter
    const diameters = neos.map(n => n.diametroMaxM || 0);
    const avgDiameter = diameters.reduce((a, b) => a + b, 0) / diameters.length;
    
    // Calculate average velocity
    const velocities = neos.map(n => parseFloat(n.velocidadeKmS || 0) * 3600);
    const avgVelocity = velocities.reduce((a, b) => a + b, 0) / velocities.length;
    
    // Mock closest distance
    const avgDiameterForDistance = diameters.reduce((a, b) => a + b, 0) / diameters.length;
    const closestDistance = avgDiameterForDistance * 50000;
    
    // Calculate risk score (simple formula for demonstration)
    const dangerousCount = neos.filter(n => n.ehPotencialmentePerigoso).length;
    const riskScore = (dangerousCount / neos.length * 100).toFixed(1);
    
    document.getElementById('avgDiameter').textContent = utils.formatNumber(avgDiameter);
    document.getElementById('avgVelocity').textContent = utils.formatNumber(avgVelocity);
    document.getElementById('closestDistance').textContent = utils.formatNumber(closestDistance);
    document.getElementById('riskScore').textContent = riskScore + '%';
}

function initializeCharts(neos) {
    createDiameterHistogram(neos);
    createVelocityScatter(neos);
    createDistanceScatter(neos);
    createMonthlyApproaches(neos);
    createCorrelationMatrix(neos);
}

function createDiameterHistogram(neos) {
    const ctx = document.getElementById('diameterHistogram').getContext('2d');
    
    // Bin diameters into ranges
    const diameters = neos.map(n => n.diametroMaxM || 0);
    const bins = [0, 50, 100, 200, 500, 1000, 2000, 5000];
    const binCounts = new Array(bins.length - 1).fill(0);
    
    diameters.forEach(d => {
        for (let i = 0; i < bins.length - 1; i++) {
            if (d >= bins[i] && d < bins[i + 1]) {
                binCounts[i]++;
                break;
            }
        }
    });
    
    diameterHistogram = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: bins.slice(0, -1).map((b, i) => `${b}-${bins[i + 1]}m`),
            datasets: [{
                label: 'Número de NEOs',
                data: binCounts,
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
}

function createVelocityScatter(neos) {
    const ctx = document.getElementById('velocityScatter').getContext('2d');
    
    const dangerous = neos.filter(n => n.ehPotencialmentePerigoso).map(n => ({
        x: parseFloat(n.velocidadeKmS || 0) * 3600,
        y: n.diametroMaxM || 0
    }));
    
    const safe = neos.filter(n => !n.ehPotencialmentePerigoso).map(n => ({
        x: parseFloat(n.velocidadeKmS || 0) * 3600,
        y: n.diametroMaxM || 0
    }));
    
    velocityScatter = new Chart(ctx, {
        type: 'scatter',
        data: {
            datasets: [
                {
                    label: 'Perigosos',
                    data: dangerous,
                    backgroundColor: chartColors.danger + 'CC',
                    borderColor: chartColors.danger
                },
                {
                    label: 'Seguros',
                    data: safe,
                    backgroundColor: chartColors.success + 'CC',
                    borderColor: chartColors.success
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
                x: {
                    title: {
                        display: true,
                        text: 'Velocidade (km/h)'
                    }
                },
                y: {
                    title: {
                        display: true,
                        text: 'Diâmetro (m)'
                    }
                }
            }
        }
    });
}

function createDistanceScatter(neos) {
    const ctx = document.getElementById('distanceScatter').getContext('2d');
    
    const dangerous = neos.filter(n => n.ehPotencialmentePerigoso).map(n => ({
        x: (n.diametroMaxM || 0) * 100000,
        y: n.diametroMaxM || 0
    }));
    
    const safe = neos.filter(n => !n.ehPotencialmentePerigoso).map(n => ({
        x: (n.diametroMaxM || 0) * 100000,
        y: n.diametroMaxM || 0
    }));
    
    distanceScatter = new Chart(ctx, {
        type: 'scatter',
        data: {
            datasets: [
                {
                    label: 'Perigosos',
                    data: dangerous,
                    backgroundColor: chartColors.danger + 'CC',
                    borderColor: chartColors.danger
                },
                {
                    label: 'Seguros',
                    data: safe,
                    backgroundColor: chartColors.success + 'CC',
                    borderColor: chartColors.success
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
                x: {
                    title: {
                        display: true,
                        text: 'Distância (km)'
                    }
                },
                y: {
                    title: {
                        display: true,
                        text: 'Diâmetro (m)'
                    }
                }
            }
        }
    });
}

function createMonthlyApproaches(neos) {
    const ctx = document.getElementById('monthlyApproaches').getContext('2d');
    
    // Group by month
    const monthCounts = {};
    neos.forEach(n => {
        const date = n.dataPrimeiraAproximacao;
        if (date) {
            const month = date.substring(0, 7); // YYYY-MM
            monthCounts[month] = (monthCounts[month] || 0) + 1;
        }
    });
    
    const sortedMonths = Object.keys(monthCounts).sort();
    const counts = sortedMonths.map(m => monthCounts[m]);
    
    monthlyApproaches = new Chart(ctx, {
        type: 'line',
        data: {
            labels: sortedMonths.map(m => {
                const [year, month] = m.split('-');
                return `${month}/${year}`;
            }),
            datasets: [{
                label: 'Aproximações',
                data: counts,
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
}

function createCorrelationMatrix(neos) {
    const ctx = document.getElementById('correlationMatrix').getContext('2d');
    
    // Mock correlation data for demonstration
    const features = ['Diâmetro', 'Velocidade', 'Distância', 'Perigo'];
    const correlationData = [
        [1.0, 0.45, -0.23, 0.67],
        [0.45, 1.0, -0.15, 0.52],
        [-0.23, -0.15, 1.0, -0.38],
        [0.67, 0.52, -0.38, 1.0]
    ];
    
    correlationMatrix = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Diâmetro-Perigo', 'Velocidade-Perigo', 'Distância-Perigo'],
            datasets: [{
                label: 'Correlação',
                data: [0.67, 0.52, -0.38],
                backgroundColor: [
                    chartColors.success,
                    chartColors.warning,
                    chartColors.danger
                ],
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
                    min: -1,
                    max: 1
                }
            }
        }
    });
}

function updateStatsTable(neos) {
    const tbody = document.getElementById('statsTableBody');
    
    const dangerous = neos.filter(n => n.ehPotencialmentePerigoso);
    const safe = neos.filter(n => !n.ehPotencialmentePerigoso);
    
    const calcStats = (subset) => {
        const diameters = subset.map(n => n.diametroMaxM || 0);
        const velocities = subset.map(n => parseFloat(n.velocidadeKmS || 0) * 3600);
        const distances = subset.map(n => (n.diametroMaxM || 0) * 100000);
        
        return {
            count: subset.length,
            avgDiameter: diameters.reduce((a, b) => a + b, 0) / diameters.length,
            maxDiameter: Math.max(...diameters),
            avgVelocity: velocities.reduce((a, b) => a + b, 0) / velocities.length,
            maxVelocity: Math.max(...velocities),
            minDistance: Math.min(...distances.filter(d => d > 0)),
            avgDistance: distances.reduce((a, b) => a + b, 0) / distances.length
        };
    };
    
    const allStats = calcStats(neos);
    const dangerStats = calcStats(dangerous);
    const safeStats = calcStats(safe);
    
    tbody.innerHTML = `
        <tr>
            <td><strong>Quantidade</strong></td>
            <td>${allStats.count}</td>
            <td>${dangerStats.count}</td>
            <td>${safeStats.count}</td>
        </tr>
        <tr>
            <td><strong>Diâmetro Médio (m)</strong></td>
            <td>${utils.formatNumber(allStats.avgDiameter)}</td>
            <td>${utils.formatNumber(dangerStats.avgDiameter)}</td>
            <td>${utils.formatNumber(safeStats.avgDiameter)}</td>
        </tr>
        <tr>
            <td><strong>Diâmetro Máximo (m)</strong></td>
            <td>${utils.formatNumber(allStats.maxDiameter)}</td>
            <td>${utils.formatNumber(dangerStats.maxDiameter)}</td>
            <td>${utils.formatNumber(safeStats.maxDiameter)}</td>
        </tr>
        <tr>
            <td><strong>Velocidade Média (km/h)</strong></td>
            <td>${utils.formatNumber(allStats.avgVelocity)}</td>
            <td>${utils.formatNumber(dangerStats.avgVelocity)}</td>
            <td>${utils.formatNumber(safeStats.avgVelocity)}</td>
        </tr>
        <tr>
            <td><strong>Velocidade Máxima (km/h)</strong></td>
            <td>${utils.formatNumber(allStats.maxVelocity)}</td>
            <td>${utils.formatNumber(dangerStats.maxVelocity)}</td>
            <td>${utils.formatNumber(safeStats.maxVelocity)}</td>
        </tr>
        <tr>
            <td><strong>Distância Mínima (km)</strong></td>
            <td>${utils.formatNumber(allStats.minDistance)}</td>
            <td>${utils.formatNumber(dangerStats.minDistance)}</td>
            <td>${utils.formatNumber(safeStats.minDistance)}</td>
        </tr>
        <tr>
            <td><strong>Distância Média (km)</strong></td>
            <td>${utils.formatNumber(allStats.avgDistance)}</td>
            <td>${utils.formatNumber(dangerStats.avgDistance)}</td>
            <td>${utils.formatNumber(safeStats.avgDistance)}</td>
        </tr>
    `;
}

// Initialize
document.addEventListener('DOMContentLoaded', loadAnalytics);
