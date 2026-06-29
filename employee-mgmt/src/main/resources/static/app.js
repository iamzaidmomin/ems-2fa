const loginView = document.getElementById("login-view");
const appView = document.getElementById("app-view");
const loginMessage = document.getElementById("login-message");
const appMessage = document.getElementById("app-message");
const stepEmail = document.getElementById("step-email");
const stepOtp = document.getElementById("step-otp");

async function api(path, options = {}) {
  const response = await fetch(path, {
    credentials: "same-origin",
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options,
  });

  if (!response.ok) {
    let detail = "Request failed";
    try {
      const body = await response.json();
      detail = body.detail || body.error || body.message || detail;
    } catch (_) {
      // ignore parse errors
    }
    throw new Error(detail);
  }

  if (response.status === 204) return null;
  return response.json();
}

function showMessage(el, text, type = "info") {
  el.innerHTML = `<div class="message ${type}">${text}</div>`;
}

function clearMessage(el) {
  el.innerHTML = "";
}

function showLogin() {
  loginView.classList.remove("hidden");
  appView.classList.add("hidden");
}

function showApp(email) {
  loginView.classList.add("hidden");
  appView.classList.remove("hidden");
  document.getElementById("user-email").textContent = email;
}

async function checkAuth() {
  try {
    const status = await api("/api/auth/status");
    if (status.authenticated) {
      showApp(status.email);
      await loadEmployees();
      return true;
    }
  } catch (_) {
    // not authenticated
  }
  showLogin();
  return false;
}

document.getElementById("send-otp-btn").addEventListener("click", async () => {
  clearMessage(loginMessage);
  const email = document.getElementById("email").value.trim();
  if (!email) {
    showMessage(loginMessage, "Please enter your email", "error");
    return;
  }

  const btn = document.getElementById("send-otp-btn");
  btn.disabled = true;
  try {
    const result = await api("/api/auth/send-otp", {
      method: "POST",
      body: JSON.stringify({ email }),
    });
    showMessage(loginMessage, `OTP sent. Expires in ${result.expires_in / 60} minutes.`, "success");
    stepEmail.classList.add("hidden");
    stepOtp.classList.remove("hidden");
    document.getElementById("otp").focus();
  } catch (err) {
    showMessage(loginMessage, err.message, "error");
  } finally {
    btn.disabled = false;
  }
});

document.getElementById("verify-otp-btn").addEventListener("click", async () => {
  clearMessage(loginMessage);
  const email = document.getElementById("email").value.trim();
  const otp = document.getElementById("otp").value.trim();
  if (!otp) {
    showMessage(loginMessage, "Please enter the OTP", "error");
    return;
  }

  const btn = document.getElementById("verify-otp-btn");
  btn.disabled = true;
  try {
    const result = await api("/api/auth/verify-otp", {
      method: "POST",
      body: JSON.stringify({ email, otp }),
    });
    showApp(result.email);
    await loadEmployees();
  } catch (err) {
    showMessage(loginMessage, err.message, "error");
  } finally {
    btn.disabled = false;
  }
});

document.getElementById("back-btn").addEventListener("click", () => {
  stepOtp.classList.add("hidden");
  stepEmail.classList.remove("hidden");
  document.getElementById("otp").value = "";
  clearMessage(loginMessage);
});

document.getElementById("logout-btn").addEventListener("click", async () => {
  await api("/api/auth/logout", { method: "POST" });
  resetForm();
  showLogin();
});

function resetForm() {
  document.getElementById("employee-form").reset();
  document.getElementById("employee-id").value = "";
  document.getElementById("form-title").textContent = "Add Employee";
  document.getElementById("cancel-btn").classList.add("hidden");
}

async function loadEmployees() {
  clearMessage(appMessage);
  try {
    const employees = await api("/api/employees");
    const tbody = document.getElementById("employee-table");
    tbody.innerHTML = employees
      .map(
        (e) => `
      <tr>
        <td>${escapeHtml(e.name)}</td>
        <td>${escapeHtml(e.email)}</td>
        <td>${escapeHtml(e.department)}</td>
        <td>${escapeHtml(e.position)}</td>
        <td class="actions">
          <button class="btn btn-secondary btn-sm" data-edit="${e.id}">Edit</button>
          <button class="btn btn-danger btn-sm" data-delete="${e.id}">Delete</button>
        </td>
      </tr>`
      )
      .join("");

    tbody.querySelectorAll("[data-edit]").forEach((btn) => {
      btn.addEventListener("click", () => editEmployee(btn.dataset.edit, employees));
    });
    tbody.querySelectorAll("[data-delete]").forEach((btn) => {
      btn.addEventListener("click", () => deleteEmployee(btn.dataset.delete));
    });
  } catch (err) {
    showMessage(appMessage, err.message, "error");
  }
}

function editEmployee(id, employees) {
  const emp = employees.find((e) => String(e.id) === String(id));
  if (!emp) return;
  document.getElementById("employee-id").value = emp.id;
  document.getElementById("name").value = emp.name;
  document.getElementById("emp-email").value = emp.email;
  document.getElementById("department").value = emp.department;
  document.getElementById("position").value = emp.position;
  document.getElementById("form-title").textContent = "Edit Employee";
  document.getElementById("cancel-btn").classList.remove("hidden");
  window.scrollTo({ top: 0, behavior: "smooth" });
}

document.getElementById("cancel-btn").addEventListener("click", resetForm);

document.getElementById("employee-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  clearMessage(appMessage);

  const id = document.getElementById("employee-id").value;
  const payload = {
    name: document.getElementById("name").value,
    email: document.getElementById("emp-email").value,
    department: document.getElementById("department").value,
    position: document.getElementById("position").value,
  };

  try {
    if (id) {
      await api(`/api/employees/${id}`, { method: "PUT", body: JSON.stringify(payload) });
      showMessage(appMessage, "Employee updated", "success");
    } else {
      await api("/api/employees", { method: "POST", body: JSON.stringify(payload) });
      showMessage(appMessage, "Employee added", "success");
    }
    resetForm();
    await loadEmployees();
  } catch (err) {
    showMessage(appMessage, err.message, "error");
  }
});

async function deleteEmployee(id) {
  if (!confirm("Delete this employee?")) return;
  clearMessage(appMessage);
  try {
    await api(`/api/employees/${id}`, { method: "DELETE" });
    showMessage(appMessage, "Employee deleted", "success");
    await loadEmployees();
  } catch (err) {
    showMessage(appMessage, err.message, "error");
  }
}

function escapeHtml(str) {
  const div = document.createElement("div");
  div.textContent = str;
  return div.innerHTML;
}

checkAuth();
