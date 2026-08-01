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

const EVENT_ICONS = {
  joined: '➕',
  left: '➖',
  team: '🔁',
  started: '▶️',
  base: '📍'
};

const params = new URLSearchParams(window.location.search);
const gameId = params.get('game');

if (!gameId) {
  window.location.href = '/';
}

let map;
let playerMarkers = {};
let baseMarkers = {};
let playerTrails = {};
let startTime = null;
let timerInterval = null;
let lastGame = null;
let healthHistory = {};
let joinedTimes = {};
let notificationsEnabled = localStorage.getItem('nerfNotifications') === 'true';
let autoCenterEnabled = localStorage.getItem('nerfAutoCenter') === 'true';

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

  marker.on('click', () => openPlayerModal(player.id));
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

function createShootingFlash(lat, lon) {
  const circle = L.circle([lat, lon], {
    radius: 0,
    color: '#ffaa00',
    weight: 3,
    opacity: 0.9,
    fill: false
  }).addTo(map);

  let step = 0;
  const steps = 20;
  const interval = setInterval(() => {
    step++;
    const ratio = step / steps;
    circle.setRadius(20 * ratio);
    circle.setStyle({ opacity: 0.9 * (1 - ratio) });
    if (step >= steps) {
      clearInterval(interval);
      map.removeLayer(circle);
    }
  }, 30);
}

function sparkline(history, currentHealth, width, height) {
  const vals = (history || []);
  if (vals.length === 0) {
    return `<svg width="${width}" height="${height}" class="sparkline"><rect width="100%" height="100%" fill="transparent"/></svg>`;
  }
  const min = 0;
  const max = 100;
  const xStep = width / (vals.length - 1 || 1);
  const points = vals.map((v, i) => {
    const x = vals.length === 1 ? width / 2 : i * xStep;
    const y = height - ((Math.max(min, Math.min(max, v || 0)) - min) / (max - min)) * height;
    return `${x},${y}`;
  }).join(' ');
  const color = healthColor(currentHealth);
  return `<svg width="${width}" height="${height}" class="sparkline" preserveAspectRatio="none">
    <polyline points="${points}" fill="none" stroke="${color}" stroke-width="2" vector-effect="non-scaling-stroke"/>
    <circle cx="${width}" cy="${height - ((Math.max(min, Math.min(max, currentHealth || 0)) - min) / (max - min)) * height}" r="2.5" fill="${color}"/>
  </svg>`;
}

function updateHeader(game) {
  document.getElementById('game-title').textContent = game.gameName;
  document.getElementById('game-type').textContent = TYPE_LABELS[game.gameType] || game.gameType;
  document.getElementById('player-count').textContent = game.playerCount;
  document.getElementById('ready-count').textContent = game.playersReady;
}

function pushEvent(type, text) {
  const feed = document.getElementById('event-feed');
  if (!feed) return;
  const time = new Date().toLocaleTimeString();
  const icon = EVENT_ICONS[type] || '•';
  const li = document.createElement('li');
  li.className = `event-item event-${type}`;
  li.innerHTML = `<span class="event-time">${time}</span>
    <span class="event-icon">${icon}</span>
    <span class="event-text">${escapeHtml(text)}</span>`;
  feed.insertBefore(li, feed.firstChild);
  while (feed.children.length > 50) {
    feed.removeChild(feed.lastChild);
  }
}

function detectEvents(game) {
  if (!lastGame) return;

  const prevPlayers = new Map((lastGame.players || []).map(p => [p.id, p]));
  const currPlayers = new Map((game.players || []).map(p => [p.id, p]));

  // player joined / left
  for (const [id, p] of currPlayers) {
    if (!prevPlayers.has(id)) {
      pushEvent('joined', `${p.name} joined${p.team ? ` (${p.team})` : ''}`);
      maybeNotify('Player joined', `${p.name} joined the game`);
    }
  }
  for (const [id, p] of prevPlayers) {
    if (!currPlayers.has(id)) {
      pushEvent('left', `${p.name} left`);
      maybeNotify('Player left', `${p.name} left the game`);
    }
  }
  // team switches
  for (const [id, p] of currPlayers) {
    const prev = prevPlayers.get(id);
    if (prev && prev.team !== p.team) {
      pushEvent('team', `${p.name} switched to ${p.team || 'No team'}`);
    }
  }

  // game started: playerCount changed from 0 or state changed
  if ((lastGame.playerCount === 0 && game.playerCount > 0) ||
      (lastGame.state !== game.state && game.state === 'Running')) {
    pushEvent('started', `Game started: ${game.gameName}`);
    maybeNotify('Game started', `${game.gameName} is now running`);
  }

  // base set / moved
  const prevBases = new Map((lastGame.bases || []).map(b => [b.team, b]));
  const currBases = new Map((game.bases || []).map(b => [b.team, b]));
  for (const [team, base] of currBases) {
    const prev = prevBases.get(team);
    if (!prev || prev.lat !== base.lat || prev.lon !== base.lon) {
      pushEvent('base', `${team || 'Team'} base set at ${base.lat?.toFixed?.(6) || '?'}, ${base.lon?.toFixed?.(6) || '?'}`);
    }
  }
}

