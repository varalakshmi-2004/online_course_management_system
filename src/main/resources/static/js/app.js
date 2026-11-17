/**
 * EDUHUB PRO - PRODUCTION JAVASCRIPT APPLICATION CORE
 * Enterprise Online Course Management System
 */

const API_BASE = '';

// Global Application State
const state = {
  currentUser: null,
  activeView: 'courses',
  activeStudentTab: 'activeCourses',
  activeInstructorTab: 'coursesList',
  courses: [],
  categories: ['All', 'Software Engineering', 'Artificial Intelligence', 'Cloud Computing', 'Data Science'],
  activeCategory: 'All',
  searchQuery: '',
  studentProgress: null,
  instructorCourses: [],
  instructorStudents: [],
  activeEnrollment: null,
  activeCourse: null,
  activeCurriculumCourseId: null,
  activeClassroomContents: [],
  activeClassroomContentsMap: {},
  activeCurriculumItemsMap: {},
  notifications: []
};

// ============================================================================
// INITIALIZATION
// ============================================================================
document.addEventListener('DOMContentLoaded', () => {
  initSession();
  initEventListeners();
  loadCourses();
});

function initSession() {
  const saved = localStorage.getItem('eduhub_user');
  if (saved) {
    try {
      state.currentUser = JSON.parse(saved);
      updateNavbarSessionUI();
    } catch (e) {
      localStorage.removeItem('eduhub_user');
    }
  }
}

function initEventListeners() {
  // Global search input
  const searchInput = document.getElementById('globalSearchInput');
  if (searchInput) {
    searchInput.addEventListener('input', debounce((e) => {
      state.searchQuery = e.target.value.trim();
      loadCourses();
    }, 250));
  }
}

// Utility: Debounce
function debounce(fn, delay) {
  let timeoutId;
  return function (...args) {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => fn.apply(this, args), delay);
  };
}

// HTTP Helper with JWT Authentication
async function apiRequest(endpoint, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  };

  if (state.currentUser && state.currentUser.token) {
    headers['Authorization'] = `Bearer ${state.currentUser.token}`;
  }

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers
  });

  const contentType = response.headers.get('content-type');
  let data = null;
  if (contentType && contentType.includes('application/json')) {
    data = await response.json();
  } else {
    data = await response.text();
  }

  if (!response.ok) {
    const errorMsg = (data && data.message) ? data.message : (typeof data === 'string' ? data : 'API Request Failed');
    throw new Error(errorMsg);
  }

  return data;
}

// ============================================================================
// NAVIGATION & VIEW SWITCHING
// ============================================================================
function navigateTo(viewName) {
  state.activeView = viewName;

  // Update navbar links
  document.querySelectorAll('.nav-link-btn').forEach(btn => btn.classList.remove('active'));
  const activeBtn = document.getElementById(`nav-${viewName}`);
  if (activeBtn) activeBtn.classList.add('active');

  // Hide all sections, show active
  document.querySelectorAll('.view-section').forEach(sec => sec.classList.remove('active'));
  const activeSec = document.getElementById(`view-${viewName}`);
  if (activeSec) activeSec.classList.add('active');

  // Load view-specific data
  if (viewName === 'courses') {
    loadCourses();
  } else if (viewName === 'student') {
    loadStudentDashboard();
  } else if (viewName === 'instructor') {
    loadInstructorStudio();
  } else if (viewName === 'notifications') {
    loadNotifications();
  }

  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function updateNavbarSessionUI() {
  const guestActions = document.getElementById('guestActions');
  const userActions = document.getElementById('userActions');
  const navStudent = document.getElementById('nav-student');
  const navInstructor = document.getElementById('nav-instructor');
  const notifBell = document.getElementById('notifBellWrapper');

  if (state.currentUser) {
    guestActions.style.display = 'none';
    userActions.style.display = 'flex';
    notifBell.style.display = 'flex';

    document.getElementById('userNameLabel').innerText = state.currentUser.name || state.currentUser.username;
    
    // User initials avatar
    const name = state.currentUser.name || state.currentUser.username || 'User';
    const initials = name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
    document.getElementById('userAvatarInitials').innerText = initials;

    const isInstructor = state.currentUser.role === 'ROLE_INSTRUCTOR' || state.currentUser.role === 'ROLE_ADMIN';
    const isStudent = state.currentUser.role === 'ROLE_STUDENT';

    document.getElementById('userRoleLabel').innerText = isInstructor ? 'Instructor' : 'Student';
    navStudent.style.display = isStudent ? 'inline-flex' : 'none';
    navInstructor.style.display = isInstructor ? 'inline-flex' : 'none';

    checkUnreadNotifications();
  } else {
    guestActions.style.display = 'flex';
    userActions.style.display = 'none';
    navStudent.style.display = 'none';
    navInstructor.style.display = 'none';
    notifBell.style.display = 'none';
  }
}

// ============================================================================
// AUTHENTICATION MODULE
// ============================================================================
let currentAuthTab = 'login';

function openAuthModal(mode = 'login') {
  currentAuthTab = mode;
  switchAuthTab(mode);
  document.getElementById('authModalBackdrop').classList.add('active');
}

function closeAuthModal() {
  document.getElementById('authModalBackdrop').classList.remove('active');
}

function switchAuthTab(tab) {
  currentAuthTab = tab;
  document.getElementById('loginTabBtn').classList.toggle('active', tab === 'login');
  document.getElementById('registerTabBtn').classList.toggle('active', tab === 'register');

  document.getElementById('loginFormContainer').style.display = tab === 'login' ? 'block' : 'none';
  document.getElementById('registerFormContainer').style.display = tab === 'register' ? 'block' : 'none';
  document.getElementById('authErrorBox').style.display = 'none';
}

function toggleInstructorFields() {
  const role = document.getElementById('regRoleSelect').value;
  document.getElementById('instructorSpecGroup').style.display = role === 'ROLE_INSTRUCTOR' ? 'block' : 'none';
}

async function handleLoginSubmit(e) {
  if (e) e.preventDefault();
  const username = document.getElementById('loginEmailInput').value.trim();
  const password = document.getElementById('loginPasswordInput').value;

  try {
    const res = await apiRequest('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password })
    });

    state.currentUser = res;
    localStorage.setItem('eduhub_user', JSON.stringify(res));
    closeAuthModal();
    updateNavbarSessionUI();
    showToast('Signed In', `Welcome back, ${res.name || res.username}!`, 'success');

    if (res.role === 'ROLE_INSTRUCTOR') {
      navigateTo('instructor');
    } else {
      navigateTo('student');
    }
  } catch (err) {
    showAuthError(err.message);
  }
}

async function handleRegisterSubmit(e) {
  if (e) e.preventDefault();
  const name = document.getElementById('regNameInput').value.trim();
  const email = document.getElementById('regEmailInput').value.trim();
  const password = document.getElementById('regPasswordInput').value;
  const contact = document.getElementById('regContactInput').value.trim();
  const role = document.getElementById('regRoleSelect').value;
  const specialization = document.getElementById('regSpecInput').value.trim();

  if (!name || !email || !password) {
    showAuthError('Please fill in all required fields.');
    return;
  }

  try {
    const res = await apiRequest('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ name, email, password, contact, role, specialization })
    });

    state.currentUser = res;
    localStorage.setItem('eduhub_user', JSON.stringify(res));
    closeAuthModal();
    updateNavbarSessionUI();
    showToast('Account Created', `Welcome to EduHub, ${res.name}!`, 'success');

    if (res.role === 'ROLE_INSTRUCTOR') {
      navigateTo('instructor');
    } else {
      navigateTo('student');
    }
  } catch (err) {
    showAuthError(err.message);
  }
}

function showAuthError(msg) {
  const errBox = document.getElementById('authErrorBox');
  errBox.innerText = msg;
  errBox.style.display = 'block';
}

async function loginAsDemo(roleType) {
  const creds = roleType === 'instructor' 
    ? { username: 'instructor@learn.com', password: 'Password@123' }
    : { username: 'student@learn.com', password: 'Password@123' };

  try {
    const res = await apiRequest('/auth/login', {
      method: 'POST',
      body: JSON.stringify(creds)
    });

    state.currentUser = res;
    localStorage.setItem('eduhub_user', JSON.stringify(res));
    updateNavbarSessionUI();
    showToast('Demo Session Active', `Switched to ${roleType.toUpperCase()} profile (${res.name})`, 'info');

    if (roleType === 'instructor') {
      navigateTo('instructor');
    } else {
      navigateTo('student');
    }
  } catch (err) {
    showToast('Demo Login Failed', err.message, 'error');
  }
}

function handleLogout() {
  state.currentUser = null;
  localStorage.removeItem('eduhub_user');
  updateNavbarSessionUI();
  navigateTo('courses');
  showToast('Logged Out', 'You have been successfully signed out.', 'info');
}

