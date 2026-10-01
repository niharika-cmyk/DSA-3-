/* ============================================================
   CineMatch — app.js
   ============================================================ */

const API = '';

// ── Gradient Wave Background ─────────────────────────────────
// Soft pastel palette — very subtle on the white hero section
const WAVE_COLORS = [
  "#dbeafe",  // light blue
  "#ffffff",  // white
  "#ede9fe",  // light lavender
  "#fce7f3",  // light pink
  "#d1fae5",  // light mint
  "#ffffff",  // white
];

let waveInstance = null;

function initWave() {
  const canvas = document.getElementById('gradient-canvas');
  if (!canvas || waveInstance) return;
  try {
    waveInstance = new GradientWave(canvas, WAVE_COLORS, {
      noiseSpeed: 0.0000028,      // very slow drift
      noiseFreq:  [0.00012, 0.00025],
      deform: {
        incline:     0,
        offsetTop:   -0.5,
        offsetBottom:-0.5,
        noiseFreq:   [2.5, 3.5],
        noiseAmp:    120,           // gentle waves
        noiseSpeed:  6,
        noiseFlow:   1.5,
        noiseSeed:   5,
      }
    });
    waveInstance.start();
  } catch (e) {
    // WebGL not available — hero still looks fine without it
    console.warn('GradientWave: WebGL unavailable', e);
  }
}

function startWave()  { waveInstance && waveInstance.start(); }
function pauseWave()  { waveInstance && waveInstance.stop(); }


// Poster gradient classes (matches CSS)
const POSTER_CLASSES = [
  'poster-0','poster-1','poster-2','poster-3','poster-4','poster-5',
  'poster-6','poster-7','poster-8','poster-9','poster-10','poster-11'
];

function posterClass(movie) {
  return POSTER_CLASSES[Math.abs(movie.id || 0) % POSTER_CLASSES.length];
}

function initial(movie) {
  return (movie.title || '?').trim().charAt(0).toUpperCase();
}

function movieYear(movie) {
  return movie.releaseDate ? movie.releaseDate.substring(0, 4) : '';
}

function movieRating(movie) {
  return movie.rating ? movie.rating.toFixed(1) : '–';
}

function movieGenres(movie) {
  return (movie.genres || []).slice(0, 2);
}

// ── DOM refs ─────────────────────────────────────────────────
const searchInput    = document.getElementById('search-input');
const searchClear    = document.getElementById('search-clear');
const searchDropdown = document.getElementById('search-dropdown');
const browseSection  = document.getElementById('browse-section');
const searchSection  = document.getElementById('search-section');
const detailPanel    = document.getElementById('detail-panel');
const detailCard     = document.getElementById('detail-card');
const recsGrid       = document.getElementById('recs-grid');
const recsSub        = document.getElementById('recs-sub');
const topGrid        = document.getElementById('top-grid');
const searchGrid     = document.getElementById('search-grid');
const searchTitle    = document.getElementById('search-results-title');
const backBtn        = document.getElementById('back-btn');
const spinner        = document.getElementById('spinner');
const heroSection    = document.getElementById('hero-section');
const logoHome       = document.getElementById('logo-home');

// ── State ─────────────────────────────────────────────────────
let searchTimer = null;
let currentView = 'browse';

// ── Spinner ───────────────────────────────────────────────────
function showSpinner() { spinner.classList.add('active'); }
function hideSpinner() { spinner.classList.remove('active'); }

// ── API ───────────────────────────────────────────────────────
async function apiFetch(path) {
  const res = await fetch(API + path);
  if (!res.ok) throw new Error(res.status);
  return res.json();
}

// ── Card builder ──────────────────────────────────────────────
function buildCard(movie, opts = {}, index = null) {
  const card = document.createElement('div');
  card.className = 'movie-card';
  card.setAttribute('data-id', movie.id);

  const pc = posterClass(movie);
  const letter = initial(movie);
  const yr = movieYear(movie);
  const rating = movieRating(movie);
  const genres = movieGenres(movie);

  const genreTags = genres
    .map(g => `<span class="card-genre">${g}</span>`)
    .join('');

  const scoreBadge = opts.score != null
    ? `<div class="card-score-badge">${opts.score.toFixed(1)}</div>`
    : '';

  const delay = index !== null ? `style="animation-delay:${Math.min(index * 0.04, 0.4)}s"` : '';

  card.innerHTML = `
    <div class="card-poster ${pc}">
      <span class="card-initial">${letter}</span>
      <img src="/api/movies/${movie.id}/poster" class="card-poster-img" onload="if(this.naturalWidth>1) this.classList.add('loaded')" onerror="this.style.display='none'">
      <div class="card-rating-badge">${rating}</div>
      ${scoreBadge}
    </div>
    <div class="card-body">
      <div class="card-title">${movie.title || 'Untitled'}</div>
      <div class="card-meta">
        <span class="card-year">${yr}</span>
        ${genres.length ? '&nbsp;&middot;&nbsp;' : ''}
        ${genreTags}
      </div>
    </div>
  `;

  if (delay) card.setAttribute('style', `animation-delay:${Math.min((index || 0) * 0.04, 0.4)}s`);
  card.addEventListener('click', () => showDetail(movie.id));
  return card;
}