function maybeNotify(title, body) {
  if (!notificationsEnabled) return;
  if (!('Notification' in window)) return;
  if (Notification.permission === 'granted') {
    try { new Notification(title, { body, icon: '/favicon.ico' }); } catch (e) { /* ignore */ }
  } else if (Notification.permission !== 'denied') {
    Notification.requestPermission().then(permission => {
      if (permission === 'granted') {
        try { new Notification(title, { body, icon: '/favicon.ico' }); } catch (e) { /* ignore */ }
      }
    });
  }
}

function timeSinceJoined(playerId) {
  const t = joinedTimes[playerId];
  if (!t) return null;
  const seconds = Math.floor((Date.now() - t) / 1000);
  if (seconds < 60) return `${seconds}s`;
  if (seconds < 3600) return `${Math.floor(seconds / 60)}m ${seconds % 60}s`;
  return `${Math.floor(seconds / 3600)}h ${Math.floor((seconds % 3600) / 60)}m`;
}

function openPlayerModal(playerId) {
  const modal = document.getElementById('player-modal');
  const players = (lastGame?.players || []);
  const player = players.find(p => p.id === playerId);
  if (!player) return;

  const health = player.health || 0;
  const teamLabel = player.team || 'None';
  document.getElementById('modal-name').textContent = player.name || 'Player';
  const teamBadge = document.getElementById('modal-team');
  teamBadge.textContent = teamLabel;
  teamBadge.className = `${teamBadgeClass(player.team)} badge`;
  document.getElementById('modal-health').textContent = `${health}%`;
  document.getElementById('modal-health-fill').style.width = `${health}%`;
  document.getElementById('modal-health-fill').style.backgroundColor = healthColor(health);
  document.getElementById('modal-weapon').textContent = player.weaponType ? `${WEAPON_EMOJIS[player.weaponType] || '🔫'} ${player.weaponType}` : '–';
  document.getElementById('modal-coords').textContent = `${player.lat?.toFixed?.(6) ?? '–'}, ${player.lon?.toFixed?.(6) ?? '–'}`;

  const joined = timeSinceJoined(playerId);
  const joinedRow = document.getElementById('modal-joined-row');
  if (joined) {
    joinedRow.style.display = 'block';
    document.getElementById('modal-joined').textContent = joined;
  } else {
    joinedRow.style.display = 'none';
  }

  modal.classList.remove('hidden');
}

function closePlayerModal() {
  document.getElementById('player-modal').classList.add('hidden');
}