// ============================================================================
// COURSE MODULE (DISCOVERY & CATALOG)
// ============================================================================
async function loadCourses() {
  try {
    let url = '/courses';
    const params = new URLSearchParams();
    if (state.searchQuery) params.append('search', state.searchQuery);
    if (state.activeCategory && state.activeCategory !== 'All') {
      params.append('category', state.activeCategory);
    }
    if (params.toString()) url += `?${params.toString()}`;

    const courses = await apiRequest(url);
    state.courses = courses;
    renderCoursesGrid();
    renderCategoryPills();
  } catch (err) {
    console.error('Failed to load courses', err);
    showToast('Error', 'Unable to retrieve course catalog.', 'error');
  }
}

function renderCategoryPills() {
  const container = document.getElementById('categoryPillsContainer');
  if (!container) return;

  container.innerHTML = state.categories.map(cat => `
    <button class="chip-btn ${state.activeCategory === cat ? 'active' : ''}" onclick="selectCategory('${cat}')">
      ${cat}
    </button>
  `).join('');
}

function selectCategory(cat) {
  state.activeCategory = cat;
  renderCategoryPills();
  loadCourses();
}

function renderCoursesGrid() {
  const grid = document.getElementById('coursesCatalogGrid');
  if (!grid) return;

  if (!state.courses || state.courses.length === 0) {
    grid.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 4rem 1rem; color: var(--slate-500);">
        <div style="font-size: 3rem; margin-bottom: 0.75rem;">🔍</div>
        <h3 style="font-size: 1.25rem; font-weight: 700; color: var(--slate-800);">No Courses Found</h3>
        <p style="margin-top: 0.25rem;">Try adjusting your keyword search or category filter.</p>
      </div>
    `;
    return;
  }

  // Pre-determined gradient palettes for cards
  const gradients = [
    'linear-gradient(135deg, #4f46e5, #06b6d4)',
    'linear-gradient(135deg, #059669, #10b981)',
    'linear-gradient(135deg, #7c3aed, #a855f7)',
    'linear-gradient(135deg, #ea580c, #f59e0b)',
    'linear-gradient(135deg, #0284c7, #38bdf8)'
  ];

  grid.innerHTML = state.courses.map((course, idx) => {
    const bgGrad = gradients[idx % gradients.length];
    const contentCount = course.contents ? course.contents.length : 3;

    return `
      <div class="course-card-pro">
        <div class="course-card-banner" style="background: ${bgGrad};">
          <span class="course-badge-category">${escapeHtml(course.category || 'General')}</span>
          <span style="color: white; font-size: 1.25rem;">★ 4.9</span>
        </div>
        <div class="course-card-body">
          <h3 class="course-card-title">${escapeHtml(course.title)}</h3>
          <p class="course-card-description">${escapeHtml(course.description || 'Comprehensive curriculum covering practical implementation and core principles.')}</p>
          
          <div class="course-instructor-row">
            <div class="instructor-avatar-mini">${(course.instructor || 'I')[0].toUpperCase()}</div>
            <span class="instructor-name-label">Instructor: <strong>${escapeHtml(course.instructor || 'Staff')}</strong></span>
          </div>

          <div class="course-meta-tags">
            <div class="course-meta-item">
              <span>📚</span>
              <span>${contentCount} Lessons</span>
            </div>
            <div class="course-meta-item">
              <span>⏱</span>
              <span>Approx. 4 hrs</span>
            </div>
            <div class="course-meta-item">
              <span>📜</span>
              <span>Certificate</span>
            </div>
          </div>

          <div class="course-card-actions">
            <button class="btn-solid-primary" style="flex: 1;" onclick="handleEnrollClick(${course.id})">
              Enroll Now
            </button>
            <button class="btn-solid-secondary" onclick="openCurriculumPreview(${course.id}, '${escapeHtml(course.title)}')">
              Syllabus
            </button>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

async function handleEnrollClick(courseId) {
  if (!state.currentUser) {
    openAuthModal('login');
    showToast('Authentication Required', 'Please sign in or create an account to enroll.', 'info');
    return;
  }

  if (state.currentUser.role !== 'ROLE_STUDENT') {
    showToast('Role Restriction', 'Instructors cannot enroll in courses. Please sign in with a Student account.', 'warning');
    return;
  }

  try {
    const studentId = state.currentUser.profileId;
    await apiRequest(`/enrollments/student/${studentId}/course/${courseId}`, {
      method: 'POST'
    });

    showToast('Enrollment Successful!', 'Course added to "My Learning". Starting your learning journey!', 'success');
    navigateTo('student');
  } catch (err) {
    showToast('Enrollment Notice', err.message, 'warning');
  }
}

// ============================================================================
// STUDENT MODULE ("MY LEARNING")
// ============================================================================
async function loadStudentDashboard() {
  if (!state.currentUser || !state.currentUser.profileId) return;
  const studentId = state.currentUser.profileId;

  try {
    // 1. Fetch Student Profile Details
    const student = await apiRequest(`/students/${studentId}`);
    document.getElementById('studentProfileName').value = student.name || '';
    document.getElementById('studentProfileEmail').value = student.email || '';
    document.getElementById('studentProfileContact').value = student.contact || '';

    // 2. Fetch Progress & Enrolled Courses
    const progress = await apiRequest(`/students/${studentId}/progress`);
    state.studentProgress = progress;

    // Update KPI Stat Cards
    document.getElementById('kpiTotalEnrolled').innerText = progress.totalEnrolledCourses || 0;
    document.getElementById('kpiOngoingCount').innerText = progress.ongoingCoursesCount || 0;
    document.getElementById('kpiCompletedCount').innerText = progress.completedCoursesCount || 0;
    document.getElementById('kpiAvgProgress').innerText = `${Math.round(progress.overallAverageProgress || 0)}%`;

    // Render Sub-tabs
    renderStudentActiveCourses();
    renderStudentCompletedCourses();
    renderStudentHistoryTable();
  } catch (err) {
    console.error('Failed to load student dashboard', err);
    showToast('Dashboard Error', 'Could not refresh student progress data.', 'error');
  }
}

function switchStudentTab(tabName) {
  state.activeStudentTab = tabName;
  document.querySelectorAll('#studentTabsContainer .tab-btn').forEach(btn => btn.classList.remove('active'));
  document.getElementById(`tabBtn-${tabName}`).classList.add('active');

  document.querySelectorAll('#studentTabPanels .tab-content-panel').forEach(p => p.classList.remove('active'));
  document.getElementById(`panel-${tabName}`).classList.add('active');
}

function renderStudentActiveCourses() {
  const container = document.getElementById('studentActiveGrid');
  if (!container) return;

  const activeCourses = (state.studentProgress?.courses || []).filter(c => c.status === 'ONGOING');

  if (activeCourses.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 3rem 1rem; color: var(--slate-500); background: white; border-radius: 16px; border: 1px dashed var(--border-subtle);">
        <p style="font-size: 1.1rem; font-weight: 600; color: var(--slate-700);">No active courses in progress</p>
        <p style="margin: 0.5rem 0 1.25rem 0; font-size: 0.9rem;">Browse the catalog to discover new skills and enroll today.</p>
        <button class="btn-solid-primary" onclick="navigateTo('courses')">Explore Courses</button>
      </div>
    `;
    return;
  }

  container.innerHTML = activeCourses.map(c => `
    <div class="course-card-pro" style="padding: 1.5rem;">
      <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.5rem;">
        <span class="course-badge-category">${escapeHtml(c.category || 'Course')}</span>
        <span class="badge-pill badge-ongoing">Ongoing</span>
      </div>
      <h3 style="font-size: 1.15rem; font-weight: 700; color: var(--slate-900); margin-bottom: 0.4rem;">${escapeHtml(c.courseTitle)}</h3>
      <p style="font-size: 0.85rem; color: var(--slate-500); margin-bottom: 1.2rem;">Instructor: ${escapeHtml(c.instructor || 'Staff')}</p>

      <div style="margin-bottom: 1.25rem;">
        <div style="display: flex; justify-content: space-between; font-size: 0.82rem; font-weight: 700; margin-bottom: 0.3rem;">
          <span style="color: var(--slate-700);">Course Progress</span>
          <span style="color: var(--primary-600);">${c.progressPercentage}%</span>
        </div>
        <div class="progress-track-subtle" style="height: 9px;">
          <div class="progress-bar-fill" style="width: ${c.progressPercentage}%;"></div>
        </div>
      </div>

      <div style="display: flex; justify-content: space-between; align-items: center; margin-top: auto;">
        <span style="font-size: 0.8rem; color: var(--slate-500);">Grade: <strong>${c.grade || 'In Progress'}</strong></span>
        <button class="btn-solid-primary" onclick="openClassroomPlayer(${c.enrollmentId}, ${c.courseId}, '${escapeHtml(c.courseTitle)}', ${c.progressPercentage})">
          Resume Course ➔
        </button>
      </div>
    </div>
  `).join('');
}

function renderStudentCompletedCourses() {
  const container = document.getElementById('studentCompletedGrid');
  if (!container) return;

  const completed = (state.studentProgress?.courses || []).filter(c => c.status === 'COMPLETED');

  if (completed.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 3rem 1rem; color: var(--slate-500); background: white; border-radius: 16px; border: 1px dashed var(--border-subtle);">
        <p style="font-size: 1.1rem; font-weight: 600; color: var(--slate-700);">No completed courses yet</p>
        <p style="margin: 0.5rem 0; font-size: 0.9rem;">Complete all lessons and quizzes to earn official certificates!</p>
      </div>
    `;
    return;
  }

  container.innerHTML = completed.map(c => `
    <div class="course-card-pro" style="padding: 1.5rem; border-color: #86efac;">
      <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.5rem;">
        <span class="course-badge-category">${escapeHtml(c.category || 'Certified')}</span>
        <span class="badge-pill badge-completed">✓ Completed</span>
      </div>
      <h3 style="font-size: 1.15rem; font-weight: 700; color: var(--slate-900); margin-bottom: 0.4rem;">${escapeHtml(c.courseTitle)}</h3>
      <p style="font-size: 0.85rem; color: var(--slate-500); margin-bottom: 1rem;">Completion Date: <strong>${c.completionDate || 'Recent'}</strong></p>

      <div style="background: var(--slate-50); border: 1px solid var(--border-subtle); border-radius: 10px; padding: 0.85rem; margin-bottom: 1.25rem;">
        <div style="display: flex; justify-content: space-between; font-size: 0.85rem;">
          <span>Final Score:</span>
          <strong>${c.score != null ? c.score + '%' : '100%'}</strong>
        </div>
        <div style="display: flex; justify-content: space-between; font-size: 0.85rem; margin-top: 0.3rem;">
          <span>Honors Grade:</span>
          <strong style="color: var(--accent-emerald);">${c.grade || 'A (Excellent)'}</strong>
        </div>
      </div>

      <button class="btn-solid-secondary" style="width: 100%; border-color: var(--primary-500); color: var(--primary-600);"
              onclick="showCertificateModal('${escapeHtml(c.courseTitle)}', '${c.completionDate || 'November 2026'}', '${c.grade || 'A (Excellent)'}')">
        📜 View Official Certificate
      </button>
    </div>
  `).join('');
}

function renderStudentHistoryTable() {
  const tbody = document.getElementById('studentHistoryTableBody');
  if (!tbody) return;

  const allCourses = state.studentProgress?.courses || [];

  if (allCourses.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; padding: 2rem; color: var(--slate-500);">No enrollment history recorded.</td></tr>`;
    return;
  }

  tbody.innerHTML = allCourses.map(c => `
    <tr>
      <td><strong>${escapeHtml(c.courseTitle)}</strong></td>
      <td><span class="course-badge-category">${escapeHtml(c.category || '-')}</span></td>
      <td>${c.enrollmentDate || '-'}</td>
      <td>${c.completionDate || '-'}</td>
      <td><span class="badge-pill ${c.status === 'COMPLETED' ? 'badge-completed' : 'badge-ongoing'}">${c.status}</span></td>
      <td>
        <div class="progress-container-inline">
          <div style="font-size: 0.78rem; font-weight: 700;">${c.progressPercentage}%</div>
          <div class="progress-track-subtle">
            <div class="progress-bar-fill" style="width: ${c.progressPercentage}%;"></div>
          </div>
        </div>
      </td>
      <td><strong>${c.grade || (c.score != null ? c.score + '%' : '-')}</strong></td>
    </tr>
  `).join('');
}

async function saveStudentProfileChanges() {
  if (!state.currentUser || !state.currentUser.profileId) return;
  const studentId = state.currentUser.profileId;
  const name = document.getElementById('studentProfileName').value.trim();
  const contact = document.getElementById('studentProfileContact').value.trim();

  try {
    await apiRequest(`/students/${studentId}`, {
      method: 'PUT',
      body: JSON.stringify({ name, contact })
    });

    state.currentUser.name = name;
    localStorage.setItem('eduhub_user', JSON.stringify(state.currentUser));
    updateNavbarSessionUI();
    showToast('Profile Updated', 'Your personal details have been saved.', 'success');
  } catch (err) {
    showToast('Update Failed', err.message, 'error');
  }
}

// ============================================================================
// VIDEO EMBED & STREAM PARSER
// ============================================================================
function extractVideoEmbedInfo(url) {
  if (!url) return null;
  let trimmed = url.trim();

  // If user pasted raw iframe embed HTML, extract the src URL
  const iframeMatch = trimmed.match(/src=["']([^"']+)["']/i);
  if (iframeMatch && iframeMatch[1]) {
    trimmed = iframeMatch[1].trim();
  }

  // YouTube detection (watch?v=, youtu.be/, embed/, shorts/, m.youtube.com)
  const ytMatch = trimmed.match(/(?:youtu\.be\/|youtube\.com\/(?:embed\/|v\/|watch\?v=|watch\?.+&v=|shorts\/))([a-zA-Z0-9_-]{11})/i);
  if (ytMatch && ytMatch[1]) {
    return {
      type: 'youtube',
      videoId: ytMatch[1],
      embedUrl: `https://www.youtube.com/embed/${ytMatch[1]}?autoplay=1&rel=0&enablejsapi=1`,
      originalUrl: trimmed
    };
  }

  // Google Drive video preview detection
  const gDriveMatch = trimmed.match(/drive\.google\.com\/file\/d\/([a-zA-Z0-9_-]+)/i);
  if (gDriveMatch && gDriveMatch[1]) {
    return {
      type: 'gdrive',
      embedUrl: `https://drive.google.com/file/d/${gDriveMatch[1]}/preview`,
      originalUrl: trimmed
    };
  }

  // Loom video detection
  const loomMatch = trimmed.match(/loom\.com\/(?:share|embed)\/([a-zA-Z0-9_-]+)/i);
  if (loomMatch && loomMatch[1]) {
    return {
      type: 'loom',
      embedUrl: `https://www.loom.com/embed/${loomMatch[1]}`,
      originalUrl: trimmed
    };
  }

  // Vimeo detection
  const vimeoMatch = trimmed.match(/vimeo\.com\/(?:channels\/(?:\w+\/)?|groups\/(?:[^\/]*)\/videos\/|album\/(?:\d+)\/video\/|video\/|)(\d+)/i);
  if (vimeoMatch && vimeoMatch[1]) {
    return {
      type: 'vimeo',
      videoId: vimeoMatch[1],
      embedUrl: `https://player.vimeo.com/video/${vimeoMatch[1]}?autoplay=1`,
      originalUrl: trimmed
    };
  }

  // Direct video files (.mp4, .webm, .ogg)
  if (/\.(mp4|webm|ogg|mov)(\?.*)?$/i.test(trimmed)) {
    return {
      type: 'direct',
      url: trimmed,
      originalUrl: trimmed
    };
  }

  // Generic stream or cloud video URL
  return {
    type: 'generic',
    url: trimmed,
    originalUrl: trimmed
  };
}

// Completed Lesson Tracking Helper
function getCompletedLessonIds(enrollmentId) {
  if (!enrollmentId) return [];
  try {
    const raw = localStorage.getItem(`eduhub_completed_lessons_${enrollmentId}`);
    return raw ? JSON.parse(raw) : [];
  } catch (e) {
    return [];
  }
}

function isLessonCompleted(enrollmentId, lessonId) {
  const list = getCompletedLessonIds(enrollmentId);
  return list.includes(Number(lessonId));
}

function saveCompletedLessonId(enrollmentId, lessonId) {
  if (!enrollmentId) return [];
  const list = getCompletedLessonIds(enrollmentId);
  const numId = Number(lessonId);
  if (!list.includes(numId)) {
    list.push(numId);
    localStorage.setItem(`eduhub_completed_lessons_${enrollmentId}`, JSON.stringify(list));
  }
  return list;
}

// ============================================================================
// INTERACTIVE CLASSROOM PLAYER (COURSEWORK, VIDEOS, PDFS, QUIZZES)
// ============================================================================
async function openClassroomPlayer(enrollmentId, courseId, courseTitle, currentProgress) {
  state.activeEnrollment = { id: enrollmentId, courseId, title: courseTitle, progress: currentProgress };
  state.activeClassroomContents = [];
  state.activeClassroomContentsMap = {};

  document.getElementById('playerCourseTitle').innerText = courseTitle;
  document.getElementById('classroomProgressPercent').innerText = `${currentProgress}%`;
  document.getElementById('classroomProgressBar').style.width = `${currentProgress}%`;

  // Toggle certificate unlock button if already 100%
  const certBtn = document.getElementById('classroomCertUnlockBtn');
  if (certBtn) certBtn.style.display = currentProgress >= 100 ? 'inline-flex' : 'none';

  try {
    const contents = await apiRequest(`/courses/${courseId}/content`);
    state.activeClassroomContents = contents || [];
    state.activeClassroomContentsMap = {};
    (contents || []).forEach(item => {
      state.activeClassroomContentsMap[item.id] = item;
    });

    renderClassroomPlaylist(state.activeClassroomContents);

    if (state.activeClassroomContents.length > 0) {
      // Pick first incomplete lesson if any, or first lesson
      const completedIds = getCompletedLessonIds(enrollmentId);
      const firstIncomplete = state.activeClassroomContents.find(c => !completedIds.includes(Number(c.id)));
      selectClassroomLesson(firstIncomplete || state.activeClassroomContents[0]);
    } else {
      document.getElementById('lessonDisplayContainer').innerHTML = `
        <div style="text-align: center; padding: 4rem 1rem; color: var(--slate-500);">
          <p>No materials or coursework assigned yet for this course.</p>
        </div>
      `;
    }

    document.getElementById('classroomModalBackdrop').classList.add('active');
  } catch (err) {
    showToast('Error', 'Unable to load course materials.', 'error');
  }
}

function closeClassroomPlayer() {
  document.getElementById('classroomModalBackdrop').classList.remove('active');
  if (state.activeView === 'student') loadStudentDashboard();
}

function renderClassroomPlaylist(contents) {
  const container = document.getElementById('classroomPlaylistContainer');
  if (!container) return;

  container.innerHTML = contents.map((item, idx) => {
    const completed = isLessonCompleted(state.activeEnrollment?.id, item.id);
    return `
      <div class="playlist-item ${completed ? 'completed' : ''}" id="playlist-item-${item.id}" onclick="selectClassroomLessonById(${item.id})">
        <div class="playlist-type-icon type-${(item.contentType || '').toLowerCase()}">
          ${item.contentType === 'VIDEO' ? '📹' : (item.contentType === 'PDF' ? '📄' : '📝')}
        </div>
        <div style="flex: 1; overflow: hidden;">
          <div style="display: flex; align-items: center; justify-content: space-between; gap: 0.4rem;">
            <h5 style="font-size: 0.88rem; font-weight: 700; color: var(--slate-900); white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
              ${idx + 1}. ${escapeHtml(item.title)}
            </h5>
            ${completed ? '<span style="color: var(--accent-emerald); font-size: 0.82rem; font-weight: 800;">✓ Done</span>' : ''}
          </div>
          <span style="font-size: 0.75rem; color: var(--slate-500);">${item.durationMinutes || 30} mins • ${item.contentType}</span>
        </div>
      </div>
    `;
  }).join('');
}

function selectClassroomLessonById(lessonId) {
  const item = state.activeClassroomContentsMap[lessonId];
  if (item) {
    selectClassroomLesson(item);
  }
}

function selectClassroomLesson(item) {
  if (!item) return;
  document.querySelectorAll('.playlist-item').forEach(el => el.classList.remove('active'));
  const activeEl = document.getElementById(`playlist-item-${item.id}`);
  if (activeEl) activeEl.classList.add('active');

  const container = document.getElementById('lessonDisplayContainer');
  let contentHtml = '';

  const enrollmentId = state.activeEnrollment?.id;
  const completed = isLessonCompleted(enrollmentId, item.id);
  const totalCount = state.activeClassroomContents?.length || 1;
  const progressStep = Math.max(1, Math.round(100 / totalCount));

  if (item.contentType === 'VIDEO') {
    const embedInfo = extractVideoEmbedInfo(item.url);
    let playerHtml = '';

    if (embedInfo && embedInfo.type === 'youtube') {
      playerHtml = `
        <div class="video-player-container">
          <iframe src="${embedInfo.embedUrl}" 
                  title="${escapeHtml(item.title)}"
                  frameborder="0"
                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" 
                  referrerpolicy="strict-origin-when-cross-origin"
                  allowfullscreen>
          </iframe>
        </div>
      `;
    } else if (embedInfo && embedInfo.type === 'vimeo') {
      playerHtml = `
        <div class="video-player-container">
          <iframe src="${embedInfo.embedUrl}" 
                  title="${escapeHtml(item.title)}"
                  frameborder="0"
                  allow="autoplay; fullscreen; picture-in-picture" 
                  allowfullscreen>
          </iframe>
        </div>
      `;
    } else if (embedInfo && embedInfo.type === 'gdrive') {
      playerHtml = `
        <div class="video-player-container">
          <iframe src="${embedInfo.embedUrl}" 
                  title="${escapeHtml(item.title)}"
                  frameborder="0"
                  allow="autoplay" 
                  allowfullscreen>
          </iframe>
        </div>
      `;
    } else if (embedInfo && embedInfo.type === 'loom') {
      playerHtml = `
        <div class="video-player-container">
          <iframe src="${embedInfo.embedUrl}" 
                  title="${escapeHtml(item.title)}"
                  frameborder="0"
                  allowfullscreen>
          </iframe>
        </div>
      `;
    } else if (embedInfo && embedInfo.type === 'direct') {
      playerHtml = `
        <div class="video-player-container">
          <video controls autoplay playsinline style="width: 100%; height: 100%; background: #000; object-fit: contain;">
            <source src="${embedInfo.url}">
            Your browser does not support the video tag.
          </video>
        </div>
      `;
    } else if (item.url) {
      playerHtml = `
        <div class="video-player-container" style="display: flex; flex-direction: column; align-items: center; justify-content: center; background: linear-gradient(135deg, #0f172a, #1e293b); color: white; text-align: center; padding: 2.5rem 1.5rem;">
          <div style="font-size: 3.5rem; margin-bottom: 0.75rem;">📹</div>
          <h4 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 0.5rem; color: #f8fafc;">${escapeHtml(item.title)}</h4>
          <p style="color: #94a3b8; max-width: 500px; margin-bottom: 1.5rem; font-size: 0.92rem; line-height: 1.5;">
            External video stream is available. Click below to launch the video in a dedicated tab and automatically record your progress.
          </p>
          <div style="display: flex; gap: 0.75rem; flex-wrap: wrap; justify-content: center;">
            <button class="btn-solid-primary" onclick="openLessonExternalSource(${item.id})" style="font-size: 0.95rem; padding: 0.75rem 1.5rem; font-weight: 700;">
              ▶ Open Video Stream &amp; Update Progress ↗
            </button>
            <button class="btn-solid-secondary" style="background: rgba(255,255,255,0.1); border-color: rgba(255,255,255,0.2); color: #f1f5f9;" onclick="markLessonCompletedAndAdvance(${item.id})">
              ✓ Mark Watched
            </button>
          </div>
        </div>
      `;
    } else {
      playerHtml = `
        <div class="video-player-container" style="display: flex; align-items: center; justify-content: center; color: #94a3b8; background: #0f172a;">
          <p>No video URL attached to this lesson by the instructor.</p>
        </div>
      `;
    }

    contentHtml = `
      ${playerHtml}

      <div style="margin-top: 1.25rem;">
        <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem;">
          <div>
            <div style="display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.35rem;">
              <h3 style="font-weight: 800; color: var(--slate-900); font-size: 1.25rem;">${escapeHtml(item.title)}</h3>
              ${completed ? `<span class="lesson-badge-completed">✓ Completed</span>` : `<span class="badge-pill badge-ongoing">Active Lesson</span>`}
            </div>
            <p style="color: var(--slate-600); font-size: 0.92rem; line-height: 1.5;">${escapeHtml(item.description || 'Watch through the lecture video module to grasp foundational and applied concepts.')}</p>
          </div>
        </div>

        <div class="lesson-action-bar">
          <div>
            <span style="font-size: 0.82rem; color: var(--slate-500); font-weight: 600;">
              Duration: <strong>${item.durationMinutes || 30} mins</strong> • Worth <strong>+${progressStep}%</strong> towards completion
            </span>
          </div>
          <div style="display: flex; gap: 0.65rem; align-items: center;">
            ${item.url ? `
              <button class="btn-solid-secondary" onclick="openLessonExternalSource(${item.id})" title="Opens external stream in a new tab and updates course progress">
                Open in External Source ↗
              </button>
            ` : ''}
            <button class="${completed ? 'btn-solid-secondary' : 'btn-success'}" onclick="markLessonCompletedAndAdvance(${item.id})">
              ${completed ? '✓ Re-mark Completed' : '✓ Mark Video as Watched (+Next)'}
            </button>
          </div>
        </div>
      </div>
    `;
  } else if (item.contentType === 'PDF') {
    contentHtml = `
      <div style="background: var(--slate-50); border: 1px solid var(--border-subtle); border-radius: 14px; padding: 2.5rem 1.5rem; text-align: center;">
        <div style="font-size: 3.5rem; margin-bottom: 0.5rem;">📄</div>
        <div style="display: flex; align-items: center; justify-content: center; gap: 0.5rem; margin-bottom: 0.5rem;">
          <h3 style="font-weight: 800; font-size: 1.25rem; color: var(--slate-900);">${escapeHtml(item.title)}</h3>
          ${completed ? `<span class="lesson-badge-completed">✓ Completed</span>` : `<span class="badge-pill badge-ongoing">Reading Material</span>`}
        </div>
        <p style="color: var(--slate-600); max-width: 520px; margin: 0 auto 1.5rem auto; font-size: 0.92rem; line-height: 1.6;">
          ${escapeHtml(item.description || 'Download and review this comprehensive lecture note and architecture reference guide.')}
        </p>
        
        <div style="display: inline-flex; gap: 0.75rem; flex-wrap: wrap; justify-content: center;">
          ${item.url ? `
            <button class="btn-solid-primary" onclick="openLessonExternalSource(${item.id})">
              Open Document / PDF ↗
            </button>
          ` : `<span class="btn-solid-secondary">Cloud Document</span>`}
          <button class="${completed ? 'btn-solid-secondary' : 'btn-success'}" onclick="markLessonCompletedAndAdvance(${item.id})">
            ${completed ? '✓ Re-mark as Read' : '✓ Mark as Read & Continue (+Next)'}
          </button>
        </div>
      </div>

      <div class="lesson-action-bar">
        <span style="font-size: 0.82rem; color: var(--slate-500); font-weight: 600;">
          Estimated reading: <strong>${item.durationMinutes || 20} mins</strong> • Worth <strong>+${progressStep}%</strong> progress
        </span>
        ${item.url ? `
          <button class="btn-solid-secondary" style="font-size: 0.8rem; padding: 0.4rem 0.8rem;" onclick="openLessonExternalSource(${item.id})">
            Direct Link ↗
          </button>
        ` : ''}
      </div>
    `;
  } else if (item.contentType === 'QUIZ') {
    contentHtml = `
      <div class="quiz-box-card">
        <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 1rem;">
          <h4 style="font-weight: 800; font-size: 1.15rem; color: var(--slate-900);">📝 Assessment: ${escapeHtml(item.title)}</h4>
          ${completed ? `<span class="lesson-badge-completed">✓ Completed</span>` : `<span class="badge-pill badge-ongoing">Graded</span>`}
        </div>
        <p style="color: var(--slate-600); font-size: 0.9rem; margin-bottom: 1.25rem;">${escapeHtml(item.description || 'Select the correct answer below to test your understanding.')}</p>

        <div style="background: white; border: 1px solid var(--border-subtle); border-radius: 10px; padding: 1.25rem; margin-bottom: 1.25rem;">
          <strong style="display: block; font-size: 0.95rem; margin-bottom: 0.85rem; color: var(--slate-900);">
            ${escapeHtml(item.quizQuestions || 'Q1: Which architecture component manages role-based endpoint authorization in Spring Security?')}
          </strong>
          <label class="quiz-option-label">
            <input type="radio" name="quizChoice" value="correct">
            <span>A) SecurityFilterChain &amp; AuthorizationManager</span>
          </label>
          <label class="quiz-option-label">
            <input type="radio" name="quizChoice" value="wrong1">
            <span>B) ApplicationContextEventListener</span>
          </label>
          <label class="quiz-option-label">
            <input type="radio" name="quizChoice" value="wrong2">
            <span>C) GenericServletWrapper</span>
          </label>
        </div>

        <div style="display: flex; gap: 0.75rem;">
          <button class="btn-success" style="flex: 1;" onclick="submitInteractiveQuiz(${item.id})">
            Submit Assessment &amp; Record Score
          </button>
          <button class="btn-solid-secondary" onclick="markLessonCompletedAndAdvance(${item.id})">
            Skip / Mark Complete
          </button>
        </div>
      </div>
    `;
  }

  container.innerHTML = contentHtml;
}

// Opens External Source Synchronously (Preventing Browser Popup Blockers) and Updates Progress
function openLessonExternalSource(lessonId) {
  const item = state.activeClassroomContentsMap[lessonId];
  if (!item || !item.url) {
    showToast('Notice', 'No external URL attached to this lesson.', 'warning');
    return;
  }

  const rawUrl = item.url.trim();
  const urlToOpen = (rawUrl.startsWith('http://') || rawUrl.startsWith('https://'))
    ? rawUrl
    : `https://${rawUrl}`;

  // Synchronously open window immediately so the browser does not block the popup
  window.open(urlToOpen, '_blank', 'noopener,noreferrer');

  // Automatically mark completed and update progress in backend & UI
  markLessonCompletedAndAdvance(lessonId, true);
}

// Automatically Marks Lesson Complete, Calculates Progress %, Persists to Backend, and Advances
async function markLessonCompletedAndAdvance(lessonId, isFromExternalOpen = false) {
  if (!state.activeEnrollment) return;
  const enrollmentId = state.activeEnrollment.id;

  const completedList = saveCompletedLessonId(enrollmentId, lessonId);
  const totalCount = (state.activeClassroomContents && state.activeClassroomContents.length > 0)
    ? state.activeClassroomContents.length
    : 1;

  // Compute progress: completed / total * 100
  let newPercent = Math.min(100, Math.round((completedList.length / totalCount) * 100));
  if (completedList.length >= totalCount) {
    newPercent = 100;
  }

  // Ensure progress never drops below previous enrollment progress
  if (state.activeEnrollment.progress && state.activeEnrollment.progress > newPercent) {
    newPercent = state.activeEnrollment.progress;
  }

  try {
    // Persist to backend database via PUT /enrollments/{id}/progress
    await apiRequest(`/enrollments/${enrollmentId}/progress?progress=${newPercent}`, {
      method: 'PUT'
    });

    state.activeEnrollment.progress = newPercent;
    document.getElementById('classroomProgressPercent').innerText = `${newPercent}%`;
    document.getElementById('classroomProgressBar').style.width = `${newPercent}%`;

    // Re-render playlist to show new checkmark
    renderClassroomPlaylist(state.activeClassroomContents);

    if (newPercent >= 100) {
      showToast('🎉 Course 100% Completed!', "You've completed all coursework! Your official verified certificate is unlocked.", 'success');
      const certBtn = document.getElementById('classroomCertUnlockBtn');
      if (certBtn) certBtn.style.display = 'inline-flex';
    } else {
      if (isFromExternalOpen) {
        showToast('Resource Opened & Saved', `Progress automatically recorded: ${newPercent}%!`, 'success');
      } else {
        showToast('Lesson Completed', `Progress advanced to ${newPercent}%.`, 'success');
      }
    }

    // Refresh student dashboard if currently in student view
    if (state.activeView === 'student' && state.currentUser?.profileId) {
      apiRequest(`/students/${state.currentUser.profileId}/progress`).then(p => {
        state.studentProgress = p;
        renderStudentActiveCourses();
        renderStudentCompletedCourses();
        renderStudentHistoryTable();
      }).catch(() => {});
    }

    // Advance to next lesson if not from external window open
    if (!isFromExternalOpen && state.activeClassroomContents) {
      const idx = state.activeClassroomContents.findIndex(c => Number(c.id) === Number(lessonId));
      if (idx !== -1 && idx + 1 < state.activeClassroomContents.length) {
        selectClassroomLesson(state.activeClassroomContents[idx + 1]);
      } else {
        const currentItem = state.activeClassroomContents.find(c => Number(c.id) === Number(lessonId));
        if (currentItem) selectClassroomLesson(currentItem);
      }
    } else {
      // Re-render current item so its "Completed" badge shows immediately
      const currentItem = state.activeClassroomContents.find(c => Number(c.id) === Number(lessonId));
      if (currentItem) selectClassroomLesson(currentItem);
    }
  } catch (err) {
    console.error('Failed to update progress', err);
    showToast('Progress Error', err.message, 'error');
  }
}

async function submitInteractiveQuiz(lessonId) {
  const selected = document.querySelector('input[name="quizChoice"]:checked');
  if (!selected) {
    showToast('Selection Required', 'Please select an answer choice before submitting.', 'warning');
    return;
  }

  const isCorrect = selected.value === 'correct';
  const score = isCorrect ? 95.0 : 70.0;

  try {
    await apiRequest(`/enrollments/${state.activeEnrollment.id}/score?score=${score}`, {
      method: 'POST'
    });

    // Mark quiz lesson completed & advance progress
    await markLessonCompletedAndAdvance(lessonId);

    showToast('Assessment Completed', `Score recorded: ${score}% (${isCorrect ? 'Grade: A (Excellent)' : 'Grade: C (Pass)'}).`, 'success');
  } catch (err) {
    showToast('Submission Notice', err.message, 'warning');
  }
}

function openCompletedCertificateFromPlayer() {
  if (!state.activeEnrollment) return;
  const courseTitle = state.activeEnrollment.title;
  closeClassroomPlayer();
  showCertificateModal(courseTitle, 'Completed Today', 'A (Excellent)');
}

// Certificate Generator Modal
function showCertificateModal(courseTitle, completionDate, grade) {
  const studentName = state.currentUser?.name || 'Verified Student';
  document.getElementById('certStudentNameDisplay').innerText = studentName;
  document.getElementById('certCourseTitleDisplay').innerText = courseTitle;
  document.getElementById('certDateDisplay').innerText = completionDate;
  document.getElementById('certGradeDisplay').innerText = grade;
  document.getElementById('certIdDisplay').innerText = `EDU-${Math.floor(100000 + Math.random() * 900000)}`;

  document.getElementById('certificateModalBackdrop').classList.add('active');
}

function closeCertificateModal() {
  document.getElementById('certificateModalBackdrop').classList.remove('active');
}

// ============================================================================
// INSTRUCTOR MODULE (STUDIO, CURRICULUM, GRADEBOOK)
// ============================================================================
async function loadInstructorStudio() {
  if (!state.currentUser || !state.currentUser.profileId) return;
  const instructorId = state.currentUser.profileId;

  try {
    // 1. Fetch Instructor Courses
    const courses = await apiRequest(`/instructor/${instructorId}/courses`);
    state.instructorCourses = courses;

    // 2. Fetch Enrolled Students Performance
    const performance = await apiRequest(`/instructor/${instructorId}/students`);
    state.instructorStudents = performance;

    // Update KPI cards
    document.getElementById('instKpiTotalCourses').innerText = courses.length || 0;
    document.getElementById('instKpiTotalStudents').innerText = performance.length || 0;
    
    const completedCount = performance.filter(p => p.status === 'COMPLETED').length;
    document.getElementById('instKpiCompletedStudents').innerText = completedCount;

    let avgScore = 0;
    const scoredList = performance.filter(p => p.score != null);
    if (scoredList.length > 0) {
      const sum = scoredList.reduce((acc, curr) => acc + curr.score, 0);
      avgScore = Math.round(sum / scoredList.length);
    }
    document.getElementById('instKpiAvgScore').innerText = `${avgScore}%`;

    renderInstructorCoursesTable();
    renderInstructorStudentsTable();
  } catch (err) {
    console.error('Failed to load instructor studio', err);
    showToast('Studio Error', 'Could not refresh instructor dashboard.', 'error');
  }
}

function switchInstructorTab(tabName) {
  state.activeInstructorTab = tabName;
  document.querySelectorAll('#instructorTabsContainer .tab-btn').forEach(btn => btn.classList.remove('active'));
  document.getElementById(`instTabBtn-${tabName}`).classList.add('active');

  document.querySelectorAll('#instructorTabPanels .tab-content-panel').forEach(p => p.classList.remove('active'));
  document.getElementById(`instPanel-${tabName}`).classList.add('active');
}

function renderInstructorCoursesTable() {
  const tbody = document.getElementById('instructorCoursesTableBody');
  if (!tbody) return;

  if (state.instructorCourses.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; padding: 3rem; color: var(--slate-500);">No courses published yet. Click "+ Create New Course" to begin teaching!</td></tr>`;
    return;
  }

  tbody.innerHTML = state.instructorCourses.map(c => `
    <tr>
      <td><strong>#${c.id}</strong></td>
      <td><strong>${escapeHtml(c.title)}</strong></td>
      <td><span class="course-badge-category">${escapeHtml(c.category || 'General')}</span></td>
      <td style="max-width: 280px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: var(--slate-500);">${escapeHtml(c.description || '')}</td>
      <td><strong>${c.contents ? c.contents.length : 0}</strong> items</td>
      <td>
        <button class="btn-solid-secondary" style="padding: 0.4rem 0.85rem; font-size: 0.82rem;" onclick="openCurriculumBuilder(${c.id}, '${escapeHtml(c.title)}')">
          ⚙ Manage Curriculum
        </button>
      </td>
    </tr>
  `).join('');
}

function renderInstructorStudentsTable() {
  const tbody = document.getElementById('instructorStudentsTableBody');
  if (!tbody) return;

  if (state.instructorStudents.length === 0) {
    tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; padding: 3rem; color: var(--slate-500);">No student enrollments registered yet.</td></tr>`;
    return;
  }

  tbody.innerHTML = state.instructorStudents.map(p => `
    <tr>
      <td><strong>${escapeHtml(p.courseTitle)}</strong></td>
      <td>
        <strong>${escapeHtml(p.studentName)}</strong>
        <div style="font-size: 0.78rem; color: var(--slate-500);">${escapeHtml(p.studentEmail)}</div>
      </td>
      <td>${p.enrollmentDate || '-'}</td>
      <td><span class="badge-pill ${p.status === 'COMPLETED' ? 'badge-completed' : 'badge-ongoing'}">${p.status}</span></td>
      <td>
        <div class="progress-container-inline">
          <div style="font-size: 0.78rem; font-weight: 700;">${p.progressPercentage}%</div>
          <div class="progress-track-subtle">
            <div class="progress-bar-fill" style="width: ${p.progressPercentage}%;"></div>
          </div>
        </div>
      </td>
      <td><strong style="color: var(--primary-700);">${p.grade || 'Pending'}</strong></td>
      <td>
        <div style="display: flex; gap: 0.4rem; align-items: center;">
          <input type="number" min="0" max="100" id="gradeScoreInput_${p.enrollmentId}" value="${p.score || ''}" placeholder="0-100"
                 style="width: 70px; padding: 0.35rem 0.5rem; border: 1px solid var(--border-subtle); border-radius: 6px; font-size: 0.85rem;">
          <button class="btn-solid-primary" style="padding: 0.35rem 0.65rem; font-size: 0.78rem;" onclick="saveInstructorGrade(${p.enrollmentId})">
            Save
          </button>
        </div>
      </td>
      <td>
        <button class="btn-solid-secondary" style="padding: 0.35rem 0.65rem; font-size: 0.78rem;" onclick="sendSingleStudentReminder('${escapeHtml(p.studentEmail)}', '${escapeHtml(p.courseTitle)}')">
          🔔 Ping
        </button>
      </td>
    </tr>
  `).join('');
}

async function saveInstructorGrade(enrollmentId) {
  const input = document.getElementById(`gradeScoreInput_${enrollmentId}`);
  const score = parseFloat(input.value);
  if (isNaN(score) || score < 0 || score > 100) {
    showToast('Invalid Score', 'Please input a valid percentage score between 0 and 100.', 'warning');
    return;
  }

  try {
    await apiRequest(`/enrollments/${enrollmentId}/score?score=${score}`, {
      method: 'POST'
    });

    showToast('Grade Recorded', `Student score updated to ${score}%.`, 'success');
    loadInstructorStudio();
  } catch (err) {
    showToast('Grading Notice', err.message, 'error');
  }
}

async function sendSingleStudentReminder(studentEmail, courseTitle) {
  try {
    await apiRequest('/notifications/send', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({
        recipientEmail: studentEmail,
        role: 'ROLE_STUDENT',
        title: `Reminder: Coursework in ${courseTitle}`,
        message: `Your instructor reminded you to continue your active coursework in ${courseTitle}.`,
        type: 'COURSEWORK_REMINDER'
      })
    });

    showToast('Reminder Dispatched', `Notification sent directly to ${studentEmail}.`, 'success');
  } catch (err) {
    showToast('Error', err.message, 'error');
  }
}

// Course Creator Modal
function openCreateCourseModal() {
  document.getElementById('createCourseModalBackdrop').classList.add('active');
}

function closeCreateCourseModal() {
  document.getElementById('createCourseModalBackdrop').classList.remove('active');
}

async function handleCreateCourseSubmit(e) {
  if (e) e.preventDefault();
  const title = document.getElementById('newCourseTitleInput').value.trim();
  const category = document.getElementById('newCourseCategorySelect').value;
  const description = document.getElementById('newCourseDescInput').value.trim();

  if (!title) {
    showToast('Missing Field', 'Please provide a course title.', 'warning');
    return;
  }

  try {
    await apiRequest('/courses', {
      method: 'POST',
      body: JSON.stringify({
        title,
        category,
        description,
        instructor: state.currentUser ? state.currentUser.name : 'Instructor'
      })
    });

    closeCreateCourseModal();
    showToast('Course Published!', 'All students on the platform have been notified.', 'success');
    loadInstructorStudio();
  } catch (err) {
    showToast('Error', err.message, 'error');
  }
}

// Curriculum Manager Drawer
async function openCurriculumBuilder(courseId, courseTitle) {
  state.activeCurriculumCourseId = courseId;
  document.getElementById('curriculumModalTitle').innerText = `Curriculum Builder: ${courseTitle}`;
  await reloadCurriculumList(courseId);
  document.getElementById('curriculumModalBackdrop').classList.add('active');
}

function closeCurriculumBuilder() {
  document.getElementById('curriculumModalBackdrop').classList.remove('active');
}

async function reloadCurriculumList(courseId) {
  try {
    const list = await apiRequest(`/courses/${courseId}/content`);
    const container = document.getElementById('curriculumItemsList');
    state.activeCurriculumItemsMap = {};

    if (!list || list.length === 0) {
      container.innerHTML = `<p style="color: var(--slate-500); text-align: center; padding: 2rem;">No curriculum materials added yet.</p>`;
      return;
    }

    (list || []).forEach(item => {
      state.activeCurriculumItemsMap[item.id] = item;
    });

    container.innerHTML = list.map((item, idx) => `
      <div style="background: var(--slate-50); border: 1px solid var(--border-subtle); border-radius: 12px; padding: 1rem 1.25rem; margin-bottom: 0.75rem; display: flex; align-items: center; justify-content: space-between; gap: 1rem;">
        <div style="display: flex; align-items: center; gap: 0.85rem; flex: 1; overflow: hidden;">
          <div class="playlist-type-icon type-${(item.contentType || '').toLowerCase()}">
            ${item.contentType === 'VIDEO' ? '📹' : (item.contentType === 'PDF' ? '📄' : '📝')}
          </div>
          <div style="flex: 1; overflow: hidden;">
            <div style="display: flex; align-items: center; gap: 0.5rem;">
              <strong style="font-size: 0.95rem; color: var(--slate-900); white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${idx + 1}. ${escapeHtml(item.title)}</strong>
              <span class="badge-pill badge-ongoing" style="font-size: 0.72rem; padding: 0.15rem 0.5rem;">${item.contentType}</span>
            </div>
            <div style="font-size: 0.78rem; color: var(--slate-500); margin-top: 0.15rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
              ${item.durationMinutes || 30} mins ${item.url ? `• <span style="color: var(--primary-600);">${escapeHtml(item.url.substring(0, 45))}...</span>` : ''}
            </div>
          </div>
        </div>
        <div style="display: flex; align-items: center; gap: 0.5rem;">
          <button class="btn-solid-secondary" style="padding: 0.4rem 0.8rem; font-size: 0.82rem; font-weight: 700;" onclick="testInstructorMaterialById(${item.id})">
            👁 Test / Play
          </button>
          <button class="btn-danger-outline" style="padding: 0.4rem 0.8rem; font-size: 0.82rem;" onclick="deleteCurriculumItem(${item.id})">
            Remove
          </button>
        </div>
      </div>
    `).join('');
  } catch (err) {
    console.error(err);
  }
}

async function handleAddContentSubmit(e) {
  if (e) e.preventDefault();
  if (!state.activeCurriculumCourseId) return;

  const title = document.getElementById('newContentTitleInput').value.trim();
  const contentType = document.getElementById('newContentTypeSelect').value;
  let url = document.getElementById('newContentUrlInput').value.trim();
  const quizQuestions = document.getElementById('newContentQuizInput').value.trim();
  const durationMinutes = parseInt(document.getElementById('newContentDurationInput').value, 10) || 30;

  if (!title) {
    showToast('Missing Field', 'Please provide a content title.', 'warning');
    return;
  }

  // If instructor pasted an iframe embed snippet, extract the raw src URL
  const iframeMatch = url.match(/src=["']([^"']+)["']/i);
  if (iframeMatch && iframeMatch[1]) {
    url = iframeMatch[1].trim();
  }

  try {
    await apiRequest(`/courses/${state.activeCurriculumCourseId}/content`, {
      method: 'POST',
      body: JSON.stringify({ title, contentType, url, quizQuestions, durationMinutes })
    });

    document.getElementById('newContentTitleInput').value = '';
    document.getElementById('newContentUrlInput').value = '';
    document.getElementById('newContentQuizInput').value = '';
    showToast('Material Added', 'Course curriculum updated successfully. You can test play it below.', 'success');
    reloadCurriculumList(state.activeCurriculumCourseId);
  } catch (err) {
    showToast('Error', err.message, 'error');
  }
}

function testInstructorMaterialById(contentId) {
  const item = state.activeCurriculumItemsMap[contentId];
  if (!item) return;

  document.getElementById('instPreviewTitle').innerText = `Preview: ${item.title} (${item.contentType})`;
  const body = document.getElementById('instPreviewBody');

  if (item.contentType === 'VIDEO') {
    const embedInfo = extractVideoEmbedInfo(item.url);
    let playerHtml = '';

    if (embedInfo && embedInfo.type === 'youtube') {
      playerHtml = `
        <div class="video-player-container" style="margin-bottom: 1rem;">
          <iframe src="${embedInfo.embedUrl}" 
                  title="${escapeHtml(item.title)}"
                  frameborder="0"
                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" 
                  referrerpolicy="strict-origin-when-cross-origin"
                  allowfullscreen>
          </iframe>
        </div>
      `;
    } else if (embedInfo && embedInfo.type === 'vimeo') {
      playerHtml = `
        <div class="video-player-container" style="margin-bottom: 1rem;">
          <iframe src="${embedInfo.embedUrl}" 
                  title="${escapeHtml(item.title)}"
                  frameborder="0"
                  allow="autoplay; fullscreen; picture-in-picture" 
                  allowfullscreen>
          </iframe>
        </div>
      `;
    } else if (embedInfo && embedInfo.type === 'gdrive') {
      playerHtml = `
        <div class="video-player-container" style="margin-bottom: 1rem;">
          <iframe src="${embedInfo.embedUrl}" 
                  title="${escapeHtml(item.title)}"
                  frameborder="0"
                  allow="autoplay" 
                  allowfullscreen>
          </iframe>
        </div>
      `;
    } else if (embedInfo && embedInfo.type === 'loom') {
      playerHtml = `
        <div class="video-player-container" style="margin-bottom: 1rem;">
          <iframe src="${embedInfo.embedUrl}" 
                  title="${escapeHtml(item.title)}"
                  frameborder="0"
                  allowfullscreen>
          </iframe>
        </div>
      `;
    } else if (embedInfo && embedInfo.type === 'direct') {
      playerHtml = `
        <div class="video-player-container" style="margin-bottom: 1rem;">
          <video controls autoplay playsinline style="width: 100%; height: 100%; background: #000; object-fit: contain;">
            <source src="${embedInfo.url}">
            Your browser does not support the video tag.
          </video>
        </div>
      `;
    } else if (item.url) {
      playerHtml = `
        <div class="video-player-container" style="margin-bottom: 1rem; display: flex; flex-direction: column; align-items: center; justify-content: center; background: linear-gradient(135deg, #0f172a, #1e293b); color: white; text-align: center; padding: 2rem 1rem;">
          <div style="font-size: 3rem; margin-bottom: 0.5rem;">📹</div>
          <h4 style="font-size: 1.15rem; font-weight: 700; margin-bottom: 0.4rem; color: #f8fafc;">External Stream Configured</h4>
          <p style="color: #94a3b8; font-size: 0.88rem; max-width: 460px; margin-bottom: 1.25rem;">
            URL: <code style="background: rgba(255,255,255,0.1); padding: 0.2rem 0.4rem; border-radius: 4px; color: #e2e8f0;">${escapeHtml(item.url)}</code>
          </p>
          <button class="btn-solid-primary" onclick="window.open('${escapeHtml(item.url)}', '_blank')">
            ▶ Test Launch Stream in New Tab ↗
          </button>
        </div>
      `;
    } else {
      playerHtml = `<p style="color: var(--slate-500); padding: 1rem;">No URL specified for this video lesson.</p>`;
    }

    body.innerHTML = `
      ${playerHtml}
      <div style="background: var(--slate-50); border: 1px solid var(--border-subtle); border-radius: 10px; padding: 1rem;">
        <h5 style="font-weight: 700; margin-bottom: 0.25rem; color: var(--slate-900);">${escapeHtml(item.title)}</h5>
        <p style="color: var(--slate-600); font-size: 0.88rem; margin: 0;">${escapeHtml(item.description || 'Video lecture coursework module.')}</p>
        <div style="margin-top: 0.75rem; font-size: 0.8rem; color: var(--slate-500);">
          Duration: <strong>${item.durationMinutes || 30} mins</strong> • Students can play embedded or open in external window.
        </div>
      </div>
    `;
  } else if (item.contentType === 'PDF') {
    body.innerHTML = `
      <div style="text-align: center; padding: 2.5rem 1rem; background: var(--slate-50); border: 1px solid var(--border-subtle); border-radius: 12px; margin-bottom: 1rem;">
        <div style="font-size: 3rem; margin-bottom: 0.5rem;">📄</div>
        <h4 style="font-weight: 800; color: var(--slate-900); margin-bottom: 0.5rem;">${escapeHtml(item.title)}</h4>
        <p style="color: var(--slate-600); font-size: 0.9rem; max-width: 480px; margin: 0 auto 1.25rem auto;">${escapeHtml(item.description || 'Document / Reading resource for students.')}</p>
        ${item.url ? `
          <button class="btn-solid-primary" onclick="window.open('${escapeHtml(item.url)}', '_blank')">
            Open Document Link in New Tab ↗
          </button>
        ` : `<span style="color: var(--slate-500); font-size: 0.85rem;">No external link attached</span>`}
      </div>
    `;
  } else if (item.contentType === 'QUIZ') {
    body.innerHTML = `
      <div class="quiz-box-card">
        <h4 style="font-weight: 800; color: var(--slate-900); margin-bottom: 0.5rem;">📝 ${escapeHtml(item.title)}</h4>
        <p style="color: var(--slate-600); font-size: 0.88rem; margin-bottom: 1rem;">${escapeHtml(item.description || 'Sample Quiz Assessment')}</p>
        <div style="background: white; border: 1px solid var(--border-subtle); border-radius: 8px; padding: 1rem;">
          <strong style="display: block; margin-bottom: 0.5rem; color: var(--slate-800);">
            ${escapeHtml(item.quizQuestions || 'Sample Question: What is Spring Security?')}
          </strong>
          <span style="font-size: 0.82rem; color: var(--accent-emerald); font-weight: 700;">✓ Graded assessment interactive widget configured</span>
        </div>
      </div>
    `;
  }

  document.getElementById('instructorPreviewModalBackdrop').classList.add('active');
}

function closeInstructorPreviewModal() {
  document.getElementById('instructorPreviewModalBackdrop').classList.remove('active');
  const body = document.getElementById('instPreviewBody');
  if (body) body.innerHTML = '';
}

async function deleteCurriculumItem(contentId) {
  if (!confirm('Are you sure you want to remove this coursework item?')) return;
  try {
    await apiRequest(`/courses/content/${contentId}`, { method: 'DELETE' });
    showToast('Item Removed', 'Curriculum material deleted.', 'info');
    reloadCurriculumList(state.activeCurriculumCourseId);
  } catch (err) {
    showToast('Error', err.message, 'error');
  }
}

// Curriculum Preview Modal (for Students browsing catalog)
async function openCurriculumPreview(courseId, courseTitle) {
  document.getElementById('previewModalTitle').innerText = `Syllabus & Overview: ${courseTitle}`;
  try {
    const list = await apiRequest(`/courses/${courseId}/content`);
    const container = document.getElementById('previewLessonsList');

    if (!list || list.length === 0) {
      container.innerHTML = `<p style="color: var(--slate-500); text-align: center; padding: 2rem;">Curriculum details are currently being finalized by the instructor.</p>`;
    } else {
      container.innerHTML = list.map((item, idx) => `
        <div style="display: flex; align-items: center; justify-content: space-between; padding: 0.85rem; border-bottom: 1px solid var(--border-subtle);">
          <div style="display: flex; align-items: center; gap: 0.75rem;">
            <div class="playlist-type-icon type-${item.contentType.toLowerCase()}">
              ${item.contentType === 'VIDEO' ? '📹' : (item.contentType === 'PDF' ? '📄' : '📝')}
            </div>
            <div>
              <strong style="font-size: 0.92rem; color: var(--slate-900);">${idx + 1}. ${escapeHtml(item.title)}</strong>
              <div style="font-size: 0.78rem; color: var(--slate-500);">${item.contentType} • ${item.durationMinutes || 30} mins</div>
            </div>
          </div>
          <span style="font-size: 0.78rem; font-weight: 700; color: var(--primary-600);">Included</span>
        </div>
      `).join('');
    }

    document.getElementById('curriculumPreviewModalBackdrop').classList.add('active');
  } catch (err) {
    console.error(err);
  }
}

function closeCurriculumPreview() {
  document.getElementById('curriculumPreviewModalBackdrop').classList.remove('active');
}

// ============================================================================
// NOTIFICATION MODULE
// ============================================================================
async function checkUnreadNotifications() {
  if (!state.currentUser || !state.currentUser.username) return;
  try {
    const unread = await apiRequest(`/notifications/user/${encodeURIComponent(state.currentUser.username)}/unread`);
    const badge = document.getElementById('notifBellCounter');
    if (badge) {
      if (unread && unread.length > 0) {
        badge.innerText = unread.length;
        badge.style.display = 'grid';
      } else {
        badge.style.display = 'none';
      }
    }
  } catch (err) {
    console.error('Failed to check notifications', err);
  }
}

async function loadNotifications() {
  if (!state.currentUser || !state.currentUser.username) {
    document.getElementById('notificationsFeedList').innerHTML = `
      <div style="text-align: center; padding: 3rem; color: var(--slate-500);">
        Please sign in to view your personalized notification feed.
      </div>
    `;
    return;
  }

  try {
    const list = await apiRequest(`/notifications/user/${encodeURIComponent(state.currentUser.username)}`);
    state.notifications = list;
    const container = document.getElementById('notificationsFeedList');

    if (!list || list.length === 0) {
      container.innerHTML = `
        <div style="text-align: center; padding: 4rem 1rem; color: var(--slate-500); background: white; border-radius: 16px; border: 1px dashed var(--border-subtle);">
          <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">🔔</div>
          <strong style="color: var(--slate-800); font-size: 1.1rem;">All caught up!</strong>
          <p style="font-size: 0.9rem; margin-top: 0.25rem;">You will receive alerts when new courses are published, coursework is due, or students enroll.</p>
        </div>
      `;
      return;
    }

    container.innerHTML = list.map(n => `
      <div style="background: ${n.read ? 'white' : '#f0fdf4'}; border: 1px solid ${n.read ? 'var(--border-subtle)' : '#86efac'}; border-radius: 14px; padding: 1.25rem; box-shadow: var(--shadow-xs); display: flex; align-items: flex-start; justify-content: space-between; gap: 1rem;">
        <div>
          <div style="display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.4rem;">
            <span class="badge-pill ${n.type === 'NEW_ENROLLMENT' ? 'badge-completed' : 'badge-ongoing'}">${n.type}</span>
            <strong style="font-size: 0.98rem; color: var(--slate-900);">${escapeHtml(n.title)}</strong>
            ${!n.read ? '<span style="font-size: 0.65rem; background: var(--accent-emerald); color: white; padding: 0.1rem 0.45rem; border-radius: 999px; font-weight: 700;">NEW</span>' : ''}
          </div>
          <p style="font-size: 0.88rem; color: var(--slate-600); margin-bottom: 0.4rem;">${escapeHtml(n.message)}</p>
          <span style="font-size: 0.75rem; color: var(--slate-400);">${new Date(n.createdAt).toLocaleString()}</span>
        </div>
        ${!n.read ? `<button class="btn-solid-secondary" style="padding: 0.35rem 0.75rem; font-size: 0.78rem;" onclick="markNotificationRead(${n.id})">Mark as read</button>` : ''}
      </div>
    `).join('');
  } catch (err) {
    console.error('Failed to load notifications', err);
  }
}

async function markNotificationRead(id) {
  try {
    await apiRequest(`/notifications/${id}/read`, { method: 'PUT' });
    loadNotifications();
    checkUnreadNotifications();
  } catch (err) {
    console.error(err);
  }
}

async function triggerAutomatedReminders() {
  try {
    const list = await apiRequest('/notifications/reminders/send-pending-coursework', { method: 'POST' });
    showToast('Reminders Sent', `Successfully dispatched ${list.length} reminder(s) to students with pending coursework!`, 'success');
    loadNotifications();
    checkUnreadNotifications();
  } catch (err) {
    showToast('Error', err.message, 'error');
  }
}

// ============================================================================
// TOAST NOTIFICATION UTILITY
// ============================================================================
function showToast(title, message, type = 'info') {
  let stack = document.getElementById('toastStackContainer');
  if (!stack) {
    stack = document.createElement('div');
    stack.id = 'toastStackContainer';
    stack.className = 'toast-stack';
    document.body.appendChild(stack);
  }

  const icons = {
    success: '✅',
    info: 'ℹ️',
    warning: '⚠️',
    error: '❌'
  };

  const toast = document.createElement('div');
  toast.className = 'toast-item';
  toast.innerHTML = `
    <div class="toast-icon">${icons[type] || '🔔'}</div>
    <div class="toast-body">
      <div class="toast-title">${escapeHtml(title)}</div>
      <div class="toast-msg">${escapeHtml(message)}</div>
    </div>
  `;

  stack.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(20px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

// Helper: Escape HTML string to prevent XSS
function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
