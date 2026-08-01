const TEAM_COLORS = {
  Red: '#ff4444',
  Blue: '#4488ff',
  Violet: '#aa44ff'
};

const TEAM_LABELS = {
  FreeForAll: 'Free for All',
  TeamDeathMatch: 'Team Deathmatch'
};

function teamBadgeClass(team) {
  if (team === 'Red') return 'badge team-red';
  if (team === 'Blue') return 'badge team-blue';
  if (team === 'Violet') return 'badge team-violet';
  return 'badge';
}

function renderGames(games) {
  const container = document.getElementById('games-container');
  if (!games || games.length === 0) {
    container.innerHTML = '<div class="empty">No active games right now.</div>';
    return;
  }

  container.innerHTML = games.map(game => {
    const typeLabel = TEAM_LABELS[game.gameType] || game.gameType;
    return `
      <article class="card" onclick="enterGame('${escapeHtml(game.gameId)}')">
        <h2>${escapeHtml(game.gameName)}</h2>
        <div class="card-meta">
          <span class="badge">${escapeHtml(typeLabel)}</span>
          <span class="badge">ID: ${escapeHtml(game.gameId)}</span>
        </div>
        <div class="card-stats">
          <div class="stat">
            <span class="stat-value">${game.playerCount}</span>
            <span class="stat-label">Players</span>
          </div>
          <div class="stat">
            <span class="stat-value">${game.maxPlayers || '∞'}</span>
            <span class="stat-label">Max</span>
          </div>
          <div class="stat">
            <span class="stat-value">${game.playersReady}</span>
            <span class="stat-label">Ready</span>
          </div>
        </div>
      </article>
    `;
  }).join('');
}

function enterGame(gameId) {
  window.location.href = `game.html?game=${encodeURIComponent(gameId)}`;
}

function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = String(text ?? '');
  return div.innerHTML;
}

async function loadGames() {
  try {
    const res = await fetch('/api/games');
    if (!res.ok) throw new Error('Failed to load games');
    const games = await res.json();
    renderGames(games);
  } catch (err) {
    const container = document.getElementById('games-container');
    container.innerHTML = `<div class="empty">Error loading games: ${escapeHtml(err.message)}</div>`;
  }
}

function initTheme() {
  const saved = localStorage.getItem('nerfTheme');
  const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
  const theme = saved || (prefersDark ? 'dark' : 'light');
  document.documentElement.setAttribute('data-theme', theme);
  updateThemeButton(theme);
}

function toggleTheme() {
  const current = document.documentElement.getAttribute('data-theme') || 'dark';
  const next = current === 'dark' ? 'light' : 'dark';
  document.documentElement.setAttribute('data-theme', next);
  localStorage.setItem('nerfTheme', next);
  updateThemeButton(next);
}

function updateThemeButton(theme) {
  const btn = document.getElementById('theme-toggle');
  if (btn) btn.textContent = theme === 'dark' ? '🌙' : '☀️';
}

initTheme();
document.getElementById('theme-toggle')?.addEventListener('click', toggleTheme);

loadGames();
setInterval(loadGames, 2000);
