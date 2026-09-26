// ---------------------------------------------------------------------------
// Constants & shared helpers
// ---------------------------------------------------------------------------

const PAGE_SIZE = 8;

const app = document.getElementById('app');
const toastContainer = document.getElementById('toastContainer');

function escapeHtml(value) {
  return String(value).replace(/[&<>"']/g, (c) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
  }[c]));
}

function formatDate(isoString) {
  if (!isoString) return '—';
  const d = new Date(isoString);
  if (isNaN(d.getTime())) return isoString;
  return d.toLocaleString(undefined, {
    year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit',
  });
}

function stockStatus(product) {
  if (product.quantity <= 0) return { label: 'Out of Stock', cls: 'badge-danger' };
  if (product.lowStock) return { label: 'Low Stock', cls: 'badge-warning' };
  return { label: 'In Stock', cls: 'badge-success' };
}

function showToast(message, type = 'success') {
  const el = document.createElement('div');
  el.className = `toast toast-${type}`;
  el.textContent = message;
  toastContainer.appendChild(el);
  requestAnimationFrame(() => el.classList.add('toast-visible'));
  setTimeout(() => {
    el.classList.remove('toast-visible');
    setTimeout(() => el.remove(), 250);
  }, 3500);
}

function renderLoading() {
  return `<div class="loading">Loading&hellip;</div>`;
}

function renderErrorBox(message) {
  return `<div class="error-box">${escapeHtml(message)}</div>`;
}

const ICONS = {
  dashboard: '<svg viewBox="0 0 24 24"><path d="M3 12l9-9 9 9M5 10v10a1 1 0 0 0 1 1h4v-6h4v6h4a1 1 0 0 0 1-1V10"/></svg>',
  products: '<svg viewBox="0 0 24 24"><path d="M21 8l-9-5-9 5 9 5 9-5z"/><path d="M3 8v8l9 5 9-5V8"/><path d="M12 13v8"/></svg>',
  add: '<svg viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></svg>',
  update: '<svg viewBox="0 0 24 24"><path d="M23 4v6h-6M1 20v-6h6"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>',
  transactions: '<svg viewBox="0 0 24 24"><path d="M3 6h18M3 12h18M3 18h18"/></svg>',
  eye: '<svg viewBox="0 0 24 24"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>',
  edit: '<svg viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.12 2.12 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>',
  trash: '<svg viewBox="0 0 24 24"><path d="M3 6h18M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2m3 0v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6h14z"/></svg>',
  back: '<svg viewBox="0 0 24 24"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>',
  box: '<svg viewBox="0 0 24 24"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><path d="M3.27 6.96L12 12l8.73-5.04M12 22.08V12"/></svg>',
  layers: '<svg viewBox="0 0 24 24"><path d="M12 2 2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5M2 12l10 5 10-5"/></svg>',
  warning: '<svg viewBox="0 0 24 24"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><path d="M12 9v4M12 17h.01"/></svg>',
};

function icon(name) {
  return ICONS[name] || '';
}

// ---------------------------------------------------------------------------
// Router
// ---------------------------------------------------------------------------

