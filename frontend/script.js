const API_BASE = 'http://localhost:8080/api';

// --- Student: Submit Complaint ---
const complaintForm = document.getElementById('complaintForm');
if (complaintForm) {
    complaintForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const btnText = document.getElementById('btnText');
        const btnIcon = document.getElementById('btnIcon');
        const btnLoader = document.getElementById('btnLoader');
        const submitBtn = document.getElementById('submitBtn');

        // UI Loading state
        btnText.textContent = 'AI is analyzing...';
        btnIcon.classList.add('hidden');
        btnLoader.classList.remove('hidden');
        submitBtn.disabled = true;

        const payload = {
            title: document.getElementById('title').value,
            description: document.getElementById('description').value,
            studentName: document.getElementById('studentName').value,
            rollNumber: document.getElementById('rollNumber').value
        };

        try {
            const res = await fetch(`${API_BASE}/complaints`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            const data = await res.json();
            
            if (res.ok) {
                // Show Success Modal
                document.getElementById('modalId').textContent = '#' + data.id;
                document.getElementById('modalCategory').textContent = data.aiCategory;
                document.getElementById('modalPriority').textContent = data.priority;
                document.getElementById('modalDept').textContent = data.departmentName;
                
                document.getElementById('successModal').classList.add('active');
                complaintForm.reset();
            } else {
                let errorMsg = 'Failed to submit';
                if (data.message) {
                    errorMsg = data.message;
                } else if (data.errors && data.errors.length > 0) {
                    // Handle Spring validation errors
                    errorMsg = data.errors.map(err => err.defaultMessage).join('\n');
                } else if (data.error) {
                    errorMsg = data.error;
                }
                alert('Error:\n' + errorMsg);
            }
        } catch (error) {
            console.error(error);
            alert('Failed to connect to the server.');
        } finally {
            // Restore UI
            btnText.textContent = 'Submit to AI Analysis';
            btnIcon.classList.remove('hidden');
            btnLoader.classList.add('hidden');
            submitBtn.disabled = false;
        }
    });
}

