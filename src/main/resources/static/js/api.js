const API_BASE = '/api';

async function apiFetch(path, options) {
  let res;
  try {
    res = await fetch(API_BASE + path, {
      headers: { 'Content-Type': 'application/json' },
      ...(options || {}),
    });
  } catch (networkErr) {
    throw new Error('Could not reach the server. Is the backend running?');
  }

  if (!res.ok) {
    let message = `Request failed (HTTP ${res.status})`;
    try {
      const body = await res.json();
      if (body && body.message) message = body.message;
    } catch (parseErr) {
      // response wasn't JSON (e.g. a container-level error page) - keep the default message
    }
    throw new Error(message);
  }

  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

const Api = {
  getAllProducts() {
    return apiFetch('/products');
  },
  getProductById(id) {
    return apiFetch(`/products/${id}`);
  },
  deleteProduct(id) {
    return apiFetch(`/products/${id}`, { method: 'DELETE' });
  },
  addProduct(name, quantity, reorderLevel) {
    return apiFetch('/products', {
      method: 'POST',
      body: JSON.stringify({ name, quantity, reorderLevel }),
    });
  },
  addQuantity(id, quantity) {
    return apiFetch(`/products/${id}/stock-in`, {
      method: 'POST',
      body: JSON.stringify({ quantity }),
    });
  },
  deductQuantity(id, quantity) {
    return apiFetch(`/products/${id}/stock-out`, {
      method: 'POST',
      body: JSON.stringify({ quantity }),
    });
  },
  getAllTransactions() {
    return apiFetch('/transactions');
  },
  getTransactionsByItemId(id) {
    return apiFetch(`/products/${id}/transactions`);
  },
};
