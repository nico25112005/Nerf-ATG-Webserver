const TEAM_COLORS = {
  Red: '#ff4444',
  Blue: '#4488ff',
  Violet: '#aa44ff'
};

const WEAPON_EMOJIS = {
  Sniper: '🎯',
  Mp: '🔫',
  Rifle: '🔫'
};

const TYPE_LABELS = {
  FreeForAll: 'Free for All',
  TeamDeathMatch: 'Team Deathmatch'
};

const params = new URLSearchParams(window.location.search);
const gameId = params.get('game');

if (!gameId) {
  window.location.href = '/';
}

let map;
let playerMarkers = {};
let baseMarkers = {};
let startTime = null;
let timerInterval = null;

function initMap() {
  map = L.map('map').setView([51.505, -0.09], 13);

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(map);
}

function healthColor(health) {
  const h = Math.max(0, Math.min(100, health || 0));
  const r = Math.round(255 * (1 - h / 100));
  const g = Math.round(144 * (h / 100));
  return `rgb(${r}, ${g}, 48)`;
}

function formatTime(seconds) {
  const m = Math.floor(seconds / 60).toString().padStart(2, '0');
  const s = Math.floor(seconds % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
}

function teamBadgeClass(team) {
  if (team === 'Red') return 'badge team-red';
  if (team === 'Blue') return 'badge team-blue';
  if (team === 'Violet') return 'badge team-violet';
  return 'badge';
}

function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = String(text ?? '');
  return div.innerHTML;
}

function createPlayerMarker(player) {
  const color = TEAM_COLORS[player.team] || '#888888';
  const radius = player.team ? 10 : 8;

  const marker = L.circleMarker([player.lat, player.lon], {
    radius: radius,
    fillColor: color,
    color: '#ffffff',
    weight: 2,
    opacity: 1,
    fillOpacity: 0.85
  }).addTo(map);

  marker.bindTooltip(`${escapeHtml(player.name)} (${player.health || 0} HP)`, {
    permanent: false,
    direction: 'top',
    offset: [0, -8]
  });

  return marker;
}

function createBaseMarker(base) {
  const color = TEAM_COLORS[base.team] || '#888888';
  const icon = L.divIcon({
    className: 'base-marker',
    html: `
      <svg width="28" height="28" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
        <path fill="${color}" d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z"
          stroke="#ffffff" stroke-width="1.5"/>
        <circle cx="12" cy="9" r="2.5" fill="#ffffff"/>
      </svg>
    `,
    iconSize: [28, 28],
    iconAnchor: [14, 28]
  });

  const marker = L.marker([base.lat, base.lon], { icon }).addTo(map);
  marker.bindTooltip(`${escapeHtml(base.team)} Base`, {
    permanent: false,
    direction: 'top'
  });
  return marker;
}

function updateHeader(game) {
  document.getElementById('game-title').textContent = game.gameName;
  document.getElementById('game-type').textContent = TYPE_LABELS[game.gameType] || game.gameType;
  document.getElementById('player-count').textContent = game.playerCount;
  document.getElementById('ready-count').textContent = game.playersReady;
}

function updatePlayers(game) {
  const list = document.getElementById('player-list');
  const players = game.players || [];
  const bases = game.bases || [];

  if (players.length === 0) {
    list.innerHTML = '<li class="empty">No players yet.</li>';
  } else {
    list.innerHTML = players.map(player => {
      const color = TEAM_COLORS[player.team] || '#888888';
      const weapon = player.weaponType ? `${WEAPON_EMOJIS[player.weaponType] || '🔫'} ${escapeHtml(player.weaponType)}` : '–';
      const health = player.health || 0;
      const barColor = healthColor(health);
      const teamLabel = player.team || 'None';
      return `
        <li class="player-item" data-player-id="${escapeHtml(player.id)}">
          <div class="player-row">
            <span class="player-name">${escapeHtml(player.name)}</span>
            <div class="player-meta">
              <span class="${teamBadgeClass(player.team)}">${escapeHtml(teamLabel)}</span>
              <span class="weapon">${weapon}</span>
            </div>
          </div>
          <div class="health-bar" title="${health}%">
            <div class="health-fill" style="width: ${health}%; background-color: ${barColor};"></div>
          </div>
          <div class="coords" style="color: ${color}">
            ${player.lat?.toFixed?.(6) ?? '–'}, ${player.lon?.toFixed?.(6) ?? '–'}
          </div>
        </li>
      `;
    }).join('');
  }

  // Update player markers
  const currentIds = new Set();
  let bounds = [];

  players.forEach(player => {
    if (player.lat == null || player.lon == null) return;
    currentIds.add(player.id);
    bounds.push([player.lat, player.lon]);

    if (playerMarkers[player.id]) {
      playerMarkers[player.id].setLatLng([player.lat, player.lon]);
      playerMarkers[player.id].setStyle({
        fillColor: TEAM_COLORS[player.team] || '#888888'
      });
      playerMarkers[player.id].setTooltipContent(`${escapeHtml(player.name)} (${player.health || 0} HP)`);
    } else {
      playerMarkers[player.id] = createPlayerMarker(player);
    }
  });

  Object.keys(playerMarkers).forEach(id => {
    if (!currentIds.has(id)) {
      map.removeLayer(playerMarkers[id]);
      delete playerMarkers[id];
    }
  });

  // Update base markers
  const baseIds = new Set();
  bases.forEach(base => {
    if (base.lat == null || base.lon == null) return;
    const key = base.team;
    baseIds.add(key);
    bounds.push([base.lat, base.lon]);

    if (baseMarkers[key]) {
      baseMarkers[key].setLatLng([base.lat, base.lon]);
    } else {
      baseMarkers[key] = createBaseMarker(base);
    }
  });

  Object.keys(baseMarkers).forEach(key => {
    if (!baseIds.has(key)) {
      map.removeLayer(baseMarkers[key]);
      delete baseMarkers[key];
    }
  });

  if (bounds.length > 0) {
    map.fitBounds(bounds, { padding: [40, 40], maxZoom: 18 });
  }
}

async function loadGame() {
  try {
    const res = await fetch(`/api/games/${encodeURIComponent(gameId)}`);
    if (!res.ok) throw new Error('Failed to load game');
    const game = await res.json();

    if (game.error) {
      throw new Error(game.error);
    }

    updateHeader(game);
    updatePlayers(game);

    if (!startTime) {
      startTime = Date.now();
      if (timerInterval) clearInterval(timerInterval);
      timerInterval = setInterval(() => {
        const elapsed = Math.floor((Date.now() - startTime) / 1000);
        document.getElementById('elapsed-time').textContent = formatTime(elapsed);
      }, 1000);
    }

    document.getElementById('last-updated').textContent = new Date().toLocaleTimeString();
  } catch (err) {
    document.getElementById('game-title').textContent = 'Error';
    document.getElementById('player-list').innerHTML = `<li class="empty">${escapeHtml(err.message)}</li>`;
  }
}

initMap();
loadGame();
setInterval(loadGame, 2000);
