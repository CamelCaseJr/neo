// NEOs page logic
let currentPage = 0;
let pageSize = 20;
let filterDanger = '';
let filterName = '';
let allNeos = [];

async function loadNeos() {
    try {
        document.getElementById('neosGrid').innerHTML = `
            <div class="loading-state">
                <div class="loader"></div>
                <p>Carregando NEOs...</p>
            </div>
        `;
        
        const neos = await fetchAPI(API.getNeos(currentPage, pageSize, filterDanger !== '' ? filterDanger : null));
        allNeos = neos;
        
        displayNeos(neos);
        updatePagination();
        
    } catch (error) {
        console.error('Error loading NEOs:', error);
        document.getElementById('neosGrid').innerHTML = `
            <div class="loading-state">
                <p>❌ Erro ao carregar NEOs</p>
            </div>
        `;
    }
}

function displayNeos(neos) {
    const grid = document.getElementById('neosGrid');
    
    if (neos.length === 0) {
        grid.innerHTML = `
            <div class="loading-state">
                <p>Nenhum NEO encontrado</p>
            </div>
        `;
        return;
    }
    
    // Apply name filter if exists
    let filteredNeos = neos;
    if (filterName) {
        filteredNeos = neos.filter(neo => 
            neo.nome.toLowerCase().includes(filterName.toLowerCase())
        );
    }
    
    grid.innerHTML = filteredNeos.map(neo => {
        const diameter = neo.diametroMaxM || 0;
        const velocity = (neo.velocidadeKmS || 0) * 3600;
        const distance = diameter * 100000;
        const date = neo.dataPrimeiraAproximacao || '-';
        
        return `
            <div class="neo-card" onclick="showNeoDetails(${neo.id})">
                <div class="neo-card-header">
                    <div>
                        <div class="neo-name">${utils.truncate(neo.nome, 30)}</div>
                        <div class="neo-id">ID: ${neo.id}</div>
                    </div>
                    <span class="badge ${neo.ehPotencialmentePerigoso ? 'badge-danger' : 'badge-success'}">
                        ${neo.ehPotencialmentePerigoso ? '⚠️' : '✓'}
                    </span>
                </div>
                
                <div class="neo-data">
                    <div class="neo-data-item">
                        <span class="neo-data-label">📏 Diâmetro:</span>
                        <span class="neo-data-value">${utils.formatNumber(diameter)} m</span>
                    </div>
                    <div class="neo-data-item">
                        <span class="neo-data-label">🚀 Velocidade:</span>
                        <span class="neo-data-value">${utils.formatNumber(velocity)} km/h</span>
                    </div>
                    <div class="neo-data-item">
                        <span class="neo-data-label">📍 Distância:</span>
                        <span class="neo-data-value">${utils.formatNumber(distance)} km</span>
                    </div>
                    <div class="neo-data-item">
                        <span class="neo-data-label">📅 Aproximação:</span>
                        <span class="neo-data-value">${utils.formatDate(date)}</span>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function updatePagination() {
    const pagination = document.getElementById('pagination');
    pagination.style.display = 'flex';
    
    document.getElementById('currentPage').textContent = currentPage + 1;
    document.getElementById('totalPages').textContent = '?'; // Unknown total pages
    
    document.getElementById('prevBtn').disabled = currentPage === 0;
    document.getElementById('nextBtn').disabled = allNeos.length < pageSize;
}

function previousPage() {
    if (currentPage > 0) {
        currentPage--;
        loadNeos();
    }
}

function nextPage() {
    if (allNeos.length >= pageSize) {
        currentPage++;
        loadNeos();
    }
}

function applyFilters() {
    filterDanger = document.getElementById('filterDanger').value;
    filterName = document.getElementById('filterName').value;
    pageSize = parseInt(document.getElementById('pageSize').value);
    currentPage = 0;
    loadNeos();
}

function clearFilters() {
    document.getElementById('filterDanger').value = '';
    document.getElementById('filterName').value = '';
    document.getElementById('pageSize').value = '20';
    filterDanger = '';
    filterName = '';
    pageSize = 20;
    currentPage = 0;
    loadNeos();
}

async function showNeoDetails(id) {
    try {
        const neo = await fetchAPI(API.getNeoById(id));
        
        const modal = document.getElementById('neoModal');
        const modalBody = document.getElementById('modalBody');
        const modalTitle = document.getElementById('modalTitle');
        
        modalTitle.textContent = neo.nome;
        
        modalBody.innerHTML = `
            <div class="info-grid">
                <div class="info-item">
                    <strong>ID NASA:</strong>
                    <span>${neo.neoId}</span>
                </div>
                <div class="info-item">
                    <strong>Nome:</strong>
                    <span>${neo.nome}</span>
                </div>
                <div class="info-item">
                    <strong>Perigoso:</strong>
                    <span class="badge ${neo.ehPotencialmentePerigoso ? 'badge-danger' : 'badge-success'}">
                        ${neo.ehPotencialmentePerigoso ? '⚠️ Sim' : '✓ Não'}
                    </span>
                </div>
                <div class="info-item">
                    <strong>Diâmetro Mínimo:</strong>
                    <span>${utils.formatNumber(neo.diametroMinM || 0)} m</span>
                </div>
                <div class="info-item">
                    <strong>Diâmetro Máximo:</strong>
                    <span>${utils.formatNumber(neo.diametroMaxM || 0)} m</span>
                </div>
                <div class="info-item">
                    <strong>Velocidade Relativa:</strong>
                    <span>${utils.formatNumber((neo.velocidadeKmS || 0) * 3600)} km/h</span>
                </div>
                <div class="info-item">
                    <strong>Velocidade:</strong>
                    <span>${utils.formatNumber(neo.velocidadeKmS || 0)} km/s</span>
                </div>
                <div class="info-item">
                    <strong>Data de Aproximação:</strong>
                    <span>${utils.formatDate(neo.dataPrimeiraAproximacao)}</span>
                </div>
                <div class="info-item">
                    <strong>Planeta Alvo:</strong>
                    <span>${neo.planetaAlvo || '-'}</span>
                </div>
                <div class="info-item">
                    <strong>Magnitude Absoluta:</strong>
                    <span>${neo.magnitudeAbsoluta || '-'}</span>
                </div>
            </div>
        `;
        
        modal.classList.add('active');
        
    } catch (error) {
        console.error('Error loading NEO details:', error);
        utils.showToast('Erro ao carregar detalhes do NEO', 'error');
    }
}

function closeModal() {
    const modal = document.getElementById('neoModal');
    modal.classList.remove('active');
}

// Close modal when clicking outside
document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('neoModal');
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            closeModal();
        }
    });
    
    loadNeos();
});
