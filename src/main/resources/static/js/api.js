const TOKEN_KEY = "canteen.token";

export class ApiError extends Error {
    constructor(status, message, fields) {
        super(message);
        this.status = status;
        this.fields = fields || {};
    }
}

export function savedToken() {
    return sessionStorage.getItem(TOKEN_KEY);
}

export function saveToken(token) {
    sessionStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
    sessionStorage.removeItem(TOKEN_KEY);
}

let onSessionLost = () => {};

export function whenSessionLost(callback) {
    onSessionLost = callback;
}

export async function api(metodo, path, body) {
    const headers = { Accept: "application/json" };
    const token = savedToken();
    if (token) {
        headers.Authorization = "Bearer " + token;
    }
    if (body !== undefined) {
        headers["Content-Type"] = "application/json";
    }

    let response;
    try {
        response = await fetch(path, {
            method: metodo,
            headers: headers,
            body: body === undefined ? undefined : JSON.stringify(body)
        });
    } catch (error) {
        throw new ApiError(0, "No connection to the server. Check that the API is running.");
    }

    const text = await response.text();
    const data = text ? JSON.parse(text) : null;

    if (response.status === 401 && token) {
        clearToken();
        onSessionLost();
    }
    if (!response.ok) {
        const message = data && data.message ? data.message : "Could not complete the operation.";
        throw new ApiError(response.status, message, data ? data.fields : null);
    }
    return data;
}

export const get = (path) => api("GET", path);
export const post = (path, body) => api("POST", path, body);
export const put = (path, body) => api("PUT", path, body);
export const patch = (path, body) => api("PATCH", path, body);
export const del = (path) => api("DELETE", path);