const routes = [
  { pattern: /^#\/?$/, view: viewDashboard },
  { pattern: /^#\/dashboard$/, view: viewDashboard },
  { pattern: /^#\/products$/, view: viewProducts },
  { pattern: /^#\/products\/(\d+)$/, view: viewProductDetails },
  { pattern: /^#\/add-product$/, view: viewAddProduct },
  { pattern: /^#\/update-stock/, view: viewUpdateStock },
  { pattern: /^#\/transactions$/, view: viewTransactions },
];

function currentRouteName() {
  const hash = location.hash || '#/dashboard';
  if (hash.startsWith('#/products/')) return 'products';
  const match = hash.match(/^#\/([a-z-]+)/);
  return match ? match[1] : 'dashboard';
}

function setActiveNav() {
  const routeName = currentRouteName();
  document.querySelectorAll('.nav-link').forEach((link) => {
    link.classList.toggle('active', link.dataset.route === routeName);
  });
}

function router() {
  const hash = location.hash || '#/dashboard';
  closeSidebar();
  setActiveNav();
  for (const route of routes) {
    const match = hash.match(route.pattern);
    if (match) {
      route.view(...match.slice(1));
      return;
    }
  }
  app.innerHTML = renderErrorBox('Page not found.');
}

window.addEventListener('hashchange', router);
window.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.nav-icon').forEach((el) => {
    el.innerHTML = icon(el.dataset.icon);
  });
  router();
});

// ---------------------------------------------------------------------------
// Sidebar (mobile)
// ---------------------------------------------------------------------------

const sidebar = document.getElementById('sidebar');
const sidebarOverlay = document.getElementById('sidebarOverlay');
const hamburgerBtn = document.getElementById('hamburgerBtn');

function openSidebar() {
  sidebar.classList.add('open');
  sidebarOverlay.classList.add('visible');
}
function closeSidebar() {
  sidebar.classList.remove('open');
  sidebarOverlay.classList.remove('visible');
}
hamburgerBtn.addEventListener('click', () => {
  sidebar.classList.contains('open') ? closeSidebar() : openSidebar();
});
sidebarOverlay.addEventListener('click', closeSidebar);

// ---------------------------------------------------------------------------
// Dashboard
// ---------------------------------------------------------------------------

async function viewDashboard() {
  app.innerHTML = renderLoading();
  try {
    const products = await Api.getAllProducts();
    const totalProducts = products.length;
    const totalStock = products.reduce((sum, p) => sum + p.quantity, 0);
    const lowStockItems = products.filter((p) => p.lowStock).length;
    const recent = [...products].sort((a, b) => b.id - a.id).slice(0, 5);

    app.innerHTML = `
      <div class="page-header">
        <div>
          <h1>Welcome to</h1>
          <h1 class="page-title">Stock Manager</h1>
          <p class="page-subtitle">Manage your products and inventory efficiently.</p>
        </div>
      </div>

      <div class="stat-grid">
        <div class="stat-card">
          <div class="stat-icon stat-icon-blue">${icon('layers')}</div>
          <div>
            <div class="stat-label">Total Products</div>
            <div class="stat-value">${totalProducts}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-icon-green">${icon('box')}</div>
          <div>
            <div class="stat-label">Total Stock</div>
            <div class="stat-value">${totalStock}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-icon-amber">${icon('warning')}</div>
          <div>
            <div class="stat-label">Low Stock Items</div>
            <div class="stat-value">${lowStockItems}</div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <h2>Recent Products</h2>
          <a href="#/products" class="link">View All &rarr;</a>
        </div>
        ${recent.length ? renderProductsTableHtml(recent, { actions: false }) : '<p class="empty-msg">No products yet.</p>'}
      </div>
    `;
  } catch (err) {
    app.innerHTML = renderErrorBox(err.message);
  }
}

// ---------------------------------------------------------------------------
// Products list
// ---------------------------------------------------------------------------

function renderProductsTableHtml(products, { actions = true } = {}) {
  return `
    <table class="table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Product Name</th>
          <th>Quantity</th>
          <th>Reorder Level</th>
          <th>Status</th>
          ${actions ? '<th>Actions</th>' : ''}
        </tr>
      </thead>
      <tbody>
        ${products.map((p) => {
          const status = stockStatus(p);
          return `
            <tr>
              <td>${p.id}</td>
              <td>${escapeHtml(p.name)}</td>
              <td>${p.quantity}</td>
              <td>${p.reorderLevel}</td>
              <td><span class="badge ${status.cls}">${status.label}</span></td>
              ${actions ? `
                <td class="actions-cell">
                  <button class="icon-btn" title="View" data-action="view" data-id="${p.id}">${icon('eye')}</button>
                  <button class="icon-btn" title="Update Stock" data-action="edit" data-id="${p.id}">${icon('edit')}</button>
                  <button class="icon-btn icon-btn-danger" title="Delete" data-action="delete" data-id="${p.id}" data-name="${escapeHtml(p.name)}" data-quantity="${p.quantity}">${icon('trash')}</button>
                </td>
              ` : ''}
            </tr>
          `;
        }).join('')}
      </tbody>
    </table>
  `;
}

let productsCache = [];

async function viewProducts() {
  app.innerHTML = renderLoading();
  try {
    productsCache = await Api.getAllProducts();
    renderProductsPage(1, '');
  } catch (err) {
    app.innerHTML = renderErrorBox(err.message);
  }
}

function renderProductsPage(page, search) {
  const filtered = search
    ? productsCache.filter((p) => p.name.toLowerCase().includes(search.toLowerCase()))
    : productsCache;

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  page = Math.min(Math.max(1, page), totalPages);
  const pageItems = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  app.innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">Products</h1>
        <p class="page-subtitle">View and manage all products.</p>
      </div>
      <a href="#/add-product" class="btn btn-primary">${icon('add')} Add Product</a>
    </div>

    <div class="card">
      <div class="card-toolbar">
        <input type="text" id="productSearch" class="input" placeholder="Search products..." value="${escapeHtml(search)}">
      </div>
      ${pageItems.length ? renderProductsTableHtml(pageItems) : '<p class="empty-msg">No products match your search.</p>'}
      ${filtered.length ? `
        <div class="pagination">
          <button class="btn btn-secondary" id="prevPage" ${page <= 1 ? 'disabled' : ''}>&larr; Previous</button>
          <span class="page-indicator">Page ${page} of ${totalPages}</span>
          <button class="btn btn-secondary" id="nextPage" ${page >= totalPages ? 'disabled' : ''}>Next &rarr;</button>
        </div>
      ` : ''}
    </div>
  `;

  const searchInput = document.getElementById('productSearch');
  searchInput.addEventListener('input', (e) => renderProductsPage(1, e.target.value));
  searchInput.focus();
  searchInput.selectionStart = searchInput.selectionEnd = searchInput.value.length;

  const prevBtn = document.getElementById('prevPage');
  const nextBtn = document.getElementById('nextPage');
  if (prevBtn) prevBtn.addEventListener('click', () => renderProductsPage(page - 1, search));
  if (nextBtn) nextBtn.addEventListener('click', () => renderProductsPage(page + 1, search));

  app.querySelectorAll('[data-action="view"]').forEach((btn) => {
    btn.addEventListener('click', () => { location.hash = `#/products/${btn.dataset.id}`; });
  });
  app.querySelectorAll('[data-action="edit"]').forEach((btn) => {
    btn.addEventListener('click', () => { location.hash = `#/update-stock?id=${btn.dataset.id}`; });
  });
  app.querySelectorAll('[data-action="delete"]').forEach((btn) => {
    btn.addEventListener('click', () => handleDeleteProduct(btn.dataset.id, btn.dataset.name, Number(btn.dataset.quantity), () => renderProductsPage(page, search)));
  });
}

async function handleDeleteProduct(id, name, quantity, onDeleted) {
  if (quantity !== 0) {
    showToast(`Cannot delete "${name}": quantity must be 0 first.`, 'error');
    return;
  }
  if (!confirm(`Delete "${name}"? This cannot be undone.`)) return;
  try {
    await Api.deleteProduct(id);
    showToast(`"${name}" deleted.`, 'success');
    productsCache = productsCache.filter((p) => String(p.id) !== String(id));
    if (onDeleted) onDeleted();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// ---------------------------------------------------------------------------
// Product details
// ---------------------------------------------------------------------------

async function viewProductDetails(id) {
  app.innerHTML = renderLoading();
  try {
    const [product, transactions] = await Promise.all([
      Api.getProductById(id),
      Api.getTransactionsByItemId(id).catch(() => []),
    ]);
    const status = stockStatus(product);

    app.innerHTML = `
      <a href="#/products" class="link back-link">${icon('back')} Back to Products</a>

      <div class="card details-card">
        <div class="details-icon">${icon('box')}</div>
        <div class="details-info">
          <h2>${escapeHtml(product.name)}</h2>
          <p class="details-meta">ID: ${product.id}</p>
          <p class="details-meta">Current Quantity: <strong>${product.quantity}</strong></p>
          <p class="details-meta">Reorder Level: ${product.reorderLevel}</p>
          <span class="badge ${status.cls}">${status.label}</span>
          <div class="details-actions">
            <button class="btn btn-primary" id="editStockBtn">${icon('edit')} Update Stock</button>
            <button class="btn btn-danger" id="deleteProductBtn">${icon('trash')} Delete</button>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header"><h2>Recent Transactions</h2></div>
        ${transactions.length ? renderTransactionsTableHtml(transactions, { showProduct: false }) : '<p class="empty-msg">No transactions recorded for this product yet.</p>'}
      </div>
    `;

    document.getElementById('editStockBtn').addEventListener('click', () => {
      location.hash = `#/update-stock?id=${product.id}`;
    });
    document.getElementById('deleteProductBtn').addEventListener('click', () => {
      handleDeleteProduct(product.id, product.name, product.quantity, () => { location.hash = '#/products'; });
    });
  } catch (err) {
    app.innerHTML = `
      <a href="#/products" class="link back-link">${icon('back')} Back to Products</a>
      ${renderErrorBox('Product not found or an error occurred while loading it.')}
    `;
  }
}

// ---------------------------------------------------------------------------
// Add product
// ---------------------------------------------------------------------------

function viewAddProduct() {
  app.innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">Add New Product</h1>
        <p class="page-subtitle">Enter product details to add to inventory.</p>
      </div>
    </div>

    <div class="card form-card">
      <form id="addProductForm">
        <label class="field-label" for="productName">Product Name</label>
        <input type="text" id="productName" class="input" placeholder="e.g. Laptop" required>

        <label class="field-label" for="initialQuantity">Initial Quantity</label>
        <input type="number" id="initialQuantity" class="input" placeholder="e.g. 10" min="0" required>

        <label class="field-label" for="reorderLevel">Reorder Level (optional)</label>
        <input type="number" id="reorderLevel" class="input" placeholder="Default: 5" min="0">

        <div id="formError"></div>

        <button type="submit" class="btn btn-primary btn-block" id="submitBtn">${icon('add')} Add Product</button>
      </form>
    </div>
  `;

  document.getElementById('addProductForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('productName').value.trim();
    const quantity = Number(document.getElementById('initialQuantity').value);
    const reorderLevelRaw = document.getElementById('reorderLevel').value;
    const reorderLevel = reorderLevelRaw ? Number(reorderLevelRaw) : null;
    const errorBox = document.getElementById('formError');
    const submitBtn = document.getElementById('submitBtn');
    errorBox.innerHTML = '';

    if (!name) {
      errorBox.innerHTML = renderErrorBox('Product name is required.');
      return;
    }
    if (!Number.isFinite(quantity) || quantity < 0) {
      errorBox.innerHTML = renderErrorBox('Quantity must be a non-negative number.');
      return;
    }

    submitBtn.disabled = true;
    try {
      const product = await Api.addProduct(name, quantity, reorderLevel);
      showToast(`"${product.name}" added to inventory.`, 'success');
      location.hash = `#/products/${product.id}`;
    } catch (err) {
      errorBox.innerHTML = renderErrorBox(`Could not add product. ${err.message}`);
      submitBtn.disabled = false;
    }
  });
}

// ---------------------------------------------------------------------------
// Update stock
// ---------------------------------------------------------------------------

async function viewUpdateStock() {
  app.innerHTML = renderLoading();
  const params = new URLSearchParams((location.hash.split('?')[1] || ''));
  const preselectId = params.get('id');

  try {
    const products = await Api.getAllProducts();

    app.innerHTML = `
      <div class="page-header">
        <div>
          <h1 class="page-title">Update Stock</h1>
          <p class="page-subtitle">Add or deduct quantity from existing products.</p>
        </div>
      </div>

      <div class="card form-card">
        <form id="updateStockForm">
          <label class="field-label" for="productSelect">Product</label>
          <select id="productSelect" class="input" required>
            <option value="">Select a product</option>
            ${products.map((p) => `<option value="${p.id}" ${String(p.id) === preselectId ? 'selected' : ''}>${escapeHtml(p.name)} (qty: ${p.quantity})</option>`).join('')}
          </select>

          <label class="field-label" for="quantityInput">Quantity</label>
          <input type="number" id="quantityInput" class="input" placeholder="e.g. 10" min="1" required>

          <label class="field-label">Action</label>
          <div class="action-toggle">
            <button type="button" class="toggle-btn toggle-add active" data-action="ADD">${icon('add')} Add to Stock (IN)</button>
            <button type="button" class="toggle-btn toggle-deduct" data-action="DEDUCT">Deduct from Stock (OUT)</button>
          </div>

          <div id="formError"></div>

          <button type="submit" class="btn btn-primary btn-block" id="submitBtn">${icon('update')} Update Stock</button>
        </form>
      </div>
    `;

    let action = 'ADD';
    const toggleButtons = app.querySelectorAll('.toggle-btn');
    toggleButtons.forEach((btn) => {
      btn.addEventListener('click', () => {
        action = btn.dataset.action;
        toggleButtons.forEach((b) => b.classList.remove('active'));
        btn.classList.add('active');
      });
    });

    document.getElementById('updateStockForm').addEventListener('submit', async (e) => {
      e.preventDefault();
      const id = document.getElementById('productSelect').value;
      const quantity = Number(document.getElementById('quantityInput').value);
      const errorBox = document.getElementById('formError');
      const submitBtn = document.getElementById('submitBtn');
      errorBox.innerHTML = '';

      if (!id) {
        errorBox.innerHTML = renderErrorBox('Please select a product.');
        return;
      }
      if (!Number.isFinite(quantity) || quantity <= 0) {
        errorBox.innerHTML = renderErrorBox('Quantity must be a positive number.');
        return;
      }

      submitBtn.disabled = true;
      try {
        const updated = action === 'ADD'
          ? await Api.addQuantity(Number(id), quantity)
          : await Api.deductQuantity(Number(id), quantity);
        showToast(`Stock updated: "${updated.name}" now has ${updated.quantity} units.`, 'success');
        location.hash = `#/products/${updated.id}`;
      } catch (err) {
        errorBox.innerHTML = renderErrorBox(`Could not update stock. ${err.message}`);
        submitBtn.disabled = false;
      }
    });
  } catch (err) {
    app.innerHTML = renderErrorBox(err.message);
  }
}

// ---------------------------------------------------------------------------
// Transactions
// ---------------------------------------------------------------------------

function renderTransactionsTableHtml(transactions, { showProduct = true } = {}) {
  return `
    <table class="table">
      <thead>
        <tr>
          <th>ID</th>
          ${showProduct ? '<th>Product</th>' : ''}
          <th>Type</th>
          <th>Quantity</th>
          <th>Date</th>
        </tr>
      </thead>
      <tbody>
        ${transactions.map((t) => {
          const isIn = t.type === 'STOCK_IN';
          return `
            <tr>
              <td>${t.id ?? '—'}</td>
              ${showProduct ? `<td>${t.productName ? escapeHtml(t.productName) : '—'}</td>` : ''}
              <td><span class="badge ${isIn ? 'badge-success' : 'badge-danger'}">${isIn ? 'IN' : 'OUT'}</span></td>
              <td>${t.quantityChange}</td>
              <td>${formatDate(t.occurredAt)}</td>
            </tr>
          `;
        }).join('')}
      </tbody>
    </table>
  `;
}

async function viewTransactions() {
  app.innerHTML = renderLoading();
  try {
    const transactions = await Api.getAllTransactions();
    app.innerHTML = `
      <div class="page-header">
        <div>
          <h1 class="page-title">Inventory Transactions</h1>
          <p class="page-subtitle">View all stock movements.</p>
        </div>
      </div>
      <div class="card">
        ${transactions.length ? renderTransactionsTableHtml(transactions) : '<p class="empty-msg">No transactions recorded yet.</p>'}
      </div>
    `;
  } catch (err) {
    app.innerHTML = renderErrorBox(err.message);
  }
}