// ── Render grid ───────────────────────────────────────────────
function renderGrid(container, movies, scoreMap = {}) {
  container.innerHTML = '';
  if (!movies || movies.length === 0) {
    container.innerHTML = '<div class="empty-state">No titles found.</div>';
    return;
  }
  movies.forEach((m, i) => {
    container.appendChild(buildCard(m, scoreMap[m.id] || {}, i));
  });
}

// ── View management ───────────────────────────────────────────
function showBrowse() {
  currentView = 'browse';
  browseSection.style.display  = '';
  searchSection.style.display  = 'none';
  detailPanel.style.display    = 'none';
  heroSection.style.display    = '';
  searchInput.value = '';
  searchClear.classList.remove('visible');
  closeDropdown();
  startWave();
}

function showSearchResults(movies, query) {
  currentView = 'search';
  browseSection.style.display  = 'none';
  searchSection.style.display  = '';
  detailPanel.style.display    = 'none';
  heroSection.style.display    = 'none';
  pauseWave();
  searchTitle.textContent = `"${query}" — ${movies.length} result${movies.length !== 1 ? 's' : ''}`;
  renderGrid(searchGrid, movies);
}

async function showDetail(id) {
  showSpinner();
  try {
    const [movie, recsData] = await Promise.all([
      apiFetch(`/api/movies/${id}`),
      apiFetch(`/api/movies/${id}/recommendations`)
    ]);

    renderDetail(movie);
    renderRecommendations(recsData, movie);

    currentView = 'detail';
    browseSection.style.display = 'none';
    searchSection.style.display = 'none';
    detailPanel.style.display   = '';
    heroSection.style.display   = 'none';
    pauseWave();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  } catch (e) {
    console.error(e);
  } finally {
    hideSpinner();
    closeDropdown();
  }
}

// ── Detail render ─────────────────────────────────────────────
function renderDetail(movie) {
  const pc = posterClass(movie);
  const letter = initial(movie);
  const yr = movieYear(movie);
  const rating = movieRating(movie);
  const genres = movie.genres || [];
  const genreMask = encodeGenreMask(genres);
  const binaryStr = genreMask.toString(2).padStart(20, '0');

  const genreTags = genres
    .map(g => `<span class="detail-genre-tag">${g}</span>`)
    .join('');

  detailCard.innerHTML = `
    <div class="detail-poster-block ${pc}">
      <span class="detail-initial">${letter}</span>
      <img src="/api/movies/${movie.id}/poster" class="detail-poster-img" onload="if(this.naturalWidth>1) this.classList.add('loaded')" onerror="this.style.display='none'">
    </div>
    <div class="detail-info">
      <h1 class="detail-title">${movie.title || 'Untitled'}</h1>
      <div class="detail-meta-row">
        <span class="detail-meta-item"><strong>${yr}</strong></span>
        <span class="detail-meta-item">Rating <strong>${rating}/10</strong></span>
        ${movie.runtime ? `<span class="detail-meta-item"><strong>${movie.runtime} min</strong></span>` : ''}
        <span class="detail-meta-item">Popularity <strong>${movie.popularity ? movie.popularity.toFixed(0) : '–'}</strong></span>
      </div>
      <div class="detail-genres">${genreTags}</div>
      <p class="detail-overview">${movie.overview || 'No overview available.'}</p>
      
      <a href="https://www.themoviedb.org/movie/${movie.id}/watch" target="_blank" class="watch-btn">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
          <path d="M8 5v14l11-7z"/>
        </svg>
        Find where to watch
      </a>
    </div>
  `;
}

function encodeGenreMask(genres) {
  const INDEX = [
    'Action','Adventure','Animation','Comedy','Crime','Documentary','Drama','Family',
    'Fantasy','History','Horror','Music','Mystery','Romance','Science Fiction',
    'TV Movie','Thriller','War','Western','Foreign'
  ];
  let mask = 0;
  genres.forEach(g => {
    const i = INDEX.findIndex(x => x.toLowerCase() === g.toLowerCase());
    if (i >= 0) mask |= (1 << i);
  });
  return mask;
}