function updatePlayers(game) {
  const list = document.getElementById('player-list');
  const players = game.players || [];
  const bases = game.bases || [];

  // track health history and joined times
  players.forEach(p => {
    if (!healthHistory[p.id]) healthHistory[p.id] = [];
    healthHistory[p.id].push(p.health || 0);
    if (healthHistory[p.id].length > 20) healthHistory[p.id].shift();
    if (!joinedTimes[p.id]) joinedTimes[p.id] = Date.now();
  });

  if (players.length === 0) {
    list.innerHTML = '<li class="empty">No players yet.</li>';
  } else {
    list.innerHTML = players.map(player => {
      const color = TEAM_COLORS[player.team] || '#888888';
      const weapon = player.weaponType ? `${WEAPON_EMOJIS[player.weaponType] || '🔫'} ${escapeHtml(player.weaponType)}` : '–';
      const health = player.health || 0;
      const barColor = healthColor(health);
      const teamLabel = player.team || 'None';
      const hist = healthHistory[player.id] || [];
      const spark = sparkline(hist, health, 60, 20);
      return `
        <li class="player-item" data-player-id="${escapeHtml(player.id)}" onclick="openPlayerModal('${escapeHtml(player.id)}')">
          <div class="player-row">
            <span class="player-name">${escapeHtml(player.name)}</span>
            <div class="player-meta">
              <span class="${teamBadgeClass(player.team)}">${escapeHtml(teamLabel)}</span>
              <span class="weapon">${weapon}</span>
            </div>
          </div>
          <div class="health-sparkline-row">
            <div class="health-bar" title="${health}%">
              <div class="health-fill" style="width: ${health}%; background-color: ${barColor};"></div>
            </div>
            <div class="sparkline-wrap">${spark}</div>
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

    // Update trail (last 20 positions)
    if (!playerTrails[player.id]) playerTrails[player.id] = [];
    playerTrails[player.id].push([player.lat, player.lon]);
    if (playerTrails[player.id].length > 20) playerTrails[player.id].shift();
    if (playerTrails[player.id].length > 1) {
      const polyline = L.polyline(playerTrails[player.id], {
        color: TEAM_COLORS[player.team] || '#888888',
        weight: 3,
        opacity: 0.5
      }).addTo(map);
      // Keep only the latest polyline per player to avoid clutter
      if (playerMarkers[player.id].currentTrail) {
        map.removeLayer(playerMarkers[player.id].currentTrail);
      }
      playerMarkers[player.id].currentTrail = polyline;
    }

    // Shoot indicator: API does not currently expose a firing/shooting state.
    // To enable this feature, the backend must include a boolean such as
    // `isShooting` or `firing` on each player object in /api/games/{gameId}.
    if (player.isShooting === true) {
      createShootingFlash(player.lat, player.lon);
    }
  });

  Object.keys(playerMarkers).forEach(id => {
    if (!currentIds.has(id)) {
      if (playerMarkers[id].currentTrail) {
        map.removeLayer(playerMarkers[id].currentTrail);
      }
      map.removeLayer(playerMarkers[id]);
      delete playerMarkers[id];
      delete playerTrails[id];
      delete healthHistory[id];
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

  // Auto-center on average player position
  if (autoCenterEnabled && players.length > 0) {
    const latSum = players.reduce((sum, p) => sum + (p.lat || 0), 0);
    const lonSum = players.reduce((sum, p) => sum + (p.lon || 0), 0);
    const latAvg = latSum / players.length;
    const lonAvg = lonSum / players.length;
    map.panTo([latAvg, lonAvg], { animate: true });
  } else if (bounds.length > 0) {
    // only fit bounds on first load when auto-center is off
    if (!window._initialFitDone) {
      map.fitBounds(bounds, { padding: [40, 40], maxZoom: 18 });
      window._initialFitDone = true;
    }
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

    detectEvents(game);
    lastGame = game;

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

function createControlBar() {
  const header = document.querySelector('.header-right');
  if (!header) return;

  const autoCenterBtn = document.createElement('button');
  autoCenterBtn.id = 'auto-center-toggle';
  autoCenterBtn.className = 'toggle-btn';
  autoCenterBtn.title = 'Auto-center on players';
  autoCenterBtn.textContent = `🎯 ${autoCenterEnabled ? 'ON' : 'OFF'}`;
  autoCenterBtn.addEventListener('click', () => {
    autoCenterEnabled = !autoCenterEnabled;
    localStorage.setItem('nerfAutoCenter', autoCenterEnabled);
    autoCenterBtn.textContent = `🎯 ${autoCenterEnabled ? 'ON' : 'OFF'}`;
    autoCenterBtn.classList.toggle('active', autoCenterEnabled);
  });
  autoCenterBtn.classList.toggle('active', autoCenterEnabled);

  const notifyBtn = document.createElement('button');
  notifyBtn.id = 'notify-toggle';
  notifyBtn.className = 'toggle-btn';
  notifyBtn.title = 'Desktop notifications';
  notifyBtn.textContent = `🔔 ${notificationsEnabled ? 'ON' : 'OFF'}`;
  notifyBtn.addEventListener('click', async () => {
    if (!('Notification' in window)) {
      alert('Notifications are not supported in this browser.');
      return;
    }
    if (!notificationsEnabled && Notification.permission !== 'granted') {
      const permission = await Notification.requestPermission();
      if (permission !== 'granted') {
        alert('Notification permission was denied.');
        return;
      }
    }
    notificationsEnabled = !notificationsEnabled;
    localStorage.setItem('nerfNotifications', notificationsEnabled);
    notifyBtn.textContent = `🔔 ${notificationsEnabled ? 'ON' : 'OFF'}`;
    notifyBtn.classList.toggle('active', notificationsEnabled);
  });
  notifyBtn.classList.toggle('active', notificationsEnabled);

  header.insertBefore(autoCenterBtn, header.firstChild);
  header.insertBefore(notifyBtn, header.firstChild);
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

initMap();
createControlBar();
initTheme();
document.getElementById('theme-toggle')?.addEventListener('click', toggleTheme);
document.getElementById('clear-events')?.addEventListener('click', () => {
  const feed = document.getElementById('event-feed');
  if (feed) feed.innerHTML = '';
});
document.querySelector('.modal-close')?.addEventListener('click', closePlayerModal);
document.querySelector('.modal-backdrop')?.addEventListener('click', closePlayerModal);
loadGame();
setInterval(loadGame, 2000);