// --- Student: Check Status ---
async function loadStudentComplaints(usn) {
    const statusContent = document.getElementById('statusContent');
    const statusLoader = document.getElementById('statusLoader');
    const emptyState = document.getElementById('emptyState');
    const tableBody = document.getElementById('statusTableBody');
    
    if (!tableBody) return;
    
    emptyState.classList.add('hidden');
    statusContent.classList.add('hidden');
    statusLoader.classList.remove('hidden');

    try {
        const res = await fetch(`${API_BASE}/complaints/student/${usn}`);
        const data = await res.json();
        
        statusLoader.classList.add('hidden');
        
        if (!res.ok || data.length === 0) {
            emptyState.innerHTML = `
                <i class="fa-regular fa-folder-open" style="font-size: 4rem; color: var(--border); margin-bottom: 1rem;"></i>
                <h3>No Issues Found</h3>
                <p style="color: var(--text-muted); margin-bottom: 1.5rem;">No complaints found for USN: ${usn}</p>
            `;
            emptyState.classList.remove('hidden');
        } else {
            statusContent.classList.remove('hidden');
            
            tableBody.innerHTML = '';
            data.forEach(complaint => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>#${complaint.id}</strong></td>
                    <td>
                        <div style="font-weight: 500;">${complaint.title}</div>
                        <div style="font-size: 0.85rem; color: var(--text-muted);">${complaint.description.substring(0, 50)}...</div>
                    </td>
                    <td><span class="badge badge-normal">${complaint.category}</span></td>
                    <td>${getPriorityBadge(complaint.priority)}</td>
                    <td>${complaint.departmentName}</td>
                    <td>${getStatusBadge(complaint.status)}</td>
                    <td style="color: var(--text-muted); font-size: 0.9rem;">
                        ${new Date(complaint.createdAt).toLocaleDateString()}
                    </td>
                `;
                tableBody.appendChild(tr);
            });
        }
    } catch (error) {
        console.error(error);
        statusLoader.classList.add('hidden');
        alert('Failed to load status.');
    }
}

// --- Admin: Authentication & Dashboard ---
let ADMIN_AUTH = sessionStorage.getItem('adminAuth') || '';
let allComplaintsData = [];

// Handle Admin Page Load
function checkAdminAuth() {
    const loginOverlay = document.getElementById('loginOverlay');
    if (!loginOverlay) return; // Not on admin page
    
    if (ADMIN_AUTH) {
        loginOverlay.style.display = 'none';
        loadDashboardData();
    } else {
        loginOverlay.style.display = 'flex';
    }
}

// Handle Login Form Submission
const loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const user = document.getElementById('adminUsername').value;
        const pass = document.getElementById('adminPassword').value;
        const btn = document.getElementById('loginBtn');
        const err = document.getElementById('loginError');
        
        btn.disabled = true;
        btn.textContent = 'Authenticating...';
        err.style.display = 'none';
        
        const token = 'Basic ' + btoa(user + ':' + pass);
        
        try {
            // Test auth with a fast dashboard call
            const res = await fetch(`${API_BASE}/admin/dashboard`, {
                headers: { 'Authorization': token }
            });
            
            if (res.ok) {
                // Success!
                ADMIN_AUTH = token;
                sessionStorage.setItem('adminAuth', token);
                document.getElementById('loginOverlay').style.display = 'none';
                loadDashboardData();
            } else {
                err.style.display = 'block';
            }
        } catch (error) {
            console.error('Login error', error);
            err.textContent = 'Network error. Please try again.';
            err.style.display = 'block';
        } finally {
            btn.disabled = false;
            btn.textContent = 'Login';
        }
    });
}

function logoutAdmin() {
    sessionStorage.removeItem('adminAuth');
    window.location.reload();
}

async function loadDashboardData() {
    if (!document.getElementById('statsContainer')) return;
    
    try {
        // 1. Fetch Stats
        const statsRes = await fetch(`${API_BASE}/admin/dashboard`, {
            headers: { 'Authorization': ADMIN_AUTH }
        });
        const stats = await statsRes.json();
        
        document.getElementById('statTotal').textContent = stats.totalComplaints;
        document.getElementById('statPending').textContent = stats.pendingComplaints;
        document.getElementById('statUrgent').textContent = stats.urgentComplaints;
        // The backend dashboard stats object might be slightly different, we assume these properties exist based on AdminController comment.
        document.getElementById('statResolved').textContent = stats.totalComplaints - stats.pendingComplaints; // approximation if resolved isn't explicitly returned

        // Render AI Category Insights
        const categoryStats = document.getElementById('categoryStats');
        categoryStats.innerHTML = '';
        if (stats.complaintsByCategory) {
            Object.entries(stats.complaintsByCategory).forEach(([category, count]) => {
                const percentage = Math.round((count / stats.totalComplaints) * 100) || 0;
                categoryStats.innerHTML += `
                    <div>
                        <div style="display: flex; justify-content: space-between; margin-bottom: 0.25rem; font-size: 0.9rem;">
                            <span style="text-transform: capitalize;">${category}</span>
                            <span>${percentage}% (${count})</span>
                        </div>
                        <div style="width: 100%; height: 8px; background: rgba(0,0,0,0.05); border-radius: 4px; overflow: hidden;">
                            <div style="width: ${percentage}%; height: 100%; background: var(--primary); border-radius: 4px;"></div>
                        </div>
                    </div>
                `;
            });
        }

        // 2. Fetch All Complaints
        const complaintsRes = await fetch(`${API_BASE}/admin/complaints`, {
            headers: { 'Authorization': ADMIN_AUTH }
        });
        allComplaintsData = await complaintsRes.json();
        
        renderAdminTable(allComplaintsData);

    } catch (error) {
        console.error(error);
        alert('Failed to load admin data. Check credentials or server status.');
    }
}

function renderAdminTable(data) {
    const tableBody = document.getElementById('adminTableBody');
    if (!tableBody) return;
    
    tableBody.innerHTML = '';
    
    if (data.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="6" class="text-center" style="padding: 2rem; color: var(--text-muted);">No complaints found.</td></tr>`;
        return;
    }

    data.forEach(item => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>#${item.id}</td>
            <td>
                <div style="font-weight: 500;">${item.title}</div>
                <div style="font-size: 0.85rem; color: var(--text-muted); margin-top: 0.25rem;">
                    Submitted by: ${item.studentName || 'Student ' + item.studentId}
                </div>
            </td>
            <td>
                <div style="display: flex; gap: 0.5rem; margin-bottom: 0.25rem;">
                    <span class="badge badge-normal" style="font-size: 0.75rem;">${item.category}</span>
                    ${getPriorityBadge(item.priority)}
                </div>
            </td>
            <td>${item.departmentName}</td>
            <td>${getStatusBadge(item.status)}</td>
            <td>
                <button class="btn btn-outline" style="padding: 0.4rem 0.8rem; font-size: 0.85rem;" 
                        onclick="openUpdateModal(${item.id}, '${item.title.replace(/'/g, "\\'")}', '${item.status}')">
                    Update
                </button>
            </td>
        `;
        tableBody.appendChild(tr);
    });
}

function filterTable() {
    const filter = document.getElementById('filterStatus').value;
    if (filter === 'all') {
        renderAdminTable(allComplaintsData);
    } else {
        const filtered = allComplaintsData.filter(item => item.status.toLowerCase() === filter.toLowerCase());
        renderAdminTable(filtered);
    }
}

// --- Admin: Update Status Modal ---
function openUpdateModal(id, title, currentStatus) {
    document.getElementById('updateTicketId').textContent = '#' + id;
    document.getElementById('updateTicketTitle').textContent = title;
    document.getElementById('updateId').value = id;
    
    // Set current status
    const statusSelect = document.getElementById('newStatus');
    Array.from(statusSelect.options).forEach(opt => {
        if (opt.value === currentStatus) opt.selected = true;
    });

    document.getElementById('updateModal').classList.add('active');
}

function closeUpdateModal() {
    document.getElementById('updateModal').classList.remove('active');
}

const updateForm = document.getElementById('updateForm');
if (updateForm) {
    updateForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('updateId').value;
        const newStatus = document.getElementById('newStatus').value;
        const btn = document.getElementById('updateBtn');
        
        btn.disabled = true;
        btn.textContent = 'Saving...';

        try {
            const res = await fetch(`${API_BASE}/admin/complaints/${id}/status`, {
                method: 'PUT',
                headers: { 
                    'Content-Type': 'application/json',
                    'Authorization': ADMIN_AUTH
                },
                body: JSON.stringify({ status: newStatus })
            });

            if (res.ok) {
                closeUpdateModal();
                loadDashboardData(); // Refresh data
            } else {
                const errorText = await res.text();
                alert('Update failed: ' + errorText);
            }
        } catch (error) {
            console.error(error);
            alert('Failed to connect to server.');
        } finally {
            btn.disabled = false;
            btn.textContent = 'Save Changes';
        }
    });
}


// --- Utility Helpers ---
function getPriorityBadge(priority) {
    if (!priority) return '';
    const p = priority.toLowerCase();
    if (p === 'urgent') return `<span class="badge badge-urgent"><i class="fa-solid fa-triangle-exclamation"></i> Urgent</span>`;
    return `<span class="badge badge-normal">Normal</span>`;
}

function getStatusBadge(status) {
    if (!status) return '';
    const s = status.toUpperCase();
    if (s === 'PENDING') return `<span class="badge badge-pending">Pending</span>`;
    if (s === 'IN_PROGRESS') return `<span class="badge badge-in_progress">In Progress</span>`;
    if (s === 'RESOLVED') return `<span class="badge badge-resolved">Resolved</span>`;
    return `<span class="badge badge-normal">${status}</span>`;
}