// ── Recommendations render ────────────────────────────────────
function renderRecommendations(recsData, queryMovie) {
  recsGrid.innerHTML = '';
  if (!recsData || recsData.length === 0) {
    recsGrid.innerHTML = '<div class="empty-state">No recommendations found.</div>';
    recsSub.textContent = '';
    return;
  }
  const genres = (queryMovie.genres || []).slice(0, 4).join(', ');
  recsSub.textContent = `Based on: ${genres}. Ranked by recommendation engine.`;

  const scoreMap = {};
  recsData.forEach(e => { scoreMap[e.movie.id] = { score: e.score }; });

  recsData.forEach((entry, i) => {
    recsGrid.appendChild(buildCard(entry.movie, { score: entry.score }, i));
  });
}

// ── Top movies ────────────────────────────────────────────────
async function loadTopMovies() {
  showSpinner();
  try {
    const movies = await apiFetch('/api/movies/top');
    renderGrid(topGrid, movies);
  } catch (e) {
    topGrid.innerHTML = '<div class="empty-state">Could not connect to server.</div>';
  } finally {
    hideSpinner();
  }
}

// ── Search ────────────────────────────────────────────────────
async function doSearch(query) {
  if (!query.trim()) { closeDropdown(); return; }
  try {
    const movies = await apiFetch(`/api/movies/search?q=${encodeURIComponent(query)}`);
    renderDropdown(movies, query);
  } catch (e) { /* silent */ }
}

function renderDropdown(movies, query) {
  searchDropdown.innerHTML = '';
  if (!movies || movies.length === 0) {
    searchDropdown.innerHTML = `<div class="dropdown-empty">No results for "${query}"</div>`;
    searchDropdown.classList.add('open');
    return;
  }

  movies.slice(0, 7).forEach(movie => {
    const item = document.createElement('div');
    item.className = 'dropdown-item';
    const pc = posterClass(movie);
    item.innerHTML = `
      <div class="dropdown-thumb ${pc}">
        <span class="dropdown-thumb-initial">${initial(movie)}</span>
        <img src="/api/movies/${movie.id}/poster" class="dropdown-poster-img" onload="if(this.naturalWidth>1) this.classList.add('loaded')" onerror="this.style.display='none'">
      </div>
      <div class="dropdown-info">
        <div class="dropdown-title">${highlightMatch(movie.title, query)}</div>
        <div class="dropdown-meta">${movieYear(movie)}${movie.genres?.length ? ' · ' + movie.genres.slice(0, 2).join(', ') : ''}</div>
      </div>
      <span class="dropdown-rating">${movieRating(movie)}</span>
    `;
    item.addEventListener('click', () => showDetail(movie.id));
    searchDropdown.appendChild(item);
  });

  if (movies.length > 7) {
    const more = document.createElement('div');
    more.className = 'dropdown-see-all';
    more.textContent = `View all ${movies.length} results`;
    more.addEventListener('click', () => { closeDropdown(); showSearchResults(movies, query); });
    searchDropdown.appendChild(more);
  }

  searchDropdown.classList.add('open');
}

function highlightMatch(text, query) {
  if (!query) return text;
  const i = text.toLowerCase().indexOf(query.toLowerCase());
  if (i === -1) return text;
  return text.substring(0, i)
    + `<mark>${text.substring(i, i + query.length)}</mark>`
    + text.substring(i + query.length);
}

function closeDropdown() {
  searchDropdown.classList.remove('open');
  searchDropdown.innerHTML = '';
}

// ── Events ────────────────────────────────────────────────────
searchInput.addEventListener('input', () => {
  const q = searchInput.value.trim();
  searchClear.classList.toggle('visible', q.length > 0);
  clearTimeout(searchTimer);
  if (!q) { closeDropdown(); return; }
  searchTimer = setTimeout(() => doSearch(q), 260);
});

searchInput.addEventListener('keydown', async (e) => {
  if (e.key === 'Enter') {
    const q = searchInput.value.trim();
    if (!q) return;
    closeDropdown();
    showSpinner();
    try {
      const movies = await apiFetch(`/api/movies/search?q=${encodeURIComponent(q)}`);
      showSearchResults(movies, q);
    } finally { hideSpinner(); }
  }
  if (e.key === 'Escape') { closeDropdown(); searchInput.blur(); }
});

searchClear.addEventListener('click', () => {
  searchInput.value = '';
  searchClear.classList.remove('visible');
  closeDropdown();
  if (currentView === 'search') showBrowse();
});

document.addEventListener('click', (e) => {
  if (!e.target.closest('.search-wrap')) closeDropdown();
});

backBtn.addEventListener('click', () => {
  const q = searchInput.value.trim();
  if (q) {
    apiFetch(`/api/movies/search?q=${encodeURIComponent(q)}`)
      .then(movies => showSearchResults(movies, q))
      .catch(() => showBrowse());
  } else {
    showBrowse();
  }
});

logoHome.addEventListener('click', (e) => { e.preventDefault(); showBrowse(); });

// ── Init ──────────────────────────────────────────────────────
initWave();
loadTopMovies();
