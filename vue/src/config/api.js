const backendBaseUrl = (import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8080').replace(/\/$/, '')

export const apiRoot = `${backendBaseUrl}/api`
export const apiBase = `${apiRoot}/auth`
export const ideaAuthApiBase = `${apiRoot}/idea-contest-auth`
